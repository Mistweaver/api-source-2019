package com.api.demo.tests;

import com.api.demo.pricedata.functions.ArizonaDateTimeComponent;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.*;

public class JUnitArizonaDateTimeTest {
	// documentation to follow
	// https://stackabuse.com/how-to-test-a-spring-boot-application/
	// https://www.baeldung.com/spring-boot-testing
	// https://www.javatpoint.com/junit-tutorial

	@Test(expected = NullPointerException.class)
	public void checkParseExceptionThrownNull() {
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
		arizonaDateTimeComponent.isDateStringValid(null);
	}

	@Test
	public void checkIsDateStringValid() {
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
		String yesterday = arizonaDateTimeComponent.getYesterdaysDateString();
		String today = arizonaDateTimeComponent.getTodayDateString();
		String tomorrow = arizonaDateTimeComponent.getTomorrowsDateString();

		assertFalse(arizonaDateTimeComponent.isDateStringValid(""));
		assertFalse(arizonaDateTimeComponent.isDateStringValid("5"));
		assertFalse(arizonaDateTimeComponent.isDateStringValid("13/111/424"));
		assertFalse(arizonaDateTimeComponent.isDateStringValid("13/04/2021"));

		assertTrue(arizonaDateTimeComponent.isDateStringValid(yesterday));
		assertTrue(arizonaDateTimeComponent.isDateStringValid(today));
		assertTrue(arizonaDateTimeComponent.isDateStringValid(tomorrow));
		assertTrue(arizonaDateTimeComponent.isDateStringValid("02/11/2021"));
		assertTrue(arizonaDateTimeComponent.isDateStringValid("08/17/2061"));
	}

	@Test
	public void areDatesTheSame() throws Exception {
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
		String yesterday = arizonaDateTimeComponent.getYesterdaysDateString();
		String today = arizonaDateTimeComponent.getTodayDateString();
		String tomorrow = arizonaDateTimeComponent.getTomorrowsDateString();

		assertTrue(arizonaDateTimeComponent.areDatesTheSame(yesterday, yesterday));
		assertFalse(arizonaDateTimeComponent.areDatesTheSame(yesterday, today));
		assertFalse(arizonaDateTimeComponent.areDatesTheSame(yesterday, tomorrow));
		assertTrue(arizonaDateTimeComponent.areDatesTheSame(today, today));
		assertFalse(arizonaDateTimeComponent.areDatesTheSame(today, tomorrow));
		assertTrue(arizonaDateTimeComponent.areDatesTheSame(tomorrow, tomorrow));

		assertFalse(arizonaDateTimeComponent.areDatesTheSame("03/04/2020", "04/05/2021"));
		assertFalse(arizonaDateTimeComponent.areDatesTheSame("03/04/2021", "04/05/2021"));
		assertFalse(arizonaDateTimeComponent.areDatesTheSame("03/04/2020", "04/05/2021"));
	}

	@Test
	public void isGivenDateToday() {
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
		String yesterday = arizonaDateTimeComponent.getYesterdaysDateString();
		String today = arizonaDateTimeComponent.getTodayDateString();
		String tomorrow = arizonaDateTimeComponent.getTomorrowsDateString();

		assertFalse(arizonaDateTimeComponent.isGivenDateToday(yesterday));
		assertTrue(arizonaDateTimeComponent.isGivenDateToday(today));
		assertFalse(arizonaDateTimeComponent.isGivenDateToday(tomorrow));
		assertFalse(arizonaDateTimeComponent.isGivenDateToday("02/12/2020"));
		assertFalse(arizonaDateTimeComponent.isGivenDateToday("01"));
		assertFalse(arizonaDateTimeComponent.isGivenDateToday("01/31/3000"));

	}

	@Test
	public void isDateBeforeToday() throws Exception {
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
		String yesterday = arizonaDateTimeComponent.getYesterdaysDateString();
		String today = arizonaDateTimeComponent.getTodayDateString();
		String tomorrow = arizonaDateTimeComponent.getTomorrowsDateString();

		assertTrue(arizonaDateTimeComponent.isDateBeforeToday(yesterday));
		assertFalse(arizonaDateTimeComponent.isDateBeforeToday(today));
		assertFalse(arizonaDateTimeComponent.isDateBeforeToday(tomorrow));
		assertTrue(arizonaDateTimeComponent.isDateBeforeToday("02/12/2020"));
		assertFalse(arizonaDateTimeComponent.isDateBeforeToday("01/31/3000"));

		// error cases
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isDateBeforeToday(""));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isDateBeforeToday("01"));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isDateBeforeToday("13/111/424"));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isDateBeforeToday("13/04/2021"));
	}

	@Test
	public void isDateAfterToday() throws Exception {
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
		String yesterday = arizonaDateTimeComponent.getYesterdaysDateString();
		String today = arizonaDateTimeComponent.getTodayDateString();
		String tomorrow = arizonaDateTimeComponent.getTomorrowsDateString();

		assertFalse(arizonaDateTimeComponent.isDateAfterToday(yesterday));
		assertFalse(arizonaDateTimeComponent.isDateAfterToday(today));
		assertTrue(arizonaDateTimeComponent.isDateAfterToday(tomorrow));
		assertFalse(arizonaDateTimeComponent.isDateAfterToday("02/12/2020"));
		assertTrue(arizonaDateTimeComponent.isDateAfterToday("01/31/3000"));

		// error cases
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isDateAfterToday(""));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isDateAfterToday("01"));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isDateAfterToday("13/111/424"));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isDateAfterToday("13/04/2021"));
	}

	@Test
	public void testStringToCalendarConversionError() {
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.convertDateStringToCalendar(""));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.convertDateStringToCalendar("01"));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.convertDateStringToCalendar("13/111/424"));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.convertDateStringToCalendar("13/04/2021"));
	}

	@Test
	public void isFirstDateBeforeSecondDate() throws Exception {
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
		String yesterday = arizonaDateTimeComponent.getYesterdaysDateString();
		String today = arizonaDateTimeComponent.getTodayDateString();
		String tomorrow = arizonaDateTimeComponent.getTomorrowsDateString();

		assertTrue(arizonaDateTimeComponent.isFirstDateBeforeSecondDate(yesterday, today));
		assertTrue(arizonaDateTimeComponent.isFirstDateBeforeSecondDate(yesterday, tomorrow));
		assertFalse(arizonaDateTimeComponent.isFirstDateBeforeSecondDate(yesterday, yesterday));
		assertFalse(arizonaDateTimeComponent.isFirstDateBeforeSecondDate(today, yesterday));
		assertFalse(arizonaDateTimeComponent.isFirstDateBeforeSecondDate(today, today));
		assertTrue(arizonaDateTimeComponent.isFirstDateBeforeSecondDate(today, tomorrow));
		assertFalse(arizonaDateTimeComponent.isFirstDateBeforeSecondDate(tomorrow, yesterday));
		assertFalse(arizonaDateTimeComponent.isFirstDateBeforeSecondDate(tomorrow, today));
		assertFalse(arizonaDateTimeComponent.isFirstDateBeforeSecondDate(tomorrow, tomorrow));

		assertTrue(arizonaDateTimeComponent.isFirstDateBeforeSecondDate("01/01/2021", "02/11/2021"));
		assertTrue(arizonaDateTimeComponent.isFirstDateBeforeSecondDate("01/01/2020", "02/11/2021"));
		assertFalse(arizonaDateTimeComponent.isFirstDateBeforeSecondDate("01/01/3000", "02/11/2021"));

		assertFalse(arizonaDateTimeComponent.isFirstDateBeforeSecondDate("02/11/2021", "01/01/2021"));
		assertFalse(arizonaDateTimeComponent.isFirstDateBeforeSecondDate("02/11/2021", "01/01/2020"));
		assertTrue(arizonaDateTimeComponent.isFirstDateBeforeSecondDate("02/11/2021", "01/01/3000"));

		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isFirstDateBeforeSecondDate("01/01/2021", ""));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isFirstDateBeforeSecondDate("01", "02/11/2021"));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isFirstDateBeforeSecondDate("13/111/424", "02/11/2021"));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isFirstDateBeforeSecondDate("01/01/2021", "13/04/2021"));
	}


	@Test
	public void isFirstDateAfterSecondDate() throws Exception {
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
		String yesterday = arizonaDateTimeComponent.getYesterdaysDateString();
		String today = arizonaDateTimeComponent.getTodayDateString();
		String tomorrow = arizonaDateTimeComponent.getTomorrowsDateString();

		assertFalse(arizonaDateTimeComponent.isFirstDateAfterSecondDate(yesterday, today));
		assertFalse(arizonaDateTimeComponent.isFirstDateAfterSecondDate(yesterday, tomorrow));
		assertFalse(arizonaDateTimeComponent.isFirstDateAfterSecondDate(yesterday, yesterday));
		assertTrue(arizonaDateTimeComponent.isFirstDateAfterSecondDate(today, yesterday));
		assertFalse(arizonaDateTimeComponent.isFirstDateAfterSecondDate(today, today));
		assertFalse(arizonaDateTimeComponent.isFirstDateAfterSecondDate(today, tomorrow));
		assertTrue(arizonaDateTimeComponent.isFirstDateAfterSecondDate(tomorrow, yesterday));
		assertTrue(arizonaDateTimeComponent.isFirstDateAfterSecondDate(tomorrow, today));
		assertFalse(arizonaDateTimeComponent.isFirstDateAfterSecondDate(tomorrow, tomorrow));

		assertFalse(arizonaDateTimeComponent.isFirstDateAfterSecondDate("01/01/2021", "02/11/2021"));
		assertFalse(arizonaDateTimeComponent.isFirstDateAfterSecondDate("01/01/2020", "02/11/2021"));
		assertTrue(arizonaDateTimeComponent.isFirstDateAfterSecondDate("01/01/3000", "02/11/2021"));

		assertTrue(arizonaDateTimeComponent.isFirstDateAfterSecondDate("02/11/2021", "01/01/2021"));
		assertTrue(arizonaDateTimeComponent.isFirstDateAfterSecondDate("02/11/2021", "01/01/2020"));
		assertFalse(arizonaDateTimeComponent.isFirstDateAfterSecondDate("02/11/2021", "01/01/3000"));

		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isFirstDateAfterSecondDate("01/01/2021", ""));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isFirstDateAfterSecondDate("01", "02/11/2021"));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isFirstDateAfterSecondDate("13/111/424", "02/11/2021"));
		assertThrows(ParseException.class, () -> arizonaDateTimeComponent.isFirstDateAfterSecondDate("01/01/2021", "13/04/2021"));
	}


}
