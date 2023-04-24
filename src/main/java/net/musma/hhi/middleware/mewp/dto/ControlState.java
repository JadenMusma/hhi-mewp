package net.musma.hhi.middleware.mewp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class ControlState {
    private String dataDiv;
    private String tagId;
    private String deviceId;
    private String eventDiv;
    private String rcvDtm;
    private String crStartYn;
    private String ulDiv;
    private String latitude;
    private String longitude;
    private String latitudeD;
    private String longitudeD;
    private String tmX;
    private String tmY;
    private String r1;
    private String r2;
    private String r3;
    private String rcvData;

}
