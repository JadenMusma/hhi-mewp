package net.musma.hhi.middleware.mewp.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class ConnectionState {
    private String danmalId;

    private String eventCd;

    private String firstConnectionTime;

    private String lastConnectionTime;

    private String fwVer;

    private String hwVer;
}
