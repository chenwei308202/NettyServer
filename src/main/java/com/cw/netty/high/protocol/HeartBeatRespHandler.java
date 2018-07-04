package com.cw.netty.high.protocol;

import com.cw.netty.high.protocol.bean.Header;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 服务器端心跳handler
 * @author chenwei
 * @create 2018-07-02 18:00
 **/

public class HeartBeatRespHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message=(NettyMessage)msg;
        //收到心跳消息后，构造心跳应答消息返回
        if (message.getHeader() != null && message.getHeader().getType() == MessageType.HEARTBEAT_REQ) {
            System.out.println("receive client heart beat message :-->");

            NettyMessage heartBeat=buildHeatBeat();
            System.out.println("send heart beat response message to client:-->"+heartBeat);
            ctx.writeAndFlush(heartBeat);
        }else {
            ctx.fireChannelRead(msg);
        }

    }


    private NettyMessage buildHeatBeat(){
        NettyMessage message=new NettyMessage();
        Header header=new Header();
        header.setType(MessageType.HEARTBEAT_RESP);
        message.setHeader(header);
        return message;
    }


}
