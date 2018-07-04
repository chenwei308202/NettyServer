package com.cw.netty.codec.serialization.client;

import com.cw.netty.codec.serialization.client.channelhandler.SubReqClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;

/**
 * @author  chenwei
 * Created by chenwei01 on 2017/4/25.
 */
public class SubReqClient {

    private String host;

    private int port;

    public SubReqClient(String host, int port){
        this.host=host;
        this.port=port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap boot=new Bootstrap();
        boot.group(group).
                channel(NioSocketChannel.class).
                remoteAddress(new InetSocketAddress(host, port)).
                handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
                        socketChannel.pipeline().addLast(new ObjectEncoder());
                        socketChannel.pipeline().addLast(new SubReqClientHandler());
                    }
                });
        ChannelFuture future=boot.connect().sync();
        future.channel().closeFuture().sync();

    }


    public static void main(String[] args) {
        try {
            new SubReqClient("localhost",20000).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
