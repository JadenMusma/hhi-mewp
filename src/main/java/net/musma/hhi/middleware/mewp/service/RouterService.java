package net.musma.hhi.middleware.mewp.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@AllArgsConstructor
public class RouterService {


//    private EnvInfoService envInfoService;

    private AaProcessService aaProcessService;
    private CrProcessService crProcessService;

    private LuProcessService luProcessService;

    private OpProcessService opProcessService;

    private UsProcessService usProcessService;

    private ErProcessService erProcessService;

    private FuProcessService fuProcessService;

    public void process(String topic, String payload) {

        Gson gson = new Gson();
        try {
            log.debug("===== input topic : {}, payload : {}", topic, payload);

            JsonObject jsonPayload = new JsonParser().parse(payload).getAsJsonObject();
            String type = jsonPayload.get("type").getAsString();

            if ("AA".equals(type)) {
                aaProcessService.process(topic, jsonPayload);
            } else if ("CR".equals(type)) {
                crProcessService.process(topic, jsonPayload);
            } else if ("LU".equals(type)) {
                luProcessService.process(topic, jsonPayload);
            } else if ("OP".equals(type)) {
                opProcessService.process(topic, jsonPayload);
            } else if ("US".equals(type)) {
                usProcessService.process(topic, jsonPayload);
            } else if ("ER".equals(type)) {
                erProcessService.process(topic, jsonPayload);
            } else if ("FV".equals(type)) {
                fuProcessService.processFV(topic, jsonPayload);
            } else if ("FU".equals(type)) {
                fuProcessService.processFU(topic, jsonPayload);
            }
        } catch (RuntimeException re) {
            log.error("RuntimeException : {}", payload);
            re.printStackTrace();
        }

    }

}
