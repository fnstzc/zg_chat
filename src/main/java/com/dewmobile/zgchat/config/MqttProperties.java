package com.dewmobile.zgchat.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置文件
 *
 * @author zc
 * @date 2019-10-08
 */
@Data
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {
    private String name;
    private int version;
    private String host;
    private int port;
    private String clientId;
    private String username;
    private String password;
    private String topic;
}
