package net.musma.hhi.middleware.mewp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.musma.hhi.middleware.mewp.dto.ConnectionState;
import net.musma.hhi.middleware.mewp.websocket.WebSocketHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConnectionStateService {
    private HashMap<String, ConnectionState> connectionStateMap;

    private WebSocketHandler webSocketHandler;

    public ConnectionStateService(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
        this.connectionStateMap = new HashMap<>();
    }

    public void add(ConnectionState cs) {

        if (!this.connectionStateMap.containsKey(cs.getDanmalId())) {
            this.connectionStateMap.put(cs.getDanmalId(), cs);
        }

//        connectionStateMap.values().forEach(System.out::println);
    }

    public void update(ConnectionState cs) {

        ConnectionState originCs = connectionStateMap.get(cs.getDanmalId());

        /*
        통신이 안되다가 고소차가 운행중에 다시 통신이 가능할 수 있으므로 중간에 들어오는 데이터도 받아들일 수 있도록 한다.
         */
        if (originCs == null) {
            cs.setFirstConnectionTime(cs.getLastConnectionTime());
            this.connectionStateMap.put(cs.getDanmalId(), cs);
        } else {
            originCs.setEventCd(cs.getEventCd());
            originCs.setLastConnectionTime(cs.getLastConnectionTime());
        }

//        connectionStateMap.values().forEach(System.out::println);
    }

    public String getList() {
        ObjectMapper objectMapper = new ObjectMapper();

        String rtnString = null;

        try {
            rtnString = objectMapper.writeValueAsString(connectionStateMap.values().stream().collect(Collectors.toList()));
        } catch (JsonProcessingException e) {
            log.error("Json 파싱 오류");
        }

        return rtnString;
    }

    public String getFirmwareVersionList() {

        String rtnString = "DanmalId,FwVer,HwVer\n";

        String data = connectionStateMap.values().stream()
                .map(cs -> cs.getDanmalId() + "," + cs.getFwVer() + "," + cs.getHwVer())
                .collect(Collectors.joining("\n"));

        return rtnString + data;
    }

    public void updateFirmwareVersion(String danmalId, String fwVer, String hwVer) {

        if (connectionStateMap.containsKey(danmalId) &&
                (connectionStateMap.get(danmalId).getFwVer() == null || !connectionStateMap.get(danmalId).getFwVer().equals(fwVer))) {

            ConnectionState originCs = connectionStateMap.get(danmalId);

            originCs.setFwVer(fwVer);
            originCs.setHwVer(hwVer);
        }

    }

    @Scheduled(fixedRate = 5_000)
    public void sendConnectionStateList() {
        webSocketHandler.sendMessage(WebSocketHandler.WS_CONN, getList());
    }


    //매일 오전 0시 1분에 하루 전 접속 정보를 삭제 한다.
    @Scheduled(cron = "* 1 0 * * *")
    public void delete() {
        String baseDtm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        connectionStateMap.values().removeIf(map -> map.getLastConnectionTime().compareToIgnoreCase(baseDtm) < 0);

        //or
        //connectionStateMap.clear();
    }
}
