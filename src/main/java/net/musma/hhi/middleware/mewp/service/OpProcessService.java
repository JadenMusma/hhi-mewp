package net.musma.hhi.middleware.mewp.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.musma.hhi.middleware.mewp.coordinates.Point;
import net.musma.hhi.middleware.mewp.dto.ConnectionState;
import net.musma.hhi.middleware.mewp.dto.ControlState;
import net.musma.hhi.middleware.mewp.dto.DbResponse;
import net.musma.hhi.middleware.mewp.repository.ControlStateRepository;
import net.musma.hhi.middleware.mewp.websocket.WebSocketHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class OpProcessService {

    private WebSocketHandler webSocketHandler;

    private CoordinateService coordinateService;

    private ConnectionStateService connectionStateService;

    private ControlStateRepository controlStateRepository;

    private static Map<String, String> eventNm = Map.ofEntries(
            Map.entry("KO", "Key On"),
            Map.entry("KF", "Key Off"),
            Map.entry("EO", "시동 On"),
            Map.entry("EF", "시동 Off"),
            Map.entry("PO", "페달 On"),
            Map.entry("PF", "페달 Off"),
            Map.entry("MO", "Module On"),
            Map.entry("MF", "Module Off")
    );

    @Async
    public void process(String topic, JsonObject payload) {

        try {
            String danmalId = payload.get("danmal_id").getAsString();

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

            String lat = "";
            String lon = "";

            Point tmPoint = new Point(0,0);

            if(!payload.get("latitude").isJsonNull() && !payload.get("longitude").isJsonNull()) {
                lat = payload.get("latitude").getAsString();
                lon = payload.get("longitude").getAsString();

                tmPoint = coordinateService.tmCalc(lat, lon);
            }

            String eventName = eventNm.get(payload.get("event_cd").getAsString());

            String mwId = danmalId;

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

            String r1,r2,r3, ul_gbn;

            if(StringUtils.isBlank(payload.get("r1").getAsString())){
                r1="  ";
            } else {
                r1 = payload.get("r1").getAsString();
            }

            if(StringUtils.isBlank(payload.get("r2").getAsString())){
                r2="  ";
            } else {
                r2 = payload.get("r2").getAsString();
            }

            if(StringUtils.isBlank(payload.get("r3").getAsString())){
                r3="  ";
            } else {
                r3 = payload.get("r3").getAsString();
            }

            if(StringUtils.isBlank(payload.get("ul_gbn").getAsString())){
                ul_gbn =" ";
            } else {
                ul_gbn = payload.get("ul_gbn").getAsString();
            }

            String rcvData =  payload.get("type").getAsString() +
                     tagId +
                     "ID" + payload.get("danmal_id").getAsString() +
                     payload.get("event_cd").getAsString() +
//                     payload.get("r1").toString()+
//                     payload.get("r2").toString()+
//                     payload.get("r3").toString()+
                     r1 +
                     r2 +
                     r3 +
                     rcvDtm +
                     payload.get("cr_start_yn").getAsString() +
//                     payload.get("ul_gbn").toString() +
                     ul_gbn +
                     lat +
                     lon;

            ControlState cs = ControlState.builder()
                    .dataDiv( payload.get("type").getAsString())
                    .tagId( tagId)
                    .deviceId(mwId)
                    .eventDiv( payload.get("event_cd").getAsString())
                    .rcvDtm(eventDtm)
                    .crStartYn( payload.get("cr_start_yn").getAsString())
                    .ulDiv( ul_gbn )
                    .latitude( lat)
                    .longitude( lon)
                    .latitudeD("E")
                    .longitudeD("N")
                    .tmX(Double.toString(tmPoint.getX()))
                    .tmY(Double.toString(tmPoint.getY()))
                    .rcvData(rcvData)
                    .build();

            //PKG_HOCCOM_EXP_200717.RCV_DATA_SAVE02에 저장
            DbResponse dr = controlStateRepository.save(cs);
            log.debug("DbResponse : " + dr);

            //connectionState를 저장한다.
            ConnectionState connectionState = ConnectionState.builder()
                    .danmalId(danmalId)
                    .eventCd(payload.get("event_cd").getAsString())
                    .firstConnectionTime("")
                    .lastConnectionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();

            connectionStateService.update(connectionState);

            if (!dr.getAppCode().equals("0")) {
                log.info("PKG_HOCCOM_EXP_200717.RCV_DATA_SAVE02 오류 : [APP_CODE:{}, APP_MSG:{}]{}", dr.getAppCode(), dr.getAppMsg(), rcvData);
                webSocketHandler.sendMessage( WebSocketHandler.WS_LOG ,  "(운행/조작)PKG_HOCCOM_EXP_200717.RCV_DATA_SAVE02 오류 : [APP_CODE: %s, APP_MSG: %s]".formatted(dr.getAppCode(), dr.getAppMsg()));
            }

        } catch (Exception e) {
            log.warn("운행/조작 프로토콜 처리 오류" + ", RcvData:" + new Gson().toJson(payload) + "\n" + e.toString());
        }
    }
}
