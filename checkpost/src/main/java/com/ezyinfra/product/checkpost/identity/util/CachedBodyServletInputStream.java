package com.ezyinfra.product.checkpost.identity.util;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;

public class CachedBodyServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream buffer;

    public CachedBodyServletInputStream(byte[] contents) {
        this.buffer = new ByteArrayInputStream(contents);
    }

    @Override
    public boolean isFinished() {
        return buffer.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {}

    @Override
    public int read() {
        return buffer.read();
    }
}
