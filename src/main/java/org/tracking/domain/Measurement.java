package org.tracking.domain;

import java.util.Objects;

public final class Measurement {
    public enum Kind { DECIMAL, TEXT }

    private final String name;
    private final Kind kind;
    private final double decimalValue;
    private final String textValue;
    private final String unit;

    private Measurement(String name, Kind kind, double decimalValue, String textValue, String unit) {
        this.name = name;
        this.kind = kind;
        this.decimalValue = decimalValue;
        this.textValue = textValue;
        this.unit = unit;
    }

    public static Measurement decimal(String name, double value, String unit) {
        String trimmedName = validateName(name).trim();
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("value must be finite number: " + value);
        }
        return new Measurement(trimmedName, Kind.DECIMAL, value, null, unit.trim());
    }

    public static Measurement decimal(String name, double value) {
        return decimal(name, value, "");
    }

    public static Measurement text(String name, String value) {
        String trimmedName = validateName(name).trim();
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("name must not be empty: \"" + value + "\"");
        }
        return new Measurement(trimmedName, Kind.TEXT, Double.NaN, value.trim(), "");
    }

    private static String validateName(String name) {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be empty: \"" + name + "\"");
        }
        return name;
    }

    public String getName() {
        return name;
    }

    public Kind getKind() {
        return kind;
    }

    public boolean isDecimal() {
        return kind == Kind.DECIMAL;
    }

    public boolean isText() {
        return kind == Kind.TEXT;
    }

    public double getDecimal() {
        if (kind != Kind.DECIMAL) {
            throw new IllegalStateException("'" + name + "' is not a text measurement");
        }
        return decimalValue;
    }

    public String getText() {
        if (kind != Kind.TEXT) {
            throw new IllegalStateException("'" + name + "' is not a text measurement");
        }
        return textValue;
    }

    public String getUnit() {
        return unit;
    }
}
