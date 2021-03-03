package com.api.demo.tests;

import com.api.demo.mongorepositories.applicationpackage.promotions.Promotion;
import com.api.demo.mongorepositories.applicationpackage.promotions.PromotionRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

// @SpringBootTest
public class JUnitPromotionTests {
	@Autowired
	private PromotionRepository promotionRepository;

	@Test
	public void test2021PromotionDates() {
		// Feb 2, 10, 11, March 1
		// March 2, 15, 16, 31
		// April 1, 19, 20, May 3
		// May 4, 17, 18, June 1
		// June 2, 14, 15, July 1
		// July 2, 19, 20, August 2
		// Aug 3, 16, 17, 31
		// Sept 1, 20, 21, Oct 4
		// Oct 5, 18, 19, Nov 1
		// Nov 2, 15, 16, 30
		// Dec 1, 13, 14, 31
	}

	// January 1, 18, 19, Feb 1
	@Test
	public void testJanuaryPromotions() {
		int month = 1;
		int year = 2021;
		Promotion januaryPromotion = promotionRepository.findByDate(month + "/" + year);
		assertEquals(1, januaryPromotion.getMonth());
		assertEquals(2, januaryPromotion.getNextMonthInteger());
		assertEquals(12, januaryPromotion.getPreviousMonthInteger());
		assertEquals(2021, januaryPromotion.getNextYearInteger());
		assertEquals(2020, januaryPromotion.getPreviousYearInteger());
		assertEquals(2021, januaryPromotion.getYear());
		assertEquals("12/2020", januaryPromotion.getPreviousMonthsQueryString());
		assertEquals("2/2020", januaryPromotion.getNextMonthsQueryString());
		// assertEquals("01", januaryPromotion.addZeroPrefixToDateInteger(month));


		Promotion decemberPromotion = promotionRepository.findByDate(januaryPromotion.getPreviousMonthsQueryString());
		assertEquals(12, decemberPromotion.getMonth());
		assertEquals(2020, decemberPromotion.getYear());
		Promotion februaryPromotion = promotionRepository.findByDate(januaryPromotion.getNextMonthsQueryString());
		assertEquals(2, februaryPromotion.getMonth());
		assertEquals(2021, februaryPromotion.getYear());

		// January 1, 18, 19, Feb 1
		// assertEquals("01/01/2021", januaryPromotion.getFormattedStartDate(decemberPromotion.getSecondHalfEndDate()));
		// assertEquals("01/18/2021", januaryPromotion.getFormattedFirstHalfEndDate());
		// assertEquals("01/19/2021", januaryPromotion.getFormattedSecondHalfStartDate());
		// assertEquals("02/01/2021", januaryPromotion.getFormattedEndDate(februaryPromotion.getFirstHalfStartDate()));

	}






	/*
			1) Test all the mid-months for 2020 and 2021
			2) Test all the new month changeovers for 2020 and 2021
			3) Test month scenarios for promotion starting previous month and next month
			4) Test all the promotion starts and ends for 2020 and 2021
			5) Test the new year roll-over for 2020 and 2021
		 */
}
