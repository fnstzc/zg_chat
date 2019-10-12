package com.dewmobile.zgchat.mqtt;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import org.springframework.stereotype.Component;

/**
 * Mqtt队列消息监控
 *
 * @author zc
 * @date 2019-10-08
 */
@Component
public class MqttMsgMonitor {

    private final EventLoopGroup workerGroup;
    private final Bootstrap bootStrap;
    private final String host;
    private final int port;

    public MqttMsgMonitor(Bootstrap bootstrap, String host, int port, EventLoopGroup workerGroup) {
        this.bootStrap = bootstrap;
        this.host = host;
        this.port = port;
        this.workerGroup = workerGroup;
    }

    public void start() throws InterruptedException {
        try {
            ChannelFuture channelFuture = bootStrap.connect(host, port).sync();
            System.out.println("client connected!");
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
