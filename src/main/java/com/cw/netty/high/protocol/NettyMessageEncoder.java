package com.cw.netty.high.protocol;

import com.cw.netty.high.protocol.bean.MarshallingCodecFactory;
import com.cw.netty.high.protocol.bean.NettyMarshallingEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;
import java.util.Map;

/**
 * 消息编码器
 *
 * @author chenwei
 * @create 2018-07-02 11:32
 **/

public class NettyMessageEncoder extends MessageToMessageEncoder<NettyMessage> {

    private NettyMarshallingEncoder marshallingEncoder;

    public NettyMessageEncoder(){
        marshallingEncoder= MarshallingCodecFactory.buildMarshallingEncoder();
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage, List<Object> out)  throws Exception{
        if (nettyMessage == null || nettyMessage.getHeader()==null) {
            throw new Exception("The encode message is null");
        }
        ByteBuf buffer = Unpooled.buffer();
        System.out.println("开始编码："+nettyMessage);
        //按顺利编码后，根据定义的字段数据类型写入ByteBuf,解码时也要按顺序挨个取出
        buffer.writeInt(nettyMessage.getHeader().getCrcCode());
        buffer.writeInt(nettyMessage.getHeader().getLength());
        buffer.writeLong(nettyMessage.getHeader().getSessionID());
        buffer.writeByte(nettyMessage.getHeader().getType());
        buffer.writeByte(nettyMessage.getHeader().getPriority());
        buffer.writeInt(nettyMessage.getHeader().getAttachment().size());
        String key=null;
        Object value=null;
        byte[] keyArray=null;
        //针对header中的附件编码
        for (Map.Entry<String,Object> param : nettyMessage.getHeader().getAttachment().entrySet()) {
            key=param.getKey();
            keyArray= key.getBytes("UTF-8");
            value= param.getValue();

            buffer.writeInt(keyArray.length);
            buffer.writeBytes(keyArray);
            marshallingEncoder.encode(channelHandlerContext,value,buffer);

        }
        if (nettyMessage.getBody() != null) {
            //使用MarshallingEncoder编码消息体
            marshallingEncoder.encode(channelHandlerContext,nettyMessage.getBody(),buffer);
        }else {
            //没有消息体的话，就赋予0值
            buffer.writeInt(0);
        }
        //更新消息长度字段的值，至于为什么-8，是因为8是长度字段后的偏移量，LengthFieldBasedFrameDecoder的源码中
        //对长度字段和长度的偏移量之和做了判断，如果不-8，会导致LengthFieldBasedFrameDecoder解码返回null
        //这是 《Netty权威指南》中的写错的地方
        buffer.setInt(4, buffer.readableBytes()-8);
        //书中此处没有add，也即没有将ByteBuf加入到List中，也就没有消息进行编码了，所以导致运行了没有效果……
        out.add(buffer);
    }


}
