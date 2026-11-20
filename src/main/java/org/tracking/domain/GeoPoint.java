package org.tracking.domain;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class GeoPoint {
    private final double latitude;
    private final double longitude;
    private final Double altitudeMeters;
    private final Double speedKmh;
    private final Integer headingDegrees;
    private final Integer satellites;

    private GeoPoint(Builder b) {
        latitude = b.latitude;
        longitude = b.longitude;
        altitudeMeters = b.altitudeMeters;
        speedKmh = b.speedKmh;
        headingDegrees = b.headingDegrees;
        satellites = b.satellites;
    }

    public static Builder builder(double latitude, double longitude) {
        return new Builder(latitude, longitude);
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public OptionalDouble getAltitudeMeters() {
        return altitudeMeters == null ? OptionalDouble.empty() : OptionalDouble.of(altitudeMeters);
    }
    public OptionalDouble getSpeedKmh() {
        return speedKmh == null ? OptionalDouble.empty() : OptionalDouble.of(speedKmh);
    }
    public OptionalInt getHeadingDegrees() {
        return headingDegrees == null ? OptionalInt.empty() : OptionalInt.of(headingDegrees);
    }
    public OptionalInt getSatellites() {
        return satellites == null ? OptionalInt.empty() : OptionalInt.of(satellites);
    }

    public static final class Builder {
        private final double latitude;
        private final double longitude;
        private Double altitudeMeters;
        private Double speedKmh;
        private Integer headingDegrees;
        private Integer satellites;

        private Builder(double latitude, double longitude) {
            if (!(latitude >= -90 && latitude <= 90)) {
                throw new IllegalArgumentException("latitude out of range: " + latitude);
            }
            if (!(longitude >= -180 && longitude <= 180)) {
                throw new IllegalArgumentException("longitude out of range: " + longitude);
            }
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Builder altitudeMeters(double value) {
            this.altitudeMeters = value;
            return this;
        }

        public Builder speedKmh(double value) {
            if (!(value >= 0)) {
                throw new IllegalArgumentException("speed must be >= 0: " + value);
            }
            this.speedKmh = value;
            return this;
        }

        public Builder headingDegrees(int value) {
            if (!(value >= 0 && value <= 360)) {
                throw new IllegalArgumentException("heading out of range: " + value);
            }
            this.headingDegrees = value;
            return this;
        }

        public Builder satellites(int value) {
            if (!(value >= 0)) {
                throw new IllegalArgumentException("satellites must be >= 0: " + value);
            }
            this.satellites = value;
            return this;
        }

        public GeoPoint build() {
            return new GeoPoint(this);
        }
    }
}
