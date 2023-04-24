package net.musma.hhi.middleware.mewp.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.musma.hhi.middleware.mewp.coordinates.Point;
import net.musma.hhi.middleware.mewp.dto.ConnectionState;
import net.musma.hhi.middleware.mewp.dto.DbResponse;
import net.musma.hhi.middleware.mewp.dto.ReceiveData;
import net.musma.hhi.middleware.mewp.dto.TagCertification;
import net.musma.hhi.middleware.mewp.repository.ReceiveDataRepository;
import net.musma.hhi.middleware.mewp.repository.TagCertificationRepository;
import net.musma.hhi.middleware.mewp.websocket.WebSocketHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
@AllArgsConstructor
public class CrProcessService {

    private WebSocketHandler webSocketHandler;

    private CoordinateService coordinateService;

    private TagCertificationRepository tagCertificationRepository;

    private ResponseService responseService;

    private ReceiveDataRepository receiveDataRepository;

    private ConnectionStateService connectionStateService;

//    private EnvInfoService envInfoService;


    @Async
    public void process(String topic, JsonObject payload) {

        Gson gson = new Gson();

        try {
            String danmalId =  payload.get("danmal_id").getAsString();


            LocalDateTime dateTime;
            String eventDtm ="";

            if(!payload.get("event_dtm").isJsonNull()){
                //만약 단말에서 시간을 보내지 못하면 현재 시간으로 입력
                eventDtm = payload.get("event_dtm").getAsString();
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            try {
                dateTime = LocalDateTime.parse(eventDtm, formatter);
            } catch (DateTimeParseException e) {
                dateTime = LocalDateTime.now();
            }
            eventDtm = dateTime.format(formatter);

            log.debug("====== eventDtm : " + eventDtm);

            String lat = "";
            String lon = "";

            Point tmPoint = new Point(0,0);

            if(!payload.get("latitude").isJsonNull() && !payload.get("longitude").isJsonNull()) {
                lat = payload.get("latitude").getAsString();
                lon = payload.get("longitude").getAsString();

                tmPoint = coordinateService.tmCalc(lat, lon);
            }

            String mwId = danmalId;

            webSocketHandler.sendMessage( WebSocketHandler.WS_LOG , "인증 RCV, TAG: %s, DEVICE: %s, 상/하부: %s".formatted(  payload.get("tag_id").getAsString(), danmalId,  payload.get("ul_gbn").getAsString() ));
            webSocketHandler.sendMessage( WebSocketHandler.WS_LOG ,  "위도: %s, 경도: %s, TM X: %s, TM Y: %s".formatted(  payload.get("latitude").getAsString(),  payload.get("longitude").getAsString(), tmPoint.getX(), tmPoint.getY() ));

            //여기서 부터는 DB를 기존과 동일하게 맞추기 위해 세팅해 주는 부분
            if(StringUtils.isBlank(lat) || StringUtils.isBlank(lon)){
                lat = "00000.0000";
                lon = "00000.0000";
            } else {
                lat = StringUtils.leftPad(String.format("%.4f",Double.parseDouble(lat)),10,'0');
                lon = StringUtils.leftPad(String.format("%.4f",Double.parseDouble(lon)),10,'0');
            }

            log.debug("================= lat: " + lat );
            log.debug("================= lon: " + lon );

            String tagId = payload.get("tag_id").getAsString();
            if(StringUtils.isBlank(tagId)){
                tagId = "00000000";
            }

            String rcvDtm = "";
            if(StringUtils.isBlank(payload.get("event_dtm").getAsString())) {
                rcvDtm = "000000000000";
            } else {
                rcvDtm = (payload.get("event_dtm").getAsString()).substring(2);
            }
            String rcvData =  payload.get("type").getAsString() +
                     tagId +
                     "ID" + payload.get("danmal_id").getAsString() +
                     payload.get("event_cd").getAsString() +
                    rcvDtm +
                     payload.get("ul_gbn").getAsString() +
                     lat +
                     lon;

            //PKG_HOCCOM_EXP_200717.RCV_DATA_SAVE01 호출
            TagCertification tc = TagCertification.builder()
                    .dataDiv( payload.get("type").getAsString())
                    .tagId( tagId)
                    .deviceId(mwId)
                    .eventDiv( payload.get("event_cd").getAsString())
                    .rcvDtm(eventDtm)
                    .ulDiv( payload.get("ul_gbn").getAsString())
                    .latitude( lat)
                    .longitude( lon)
                    .latitudeD("E")
                    .longitudeD("N")
                    .tmX(Double.toString(tmPoint.getX()))
                    .tmY(Double.toString(tmPoint.getY()))
                    .rcvData(rcvData)
                    .build();

            DbResponse dr = tagCertificationRepository.requestCertification(tc);
            log.debug("DbResponse : " + dr);

            if (dr.getAppCode().equals("0")) {
                //단말로 보낼 메시지 생성
                JsonObject rtnObj = new JsonObject();
                rtnObj.addProperty("type", "CR");
//                rtnObj.put("rtn_cd", dr.getAppCode());
                rtnObj.addProperty("rtn_msg", dr.getAppMsg());
                rtnObj.addProperty("tag_id", payload.get("tag_id").getAsString());
                rtnObj.addProperty("danmal_id", payload.get("danmal_id").getAsString());
//                rtnObj.addProperty("ip_addr", envInfoService.getAddrIp(danmalId));

                //단말로 전송
                responseService.sendResponse(topic, rtnObj);
                //SendResponse("CR" + strAppMsg, "0", "SUCCESS", "(" + sTagID + strRecvData.Substring(10, 10) + "[" + sCarNo + "]" + ")");

                //데이터 저장
                ReceiveData rd = ReceiveData.builder()
                        .dtm(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                        .rsDiv("SND")
                        .deviceId(mwId)
                        .dataDiv( payload.get("type").getAsString())
                        .data("CR" + dr.getAppMsg())
                        .build();

                receiveDataRepository.save(rd);
                //RecieveDataSave(DateTime.Now.ToString("yyyyMMddHHmmss"), "SND", sMWID, sGBN, "CR" + strAppMsg);

//                log.info("SND DATA :{}({}){}, 인증 정보 SEND", mwId, payload.get("tag_id"), dr.getAppMsg());
                webSocketHandler.sendMessage( WebSocketHandler.WS_LOG ,  "SND DATA :%s(%s)%s, 인증 정보 SEND".formatted( mwId, payload.get("tag_id"), dr.getAppMsg() ));

                if(dr.getAppMsg().equals("OK")) {
                    String currentDtm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    //connectionState를 저장한다.
                    ConnectionState connectionState = ConnectionState.builder()
                            .danmalId(danmalId)
                            .eventCd( payload.get("type").getAsString())
                            .firstConnectionTime(currentDtm)
                            .lastConnectionTime(currentDtm)
                            .build();

                    connectionStateService.add(connectionState);
                }
//                webSocketHandler.sendMessage("conn",connectionStateService.getList().toString());

            } else {
                log.info("(인증처리) DB 오류 : [APP_CODE:{}, APP_MSG:{}] {} ", dr.getAppCode(), dr.getAppMsg(), gson.toJson(payload));
                webSocketHandler.sendMessage( WebSocketHandler.WS_LOG ,  "(인증처리) DB 오류 : [APP_CODE: %s, APP_MSG: %s] %s".formatted( dr.getAppCode(), dr.getAppMsg(), gson.toJson(payload) ));
            }
        } catch (Exception e) {
            log.warn("태그 인증 프로토콜 처리 오류" + ", RcvData:" + gson.toJson(payload) + "\n" + e.toString());
        }
    }
}
