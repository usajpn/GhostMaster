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
 * RequestServerHandler
 * Created on 1/13/15.
 */

import akka.actor.Status.{Failure, Success}
import akka.util.Timeout
import io.netty.channel.group.DefaultChannelGroup
import io.netty.channel.{ChannelInboundHandlerAdapter, ChannelHandlerContext}
import java.util.logging.Level

import jp.ac.keio.sfc.ht.memsys.ghost.actor.Gateway
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.{GhostResponseTypes, GhostRequestTypes}
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests.{Bundle, BundleKeys, GhostResponse, GhostRequest}

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Await, Future}
import scala.util

class RequestServerHandler(gateway: Gateway) extends ChannelInboundHandlerAdapter {

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    // Echo back the received object to the client.
    val m: GhostRequest = msg.asInstanceOf[GhostRequest]
    var res:GhostResponse = null
    if (m.TYPE.equals(GhostRequestTypes.INIT)) {
      val appId:String = gateway.registerApplication(m.PARAMS.getData(BundleKeys.APP_NAME))
      val bundle:Bundle = new Bundle()
      bundle.putData(BundleKeys.APP_ID, appId)
      res = new GhostResponse(GhostResponseTypes.SUCCESS, GhostRequestTypes.INIT, bundle)
    } else if (m.TYPE.equals(GhostRequestTypes.REGISTERTASK)) {
      val futureTask: Future[Any] = gateway.registerTask(m)
      implicit val timeout = Timeout(3000)
      // waiting for task to be registered...
      val result = Await.result(futureTask, timeout.duration).asInstanceOf[GhostResponse]
      res = new GhostResponse(GhostResponseTypes.SUCCESS, GhostRequestTypes.REGISTERTASK, null)
    } else if (m.TYPE.equals(GhostRequestTypes.EXECUTE)) {
      val futureTask: Future[Any] = gateway.executeTask(m)
      implicit val timeout = Timeout(3000)
      // waiting for task to be registered...
      val result = Await.result(futureTask, timeout.duration).asInstanceOf[GhostResponse]
      res = new GhostResponse(GhostResponseTypes.SUCCESS, GhostRequestTypes.EXECUTE, null)
    } else {
      System.out.println("[Ghost Request Server Handler] UNKNOWN REQUEST")
    }
    if (res != null) {
      ctx.write(res)
    }
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    ctx.close()
  }
}
