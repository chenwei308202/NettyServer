package com.cw.netty.high.protocol;

import com.cw.netty.high.protocol.bean.Header;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenwei
 * @create 2018-07-02 17:19
 **/

public class LoginAuthRespHandler extends ChannelHandlerAdapter {

    private Map<String, Boolean> nodeCheck = new ConcurrentHashMap<>();

    private String [] whiteList={"127.0.0.1","10.155.33.113"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message=(NettyMessage)msg;
        //若为握手认证消息，则校验并返回响应，否则传递到下一个handler
        if (message.getHeader() != null && message.getHeader().getType() == MessageType.LOGIN_REQ) {
            String nodeIndex= ctx.channel().remoteAddress().toString();
            NettyMessage loginResp=null;
            if (nodeCheck.containsKey(nodeIndex)) {
                //重复登陆，拒绝
                loginResp=buildResponse((byte)-1);
                System.out.println("重复登陆，拒绝 ：ip="+nodeIndex);
            }else{
                boolean isOk=true;
               InetSocketAddress address=(InetSocketAddress) ctx.channel().remoteAddress();
               String ip=  address.getAddress().getHostAddress();
               for (String wip:whiteList){
                   if (wip.equals(ip)) {
                       isOk=true;
                       nodeCheck.put(ip, true);
                       System.out.println("通过白名单检测 ip="+ip);
                       break;
                   }
               }
               loginResp=isOk?buildResponse((byte) 0):buildResponse((byte)-1);
            }
            ctx.writeAndFlush(loginResp);

        }else {
            ctx.fireChannelRead(msg);
        }

    }

    private NettyMessage buildResponse(byte result){
        NettyMessage message=new NettyMessage();
        Header header=new Header();
        header.setType(MessageType.LOGIN_RESP);
        message.setHeader(header);
        message.setBody(result);
        return message;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        nodeCheck.remove(ctx.channel().remoteAddress().toString());
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }
}
