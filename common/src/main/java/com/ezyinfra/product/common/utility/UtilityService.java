package com.ezyinfra.product.common.utility;

import org.apache.logging.log4j.util.Strings;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.function.BiConsumer;

public class UtilityService {
    /**
     * public static void deepMerge(Map<String, Object> target, Map<String, Object> source) {
     * if (target == null) {
     * return;
     * }
     * for (String key : source.keySet()) {
     * Object value2 = source.get(key);
     * if (target.containsKey(key)) {
     * Object value1 = target.get(key);
     * if (value1 instanceof Map && value2 instanceof Map) {
     * deepMerge((Map<String, Object>) value1, (Map<String, Object>) value2);
     * } else if (value1 instanceof List && value2 instanceof List) {
     * target.put(key, merge((List) value1, (List) value2));
     * } else if (value1 instanceof Set && value2 instanceof Set) {
     * target.put(key, merge((Set) value1, (Set) value2));
     * } else {
     * target.put(key, value2);
     * }
     * } else {
     * target.put(key, value2);
     * }
     * }
     * }
     */

    public static void deepMerge(Map<String, Object> target, Map<String, Object> source) {
        if (target == null) {
            return;
        }
        for (String key : source.keySet()) {
            Object value2 = source.get(key);
            if (target.containsKey(key)) {
                Object value1 = target.get(key);
                switch (value1) {
                    case Map map when value2 instanceof Map ->
                            deepMerge((Map<String, Object>) value1, (Map<String, Object>) value2);
                    case List list when value2 instanceof List -> target.put(key, merge(list, (List) value2));
                    case Set set when value2 instanceof Set -> target.put(key, merge(set, (Set) value2));
                    case null, default -> target.put(key, value2);
                }
            } else {
                target.put(key, value2);
            }
        }
    }

    private static Collection merge(Collection target, Collection source) {
        source.removeAll(target);
        target.addAll(source);
        return target;
    }

    public static String cleanCellNumber(String content, String target, String replacement) {
        return !Strings.isBlank(content) ?
                content.replaceAll("[^0-9]", "") :
                content;
    }

    public static String replaceSafe(String content, String target, String replacement) {
        return (!Strings.isBlank(content) && content.contains(target)) ?
                content.replace(target, replacement) :
                content;
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String normalize(String input) {
        return input == null ? null : new String(Normalizer.normalize(input, Normalizer.Form.NFKD)
                .getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    public static String encodeString(String input) {
        try {
            byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes());
            return new String(encodedBytes);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Sets the given value on the target object using the provided setter method,
     * but only if the value is not null.
     *
     * <p>This method is useful for conditionally setting properties on an object
     * when the new value may be null, in which case the property remains unchanged.</p>
     *
     * @param <T>    the type of the target object
     * @param <V>    the type of the value to set
     * @param target the target object on which the property is to be set
     * @param value  the value to set on the target object; if null, the property is not set
     * @param setter a {@link BiConsumer} representing the setter method for the property
     * @throws NullPointerException if the target or setter is null
     * @example <pre>
     * {@code
     * Employee employee = new Employee();
     * EmployeeDto employeeDto = new EmployeeDto();
     *
     * employeeDto.setName("John Doe");
     * employeeDto.setAge(null);
     * employeeDto.setDepartment("Engineering");
     *
     * PropertyUtil.setIfNotNull(employee, employeeDto.getName(), Employee::setName);
     * PropertyUtil.setIfNotNull(employee, employeeDto.getAge(), Employee::setAge);
     * PropertyUtil.setIfNotNull(employee, employeeDto.getDepartment(), Employee::setDepartment);
     *
     * System.out.println("Employee Name: " + employee.getName());  // Output: John Doe
     * System.out.println("Employee Age: " + employee.getAge());    // Output: null
     * System.out.println("Employee Department: " + employee.getDepartment());  // Output: Engineering
     * }
     * </pre>
     */
    public static <T, V> void nullSafeOperation(T target, V value, BiConsumer<T, V> setter) {
        if (value != null) {
            setter.accept(target, value);
        }
    }

    public static String stripString(String key, String beginMarker, String endMarker) {
        return key.replaceAll(beginMarker, "")
                .replaceAll(endMarker, "")
                .replaceAll("\\s+", "");
    }

    public static Map<String, String> parseFormBody(String body) {
        Map<String, String> map = new HashMap<>();

        for (String pair : body.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }
        return map;
    }

    public static Map<String, String> parseFormBody(byte[] body) {
        String payload = new String(body);
        Map<String, String> map = new HashMap<>();

        for (String pair : payload.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }
        return map;
    }

}