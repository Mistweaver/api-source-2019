package com.api.demo.pricedata.repositories.log;

import com.api.demo.pricedata.repositories.pricedata.PriceData;
import com.api.demo.pricedata.repositories.pricedata.PriceDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.List;

@RestController
public class PriceDataLogController {
    @Autowired
    private PriceDataLogRepository priceDataLogRepository;
    @Autowired
    private PriceDataRepository priceDataRepository;

    /**
     * Return the logs for the price data and it's previous iterations
     */
    @GetMapping("/pricedata/{id}/logs")
    @ResponseBody
    public ResponseEntity<Object> getEquationActiveData(@PathVariable String id) {
        try {
            List<LogEntry> logs = new ArrayList<>();
            // find the price data
            PriceData priceData = priceDataRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            logs = this.getLogForPriceData(priceData);
            return new ResponseEntity<>(logs, HttpStatus.ACCEPTED);
        } catch (HttpStatusCodeException e) {
            return new ResponseEntity<>("Could not find price data with provided id: " + id, HttpStatus.NOT_FOUND);
        }
    }

    private List<LogEntry> getLogForPriceData(PriceData priceData) {
        List<LogEntry> logs = new ArrayList<>();
        // get the logs
        logs.addAll(priceDataLogRepository.findByPriceDataId(priceData.getId()));
        // if the price data has been created from previous data, retrieve the logs from the old data as well
        String createdFromPriceDataId = priceData.getCreatedFromPriceDataId();
        if(!createdFromPriceDataId.isEmpty()) {
            try {
                priceDataRepository.findById(createdFromPriceDataId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                logs.addAll(this.getLogForPriceData(priceData));
            } catch (HttpStatusCodeException e) {
                // could not find previous data
                LogEntry notFoundEntry = new LogEntry();
                notFoundEntry.setPriceDataId(createdFromPriceDataId);
                notFoundEntry.setEntry("Could not find older logs with provided id " + createdFromPriceDataId);
                logs.add(notFoundEntry);
            }
        }

        return logs;

    }
}
