package com.cw.netty.base.server;

import com.cw.netty.base.server.channelhandler.TimeServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * Created by chenwei01 on 2017/4/27.
 */
public class TimeServer {

    private  int port;

    public TimeServer(int port){
        this.port=port;
    }

    public void run() throws Exception{
        //因为使用NIO，指定NioEventLoopGroup来接受和处理新连接
        EventLoopGroup group = new NioEventLoopGroup();
        //创建bootstrap来启动服务器
        ServerBootstrap boot = new ServerBootstrap();
        boot.group(group).
                //指定通道类型为NioServerSocketChannel
                        channel(NioServerSocketChannel.class).
                localAddress(port).
                //调用childHandler用来指定连接后调用的ChannelHandler
                        childHandler(new ChildChannelHandler());
        ChannelFuture future=boot.bind().sync();
        System.out.println( TimeServer.class.getName()+" started and listen on "+future.channel().localAddress());
    }

    public static void main(String[] args) {

        TimeServer timeServer=new TimeServer(8888);

        try {
            timeServer.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class  ChildChannelHandler extends ChannelInitializer{

        /**
         *  lineBaseFrameDecoder   会依次遍历ByteBuf中的可读字节，判断看是否有 “\n”或者“\r\n”，如果有，
         *  就以此位置为结束位置，从可读索引到结束位置区间的字节就组成了一行。它是以换行符为结束标志的解码器，
         *  支持携带结束符或者不携带结束符两种编码放肆，同时支持配置单行的最大长度。
         *  如果连续读取到最大长度后仍然没有发现换行符，就会抛出异常，同时
         *  忽略掉之前读到的异常码流
         */
        /**
         * StringDecoder    功能非常简单，就是将接收到的对象转换为字符串，然后继续调用后面的handler。
         * LineBasedFrameDecoder + StringDecoder  就是按行切换的文本解码器，它被设计用来支持TCP的粘包和拆包
         */
        @Override
        protected void initChannel(Channel channel) throws Exception {
            //增加两个解码器
            channel.pipeline().addLast(new LineBasedFrameDecoder(1024));
            channel.pipeline().addLast(new StringDecoder());
            channel.pipeline().addLast(new TimeServerHandler());

        }
    }
}
