package com.cw.netty.high.protocol;

import com.cw.netty.high.protocol.bean.Header;

/**
 * netty消息类定义
 *
 * @author chenwei
 * @create 2018-07-02 11:19
 **/

public class NettyMessage {

    private Header header;//消息头

    private Object body;//消息体

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NettyMessage{" +
                "header=" + header +
                ", body=" + body +
                '}';
    }
}
