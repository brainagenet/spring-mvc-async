package net.brainage.sample.web.task;

import lombok.extern.slf4j.Slf4j;
import net.brainage.sample.web.model.ProcessingStatus;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ms29.seo on 2016-02-02.
 */
@Slf4j
public class ProcessingTask extends TimerTask {

    private long reqId;
    private AtomicLong concurrentRequests;
    private int processingTimeMs;
    private DeferredResult<ProcessingStatus> deferredResult;

    public ProcessingTask(long reqId, AtomicLong concurrentRequests, int processingTimeMs, DeferredResult<ProcessingStatus> deferredResult) {
        this.reqId = reqId;
        this.concurrentRequests = concurrentRequests;
        this.processingTimeMs = processingTimeMs;
        this.deferredResult = deferredResult;
    }

    @Override
    public void run() {
        long concReqs = concurrentRequests.getAndDecrement();
        if (deferredResult.isSetOrExpired()) {
            log.warn("{}: Processing of non-blocking request #{} already expired.", concReqs, reqId);
        } else {
            boolean deferredStatus = deferredResult.setResult(new ProcessingStatus("OK", processingTimeMs));
            log.debug("{}: Processing of non-blocking request #{} is done, deferredStatus = {}", concReqs, reqId, deferredStatus);
        }
    }

}
