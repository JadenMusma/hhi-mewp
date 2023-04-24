package net.musma.hhi.middleware.mewp.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReceiveData {
    private String dtm;
    private String rsDiv;
    private String deviceId;
    private String dataDiv;
    private String eventDiv;
    private String data;

}
