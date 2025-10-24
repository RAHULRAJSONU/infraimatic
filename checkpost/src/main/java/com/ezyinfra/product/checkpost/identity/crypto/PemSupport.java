package com.ezyinfra.product.checkpost.identity.crypto;

import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

public final class PemSupport {
    private static final Pattern WS = Pattern.compile("\\s+");

    static byte[] readPemBody(String pem, String begin, String end) {
        int i = pem.indexOf(begin), j = pem.indexOf(end);
        if (i < 0 || j < 0 || j <= i) {
            throw new IllegalArgumentException("PEM markers missing: " + begin + " / " + end);
        }
        String b64 = WS.matcher(pem.substring(i + begin.length(), j)).replaceAll("");
        return Base64.getMimeDecoder().decode(b64);
    }

    // Accepts PKCS#8 "PRIVATE KEY" or PKCS#1 "RSA PRIVATE KEY"
    public static PrivateKey parseRsaPrivate(String pem) throws GeneralSecurityException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        if (pem.contains("BEGIN PRIVATE KEY")) {
            byte[] der = readPemBody(pem, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(der));
        }
        if (pem.contains("BEGIN RSA PRIVATE KEY")) {
            byte[] pkcs1 = readPemBody(pem, "-----BEGIN RSA PRIVATE KEY-----", "-----END RSA PRIVATE KEY-----");
            byte[] pkcs8 = Pkcs1ToPkcs8.wrapRsaPkcs1(pkcs1);
            return kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
        }
        throw new InvalidKeySpecException("Unsupported private key PEM");
    }

    // Accepts X.509 "PUBLIC KEY" or PKCS#1 "RSA PUBLIC KEY"
    public static PublicKey parseRsaPublic(String pem) throws GeneralSecurityException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        if (pem.contains("BEGIN PUBLIC KEY")) {
            byte[] der = readPemBody(pem, "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");
            return kf.generatePublic(new X509EncodedKeySpec(der));
        }
        if (pem.contains("BEGIN RSA PUBLIC KEY")) { // PKCS#1 public key
            byte[] pkcs1 = readPemBody(pem, "-----BEGIN RSA PUBLIC KEY-----", "-----END RSA PUBLIC KEY-----");
            byte[] spki = Pkcs1ToSpki.wrapRsaPkcs1Public(pkcs1);
            return kf.generatePublic(new X509EncodedKeySpec(spki));
        }
        throw new InvalidKeySpecException("Unsupported public key PEM");
    }

    private PemSupport() {}
}

/** Minimal DER wrappers (tiny, dependency-free). */
final class Pkcs1ToPkcs8 {
    // PKCS#8 = SEQ{ int v=0, SEQ{OID rsaEncryption, NULL}, OCTET STRING(pkcs1) }
    static byte[] wrapRsaPkcs1(byte[] pkcs1) {
        return Der.seq(
                Der.int0(),
                Der.seq(Der.oid("1.2.840.113549.1.1.1"), Der.nullv()),
                Der.octet(pkcs1)
        );
    }
}

final class Pkcs1ToSpki {
    // SubjectPublicKeyInfo = SEQ{ SEQ{OID rsaEncryption, NULL}, BIT STRING(pkcs1) }
    static byte[] wrapRsaPkcs1Public(byte[] pkcs1) {
        return Der.seq(
                Der.seq(Der.oid("1.2.840.113549.1.1.1"), Der.nullv()),
                Der.bitString(pkcs1)
        );
    }
}

/** Tiny DER helpers (covers just what we need). */
final class Der {
    static byte[] seq(byte[]... parts) { return enc(0x30, concat(parts)); }
    static byte[] int0()               { return new byte[]{0x02, 0x01, 0x00}; }
    static byte[] nullv()              { return new byte[]{0x05, 0x00}; }

    static byte[] oid(String dot) {
        String[] s = dot.split("\\.");
        int a = Integer.parseInt(s[0]), b = Integer.parseInt(s[1]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(40 * a + b);
        for (int i = 2; i < s.length; i++) {
            writeBase128(out, Long.parseLong(s[i]));
        }
        return enc(0x06, out.toByteArray());
    }

    static byte[] octet(byte[] v)     { return enc(0x04, v); }
    static byte[] bitString(byte[] v) { return enc(0x03, concat(new byte[]{0x00}, v)); } // 0 unused bits

    static byte[] enc(int tag, byte[] v) {
        byte[] len = length(v.length);
        byte[] out = new byte[1 + len.length + v.length];
        out[0] = (byte) tag;
        System.arraycopy(len, 0, out, 1, len.length);
        System.arraycopy(v, 0, out, 1 + len.length, v.length);
        return out;
    }

    static byte[] length(int n) {
        if (n < 128) return new byte[]{(byte) n};
        int tmp = n, bytes = 0;
        while (tmp > 0) { tmp >>>= 8; bytes++; }
        byte[] out = new byte[1 + bytes];
        out[0] = (byte) (0x80 | bytes);
        for (int i = bytes; i > 0; i--) {
            out[i] = (byte) (n & 0xFF);
            n >>>= 8;
        }
        return out;
    }

    static byte[] concat(byte[]... arrs) {
        int len = 0; for (byte[] a : arrs) len += a.length;
        byte[] out = new byte[len]; int p = 0;
        for (byte[] a : arrs) { System.arraycopy(a, 0, out, p, a.length); p += a.length; }
        return out;
    }

    /** Base-128 varint encoding used in ASN.1 OID nodes. */
    private static void writeBase128(ByteArrayOutputStream out, long value) {
        // Encode least-significant groups first, then write in reverse with continuation bits.
        int bytes = 0;
        byte[] tmp = new byte[10]; // enough for 64-bit
        do {
            tmp[bytes++] = (byte) (value & 0x7F);
            value >>>= 7;
        } while (value != 0);
        for (int i = bytes - 1; i >= 0; i--) {
            int b = tmp[i] & 0x7F;
            if (i != 0) b |= 0x80; // set continuation bit for all but last
            out.write(b);
        }
    }
}
