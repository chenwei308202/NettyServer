package com.cw.netty.high.http.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

/**
 * 文件服务器的处理逻辑
 *
 * @author chenwei
 * @create 2018-06-28 11:06
 **/

public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest>{

    private String url;

    public HttpFileServerHandler(String url) {
        this.url=url;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) throws Exception {
        if (!request.getDecoderResult().isSuccess()) {
            //bad request
            sendError(channelHandlerContext,HttpResponseStatus.BAD_REQUEST);
            return;
        }
        if (request.getMethod()!= HttpMethod.GET){
            //
            sendError(channelHandlerContext,HttpResponseStatus.METHOD_NOT_ALLOWED);
            return ;
        }
        final String uri = request.getUri();
        System.out.println("没有解码前的请求uri"+uri);
        String path = sanitizeUri(uri);
        System.out.println("该文件路径:"+path);
        if (path == null) {
            sendError(channelHandlerContext,HttpResponseStatus.NOT_FOUND);
            return;
        }
        File file = new File(path);

        if (file.isHidden() || !file.exists()) {
            System.out.println("文件隐藏或不存在");
            sendError(channelHandlerContext,HttpResponseStatus.NOT_FOUND);

            return;
        }
        if (file.isDirectory()) {
            System.out.println("是目录");
            if (uri.endsWith("/")) {
                sendListing(channelHandlerContext,file);
            }else {
                sendredirect(channelHandlerContext,uri+"/");
            }
            return;
        }

        if (!file.isFile()) {
            sendError(channelHandlerContext,HttpResponseStatus.FORBIDDEN);
            return;
        }
        System.out.println("如果是文件");
        RandomAccessFile randomAccessFile=new RandomAccessFile(file,"r");
        long length = randomAccessFile.length();

        HttpResponse response=new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);

        if (HttpHeaders.isKeepAlive(request)) {
            response.headers().set(HttpHeaders.Names.CONNECTION,HttpHeaders.Values.KEEP_ALIVE);
        }
        channelHandlerContext.write(response);
        ChannelFuture sendFileFutrue= channelHandlerContext.write(new ChunkedFile(randomAccessFile,0,length,8192),channelHandlerContext.newProgressivePromise());
        sendFileFutrue.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture channelProgressiveFuture, long l, long l1) throws Exception {
                if (l1 < 0) {
                    System.err.println("Transger progress :"+l);
                }else {
                    System.err.println("Transfer progress :"+l+"/"+l1);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture channelProgressiveFuture) throws Exception {
                System.out.println("Transfer complete.");
            }
        });

        ChannelFuture future = channelHandlerContext.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (HttpHeaders.isKeepAlive(request)) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,length);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE,new MimetypesFileTypeMap().getContentType(file));

    }

    private void sendError(ChannelHandlerContext channelHandlerContext,HttpResponseStatus status){
        FullHttpResponse response=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,status, Unpooled.copiedBuffer("Failure: "+status.toString()+"\r\n", CharsetUtil.UTF_8));
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private String sanitizeUri(String uri){
        try {
           uri= URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
              uri=  URLDecoder.decode(uri,"ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new RuntimeException("解码异常");
            }
        }

        if (!url.startsWith("/")) {
            return null;
        }
        uri=uri.replace("/", File.separator);

        return System.getProperty("user.dir")+ uri;
    }

    private static final Pattern ALLOW_FILE_NAME=Pattern.compile("[\\s\\S]*");

    private static void sendListing(ChannelHandlerContext channelHandlerContext, File dir) {
        FullHttpResponse response=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE,"text/html;charset=UTF-8");
        StringBuilder builder = new StringBuilder();
        String dirPath = dir.getPath();
        builder.append("<!DOCTYPE html>\r\n");
        builder.append("<html><head><title>");
        builder.append(dirPath);
        builder.append("目录：");
        builder.append("</title></head><body>\r\n");
        builder.append("<h3>");
        builder.append("<ul>");
        builder.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
        for (File file : dir.listFiles()) {
            if (file.isHidden() || !file.canRead()) {
                continue;
            }
            String name=file.getName();
            if (!ALLOW_FILE_NAME.matcher(name).matches()) {
                continue;
            }
            builder.append("<li>链接：<a href=\"");
            builder.append(name);
            builder.append("\">");
            builder.append(name);
            builder.append("</a></li>\r\n");
        }
        builder.append("</ul></body></html>\r\n");
        ByteBuf byteBuf = Unpooled.copiedBuffer(builder, CharsetUtil.UTF_8);
        response.content().writeBytes(byteBuf);
        byteBuf.release();
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

    }

    private static  void sendredirect(ChannelHandlerContext channelHandlerContext, String newUrl) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaders.Names.LOCATION, newUrl);
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
