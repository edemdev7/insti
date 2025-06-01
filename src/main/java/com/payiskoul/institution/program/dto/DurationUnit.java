package com.payiskoul.institution.program.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DurationUnit {
    YEAR, MONTH, DAY, HOUR;
    @JsonCreator
    public static DurationUnit fromString(String value) {
        return DurationUnit.valueOf(value.toUpperCase());
    }
}
