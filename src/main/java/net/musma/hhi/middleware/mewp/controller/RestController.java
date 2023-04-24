package net.musma.hhi.middleware.mewp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.musma.hhi.middleware.mewp.service.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.stream.Collectors;

@org.springframework.web.bind.annotation.RestController
@Slf4j
@AllArgsConstructor
public class RestController {

    private ResponseService responseService;
    private FuProcessService fuProcessService;

    private ConnectionStateService connectionStateService;
    private EnvInfoService envInfoService;
    private CrInfoService crInfoService;


    @PostMapping(value = "/sendSetting", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object sendSetting(@RequestBody JsonObject jsonObj)  {

        Gson gson = new Gson();
//        JSONParser parser = new JSONParser();
//        JSONObject jsonObj = (JSONObject) parser.parse(value);

        log.debug("===== setting: " + gson.toJson(jsonObj));

        responseService.sendResponse("hhi-mewp/" + jsonObj.get("danmal_id") + "/down", jsonObj);

        return true;
    }

    @GetMapping(value = {"/fw/{hw}", "/fw/{hw}/"})
    public Object getFirmwareLastVersion(@PathVariable("hw") String hw)  {

        return fuProcessService.getFirmwareInfo(hw.toLowerCase(), "");
    }

    @GetMapping(value = {"/fw/{hw}/{version}"})
    public Object getFirmwareVersion(@PathVariable("hw") String hw,
                                     @PathVariable("version") String version)  {

        return fuProcessService.getFirmwareInfo(hw.toLowerCase(), version.toLowerCase());
    }

    @GetMapping(value = "/st")
    public String getDanmalStatus()  {
        return connectionStateService.getList();
    }

    @GetMapping(value = "/reloadEnv")
    public void setInitEnv()  {
        envInfoService.setLastUpdateDateInit();
    }

    @GetMapping(value = "/reloadCr")
    public void setInitCr()  {
        crInfoService.setLastUpdateDateInit();
    }

    @GetMapping(value = {"/getEnv"})
    public Object getEnvInfo()  {
        return envInfoService.getEnvInfoList();
    }

    @GetMapping(value = {"/getCr"})
    public Object getCrInfo()  {
        return crInfoService.getCardList();
    }

    @GetMapping(value = {"/getFirmwareVersionList"})
    public Object getFirmwareInfo()  {
        return connectionStateService.getFirmwareVersionList();
    }

    @GetMapping(value = "/getFirstCardList")
    public Object getFirstCardList()  {
        return crInfoService.getFirstCardList().stream()
                .map(jsonArray -> jsonArray.toString())
                .collect(Collectors.joining("\n"));
    }

}
