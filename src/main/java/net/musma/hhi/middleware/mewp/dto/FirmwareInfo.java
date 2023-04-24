package net.musma.hhi.middleware.mewp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class FirmwareInfo {
    private String fwFile;

    private long fwSize;

    private long totalSegCnt;

    private String hwVer;
}
