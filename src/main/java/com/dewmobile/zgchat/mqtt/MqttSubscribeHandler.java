package com.dewmobile.zgchat.mqtt;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 订阅处理类
 *
 * @author zc
 * @date 2019-10-09
 */
@Slf4j
@Component
@Lazy
public class MqttSubscribeHandler extends ChannelInboundHandlerAdapter {
    private final MqttSubscribeMessage subscribeMessage;

    MqttSubscribeHandler(MqttSubscribeMessage subscribeMessage) {
        this.subscribeMessage = subscribeMessage;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(subscribeMessage);
        log.info("Sub Topic: " + subscribeMessage.payload().topicSubscriptions());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MqttPublishMessage) {
            MqttPublishMessage mqttPublishMessage = (MqttPublishMessage) msg;
            MqttPubAckMessage mqttPubAckMessage = getPubAckMessage(mqttPublishMessage);
            ctx.writeAndFlush(mqttPubAckMessage);
            super.channelRead(ctx, mqttPublishMessage);
        }
    }

    private MqttPubAckMessage getPubAckMessage(MqttPublishMessage mqttPublishMessage) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_LEAST_ONCE, false, 0);
        MqttMessageIdVariableHeader subAckVarHeader = MqttMessageIdVariableHeader.from(mqttPublishMessage.variableHeader().packetId());
        return new MqttPubAckMessage(fixedHeader, subAckVarHeader);
    }
}
