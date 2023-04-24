package net.musma.hhi.middleware.mewp.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.musma.hhi.middleware.mewp.config.MqttConfig;
import net.musma.hhi.middleware.mewp.util.MqttUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResponseService {

    @Autowired
    public MqttConfig.MyGateway mqttSender;

    public void sendResponse(String topic, JsonObject obj){
        try {
            Gson gson = new Gson();
//            mqttSender.sendToMqtt(gson.toJson(obj), MqttUtil.getDownTopic(topic));
            mqttSender.sendToMqtt(obj.toString(), MqttUtil.getDownTopic(topic));

            log.info("Send : [{}]", obj.toString());
//            log.info("Send : [{}]", gson.toJson(obj));
        } catch(Exception e) {
            log.warn("SendResponse 함수 오류" + e.toString());
        }
    }

    public void sendResponse(String topic, JsonObject obj, String logAddMsg){
        try{
            Gson gson = new Gson();
            mqttSender.sendToMqtt(gson.toJson(obj), MqttUtil.getDownTopic(topic) );
            log.info("Send : [{}({})]", gson.toJson(obj), logAddMsg);
        } catch(Exception e) {
            log.warn("SendResponse 함수 오류" + e.toString());
        }
    }

}
