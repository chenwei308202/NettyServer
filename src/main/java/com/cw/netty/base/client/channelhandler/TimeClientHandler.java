package com.cw.netty.base.client.channelhandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by chenwei01 on 2017/4/27.
 */
public class TimeClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private int counter;



    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        byte[] req=("query time order"+System.getProperty("line.separator")).getBytes();

        for (int i = 0; i <100 ; i++) {
           ByteBuf buf= Unpooled.buffer(req.length);
            buf.writeBytes(req);
            ctx.writeAndFlush(buf);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

         /*ByteBuf buf= (ByteBuf)msg;
         byte[] req=new byte[buf.readableBytes()];
         buf.readBytes(req);*/
         String body=(String) msg;
        System.out.println("Client receive time is :"+body+" ; counter is "+(++counter));


    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
//        System.out.println("Client received: " + o.readInt());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
