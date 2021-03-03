package com.api.demo.mongorepositories.applicationpackage.promotions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PromotionController {
    private static Logger logger = LoggerFactory.getLogger(PromotionController.class);

    @Autowired
    private PromotionRepository promotionRepository;

    @GetMapping("/promotions/year")
    public ResponseEntity<Object> getPromotionsForYear(@RequestParam int year) {
        /**
         * Get the promotion for the specified year.
         */
        List<Promotion> promotions = promotionRepository.findByYear(year);
        return new ResponseEntity<>(promotions, HttpStatus.OK);
    }

    @GetMapping("/promotions/month")
    public ResponseEntity<Object> getPromotionByMonth(@RequestParam int month, @RequestParam int year) {
        /**
         * Get the promotion for the specified month and year.
         */
        Promotion promotion = promotionRepository.findByDate(month + "/" + year);
        if(promotion != null) {
            return new ResponseEntity<>(promotion, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Promotion not found for date", HttpStatus.NOT_FOUND);
        }
    }
}
