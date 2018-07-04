package com.cw.netty.high.protocol;

import com.cw.netty.high.protocol.bean.Header;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author chenwei
 * @create 2018-07-02 17:02
 **/

public class LoginAuthReqHandler extends ChannelHandlerAdapter {


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //建立连接后，发送认证消息
        NettyMessage message=buildLoginReq();
        System.out.println("client 发送 认证消息：message="+message);
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        NettyMessage message=(NettyMessage)msg;
        //若是握手应答消息，判断是否认证成功
        if (message.getHeader() != null && message.getHeader().getType() == MessageType.LOGIN_RESP) {
           byte loginResult= (byte)message.getBody();
            if (loginResult != 0) {
                //握手失败，关闭连接
                ctx.close();
            }else {
                System.out.println("login is ok :"+message);
                ctx.fireChannelRead(msg);
            }

        }else{
            ctx.fireChannelRead(msg);
        }
    }

    private NettyMessage buildLoginReq(){
        NettyMessage message=new NettyMessage();
        Header header=new Header();
        header.setType((byte) MessageType.LOGIN_REQ);
        message.setHeader(header);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
