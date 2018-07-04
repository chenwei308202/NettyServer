package com.cw.netty.high.protocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chenwei
 * @create 2018-07-02 18:15
 **/

public class NettyClient {

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public void connect(final int port, final String host)throws Exception{

        try {

        Bootstrap boot=new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();

        boot.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(new NettyMessageDecoder(1024*1024,4,4));
                        channel.pipeline().addLast("messageEncoder",new NettyMessageEncoder());
                        //添加超时处理handler，规定时间内没有收到消息则关闭链路
                        channel.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(50));
                        channel.pipeline().addLast("loginAuthHandler", new LoginAuthReqHandler());
                        channel.pipeline().addLast("heartbeatHandler", new HeartBeatReqHandler());

                    }
                });

        ChannelFuture future = boot.connect(new InetSocketAddress(host, port),new InetSocketAddress("127.0.0.1",8889)).sync();
        System.out.println("client is start……");
        future.channel().closeFuture().sync();

        }finally {
            //释放完毕后，清空资源，再次发起重连操作
            executorService.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        System.out.println("重连……");
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        connect(port,host);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


        }

    }

    public static void main(String[] args) throws Exception {
        new NettyClient().connect(8000,"127.0.0.1");
    }


}
