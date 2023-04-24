package net.musma.hhi.middleware.mewp.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.musma.hhi.middleware.mewp.coordinates.Point;
import net.musma.hhi.middleware.mewp.dto.ConnectionState;
import net.musma.hhi.middleware.mewp.dto.DbResponse;
import net.musma.hhi.middleware.mewp.dto.SensingState;
import net.musma.hhi.middleware.mewp.repository.SensingStateRepository;
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
public class UsProcessService {

    private CoordinateService coordinateService;

    private ConnectionStateService connectionStateService;

    private SensingStateRepository sensingStateRepository;

    private WebSocketHandler webSocketHandler;

    @Async
    public void process(String topic, JsonObject payload) {
        Gson gson = new Gson();
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

            String rcvData = payload.get("type").getAsString() +
                    tagId +
                    "ID" + payload.get("danmal_id").getAsString() +
                    payload.get("event_cd").getAsString() +
                    eventDtm.substring(2) +
                    lat +   //payload.get("latitude").toString() +
                    lon +   //payload.get("longitude").toString() +
                    payload.get("sensing").getAsString();

            SensingState ss = SensingState.builder()
                    .dataDiv(payload.get("type").getAsString())
                    .tagId(tagId)
                    .deviceId(mwId)
                    .eventDiv(payload.get("event_cd").getAsString())
                    .rcvDtm(eventDtm)
                    .latitude(lat)
                    .longitude(lon)
                    .latitudeD("E")
                    .longitudeD("N")
                    .usSensing(payload.get("sensing").getAsString())
                    .tmX(Double.toString(tmPoint.getX()))
                    .tmY(Double.toString(tmPoint.getY()))
                    .rcvData(rcvData)
                    .build();

            //PKG_HOCCOM_EXP_200717.RCV_DATA_SAVE03에 저장
            DbResponse dr = sensingStateRepository.save(ss);
            log.debug("DbResponse : " + dr);

            //connectionState를 저장한다.
            ConnectionState connectionState = ConnectionState.builder()
                    .danmalId(danmalId)
                    .eventCd(payload.get("type").getAsString())
                    .firstConnectionTime("")
                    .lastConnectionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();

            connectionStateService.update(connectionState);

            if (!dr.getAppCode().equals("0")) {
                log.info("PKG_HOCCOM_EXP_200717.RCV_DATA_SAVE03 오류 : [APP_CODE:{}, APP_MSG:{}]{}", dr.getAppCode(), dr.getAppMsg(), rcvData);
                webSocketHandler.sendMessage(WebSocketHandler.WS_LOG, "(감지)PKG_HOCCOM_EXP_200717.RCV_DATA_SAVE03 오류 : [APP_CODE: %s, APP_MSG: %s]".formatted(dr.getAppCode(), dr.getAppMsg()));
            }

        } catch (
                Exception e) {
            log.warn("감지 프로토콜 처리 오류" + ", RcvData:" + gson.toJson(payload) + "\n" + e.toString());
        }

    }
}
