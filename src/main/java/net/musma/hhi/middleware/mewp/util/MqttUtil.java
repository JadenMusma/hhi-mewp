package net.musma.hhi.middleware.mewp.util;

public class MqttUtil {

    public static String getDanmalId(String topic){
        String[] topicArr = topic.split("/");
        return topicArr[1] ;
    }

    public static String getDownTopic(String topic){
        String[] topicArr = topic.split("/");
        topicArr[2] = "down";
        return String.join("/", topicArr);
    }
}
