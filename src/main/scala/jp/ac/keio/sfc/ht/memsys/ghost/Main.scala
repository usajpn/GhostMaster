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
import sample.{NQueenApp, HeapSortApp}
import akka.actor.{TypedProps, TypedActor, ActorSystem}
import com.typesafe.config.ConfigFactory
import jp.ac.keio.sfc.ht.memsys.ghost.server.ControlServer

/**
 * Main
 * Created on 11/27/14.
 */
object Main {

  val ID:Int = 0
  val queue = new LinkedBlockingQueue[Object]()

  def main(args :Array[String]): Unit = {
    args.head match {
      case "Gateway" =>
        println("[Main] Start Gateway")
        startServer()
        startGatewaySystem()

      case "Worker" =>
        println("[Main] StartWorker")
        startWorkerSystem()

      case _ =>
        println("[Main] No such command")

    }
  }

  def startServer(): Unit = {
    ControlServer.createServer(2555, queue)
  }

  def startGatewaySystem(): Unit = {
    val system = ActorSystem("Gateway", ConfigFactory.load("gateway"))
    val gateway = TypedActor(system).typedActorOf(TypedProps(classOf[Gateway], new GatewayActor(ID)))

//    println("[Main] Heap Sort App start...")
//    val heapSortApp :HeapSortApp = new HeapSortApp(gateway)
//    heapSortApp.runApp
    println("[Main] NQueen App 1 start...")

//    for (i <- 0 until 1) {
//      (new NQueenApp(gateway, "nqueen" + i.toString)).runApp(12)
//    }
    (new NQueenApp(gateway, "nqueen" + "1")).runApp(13)
    (new NQueenApp(gateway, "nqueen" + "2")).runApp(13)
    (new NQueenApp(gateway, "nqueen" + "3")).runApp(8)

  }

  def startWorkerSystem(): Unit = {
    val system = ActorSystem("Worker", ConfigFactory.load("worker"))
  }
}
