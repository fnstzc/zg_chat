package com.dewmobile.zgchat.config;

import com.dewmobile.zgchat.mqtt.MqttConnectHandler;
import com.dewmobile.zgchat.mqtt.MqttSubscribeHandler;
import com.dewmobile.zgchat.mqtt.MsgCacheHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.net.ssl.SSLException;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Mqtt相关配置
 *
 * @author zc
 * @date 2019-10-09
 */
@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfig {

    private final MqttProperties properties;
    private final MqttConnectHandler mqttConnectHandler;
    private final MqttSubscribeHandler mqttSubscribeHandler;
    private final MsgCacheHandler msgCacheHandler;

    /**
     * 懒加载，解决循环依赖
     */
    @Lazy
    public MqttConfig(MqttProperties properties, MqttConnectHandler mqttConnectHandler, MqttSubscribeHandler mqttSubscribeHandler, MsgCacheHandler msgCacheHandler) {
        this.properties = properties;
        this.mqttConnectHandler = mqttConnectHandler;
        this.mqttSubscribeHandler = mqttSubscribeHandler;
        this.msgCacheHandler = msgCacheHandler;
    }

    @Bean
    public Bootstrap bootStrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup()).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(sslHandler(socketChannel));
                pipeline.addLast("encoder", MqttEncoder.INSTANCE);
                pipeline.addLast("decoder", new MqttDecoder());
                pipeline.addLast("heartBeatHandler", new IdleStateHandler(0, 20, 0, TimeUnit.SECONDS));
                pipeline.addLast("handler", mqttConnectHandler);
                pipeline.addLast("subscribe", mqttSubscribeHandler);
                pipeline.addLast("cache", msgCacheHandler);
            }
        });
        return bootstrap;
    }


    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup();
    }

    @Bean
    public MqttConnectMessage connectMessage() {
        MqttFixedHeader connectFixedHeader =
                new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_LEAST_ONCE, false, 0);
        MqttConnectPayload connectPayload =
                new MqttConnectPayload(properties.getClientId(), null, null, properties.getUsername(), properties.getPassword().getBytes());
        MqttConnectVariableHeader connectVariableHeader =
                new MqttConnectVariableHeader(properties.getName(), properties.getVersion(), true, true, false, 0, false, false, 20);

        return new MqttConnectMessage(connectFixedHeader, connectVariableHeader, connectPayload);
    }

    @Bean
    public MqttMessage pingreqMessage() {
        MqttFixedHeader pingreqFixedHeader =
                new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0);
        return new MqttMessage(pingreqFixedHeader);
    }

    @Bean
    public MqttSubscribeMessage subscribeMessage() {
        MqttFixedHeader fixedHeader =
                new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0);
        MqttSubscribePayload subscribePayload =
                new MqttSubscribePayload(Collections.singletonList(new MqttTopicSubscription(properties.getTopic(), MqttQoS.AT_LEAST_ONCE)));
        return new MqttSubscribeMessage(fixedHeader, MqttMessageIdVariableHeader.from(new Random().nextInt(65535)), subscribePayload);
    }

    @Bean
    public String host() {
        return properties.getHost();
    }

    @Bean
    public int port() {
        return properties.getPort();
    }

    private SslHandler sslHandler(SocketChannel socketChannel) throws SSLException {
         SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
         return sslCtx.newHandler(socketChannel.alloc(), host(), port());
    }
}
