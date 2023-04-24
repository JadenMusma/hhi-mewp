package net.musma.hhi.middleware.mewp.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.musma.hhi.middleware.mewp.dto.ReceiveData;
import net.musma.hhi.middleware.mewp.entity.EnvInfo;
import net.musma.hhi.middleware.mewp.entity.EnvInfoByDanmalId;
import net.musma.hhi.middleware.mewp.repository.EnvInfoRepository;
import net.musma.hhi.middleware.mewp.repository.ReceiveDataRepository;
import net.musma.hhi.middleware.mewp.websocket.WebSocketHandler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ErProcessService {

    private EnvInfoService envInfoService;
    private ResponseService responseService;

    private ReceiveDataRepository receiveDataRepository;

    private WebSocketHandler webSocketHandler;

    private EnvInfoRepository envInfoRepository;


    @Async
    public void process(String topic, JsonObject payload) {

        Gson gson = new Gson();
        try {

            String danmalId =  payload.get("danmal_id").getAsString();

            List<EnvInfo> envInfo = envInfoService.getEnvInfo(danmalId,  payload.get("ip_addr").getAsString());

            if (null != envInfo && !envInfo.isEmpty()) {
                String val = envInfo.get(0).getVal();
                System.out.println(val);
                JsonObject rtnObj = new JsonObject();
                rtnObj.addProperty("type", "ER");
                rtnObj.addProperty("mstr_tag_id", val.substring(0, 10));
                rtnObj.addProperty("wtng_chk_batry", val.substring(10, 13));
                rtnObj.addProperty("wtng_egn_on", val.substring(13, 16));
                rtnObj.addProperty("evnt_snd_intvl", val.substring(16, 19));
                rtnObj.addProperty("cert_wait_tm", val.substring(19, 22));
                rtnObj.addProperty("voice_msg_tm", val.substring(22, 25));
                rtnObj.addProperty("eqpknd", val.substring(34, 37).trim());
                rtnObj.addProperty("danmal_id", val.substring(37, 47));


                //단말로 전송
                responseService.sendResponse(topic, rtnObj);

                String lastUpdatedDtm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

                //데이터 저장
                ReceiveData rd = ReceiveData.builder()
                        .dtm(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                        .rsDiv("SND")
                        .deviceId(danmalId)
                        .dataDiv( payload.get("type").getAsString())
                        .data( payload.get("type").getAsString() + val)
                        .build();

                receiveDataRepository.save(rd);

                webSocketHandler.sendMessage( WebSocketHandler.WS_LOG ,  "SND DATA :%s, 환경 정보 SEND".formatted(val));

            } else {
                //PKG_HOCCOM_EXP_200717.SEARCH_DATA2 조회
                List<EnvInfoByDanmalId> envInfoByDanmal = envInfoRepository.findByDanmalId(danmalId,  payload.get("ip_addr").getAsString());

                //조회해온 내용으로 데이터 전송
                if (!envInfoByDanmal.isEmpty()) {

                    String val = envInfoByDanmal.get(0).getVal();

                    JsonObject rtnObj = new JsonObject();
                    rtnObj.addProperty("type", "ER");
                    rtnObj.addProperty("mstr_tag_id", val.substring(0, 8));
                    rtnObj.addProperty("wtng_chk_batry", val.substring(8, 11));
                    rtnObj.addProperty("wtng_egn_on", val.substring(11, 14));
                    rtnObj.addProperty("evnt_snd_intvl", val.substring(14, 17));
                    rtnObj.addProperty("cert_wait_tm", val.substring(17, 20));
                    rtnObj.addProperty("voice_msg_tm", val.substring(20, 23));
                    rtnObj.addProperty("eqpknd", val.substring(31, 34).trim());
                    rtnObj.addProperty("danmal_id", val.substring(34, 44));
                    //단말로 전송
                    responseService.sendResponse(topic, rtnObj);

                    ReceiveData rd = ReceiveData.builder()
                            .dtm(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                            .rsDiv("SND")
                            .deviceId(danmalId)
                            .dataDiv( payload.get("type").getAsString())
                            .data( payload.get("type").getAsString() + val)
                            .build();

                    receiveDataRepository.save(rd);
                    webSocketHandler.sendMessage( WebSocketHandler.WS_LOG ,  "SND DATA :%s, 환경 정보 SEND".formatted(val));

                } else {
                    webSocketHandler.sendMessage( WebSocketHandler.WS_LOG ,  "ER 데이터 조회 안됨.(%s)".formatted(gson.toJson(payload)));
                    log.info("ER 데이터 조회 안됨.({})", gson.toJson(payload));
                }
            }
        } catch (Exception e) {
            log.warn("환경정보 프로토콜 처리 오류" + ", RcvData: " + gson.toJson(payload) + "\n" + e.toString());
        }
    }
}
