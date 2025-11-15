package com.ezyinfra.product.nlu.dto;

import java.util.Map;

public class ParseByTypeRequest {
    public String type;    // template type key, e.g. "visitor-entry"
    public String text;    // free-form text input
    public Map<String,Object> options; // gpNumberPrefix, strictValidation, timezone...
}