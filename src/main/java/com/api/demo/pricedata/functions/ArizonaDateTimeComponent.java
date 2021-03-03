package com.api.demo.pricedata.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/**
 * Arizona Date-Time Component provides the API a fixed point in which to make automatic date-related updates
 * For this API, that fixed point is the location of the corporate offices (Phoenix, Arizona)
 * So if an action is to occur tomorrow, it will happen once it is "tomorrow" for Phoenix
 *
 * This handles date strings only, not time related aspects.  Time is a whole other level of complexity
 * that I dare not undertake unless necessary
 */
public class ArizonaDateTimeComponent {
    private static Logger logger = LoggerFactory.getLogger(ArizonaDateTimeComponent.class);
    // Important note: SimpleDateFormat is not thread safe.  Therefore must be called in each function
    // Probably a newer way of doing it, but too busy to go find that way

    public ArizonaDateTimeComponent() { }

    public String getTodayDateString() {
        ZonedDateTime arizonaTime = LocalDateTime.now().atZone(ZoneId.of("America/Phoenix"));
        String date = getFormattedMonth() + "/" + arizonaTime.getDayOfMonth() + "/" + arizonaTime.getYear();
        return date;
    }

    public String getYesterdaysDateString() {
        ZonedDateTime arizonaTime = LocalDateTime.now().atZone(ZoneId.of("America/Phoenix")).minusDays(1);
        String date = getFormattedMonth() + "/" + arizonaTime.getDayOfMonth() + "/" + arizonaTime.getYear();
        return date;
    }

    public String getTomorrowsDateString() {
        ZonedDateTime arizonaTime = LocalDateTime.now().atZone(ZoneId.of("America/Phoenix")).plusDays(1);
        String date = getFormattedMonth() + "/" + arizonaTime.getDayOfMonth() + "/" + arizonaTime.getYear();
        return date;
    }

    public String getPreviousDayString(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        ZonedDateTime arizonaTime = LocalDateTime.parse(dateString, formatter).atZone(ZoneId.of("America/Phoenix")).minusDays(1);
        String date = getFormattedMonth() + "/" + arizonaTime.getDayOfMonth() + "/" + arizonaTime.getYear();
        return date;
    }

    public String getNextDayString(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        ZonedDateTime arizonaTime = LocalDateTime.parse(dateString, formatter).atZone(ZoneId.of("America/Phoenix")).plusDays(1);
        String date = getFormattedMonth() + "/" + arizonaTime.getDayOfMonth() + "/" + arizonaTime.getYear();
        return date;
    }

    public String getDayString() {
        ZonedDateTime arizonaTime = LocalDateTime.now().atZone(ZoneId.of("America/Phoenix"));
        return String.valueOf(arizonaTime.getDayOfMonth());
    }

    public int getDayInteger() {
        ZonedDateTime arizonaTime = LocalDateTime.now().atZone(ZoneId.of("America/Phoenix"));
        return arizonaTime.getDayOfMonth();
    }

    public String getMonthString() {
        return getFormattedMonth();
    }

    public int getMonthInteger() {
        ZonedDateTime arizonaTime = LocalDateTime.now().atZone(ZoneId.of("America/Phoenix"));
        return arizonaTime.getMonth().getValue() - 1;

    }

    public String getYearString() {
        ZonedDateTime arizonaTime = LocalDateTime.now().atZone(ZoneId.of("America/Phoenix"));
        return String.valueOf(arizonaTime.getYear());
    }

    public int getYearInteger() {
        ZonedDateTime arizonaTime = LocalDateTime.now().atZone(ZoneId.of("America/Phoenix"));
        return arizonaTime.getYear();
    }

    public boolean areDatesTheSame(String date1, String date2)  {
        return date1.equals(date2);
    }

    public boolean isGivenDateToday(String date) {
        return date.equals(getTodayDateString());
    }

    public boolean isDateBeforeToday(String dateStr) throws ParseException {
        if(isGivenDateToday(dateStr)) {
            return false;
        }
        Calendar today = convertDateStringToCalendar(getTodayDateString());
        Calendar comparisonDate = convertDateStringToCalendar(dateStr);

        if(comparisonDate.before(today)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isDateAfterToday(String dateStr) throws ParseException {
        if(isGivenDateToday(dateStr)) {
            return false;
        }
        Calendar today = convertDateStringToCalendar(getTodayDateString());
        Calendar comparisonDate = convertDateStringToCalendar(dateStr);

        if(comparisonDate.after(today)) {
            return true;
        } else {
            return false;
        }
    }

    public Calendar convertDateStringToCalendar(String dateString) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        format.setLenient(false);
        Calendar date = Calendar.getInstance();
        date.setTime(format.parse(dateString));
        return date;
    }

    public boolean isFirstDateBeforeSecondDate(String dateString, String comparisonDateString) throws ParseException {
        Calendar date = convertDateStringToCalendar(dateString);
        Calendar comparisonDate = convertDateStringToCalendar(comparisonDateString);

        if(date.before(comparisonDate)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFirstDateAfterSecondDate(String dateString, String comparisonDateString) throws ParseException {
        Calendar date = convertDateStringToCalendar(dateString);
        Calendar comparisonDate = convertDateStringToCalendar(comparisonDateString);

        if(date.after(comparisonDate)) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isDateStringValid(String dateStr) {
        // not thread safe, create new instance with each method call
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        format.setLenient(false);
        try {
            format.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Evaluates if a date string provided is on or between two other dates.  Will accept an empty string as the third parameter implying
     * there is no end date present
     * @param date
     * @param startDate
     * @param endDate
     * @return
     * @throws ParseException
     */
    public boolean isDateBetweenTwoDates(String date, String startDate, String endDate) throws ParseException {
        if(!isFirstDateBeforeSecondDate(date, startDate) && !isFirstDateAfterSecondDate(date, endDate)) { // data has a start and end date
            return true;
        } else if(!isFirstDateBeforeSecondDate(date, startDate) && endDate.isEmpty()) { // no end date present, implying the end date is open i.e. infinity or not present yet
            return true;
        } else {
            return false;
        }
    }

    /**
     * Evaluates if two given date ranges overlap.  End dates can be empty strings that will be treated as a date range
     * that extends to eternity
     *
     * @param date1Start
     * @param date1End
     * @param date2Start
     * @param date2End
     * @return
     */
    public boolean doDateRangesOverlap(String date1Start, String date1End, String date2Start, String date2End) throws ParseException {
        if(!isFirstDateBeforeSecondDate(date1End, date2Start) || !isFirstDateAfterSecondDate(date1Start, date2End)) {
            return true;
        } else {
            return false;
        }
    }

    public String formatDay(int day) {
        if(day < 10) {
            return  "0" + day;
        } else {
            return String.valueOf(day);
        }
    }

    public String getFormattedMonth() {
        ZonedDateTime arizonaTime = LocalDateTime.now().atZone(ZoneId.of("America/Phoenix"));
        int month = arizonaTime.getMonth().getValue();
        switch(month) {
            case 1:
                return "01";
            case 2:
                return "02";
            case 3:
                return "03";
            case 4:
                return "04";
            case 5:
                return "05";
            case 6:
                return "06";
            case 7:
                return "07";
            case 8:
                return "08";
            case 9:
                return "09";
            case 10:
                return "10";
            case 11:
                return "11";
            case 12:
                return "12";
            default:
                return "err";
        }
    }

    public String formatMonth(int month) {
        switch(month) {
            case 0:
                return "01";
            case 1:
                return "02";
            case 2:
                return "03";
            case 3:
                return "04";
            case 4:
                return "05";
            case 5:
                return "06";
            case 6:
                return "07";
            case 7:
                return "08";
            case 8:
                return "09";
            case 9:
                return "10";
            case 10:
                return "11";
            case 11:
                return "12";
            default:
                return "err";
        }
    }
}
