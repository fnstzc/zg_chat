package com.dewmobile.zgchat;

import com.dewmobile.zgchat.common.ThreadFactoryImpl;
import com.dewmobile.zgchat.mqtt.MqttMsgMonitor;
import com.dewmobile.zgchat.store.DefaultMsgStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * mqtt队列数据落地启动类
 *
 * @author zc
 * @date 2019-09-30
 */
@SpringBootApplication
public class ZgChatApplication {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(ZgChatApplication.class, args);
        MqttMsgMonitor mqttMsgMonitor = context.getBean(MqttMsgMonitor.class);

        new ThreadFactoryImpl("defaultMsgStore", true).newThread(() -> {
            DefaultMsgStore defaultMsgStore = context.getBean(DefaultMsgStore.class);
            defaultMsgStore.start();
        }).start();

        mqttMsgMonitor.start();
    }
}
