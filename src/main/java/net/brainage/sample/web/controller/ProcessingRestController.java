package net.brainage.sample.web.controller;

import com.sun.management.UnixOperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import net.brainage.sample.web.model.ProcessingStatus;
import net.brainage.sample.web.task.ProcessingTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ms29.seo on 2016-02-02.
 */
@Slf4j
@RestController
public class ProcessingRestController {

    private static OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private static final AtomicLong lastRequestId = new AtomicLong(0);
    private static final AtomicLong concurrentRequests = new AtomicLong(0);
    private static long maxConcurrentRequests = 0;

    private Timer timer = new Timer();

    private int defaultMinMs = 500;
    private int defaultMaxMs = 1000;

    @Value("${statistics.requestsPerLog}")
    private int statisticsRequestsPerLog;

    @RequestMapping(path = "default-processing-time", method = RequestMethod.GET)
    public String setDefaultProcessingTime(
            @RequestParam(value = "minMs", required = false) int minMs,
            @RequestParam(value = "maxMs", required = false) int maxMs) {
        this.defaultMaxMs = maxMs;
        this.defaultMinMs = minMs;
        return String.format("Set default response time to %d - %d", this.defaultMinMs, this.defaultMaxMs);
    }


    @RequestMapping(path = "/process-blocking")
    public ProcessingStatus blockingProcessing(
            @RequestParam(value = "minMs", required = false, defaultValue = "0") int minMs,
            @RequestParam(value = "maxMs", required = false, defaultValue = "0") int maxMs) {

        long reqId = lastRequestId.getAndIncrement();
        long concReqs = concurrentRequests.getAndIncrement();
        updateStatistics(reqId, concReqs);

        int processingTimeMs = calculateProcessingTime(minMs, maxMs);
        log.debug("{}: Start blocking request #{}, processing time: {} ms.", concReqs, reqId, processingTimeMs);

        try {
            Thread.sleep(processingTimeMs);
        } catch (InterruptedException e) {
        } finally {
            concurrentRequests.decrementAndGet();
            log.debug("{}: Processing of blocking request #{} is done.", concReqs, reqId);
        }
        return new ProcessingStatus("OK", processingTimeMs);
    }

    @RequestMapping(path = "/process-non-blocking")
    public DeferredResult<ProcessingStatus> nonBlockingProcessing(
            @RequestParam(value = "minMs", required = false, defaultValue = "0") int minMs,
            @RequestParam(value = "maxMs", required = false, defaultValue = "0") int maxMs) {
        long reqId = lastRequestId.getAndIncrement();
        long concReqs = concurrentRequests.getAndIncrement();
        updateStatistics(reqId, concReqs);

        int processingTimeMs = calculateProcessingTime(minMs, maxMs);
        log.debug("{}: Start non-blocking request #{}, processing time: {} ms.", concReqs, reqId, processingTimeMs);

        // Create the deferredResult and initiate a callback object, task, with it
        DeferredResult<ProcessingStatus> deferredResult = new DeferredResult<>();
        deferredResult.onCompletion(() -> {
            log.debug("---------- completed ----------");
        });
        ProcessingTask timerTask = new ProcessingTask(reqId, concurrentRequests, processingTimeMs, deferredResult);

        // Schedule the task for async completion in the future
        timer.schedule(timerTask, processingTimeMs);

        log.debug("{}: Processing of non-blocking request #{} leave the request thread", concReqs, reqId);

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }


    private int calculateProcessingTime(int minMs, int maxMs) {
        if (minMs == 0 && maxMs == 0) {
            minMs = defaultMinMs;
            maxMs = defaultMaxMs;
        }

        if (maxMs < minMs) {
            maxMs = minMs;
        }

        int processingTimeMs = minMs + (int) (Math.random() * (maxMs - minMs));
        return processingTimeMs;
    }

    private void updateStatistics(long reqId, long concReqs) {
        if (concReqs > maxConcurrentRequests) {
            maxConcurrentRequests = concReqs;
        }

        if (reqId % statisticsRequestsPerLog == 0 && reqId > 0) {
            Object openFiles = "UNKNOWN";
            if (os instanceof UnixOperatingSystemMXBean) {
                openFiles = ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount();
            }
            log.info("Statistics: noOfReqs: {}, maxConcReqs: {}, openFiles: {}", reqId, concReqs, openFiles);
        }

    }


}
