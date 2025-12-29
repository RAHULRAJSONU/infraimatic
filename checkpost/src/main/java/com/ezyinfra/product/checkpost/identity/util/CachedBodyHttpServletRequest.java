package com.ezyinfra.product.checkpost.identity.util;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    @Getter
    private final byte[] cachedBody;
    private final Map<String, String[]> parameterMap;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = request.getInputStream().readAllBytes();
        this.parameterMap = parseParameters(request);
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(
                new InputStreamReader(getInputStream(), StandardCharsets.UTF_8)
        );
    }

    // 🔑 VERY IMPORTANT OVERRIDES

    @Override
    public String getParameter(String name) {
        String[] values = parameterMap.get(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }

    private Map<String, String[]> parseParameters(HttpServletRequest request) {
        if (!isFormPost(request)) {
            return Collections.emptyMap();
        }

        String body = new String(cachedBody, StandardCharsets.UTF_8);
        Map<String, List<String>> temp = new HashMap<>();

        for (String pair : body.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                temp.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }

        Map<String, String[]> result = new HashMap<>();
        temp.forEach((k, v) -> result.put(k, v.toArray(String[]::new)));
        return result;
    }

    private boolean isFormPost(HttpServletRequest request) {
        return request.getContentType() != null &&
                request.getContentType().contains("application/x-www-form-urlencoded");
    }
}
