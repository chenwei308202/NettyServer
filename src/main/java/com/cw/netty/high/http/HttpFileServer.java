package com.cw.netty.high.http;

import com.cw.netty.high.http.handler.HttpFileServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * HTTP文件服务器
 *
 * @author chenwei
 * @create 2018-06-28 10:36
 **/

public class HttpFileServer {

    private static final String DEFAULT_URL="/usr/local/";

    public void run(final int port,final String url) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            ServerBootstrap boot=new ServerBootstrap();
            boot.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("http-decoder",new HttpRequestDecoder());
                            socketChannel.pipeline().addLast("http-aggregator",new HttpObjectAggregator(65536));
                            socketChannel.pipeline().addLast("http-encoder",new HttpResponseEncoder());
                            socketChannel.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
                            socketChannel.pipeline().addLast("fileServerHandler",new HttpFileServerHandler(url));
                        }
                    });
            // 内网ip
            ChannelFuture future = boot.bind("192.168.0.4", port).sync();
            //公网ip
            System.out.println("HTTP 文件目录服务器启动，网址是："+"http://106.12.3.152:"+port+url);

            future.channel().closeFuture().sync();
        }finally {
            //清理资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }


    public static void main(String[] args) throws Exception {
        int port=8081;
        String url=DEFAULT_URL;
        new HttpFileServer().run(port,url);
    }






















}
