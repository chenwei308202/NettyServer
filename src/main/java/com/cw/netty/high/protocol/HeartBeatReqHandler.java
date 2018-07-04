package com.cw.netty.high.protocol;

import com.cw.netty.high.protocol.bean.Header;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 客户端心跳检测
 * @author chenwei
 * @create 2018-07-02 17:36
 **/

public class HeartBeatReqHandler extends ChannelHandlerAdapter {

    private volatile ScheduledFuture heartBeaet;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message= (NettyMessage)msg;
        //认证成功后，定时发送心跳检测
        if (message.getHeader() != null && message.getHeader().getType() == MessageType.LOGIN_RESP) {
            heartBeaet=ctx.executor().scheduleAtFixedRate(new HeartBeatTask(ctx), 0, 5000, TimeUnit.MILLISECONDS);
        } else if (message.getHeader() != null && message.getHeader().getType() == MessageType.HEARTBEAT_RESP) {
            System.out.println("client receive server heart beat message :-->"+message);
        }else {
            ctx.fireChannelRead(msg);
        }
    }


    private class HeartBeatTask implements Runnable{
        ChannelHandlerContext ctx;

        public HeartBeatTask(ChannelHandlerContext ctx) {
            this.ctx=ctx;
        }

        @Override
        public void run() {
            NettyMessage nettyMessage=buildHeatBeat();
            System.out.println("client send heart beat message to server :--->"+nettyMessage);
            ctx.writeAndFlush(nettyMessage);

        }
    }
    //心跳检测仅消息头就够了
    private NettyMessage buildHeatBeat(){
        NettyMessage message=new NettyMessage();
        Header header=new Header();
        header.setType(MessageType.HEARTBEAT_REQ);
        message.setHeader(header);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (heartBeaet != null) {
            heartBeaet.cancel(true);
            heartBeaet=null;
        }
        ctx.fireExceptionCaught(cause);
    }
}
