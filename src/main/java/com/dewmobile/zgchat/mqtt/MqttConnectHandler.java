package com.dewmobile.zgchat.mqtt;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * Mqtt连接处理
 *
 * @author zc
 * @date 2019-10-08
 */
@Slf4j
@Component
public class MqttConnectHandler extends ChannelInboundHandlerAdapter {

    private MqttConnectMessage connectMessage;
    private MqttMessage pingreqMessage;

    MqttConnectHandler(MqttConnectMessage connectMessage, @Qualifier("pingreqMessage") MqttMessage pingreqMessage) {
        this.connectMessage = connectMessage;
        this.pingreqMessage = pingreqMessage;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(connectMessage);
        log.info("Sent CONNECT");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MqttConnAckMessage) {
            MqttConnAckMessage mqttConnAckMessage = (MqttConnAckMessage) msg;
            byte b = mqttConnAckMessage.variableHeader().connectReturnCode().byteValue();
            log.info("Connect ack : " + b);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.writeAndFlush(pingreqMessage);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
