package com.cw.netty.base.client;

import com.cw.netty.base.client.channelhandler.TimeClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.net.InetSocketAddress;
/*
ByteToMessageDecoder是ChannelInboundHandler的一个实现，它使得处理碎片问题变得容易。

        ByteToMessageDecoder在接收到新数据时，使用内部维护的累积缓冲区调用decode()方法。

        decode()可以决定在累积缓冲区中没有足够数据的情况下不添加任何东西。 当接收到更多数据时，ByteToMessageDecoder将再次调用decode()。

        如果decode()将对象添加到out，则意味着解码器成功地解码了消息。 ByteToMessageDecoder将丢弃累积缓冲区的读取部分。要记住，不需要解码多个消息。 ByteToMessageDecoder将继续调用decode()方法，直到它没有再有任何东西添加。
*/
/**
 * Created by chenwei01 on 2017/4/27.
 */
public class TimeClient {

    private String host;

    public int port;

    public TimeClient(String host,int port){
        this.host=host;
        this.port=port;
    }

    public void run() throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap boot=new Bootstrap();
        boot.group(group).
                channel(NioSocketChannel.class).
                remoteAddress(new InetSocketAddress(host, port)).
                handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024)).addLast(new StringDecoder()).addLast(new TimeClientHandler());
                    }
                });
        ChannelFuture future=boot.connect().sync();
        future.channel().closeFuture().sync();


    }

    public static void main(String[] args) {
        TimeClient client=new TimeClient("127.0.0.1",8888);
        try {
            client.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
