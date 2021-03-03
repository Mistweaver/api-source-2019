package com.api.demo.pricedata.functions;


import com.api.demo.mongorepositories.applicationpackage.promotions.PromotionRepository;
import com.api.demo.pricedata.controllers.ActiveDataController;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PromotionTests {
	@Autowired
	private PromotionRepository promotionRepository;
	@Autowired
	private ActiveDataController activeDataController;

	/**
	 * 2021 Promotion Dates
	 * Format: Month Start, Last Day of first half, start of second half, end of month
	 *
	 * January 1, 18, 19, Feb 1
	 * Feb 2, 15, 16, March 1
	 * March 2, 15, 16, 31
	 * April 1, 19, 20, May 3
	 * May 4, 17, 18, June 1
	 * June 2, 14, 15, July 1
	 * July 2, 19, 20, August 2
	 * Aug 3, 16, 17, 31
	 * Sept 1, 20, 21, Oct 4
	 * Oct 5, 18, 19, Nov 1
	 * Nov 2, 15, 16, 30
	 * Dec 1, 13, 14, 31
	 *
	 */


	// January 1st, 18th, 19th, February 1st
	@Test
	public void testJanuary() {

	}
}
