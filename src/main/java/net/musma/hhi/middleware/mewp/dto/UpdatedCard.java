package net.musma.hhi.middleware.mewp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class UpdatedCard {
    private String gbn;
    private String danmalId;
    private String ipAddr;
    private String updDtm;
}
