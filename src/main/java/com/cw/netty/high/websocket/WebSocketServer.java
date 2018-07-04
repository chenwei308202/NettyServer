package com.cw.netty.high.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author chenwei
 * @create 2018-06-29 15:42
 **/

public class WebSocketServer {

    public  void run(int port) throws Exception{

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            ServerBootstrap boot=new ServerBootstrap();
            boot.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //将请求和应答消息编码活解码为http消息
                            socketChannel.pipeline().addLast("http-codec",new HttpServerCodec());
                            //将http消息的多个部分组合成一条完整的http消息
                            socketChannel.pipeline().addLast("aggregator",new HttpObjectAggregator(65536));
                            //向客户端发送文件
                            socketChannel.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
                            socketChannel.pipeline().addLast("handler",null);
                        }
                    });
            ChannelFuture future = boot.bind( port).sync();
            System.out.println("Web socket server started at port "+port);
            System.out.println("Open you browser and navigate to http://localhost:"+port+"/");
            future.channel().closeFuture().sync();
        }finally {
            //清理资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) throws Exception {
        new WebSocketServer().run(8088);
    }
}
