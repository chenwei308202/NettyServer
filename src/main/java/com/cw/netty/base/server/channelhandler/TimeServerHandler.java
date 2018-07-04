package com.cw.netty.base.server.channelhandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

/**
 * 服务端发送时间 示例
 * Created by chenwei01 on 2017/4/27.
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter{

    private int counter;

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {

        System.out.println("有一个客户端接入了————");

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        /*
        由于添加了解码器，可以将接收到的msg直接强转为string
        ByteBuf buf=(ByteBuf)msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body=new String(req,"UTF-8").trim();*/
        String body=(String) msg;
        System.out.println("The server receive order : "+body +"; the counter is : "+(++counter));
        String currentTime= ("query time order".equalsIgnoreCase(body)?new Date().toString():" bad order")+System.getProperty("line.separator");
        ByteBuf resp=Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(resp);
    }
}
