package com.cw.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * nio 时间服务器代码
 * @author chenwei
 * @create 2018-06-26 17:34
 **/

public class NioTimeServer implements Runnable{

   private int port;

   private Selector selector;

   private ServerSocketChannel serverChannel;
   
   private boolean stop;

    public NioTimeServer(int port) {
        this.port = port;
        try {
            selector=Selector.open();
            serverChannel= ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(port),1024);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("The time server is start in port : "+port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!stop){

            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey key=null;
                while (iterator.hasNext()){
                    key = iterator.next();
                    iterator.remove();
                    handleInput(key);


                }


            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        //多路复用器关闭后，所有注册在上面的channel和pipe等资源都会呗自动去注册并关闭，所以不需要重复释放资源
        if (selector!=null){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {

            //处理接入的请求逻辑
            if (key.isAcceptable()) {
                ServerSocketChannel channel =(ServerSocketChannel) key.channel();
                SocketChannel sc = channel.accept();
                sc.configureBlocking(false);
                sc.register(selector, SelectionKey.OP_READ);
            }
            if (key.isReadable()) {
                SocketChannel channel= (SocketChannel)key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes= channel.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("The time server receive order : "+body);
                    String resp="query the time".equalsIgnoreCase(body)?new Date().toString():"bad order";
                    ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
                    writeBuffer.put(resp.getBytes());
                    channel.write(writeBuffer);
                    writeBuffer.flip();
                } else if (readBytes < 0) {
                    key.cancel();
                    channel.close();
                }


            }



        }




    }
}
