package net.musma.hhi.middleware.mewp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class DbResponse {
    private String appCode;
    private String appMsg;
}
