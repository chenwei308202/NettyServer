package com.cw.netty.high.protocol.bean;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import io.netty.handler.codec.marshalling.UnmarshallerProvider;

/**
 * @author chenwei
 * @create 2018-07-02 16:12
 **/

public class NettyMarshallingDecoder extends MarshallingDecoder {
    public NettyMarshallingDecoder(UnmarshallerProvider provider) {
        super(provider);
    }

    public NettyMarshallingDecoder(UnmarshallerProvider provider, int maxObjectSize) {
        super(provider, maxObjectSize);
    }

    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception{
        return super.decode(ctx,in);
    }
}
