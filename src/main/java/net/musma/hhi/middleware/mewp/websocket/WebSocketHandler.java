package net.musma.hhi.middleware.mewp.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    static final public String WS_LOG = "log";
    static final public String WS_CONN = "conn";

    //    private final static Logger LOG = LoggerFactory.getLogger(WebSocketChatHandler.class);
    private final Map<String, WebSocketSession> idToActiveSession = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        idToActiveSession.put(session.getId(), session);
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        //log.debug("payload: " + payload);
        //TextMessage textMessage = new TextMessage("Welcome chatting server~ ^^");
        session.sendMessage(message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        idToActiveSession.remove(session.getId());
        super.afterConnectionClosed(session, status);
    }

    public void sendMessage(String type, String message) {

//        log.info("------"+idToActiveSession.size());

        String parsedLocalDateTimeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String, Object> map = new HashMap<>();
        if (WS_LOG.equals(type)) {
            map.put("type", "log");
            map.put("data", "[" + parsedLocalDateTimeNow + "] " + message);
        } else {
            map.put("type", "conn");
            map.put("data", message);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        idToActiveSession.forEach((key, session) -> {
            try {
                handleTextMessage(session, new TextMessage(objectMapper.writeValueAsString(map)));
            } catch (JsonProcessingException e) {
                log.debug(e.toString());
            } catch (Exception e) {
                log.debug(e.toString());
            }
        });
    }
}