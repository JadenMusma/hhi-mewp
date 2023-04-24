package net.musma.hhi.middleware.mewp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class SensingState {
    private String dataDiv;
    private String tagId;
    private String deviceId;
    private String eventDiv;
    private String rcvDtm;
    private String latitude;
    private String longitude;
    private String latitudeD;
    private String longitudeD;
    private String tmX;
    private String tmY;
    private String usSensing;
    private String rcvData;

}
