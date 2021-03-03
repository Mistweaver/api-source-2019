package com.api.demo.pricedata.controllers;

import com.api.demo.pricedata.repositories.pricedata.PriceData;
import com.api.demo.pricedata.repositories.pricedata.PriceDataRepository;
import com.api.demo.pricedata.restpackages.DataIdListPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;

@RestController
@RequestMapping("/pricedata/pending")
public class PendingDataController {
	private static Logger logger = LoggerFactory.getLogger(DraftsController.class);


	@Autowired
	private PriceDataRepository priceDataRepository;

	// return pending data back to a draft
	/**** Validate drafts and push them to the pending queue ****/
	@PostMapping("/cancel")
	@ResponseBody
	public ResponseEntity<Object> cancelPendingData(@RequestBody DataIdListPackage cancelPendingRequest) {
		try {
			List<String> priceDataIds = cancelPendingRequest.getPriceDataIds();
			// get each price data by id
			for(String id: priceDataIds) {
				try {
					PriceData data = priceDataRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
					// if the data is pending, return the data back to a draft
					if(data.isPending()) {
						data.setToDraft();
						priceDataRepository.save(data);
					} else {
						// log as error
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			return new ResponseEntity<>(cancelPendingRequest, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
		}
	}


}
