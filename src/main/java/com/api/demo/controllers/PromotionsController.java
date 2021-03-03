package com.api.demo.controllers;

import com.api.demo.mongorepositories.applicationpackage.promotionmodellist.PromotionModelList;
import com.api.demo.mongorepositories.applicationpackage.promotionmodellist.PromotionModelListRepository;
import com.api.demo.mongorepositories.applicationpackage.promotions.PromotionRepository;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOfficeRepository;
import com.api.demo.pricedata.repositories.pricedata.PriceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.api.demo.pricedata.repositories.pricedata.PriceDataStatus.PENDING;


@RestController
public class PromotionsController {
	private static Logger logger = LoggerFactory.getLogger(PromotionsController.class);

	@Autowired
	private PromotionModelListRepository promotionModelListRepository;
	@Autowired
	private PriceDataRepository priceDataRepository;
	@Autowired
	private PromotionRepository promotionRepository;
	@Autowired
	private SalesOfficeRepository salesOfficeRepository;

	@PostMapping("/promotionmodellist/add/{locationId}/{promotionId}")
	@ResponseBody public ResponseEntity<Object> createPromotionModelList(@PathVariable("locationId") String locationId, @PathVariable("promotionId") String promotionId) {
		try {
			PromotionModelList newList = new PromotionModelList();
			newList.setLocationId(locationId);
			newList.setPromotionId(promotionId);
			promotionModelListRepository.save(newList);
			return new ResponseEntity<>(newList, HttpStatus.CREATED);
		} catch(Exception e) {
			return new ResponseEntity<>("Creation failure: " + e.getMessage(), HttpStatus.CONFLICT);
		}
	}

	@PostMapping("/promotionmodellist/update")
	@ResponseBody public ResponseEntity<Object> updatePromotionModelList(@RequestBody PromotionModelList updatedList) {
		try {
			promotionModelListRepository.save(updatedList);
			return new ResponseEntity<>(updatedList, HttpStatus.CREATED);
		} catch(Exception e) {
			return new ResponseEntity<>("Creation failure: " + e.getMessage(), HttpStatus.CONFLICT);
		}
	}

	@GetMapping("/promotions/lists/promotion")
	public ResponseEntity<Object> getPromotionListsByPromotion(@RequestParam("promotionId") String promotionId) {
		// return package
		// List<PromotionDataPackage> response = new ArrayList<>();
		// get all promotions pending approval
		List<PromotionModelList> listsForPromotion = promotionModelListRepository.findByPromotionId(promotionId);
		/*for(PromotionModelList list : listsForPromotion ) {
			response.add(buildPromoDataPackage(list));
		}*/
		// return new ResponseEntity<>(response, HttpStatus.OK);
		return new ResponseEntity<>("", HttpStatus.OK);
	}

	@GetMapping("/promotions/lists/approval")
	public ResponseEntity<Object> getPromotionsAwaitingApproval() {
		// return package
		// List<PromotionDataPackage> response = new ArrayList<>();
		// get all promotions pending approval
		List<PromotionModelList> listsPendingApproval = promotionModelListRepository.findByListState(PENDING);
		/*for(PromotionModelList list : listsPendingApproval) {
			response.add(buildPromoDataPackage(list));
		}*/
		// return new ResponseEntity<>(response, HttpStatus.OK);
		return new ResponseEntity<>("", HttpStatus.OK);

	}

	/*private PromotionDataPackage buildPromoDataPackage(PromotionModelList list) {
		// get the promotion
		Promotion promotion = promotionRepository.findById(list.getPromotionId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
		// get the sales office info
		SalesOffice office = salesOfficeRepository.findById(list.getLocationId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
		// get the price data for the model and location
		List<PriceData> listData = new ArrayList<>();
		for(String modelId : list.getModelIds()) {
			PriceData data = priceDataRepository.findByModelIdAndLocationId(modelId, list.getLocationId());
			listData.add(data);
		}
		// create package
		PromotionDataPackage data = new PromotionDataPackage(promotion, office, listData, list);
		return data;
	}*/



	// Overrides to prevent adding or updating objects that cause a conflict
	@PostMapping("/promotionmodellist")
	@ResponseBody public ResponseEntity<String> promoModelListPost() {
		return new ResponseEntity<>("Please send POST to /promotionmodellist/add", HttpStatus.CONFLICT);
	}
	@PutMapping("/promotionmodellist")
	@ResponseBody public ResponseEntity<String> promoModelListPut() {
		return new ResponseEntity<>("Please send PUT to /promotions/list/", HttpStatus.CONFLICT);
	}
}
