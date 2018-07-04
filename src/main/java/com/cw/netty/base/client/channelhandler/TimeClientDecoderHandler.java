package com.cw.netty.base.client.channelhandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by chenwei01 on 2017/4/27.
 */
public class TimeClientDecoderHandler extends ByteToMessageDecoder{
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes()<4){
            return;
        }
        list.add(byteBuf.readBytes(4));
    }
}
