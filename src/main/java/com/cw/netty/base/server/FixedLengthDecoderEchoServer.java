package com.cw.netty.base.server;

import com.cw.netty.base.server.channelhandler.FixedLengthDecoderHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;


/**
 * @author  chenwei
 *
 */
public class FixedLengthDecoderEchoServer {

    private  int port;

    public FixedLengthDecoderEchoServer(int port){
        this.port=port;
    }

    public void start() throws  Exception{
        //因为使用NIO，指定NioEventLoopGroup来接受和处理新连接
        EventLoopGroup group = new NioEventLoopGroup();
        //创建bootstrap来启动服务器
        ServerBootstrap boot = new ServerBootstrap();
        boot.group(group).
                //指定通道类型为NioServerSocketChannel
                        channel(NioServerSocketChannel.class).
                        localAddress(port).
                        childHandler(new ChannelInitializer<Channel>() {
                       //这个方法传ChannelInitializer类型的参数，ChannelInitializer是个抽象类，所以需要实现initChannel方法，这个方法就是用来设置ChannelHandler
                       @Override
                       protected void initChannel(Channel channel) throws Exception {

                           //利用FixedLengthFrameDecoder解码器，无论一次性接受到多少数据报，它都会按照
                           //构造函数中设置的固定长度进行解码，如果是半包消息，FixedLengthFrameDecoder
                           //会缓存半包消息并等待下个包到达后进行拼包，直到读取到一个完整的包
                           channel.pipeline().addLast(new FixedLengthFrameDecoder(20));
                           channel.pipeline().addLast(new StringDecoder());
                           channel.pipeline().addLast(new FixedLengthDecoderHandler());
                       }
                   });
        ChannelFuture future=boot.bind().sync();
        System.out.println( FixedLengthDecoderEchoServer.class.getSimpleName()+" started and listen on "+future.channel().localAddress());


        future.channel().closeFuture().sync();


    }


    public static void main(String[] args) {
        FixedLengthDecoderEchoServer server=new FixedLengthDecoderEchoServer(20000);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
