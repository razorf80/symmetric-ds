package org.jumpmind.symmetric.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.Monitor;
import org.jumpmind.symmetric.model.MonitorEvent;
import org.jumpmind.symmetric.util.LogSummaryAppenderUtils;
import org.jumpmind.util.LogSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class MonitorTypeLog implements IMonitorType, ISymmetricEngineAware, IBuiltInExtensionPoint {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    ISymmetricEngine engine;
    
    @Override
    public void setSymmetricEngine(ISymmetricEngine engine) {
        this.engine = engine;
    }
    
    @Override
    public MonitorEvent check(Monitor monitor) {
        List<LogSummary> all = new ArrayList<LogSummary>();
        MonitorEvent event = new MonitorEvent();
        
        if (monitor.getSeverityLevel() == Monitor.SEVERE) {
            all.addAll(LogSummaryAppenderUtils.getLogSummaryErrors(engine.getEngineName()));
        } else if (monitor.getSeverityLevel() == Monitor.WARNING) {
            all.addAll(LogSummaryAppenderUtils.getLogSummaryWarnings(engine.getEngineName()));
        }
        
        Collections.sort(all);
        
        int count = 0;
        for (LogSummary logSummary : all) {
            count += logSummary.getCount();
        }

        event.setDetails(serializeDetails(all));
        event.setValue(all.size());
        event.setCount(count);
        
        return event;
    }

    protected String serializeDetails(List<LogSummary> logs) {
        String result = null;
        try {
            result = new Gson().toJson(logs);
        } catch(Exception e) {
            log.warn("Unable to convert list of logs to JSON", e);
        }
       
        return result;
    }
    
    @Override
    public boolean requiresClusterLock() {
        return false;
    }

    @Override
    public String getName() {
        return "log";
    }
}
