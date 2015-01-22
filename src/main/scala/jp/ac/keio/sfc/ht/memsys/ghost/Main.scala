/*
 * Copyright (c) 2014. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

package jp.ac.keio.sfc.ht.memsys.ghost

import java.net.InetAddress
import java.util.concurrent.LinkedBlockingQueue

import jp.ac.keio.sfc.ht.memsys.ghost.actor.{GatewayActor, Gateway}
import sample.{HeapSortApp, NQueenApp}
import akka.actor.{TypedProps, TypedActor, ActorSystem}
import com.typesafe.config.ConfigFactory
import jp.ac.keio.sfc.ht.memsys.ghost.server.GhostRequestServer

/**
 * Main
 * Created on 11/27/14.
 */
object Main {

  val ID:Int = 0
  var gateway:Gateway = null
//  val queue = new LinkedBlockingQueue[Object]()
//  val mGateway:Gateway = null

  def main(args :Array[String]): Unit = {
    args.head match {
      case "Gateway" =>
        println("[Main] Start Gateway")
        val localhost = InetAddress.getLocalHost
        val hostIp = localhost.getHostAddress
        val hostNum:Int = args(1).toInt
        startGatewaySystem(hostIp, hostNum)

      case "Worker" =>
        println("[Main] StartWorker")
        val localhost = InetAddress.getLocalHost
        val hostIp = localhost.getHostAddress
        startWorkerSystem(hostIp)

      case "nqueen" =>
        println("[Main] Start Gateway")
        val localhost = InetAddress.getLocalHost
        val hostIp = localhost.getHostAddress
        val hostNum:Int = args(1).toInt
        val queenNum:Int = args(2).toInt
        startNQueenAndGateway(hostIp, hostNum, queenNum)

      case _ =>
        println("[Main] No such command")

    }
  }

  def startGatewaySystem(hostIp: String, hostNum: Int): Unit = {
    val config = ConfigFactory.parseString("""akka.remote.netty.tcp.hostname="""" + hostIp + """"""")
    val system = ActorSystem("Gateway", config.withFallback(ConfigFactory.load("gateway")))
    gateway = TypedActor(system).typedActorOf(TypedProps(classOf[Gateway], new GatewayActor(ID, hostNum)))
    GhostRequestServer.createServer(gateway)
  }

  def startWorkerSystem(hostIp:String): Unit = {
    val config = ConfigFactory.parseString("""akka.remote.netty.tcp.hostname="""" + hostIp + """"""")
    val system = ActorSystem("Worker", config.withFallback(ConfigFactory.load("worker")))
  }

  def startNQueenAndGateway(hostIp: String, hostNum: Int, queenNum: Int): Unit = {
    val config = ConfigFactory.parseString("""akka.remote.netty.tcp.hostname="""" + hostIp + """"""")
    val system = ActorSystem("Gateway", config.withFallback(ConfigFactory.load("gateway")))
    gateway = TypedActor(system).typedActorOf(TypedProps(classOf[Gateway], new GatewayActor(ID, hostNum)))
    new NQueenApp(gateway, queenNum).runApp()
  }

}
