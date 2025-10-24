package com.ezyinfra.product.checkpost.identity.crypto;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;

public final class RsaHybridCrypto {
    private static final String RSA_OAEP_256 = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_GCM      = "AES/GCM/NoPadding";
    private static final int    GCM_TAG_LEN  = 128; // bits
    private static final int    IV_LEN       = 12;  // bytes
    private static final byte   VERSION_1    = 0x01;

    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final SecureRandom rng;

    private RsaHybridCrypto(PrivateKey priv, PublicKey pub, SecureRandom rng) {
        KeyPairValidator.validateRsa(priv, pub);
        this.privateKey = priv;
        this.publicKey  = pub;
        this.rng        = rng;
    }

    public static RsaHybridCrypto from(KeyLoader loader) throws GeneralSecurityException, IOException {
        PrivateKey priv = loader.loadPrivateKey();
        PublicKey  pub  = loader.loadPublicKey();
        return new RsaHybridCrypto(priv, pub, SecureRandom.getInstanceStrong());
    }

    /** Encrypts all bytes in memory (use stream methods below for large inputs). */
    public byte[] encrypt(byte[] plaintext, byte[] aad) throws GeneralSecurityException {
        SecretKey cek = newSecretAesKey(32); // 32 bytes = 256-bit
        byte[] iv = new byte[IV_LEN];
        rng.nextBytes(iv);

        Cipher gcm = Cipher.getInstance(AES_GCM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LEN, iv);
        gcm.init(Cipher.ENCRYPT_MODE, cek, gcmSpec, rng);
        if (aad != null && aad.length > 0) gcm.updateAAD(aad);
        byte[] ct = gcm.doFinal(plaintext);

        Cipher rsa = Cipher.getInstance(RSA_OAEP_256);
        OAEPParameterSpec oaep = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        rsa.init(Cipher.WRAP_MODE, publicKey, oaep, rng);
        byte[] wrapped = rsa.wrap(cek);

        ByteBuffer buf = ByteBuffer.allocate(1 + 2 + wrapped.length + IV_LEN + ct.length);
        buf.put(VERSION_1);
        buf.putShort((short) wrapped.length);
        buf.put(wrapped);
        buf.put(iv);
        buf.put(ct);
        return buf.array();
    }

    /** Decrypts the format generated above. */
    public byte[] decrypt(byte[] message, byte[] aad) throws GeneralSecurityException {
        ByteBuffer buf = ByteBuffer.wrap(message);
        if (buf.remaining() < 1 + 2 + IV_LEN) throw new AEADBadTagException("Truncated message");
        if (buf.get() != VERSION_1) throw new AEADBadTagException("Unsupported version");

        int wrappedLen = Short.toUnsignedInt(buf.getShort());
        if (buf.remaining() < wrappedLen + IV_LEN) throw new AEADBadTagException("Truncated header");

        byte[] wrapped = new byte[wrappedLen]; buf.get(wrapped);
        byte[] iv = new byte[IV_LEN]; buf.get(iv);
        byte[] ct = new byte[buf.remaining()]; buf.get(ct);

        Cipher rsa = Cipher.getInstance(RSA_OAEP_256);
        OAEPParameterSpec oaep = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        rsa.init(Cipher.UNWRAP_MODE, privateKey, oaep);
        Key cek = rsa.unwrap(wrapped, "AES", Cipher.SECRET_KEY);

        Cipher gcm = Cipher.getInstance(AES_GCM);
        gcm.init(Cipher.DECRYPT_MODE, cek, new GCMParameterSpec(GCM_TAG_LEN, iv));
        if (aad != null && aad.length > 0) gcm.updateAAD(aad);
        return gcm.doFinal(ct);
    }

    /** Streaming encryption for large payloads. Does not close the provided OutputStream. */
    public void encrypt(InputStream in, OutputStream out, byte[] aad)
            throws GeneralSecurityException, IOException {
        SecretKey cek = newSecretAesKey(32);
        byte[] iv = new byte[IV_LEN];
        rng.nextBytes(iv);

        Cipher gcm = Cipher.getInstance(AES_GCM);
        gcm.init(Cipher.ENCRYPT_MODE, cek, new GCMParameterSpec(GCM_TAG_LEN, iv), rng);
        if (aad != null && aad.length > 0) gcm.updateAAD(aad);

        Cipher rsa = Cipher.getInstance(RSA_OAEP_256);
        rsa.init(Cipher.WRAP_MODE, publicKey, new OAEPParameterSpec(
                "SHA-256","MGF1",MGF1ParameterSpec.SHA256,PSource.PSpecified.DEFAULT), rng);
        byte[] wrapped = rsa.wrap(cek);

        out.write(VERSION_1);
        out.write((wrapped.length >>> 8) & 0xFF);
        out.write(wrapped.length & 0xFF);
        out.write(wrapped);
        out.write(iv);

        // don't close 'out'; close only the wrapper stream
        try (CipherOutputStream cos = new CipherOutputStream(new NoCloseOutputStream(out), gcm)) {
            in.transferTo(cos);
        }
    }

    /** Streaming decryption for large payloads. Does not close the provided OutputStream. */
    public void decrypt(InputStream in, OutputStream out, byte[] aad)
            throws GeneralSecurityException, IOException {
        int ver = in.read();
        if (ver == -1) throw new EOFException("Truncated");
        if (ver != VERSION_1) throw new AEADBadTagException("Unsupported version");

        int hi = in.read(), lo = in.read();
        if (hi < 0 || lo < 0) throw new EOFException("Truncated");
        int len = ((hi & 0xFF) << 8) | (lo & 0xFF);

        byte[] wrapped = IoExt.readN(in, len);
        byte[] iv = IoExt.readN(in, IV_LEN);

        Cipher rsa = Cipher.getInstance(RSA_OAEP_256);
        rsa.init(Cipher.UNWRAP_MODE, privateKey, new OAEPParameterSpec(
                "SHA-256","MGF1",MGF1ParameterSpec.SHA256,PSource.PSpecified.DEFAULT));
        Key cek = rsa.unwrap(wrapped, "AES", Cipher.SECRET_KEY);

        Cipher gcm = Cipher.getInstance(AES_GCM);
        gcm.init(Cipher.DECRYPT_MODE, cek, new GCMParameterSpec(GCM_TAG_LEN, iv));
        if (aad != null && aad.length > 0) gcm.updateAAD(aad);

        try (CipherInputStream cis = new CipherInputStream(in, gcm)) {
            cis.transferTo(out);
        }
    }

    private SecretKey newSecretAesKey(int bytes) {
        byte[] k = new byte[bytes];
        rng.nextBytes(k);
        return new SecretKeySpec(k, "AES");
    }

    /** Wrapper that prevents closing the underlying OutputStream when CipherOutputStream is closed. */
    private static final class NoCloseOutputStream extends FilterOutputStream {
        NoCloseOutputStream(OutputStream out) { super(out); }
        @Override public void close() throws IOException { flush(); } // don't close underlying stream
    }
}

/** Tiny IO helpers (no “extension-method” syntax). */
final class IoExt {
    private IoExt() {}
    static byte[] readN(InputStream in, int n) throws IOException {
        byte[] b = in.readNBytes(n);
        if (b.length != n) throw new EOFException("truncated");
        return b;
    }
}
