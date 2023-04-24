package net.musma.hhi.middleware.mewp.config;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.musma.hhi.middleware.mewp.service.RouterService;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * author :  sanghoonkim
 * date : 2023/01/24
 */
@Slf4j
@Configuration
@EnableAsync
public class MqttConfig {

    private final RouterService routerService;



    public MqttConfig(RouterService routerService) {
        this.routerService = routerService;
    }


    public MqttPahoClientFactory mqttClientFactory() throws Exception {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

//        System.out.println("0===================== " + System.getenv("CONNECTION_URL"));

        String mosquittoUrl = System.getenv("CONNECTION_URL");
        if(StringUtils.isBlank(mosquittoUrl)){
            mosquittoUrl = "tcp://localhost:1883";
//            mosquittoUrl = "tcp://172.30.1.111:1883";
        }

//        log.debug("========== mosquittoUrl : " + mosquittoUrl);

        options.setServerURIs(new String[]{mosquittoUrl});

        options.setUserName("musma");
        options.setPassword("musma0812!@".toCharArray());

        factory.setConnectionOptions(options);

        return factory;

    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() throws Exception {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("serverIn",
                mqttClientFactory(), "hhi-mewp/+/up", "test");
//        System.out.println("inbound");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(0);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {

                String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();
                String payload = message.getPayload().toString();

                routerService.process(topic, payload);
            }
        };
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }


    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() throws Exception {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("serverOut",
                mqttClientFactory());

//        System.out.println("mqttOutbound");
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("myTopic1");
        return messageHandler;
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    public interface MyGateway {

        void sendToMqtt(String data, @Header(MqttHeaders.TOPIC) String topic);

    }

}
