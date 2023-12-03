package com.labstyle.darioweekviewdatepicker;

import java.text.DateFormatSymbols;
import java.util.Locale;

public class CustomDateFormatSymbols extends DateFormatSymbols {
    private final Locale locale;

    public CustomDateFormatSymbols(Locale locale) {
        this.locale = locale;
    }
    @Override
    public String[] getWeekdays() {
        String[] weekdays = DateFormatSymbols.getInstance(locale).getWeekdays();
        String[] modifiedWeekdays = new String[weekdays.length];

        System.arraycopy(weekdays, 1, modifiedWeekdays, 0, weekdays.length - 1);
        modifiedWeekdays[weekdays.length - 1] = weekdays[1];

        return modifiedWeekdays;
    }

    @Override
    public String[] getShortWeekdays() {
        String[] shortWeekdays = DateFormatSymbols.getInstance(locale).getShortWeekdays();
        String[] modifiedShortWeekdays = new String[shortWeekdays.length];

        System.arraycopy(shortWeekdays, 1, modifiedShortWeekdays, 0, shortWeekdays.length - 1);
        modifiedShortWeekdays[shortWeekdays.length - 1] = shortWeekdays[1];

        return modifiedShortWeekdays;
    }
}
