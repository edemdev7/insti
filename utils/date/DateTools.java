package com.payiskoul.institution.utils.date;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTools {
    private DateTools(){}

    public static String convertDatetimeToString(LocalDateTime dateTime){
        if(dateTime == null)
            return null;
        return dateTime.format(DateTimeFormatter.ISO_DATE);

    }
}
