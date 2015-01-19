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
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import jp.ac.keio.sfc.ht.memsys.ghost.actor.Gateway;
import jp.ac.keio.sfc.ht.memsys.ghost.nqueen.NQueenTaskImpl;
import org.jboss.netty.handler.execution.ExecutionHandler;

/**
 * GhostRequestServer
 * Created on 12/21/14.
 */
public class GhostRequestServer {

//    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", ServerConstants.REQUEST_PORT));
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static void createServer(final Gateway gateway) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
//            final EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(10);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
//                            if (sslCtx != null) {
//                                p.addLast(sslCtx.newHandler(ch.alloc()));
//                            }
                            p.addLast("encoder", new ObjectEncoder());
                            p.addLast("decoder", new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            p.addLast("ghosthandler", new GhostRequestServerHandler(gateway));
                        }
                    });

            // Bind and start to accept incoming connections.
            b.bind(PORT).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

        }

    }
}

