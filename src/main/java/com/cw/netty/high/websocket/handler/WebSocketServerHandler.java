package com.cw.netty.high.websocket.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.util.Date;

/**
 * @author chenwei
 * @create 2018-06-29 15:46
 **/

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object>{

    private WebSocketServerHandshaker webSocketServerHandshaker;

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        //传统的HTTP接入,第一次魔兽请求消息由http协议承载，所以它是一个http消息，执行handleHttpRequest方法来
        //处理WebSocket握手请求。
        if (o instanceof FullHttpRequest) {
            handleHttpRequest(channelHandlerContext,(FullHttpRequest) o);
        } else if (o instanceof WebSocketFrame) {
            //websocket接入
            handleWebSocketFrame( channelHandlerContext, (WebSocketFrame)o);
        }

    }
    //首先对握手消息进行判断，如果没有包含Upgrade自福安或者它的值不是websocket，则返回400
    private void handleHttpRequest(ChannelHandlerContext context, FullHttpRequest request) {
        if (!request.getDecoderResult().isSuccess()||(!"websocket".equals(request.headers().get("Upgrade")))) {
            FullHttpResponse response=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            ByteBuf buf= Unpooled.copiedBuffer(response.getStatus().toString(), CharsetUtil.UTF_8);
            response.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(response,response.content().readableBytes());

            ChannelFuture future =context.channel().writeAndFlush(response);

            if (!HttpHeaders.isKeepAlive(request) || response.getStatus().code() != 200) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
            return ;
        }
        //握手请求简单校验通过之后，开始构造握手工厂，创建握手处理类WebSocketServerHandshaker
        //通过它构造握手响应消息返回给客户端，同时将webSocket相关的编码和解码类动态添加到channelPipeline中（进入handshake源码可看到）
        //用于webSocket消息的编解码。
        //添加了Websocket Encoder 和 WebSocket Decoder之后，服务端就可以自动对WebSocket消息进行编解码了，
        WebSocketServerHandshakerFactory  wsFactory = new WebSocketServerHandshakerFactory("ws://localhost:8088/websocket",null,false);
        webSocketServerHandshaker = wsFactory.newHandshaker(request);
        if (webSocketServerHandshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(context.channel());
        }else {
            webSocketServerHandshaker.handshake(context.channel(), request);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext context, WebSocketFrame request){
        //判断是否是关闭链路的指令
        if (request instanceof CloseWebSocketFrame) {
            webSocketServerHandshaker.close(context.channel(),(CloseWebSocketFrame) request.retain());
            return ;
        }
        //判断是否是ping消息
        if (request instanceof PingWebSocketFrame) {
            context.write(new PongWebSocketFrame(request.content().retain()));
            return;
        }

        String requestString= ((TextWebSocketFrame)request).text();
        context.channel().write(new TextWebSocketFrame(requestString+",欢迎使用Netty Websocket服务，现在时刻："+new Date().toString()));


    }
}
