package com.cw.netty.base.server;

import com.cw.netty.base.server.channelhandler.EchoServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;


/**
 * @author  chenwei
 * Created by chenwei01 on 2017/4/24.
 * 1.创建ServerBootstrap实例来引导绑定和启动服务器
 * 2.创建NioEventLoopGroup对象来处理事件，如接受新连接、接收数据、写数据等等
 * 3.指定InetSocketAddress，服务器监听此端口
 * 4.设置childHandler执行所有的连接请求
 * 5.都设置完毕了，最后调用ServerBootstrap.bind() 方法来绑定服务器
 */
public class EchoServer {

    private  int port;

    public EchoServer(int port){
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
                           ByteBuf delimiter= Unpooled.copiedBuffer("$_".getBytes());
                           //表示单条消息的最大长度，当达到该长度后仍然没有查找到分隔符，就抛出TooLongException
                           //防止由于异常码流缺失分隔符导致的内存溢出，这是netty解码器的可靠性保护
                           channel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,delimiter));
                           channel.pipeline().addLast(new StringDecoder());
                           channel.pipeline().addLast(new EchoServerHandler());
                       }
                   });
        ChannelFuture future=boot.bind().sync();
        System.out.println( EchoServer.class.getName()+" started and listen on "+future.channel().localAddress());


        future.channel().closeFuture().sync();


    }


    public static void main(String[] args) {
        EchoServer server=new EchoServer(20000);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
