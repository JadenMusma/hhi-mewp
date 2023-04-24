package net.musma.hhi.middleware.mewp.service;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Slf4j
@Service
@AllArgsConstructor
public class AaProcessService {


    private ConnectionStateService connectionStateService;

    private ResponseService responseService;

    /**
     * 통신이 정상적으로 되는지 확인
     * 단순하게 type을 "AA"로 받고, "BB"로 응답한다.
     */
    @Async
    public void process(String topic, JsonObject payload) {

        String danmalId = payload.get("danmal_id").getAsString();
        String fwVer = payload.get("fw_ver").getAsString();
        String hwVer = payload.get("hw_ver").getAsString();


        JsonObject obj = new JsonObject();
        obj.addProperty("type", "BB");
        obj.addProperty("danmal_id", danmalId);

        responseService.sendResponse(topic, obj);

        //10분에 한번씩만 처리
        if(LocalTime.now().getMinute() % 10 == 0 && LocalTime.now().getSecond() / 10 < 1) {
//            log.debug("============updateFirmwareVersion");
            connectionStateService.updateFirmwareVersion(danmalId, fwVer, hwVer);
        }

//        obj = null;
    }
}
