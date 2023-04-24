package net.musma.hhi.middleware.mewp.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatusReport {

    private String dtm;
    private String rsDiv;
    private String deviceId;
    private String dataDiv;
    private String eventDiv;
    private String data;
    private String statusCode;
    private String reserved;

}
