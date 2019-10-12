package com.dewmobile.zgchat.mqtt;

import com.dewmobile.zgchat.constant.RedisConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * MQTT消息解析
 *
 * @author zc
 * @date 2019-10-08
 */
@Slf4j
@Component
@Lazy
public class MsgCacheHandler extends SimpleChannelInboundHandler<MqttPublishMessage> {
    private final StringRedisTemplate redisTemplate;

    public MsgCacheHandler(StringRedisTemplate redisConn) {
        this.redisTemplate = redisConn;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttPublishMessage mqttPublishMessage) {
        // 取出全量消息发送到缓存队列中
        String content = getContent(mqttPublishMessage);
        redisTemplate.opsForList().leftPush(RedisConfig.KEY_CHAT_MSG, content);
    }

    private String getContent(MqttPublishMessage mpMsg) {
        ByteBuf byteBuf = mpMsg.content();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        byteBuf.resetReaderIndex();
        return new String(bytes);
    }
}
