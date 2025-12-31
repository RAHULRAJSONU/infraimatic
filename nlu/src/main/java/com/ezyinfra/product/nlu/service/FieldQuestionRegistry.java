package com.ezyinfra.product.nlu.service;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FieldQuestionRegistry {

    private final Map<String, String> templates = Map.of(
        "personRef.personDetails.personName", "What is your full name?",
        "personRef.personDetails.phoneNumber", "What is your phone number?",
        "personRef.personDetails.idType", "What type of ID are you carrying? (Aadhar / Passport / Driving License)",
        "personRef.personDetails.idNumber", "Please provide your ID number.",
        "personRef.personDetails.idValidity", "What is the validity date of your ID?",
        "purposeOfVisit", "What is the purpose of your visit?",
        "requestedFor", "Who are you visiting?",
        "requestDateTime", "When is the visit date and time?",
        "isCarryingEquipment", "Are you carrying any equipment? (Yes/No)"
    );

    public String questionFor(String fieldPath) {
        return templates.getOrDefault(
                fieldPath,
                "Please provide " + humanize(fieldPath)
        );
    }

    private String humanize(String fieldPath) {
        return fieldPath
                .substring(fieldPath.lastIndexOf('.') + 1)
                .replaceAll("([A-Z])", " $1")
                .toLowerCase();
    }
}