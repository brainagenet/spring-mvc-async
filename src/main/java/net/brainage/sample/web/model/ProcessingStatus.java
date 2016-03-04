package net.brainage.sample.web.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ms29.seo on 2016-02-02.
 */
@Data
@XmlRootElement
public class ProcessingStatus {

    private static final String PROCESSING_STAUTS_UNKNOWN = "UNKNOWN";

    @XmlElement
    private final String status;

    @XmlElement(name = "processingTime")
    private final int processingTimeMs;

    public ProcessingStatus() {
        status = PROCESSING_STAUTS_UNKNOWN;
        processingTimeMs = -1;
    }

    public ProcessingStatus(String status, int processingTimeMs) {
        this.status = status;
        this.processingTimeMs = processingTimeMs;
    }


}
