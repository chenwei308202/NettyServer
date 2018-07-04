package com.cw.netty.codec.serialization.client.channelhandler;

import com.cw.netty.codec.serialization.model.SubscribeReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
/*
可能你会问为什么在这里使用的是SimpleChannelInboundHandler而不使用ChannelInboundHandlerAdapter？主要原因是ChannelInboundHandlerAdapter在处理完消息后需要负责释放资源。在这里将调用ByteBuf.release()来释放资源。SimpleChannelInboundHandler会在完成channelRead0后释放消息，这是通过Netty处理所有消息的ChannelHandler实现了ReferenceCounted接口达到的。
为什么在服务器中不使用SimpleChannelInboundHandler呢？因为服务器要返回相同的消息给客户端，在服务器执行完成写操作之前不能释放调用读取到的消息，因为写操作是异步的，一旦写操作完成后，Netty中会自动释放消息。
*/

/**
 * @author  chenwei
 * Created by chenwei01 on 2017/4/25.
 */
public class SubReqClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private int counter;
    /**
     * 从服务器接收到数据后调用
     * @param channelHandlerContext
     * @param o
     * @throws Exception
     */
//    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf o) throws Exception {
        System.out.println("Client received: " + o.toString(CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 0; i <10 ; i++) {

        ctx.write(subReq(i));
        }
        ctx.flush();
    }

    private SubscribeReq subReq(int i){

        SubscribeReq req=new SubscribeReq();
        req.setSubReqID(i);
        req.setPhoneNumber("18034202387");
        req.setAddress("河北邯郸市");
        req.setUserName("陈伟");
        return req;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("this receive from server :["+msg+"]");
    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {

    }
}
