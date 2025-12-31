package com.ezyinfra.product.nlu.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WhatsAppMediaService {

    private final RestTemplate restTemplate;

    public WhatsAppMediaService() {
        this.restTemplate = new RestTemplate();
    }

    public byte[] download(String mediaUrl) {
        return restTemplate.getForObject(mediaUrl, byte[].class);
    }
}
