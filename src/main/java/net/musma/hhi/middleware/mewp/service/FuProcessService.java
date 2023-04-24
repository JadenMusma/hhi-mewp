package net.musma.hhi.middleware.mewp.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.musma.hhi.middleware.mewp.dto.FirmwareInfo;
import net.musma.hhi.middleware.mewp.websocket.WebSocketHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
public class FuProcessService {

    private int capacity;
    private String firmwarePath;

    private WebSocketHandler webSocketHandler;

    private ResponseService responseService;

    public FuProcessService(@Value("${spring.env.firmware-buffer-size}") int capacity,
                            @Value("${spring.env.firmware-path}") String firmwarePath,
                            WebSocketHandler webSocketHandler,
                            ResponseService responseService) {
        this.capacity = capacity;
        this.webSocketHandler = webSocketHandler;
        this.responseService = responseService;
        this.firmwarePath = firmwarePath;

    }

    public void processFU(String topic, JsonObject payload) {
        Gson gson = new Gson();

        try {
            String danmalId = payload.get("danmal_id").getAsString();

            String fwFile = payload.get("fw_file").getAsString().toLowerCase();

            String hwVer = payload.get("hw_ver").getAsString().toLowerCase();

            FirmwareInfo firmwareInfo = getFirmwareInfo(hwVer, fwFile);

            log.debug("firmwareInfo : {}", firmwareInfo != null ? firmwareInfo.toString() : "null");

            JsonObject rtnObj = new JsonObject();
            rtnObj.addProperty("type", payload.get("type").getAsString());
            rtnObj.addProperty("danmal_id", payload.get("danmal_id").getAsString());

            int index = Integer.parseInt(payload.get("seg_index").getAsString());


            if (null != firmwareInfo) {

                List<byte[]> results = readFirmwareFile(hwVer, fwFile);

                System.out.println("=====[" + HexFormat.of().formatHex(results.get(index)) + "]");
                System.out.println("=====[" + Base64.getEncoder().encodeToString(results.get(index)) + "]");
                rtnObj.addProperty("fw_file", firmwareInfo.getFwFile());
                rtnObj.addProperty("fw_size", firmwareInfo.getFwSize());
                rtnObj.addProperty("total_seg", firmwareInfo.getTotalSegCnt());
                rtnObj.addProperty("data", Base64.getEncoder().encodeToString(results.get(index)));
                rtnObj.addProperty("seg_index", index);
            } else {

                rtnObj.addProperty("fw_file", fwFile);
                rtnObj.addProperty("fw_size", 0);
                rtnObj.addProperty("total_seg", 0);
                rtnObj.addProperty("data", "Not Found");
                rtnObj.addProperty("seg_index", index);
            }

            //단말로 전송
            responseService.sendResponse(topic, rtnObj);

//            log.info("Firmware File 전송 처리, {}, Data: {}", index, gson.toJson(rtnObj));
            log.info("Firmware File 전송 처리, {}, Data: {}", index, rtnObj.toString());
            webSocketHandler.sendMessage(WebSocketHandler.WS_LOG, "Firmware File 전송 처리 완료, %s, %s, index: %s".format(payload.get("danmal_id").getAsString(), payload.get("hw_ver").getAsString(), index));

        } catch (Exception e) {
            log.warn("Firmware File 전송 처리 오류, RcvData: " + gson.toJson(payload) + "\n" + e.toString());
        }
    }

    public void processFV(String topic, JsonObject payload) {
        Gson gson = new Gson();
        try {

            String hwVer = payload.get("hw_ver").getAsString().toLowerCase();

            FirmwareInfo firmwareInfo = getFirmwareInfo(hwVer, "");

            JsonObject rtnObj = new JsonObject();
            if(null == firmwareInfo){
                rtnObj.addProperty("type", payload.get("type").getAsString());
                rtnObj.addProperty("fw_file", "");
                rtnObj.addProperty("fw_size", 0);
                rtnObj.addProperty("total_seg", 0);
                rtnObj.addProperty("danmal_id", payload.get("danmal_id").getAsString());
            } else {
                rtnObj.addProperty("type", payload.get("type").getAsString());
                rtnObj.addProperty("fw_file", firmwareInfo.getFwFile());
                rtnObj.addProperty("fw_size", firmwareInfo.getFwSize());
                rtnObj.addProperty("total_seg", firmwareInfo.getTotalSegCnt());
                rtnObj.addProperty("danmal_id", payload.get("danmal_id").getAsString());
            }

            //단말로 전송
            responseService.sendResponse(topic, rtnObj);

            log.info("Firmware Info 전송 처리, Data: {}", gson.toJson(rtnObj));
            webSocketHandler.sendMessage(WebSocketHandler.WS_LOG, "Firmware Info 전송 처리 완료, %s, %s".format(payload.get("danmal_id").getAsString(), gson.toJson(rtnObj)));

        } catch (Exception e) {
            log.warn("Firmware Info 전송 처리 오류, RcvData: " + gson.toJson(payload) + "\n" + e.toString());
        }
    }

    public FirmwareInfo getFirmwareInfo(String hwVer, String fwFile) {

        log.debug(firmwarePath + "/" + hwVer + "/" );
        File dir = new File(firmwarePath + "/" + hwVer + "/");
        String firmwareVersion = null;

        if (StringUtils.isBlank(fwFile)) {

            if(null == dir.listFiles()  || dir.listFiles().length == 0){
                return null;
            }
            firmwareVersion = Stream.of(dir.listFiles())
                    .map(File::getName)
                    .max(String::compareToIgnoreCase)
                    .get();

        } else {


            firmwareVersion = fwFile;
        }

        log.debug(firmwarePath +"/"+ hwVer + "/" + firmwareVersion);
        File file = new File(firmwarePath + "/" + hwVer + "/" + firmwareVersion);
        if (file.isFile()) {

            long firmwareSize = file.length();

            long segmentSize = (firmwareSize / capacity) + 1;

            return FirmwareInfo.builder()
                    .fwFile(firmwareVersion)
                    .fwSize(firmwareSize)
                    .totalSegCnt(segmentSize)
                    .hwVer(hwVer)
                    .build();
        } else {
            return null;
        }

    }

    private List<byte[]> readFirmwareFile(String hwVer, String fwFile) {

        byte[] file_bytes = null;    //new byte[0];
        try {
            file_bytes = Files.readAllBytes(Paths.get(firmwarePath + "/" + hwVer + "/" + fwFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] finalFile_bytes = file_bytes;
        return IntStream.iterate(0, i -> i + capacity)
                .limit((file_bytes.length + capacity - 1) / capacity)
                .mapToObj(i -> Arrays.copyOfRange(finalFile_bytes, i, Math.min(i + capacity, finalFile_bytes.length)))
                .collect(Collectors.toList());
    }
}
