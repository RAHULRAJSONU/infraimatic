package com.ezyinfra.product.common.utility;

import com.ezyinfra.product.common.dto.PhoneNumberParts;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public final class PhoneNumberParser {

    private static final PhoneNumberUtil UTIL = PhoneNumberUtil.getInstance();

    private PhoneNumberParser() {}

    /**
     * Parses and separates a global phone number.
     *
     * @param input       raw phone number input
     * @param defaultRegion fallback region (e.g. "IN", "US") if country code missing
     */
    public static PhoneNumberParts parse(String input, String defaultRegion) {
        try {
            Phonenumber.PhoneNumber number = UTIL.parse(input, defaultRegion);
            boolean valid = UTIL.isValidNumber(number);

            return new PhoneNumberParts(
                    input,
                    String.valueOf(number.getCountryCode()),
                    String.valueOf(number.getNationalNumber()),
                    UTIL.getRegionCodeForNumber(number),
                    UTIL.format(number, PhoneNumberUtil.PhoneNumberFormat.E164),
                    valid
            );

        } catch (NumberParseException ex) {
            return new PhoneNumberParts(
                    input,
                    null,
                    null,
                    null,
                    null,
                    false
            );
        }
    }
}
