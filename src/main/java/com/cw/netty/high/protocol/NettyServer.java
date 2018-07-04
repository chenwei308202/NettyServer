package com.cw.netty.high.protocol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * @author chenwei
 * @create 2018-07-02 18:36
 **/

public class NettyServer {

    public void bind() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap=new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new NettyMessageDecoder(1024*1024,4,4));
                        socketChannel.pipeline().addLast(new NettyMessageEncoder());
                        socketChannel.pipeline().addLast("readTimeoutHandler",new ReadTimeoutHandler(50));
                        socketChannel.pipeline().addLast(new LoginAuthRespHandler());
                        socketChannel.pipeline().addLast("HeartBeathandler",new HeartBeatRespHandler());

                    }
                });

        bootstrap.bind("127.0.0.1",8000).sync();
        System.out.println("netty server start ok:"+("127.0.0.1"+8000));


    }

    public static void main(String[] args) throws Exception {
        new NettyServer().bind();
    }

}
