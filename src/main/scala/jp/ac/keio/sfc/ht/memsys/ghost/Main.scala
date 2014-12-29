/*
 * Copyright (c) 2014. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

package jp.ac.keio.sfc.ht.memsys.ghost

import java.util.concurrent.LinkedBlockingQueue

import jp.ac.keio.sfc.ht.memsys.ghost.actor.{GatewayActor, Gateway}
import sample.{NQueenApp}
import akka.actor.{TypedProps, TypedActor, ActorSystem}
import com.typesafe.config.ConfigFactory
import jp.ac.keio.sfc.ht.memsys.ghost.server.{GhostRequestServer}

/**
 * Main
 * Created on 11/27/14.
 */
object Main {

  val ID:Int = 0
//  val queue = new LinkedBlockingQueue[Object]()
//  val mGateway:Gateway = null

  def main(args :Array[String]): Unit = {
    args.head match {
      case "Gateway" =>
        println("[Main] Start Gateway")
        startGatewaySystem()

      case "Worker" =>
        println("[Main] StartWorker")
        startWorkerSystem()

//      case "Server" =>
//        println("[Main] Start Server")
//        startServer()

      case _ =>
        println("[Main] No such command")

    }
  }

  def startServer(): Unit = {
//    ControlServer.createServer(2555, queue)
//    GhostRequestServer.createServer(mGateway)
  }

  def startGatewaySystem(): Unit = {
    val system = ActorSystem("Gateway", ConfigFactory.load("gateway"))
    val gateway = TypedActor(system).typedActorOf(TypedProps(classOf[Gateway], new GatewayActor(ID)))

    GhostRequestServer.createServer(gateway)

//    println("[Main] Heap Sort App start...")
//    val heapSortApp :HeapSortApp = new HeapSortApp(gateway)
//    heapSortApp.runApp
//    println("[Main] NQueen App 1 start...")

//    for (i <- 0 until 1) {
//      (new NQueenApp(gateway, "nqueen" + i.toString)).runApp(12)
//    }
//    (new NQueenApp(gateway, "nqueen" + "1")).runApp(13)
//    (new NQueenApp(gateway, "nqueen" + "2")).runApp(13)
//    (new NQueenApp(gateway, "nqueen" + "3")).runApp(8)

  }

  def startWorkerSystem(): Unit = {
    val system = ActorSystem("Worker", ConfigFactory.load("worker"))
  }
}
