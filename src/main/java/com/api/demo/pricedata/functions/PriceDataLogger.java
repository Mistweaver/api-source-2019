package com.api.demo.pricedata.functions;

import com.api.demo.pricedata.repositories.log.LogEntry;
import com.api.demo.pricedata.repositories.log.PriceDataLogRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class PriceDataLogger {

    @Autowired
    private PriceDataLogRepository priceDataLogRepository;
    private Gson gson = new Gson();
    private ArizonaDateTimeComponent arizonaDateTimeComponent;

    public PriceDataLogger() {}

    public void createNewLogEntry(String id, String entry, Object dataBeforeChange, Object dataAfterChange) {
        LogEntry logEntry = new LogEntry();
        logEntry.setPriceDataId(id);
        logEntry.setDate(arizonaDateTimeComponent.getTodayDateString());
        logEntry.setEntry(entry);
        logEntry.setDataBeforeChange(gson.toJson(dataBeforeChange));
        logEntry.setDataAfterChange(gson.toJson(dataAfterChange));

        priceDataLogRepository.save(logEntry);
    }

    public void deleteLogEntries(String id) {
        List<LogEntry> logs = priceDataLogRepository.findByPriceDataId(id);
        for(int i = 0; i < logs.size(); i++) {
            priceDataLogRepository.delete(logs.get(i));
        }
    }

}
