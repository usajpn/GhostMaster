/*
 * Copyright (c) 2015. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

package jp.ac.keio.sfc.ht.memsys.ghost.server

/**
 * RequestServer
 * Created on 1/13/15.
 */
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.group.DefaultChannelGroup
import io.netty.channel.{ChannelInitializer, ChannelPipeline}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.serialization.{ClassResolvers, ObjectDecoder, ObjectEncoder}
import io.netty.handler.logging.{LoggingHandler, LogLevel}
import java.net.InetSocketAddress

import io.netty.util.concurrent.{DefaultEventExecutorGroup, EventExecutorGroup}
import jp.ac.keio.sfc.ht.memsys.ghost.actor.Gateway

class RequestServer(gateway: Gateway) {
  val port = 2555

  val bossGroup = new NioEventLoopGroup(1000)
  val workerGroup = new NioEventLoopGroup(1000)
  val eventExecutorGroup: EventExecutorGroup = new DefaultEventExecutorGroup(1000)

  try new ServerBootstrap()
    .group(bossGroup, workerGroup)
    .channel(classOf[NioServerSocketChannel])
    .localAddress(new InetSocketAddress(port))
    .handler(new LoggingHandler(LogLevel.INFO))
    .childHandler(new ChannelInitializer[SocketChannel] {
    override def initChannel(ch: SocketChannel): Unit = {
      val p:ChannelPipeline = ch.pipeline()
      p.addLast("encoder", new ObjectEncoder())
      p.addLast("decoder", new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
      p.addLast(eventExecutorGroup, "request", new RequestServerHandler(gateway))
//      p.addLast(eventExecutorGroup, "request", new RequestServerHandler(gateway))
      //        new ResponseServerHandler(gateway, channelGroup)
    }
  })
    // Bind and start to accept incoming connections.
    .bind().sync().channel().closeFuture().sync()
  finally {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}
