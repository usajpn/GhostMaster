/*
 * Copyright (c) 2014. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

package jp.ac.keio.sfc.ht.memsys.ghost.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ControlServer.java
 * Created on 11/27/14.
 */
public class ControlServer {
    public static void createServer(final int PORT, final LinkedBlockingQueue queue) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {


                EventLoopGroup bossGroup = new NioEventLoopGroup(1);
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .option(ChannelOption.SO_BACKLOG, 100)
                            .handler(new LoggingHandler(LogLevel.INFO))
                            .childHandler(new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
                                @Override
                                public void initChannel(io.netty.channel.socket.SocketChannel ch) throws Exception {
                                    ChannelPipeline p = ch.pipeline();
                                    p.addLast(new HttpServerCodec());
                                    p.addLast(new HttpObjectAggregator(1048576));
                                    p.addLast(new ContServerHandler(queue, PORT));
                                }
                            });

                    // Start the server.
                    ChannelFuture f = b.bind(PORT).sync();

                    // Wait until the server socket is closed.
                    f.channel().closeFuture().sync();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // Shut down all event loops to terminate all threads.
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }

            }
        });

        t.start();
    }
}
