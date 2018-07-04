package com.cw.netty.nio;

/**
 * @author chenwei
 * @create 2018-06-26 17:38
 **/

public class MainTest {


    public static void main(String[] args) {


        NioTimeServer server=new NioTimeServer(8080);

        new Thread(server,"nio Time server thread").start();

    }
}
