package com.cw.netty.high.protocol;

import com.cw.netty.high.protocol.bean.Header;
import com.cw.netty.high.protocol.bean.MarshallingCodecFactory;
import com.cw.netty.high.protocol.bean.NettyMarshallingDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.HashMap;
import java.util.Map;

/**
 * Netty消息解码类
 * @author chenwei
 * @create 2018-07-02 16:31
 *
 * 继承LengthFieldBasedFrameDecoder是为了更好了使用它对tcp的粘包和半包处理，
 * 只需要给我表示消息长度的字段偏移量和消息长度自身所占的字节数，该解码器就能
 * 自动实现对半包的处理，调用父类LengthFieldBasedFrameDecoder的decode方法后，
 * 返回的就是整包消息或者为null,
 **/

public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder {

    NettyMarshallingDecoder marshallingDecoder;

    public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
        marshallingDecoder = MarshallingCodecFactory.buildMarshallingDecoder();
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in1) throws Exception {
        //父类解码后的消息，后续就针对处理后的消息体进行解码，这也是
        //《Netty权威指南》的另一处书写错误，书中仍对原ByteBuf进行读取
        //由于父类decode后，读指针已经到达了消息头总长度处，此后再对原消息
        //进行读取后报处下标越界的异常
        ByteBuf frame=(ByteBuf) super.decode(ctx, in1);
        if (frame == null) {
            return null;
        }
        NettyMessage message =new NettyMessage();
        Header header=new Header();
        header.setCrcCode(frame.readInt());
        header.setLength(frame.readInt());
        header.setSessionID(frame.readLong());
        header.setType(frame.readByte());
        header.setPriority(frame.readByte());
        int size= frame.readInt();
        if (size > 0) {
            int keySize=0;
            byte[] keyArray=null;
            String key=null;

            Map<String, Object> attch = new HashMap<String, Object>();
            for (int i = 0; i <size ; i++) {
                keySize= frame.readInt();
                keyArray = new byte[keySize];
                frame.readBytes(keyArray);
                key = new String(keyArray, "UTF-8");
                attch.put(key, marshallingDecoder.decode(ctx,frame));

            }
            header.setAttachment(attch);
        }
        //readableBytes即为判断剩余可读取的字节数（ this.writerIndex - this.readerIndex）
        //大于4说明有消息体（无消息体时readableBytes=4），故进行解码
        if (frame.readableBytes() > 4) {
            message.setBody(marshallingDecoder.decode(ctx, frame));
        }
        message.setHeader(header);
        return message;
    }
}
