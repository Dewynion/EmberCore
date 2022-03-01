package dev.blufantasyonline.embercore.util;

public enum TimeUnits {
    TICKS (1.0 / 20),
    SECONDS (1.0),
    MILLISECONDS (1.0 / Math.pow(10, -3)),
    MICROSECONDS (1.0 / Math.pow(10, -6)),
    NANOSECONDS (1.0 / Math.pow(10, -9));

    double seconds;

    TimeUnits(double seconds) {
        this.seconds = seconds;
    }

    public double seconds() {
        return seconds;
    }
}
