/*
 * Copyright (c) 2014. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

package jp.ac.keio.sfc.ht.memsys.ghost.actor

import akka.actor._
import akka.event.Logging
import akka.remote.RemoteScope
import akka.util.Timeout

import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.{GhostResponseTypes, GhostRequestTypes}
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests.{BundleKeys, Bundle, GhostRequest, GhostResponse}
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.util.Util

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

import akka.pattern.ask

/**
 * GatewayActor
 * Created on 11/27/14.
 *
 * Implementation of Gateway Trait
 */
class GatewayActor(id: Int) extends Gateway {
  private val mRefMap: mutable.HashMap[String, ActorRef] = new mutable.HashMap()
  val log = Logging(TypedActor.context.system, TypedActor.context.self)

  override def registerApplication(APPNAME: String): String = {
    //TODO return address too

    val APP_ID :String = Util.makeSHA1Hash(APPNAME)
    val host = Address("akka.tcp", "Worker", "127.0.0.1", 2552)
    val ref = TypedActor.context.actorOf(HeadActor.props(APP_ID).withDeploy(Deploy(scope = RemoteScope(host))))

    mRefMap.put(APP_ID, ref)

    APP_ID
  }

  override def removeApplication(request: GhostRequest): Future[Any] = ???

  override def registerTask(request: GhostRequest): Future[Any] = {
    val bundle :Bundle = request.PARAMS
    val appId = bundle.getData(BundleKeys.APP_ID)
    val taskId = bundle.getData(BundleKeys.TASK_ID)

    val head = mRefMap.get(appId)

    head match {
      case Some(ref) => {
        val bundle = new Bundle()
        bundle.putData(BundleKeys.APP_ID, appId)
        bundle.putData(BundleKeys.TASK_ID, taskId)
        val mes = new GhostRequest(GhostRequestTypes.REGISTERTASK, bundle)

        implicit val timeout = Timeout(10 seconds)
        val future = ref ? mes

        return future
      }
      case None => {
        Future {
          val bundle = new Bundle()
          bundle.putData(BundleKeys.APP_ID, appId)
          bundle.putData(BundleKeys.TASK_ID, taskId)
          bundle.putData(BundleKeys.MESSAGE, "ERROR: NO SUCH APPLICATION")
          new GhostResponse(GhostResponseTypes.FAIL, "", bundle)
        }
      }
    }
  }

  override def checkApplicationHealth(request: GhostRequest): Future[Any] = ???

  override def executeTask(request: GhostRequest): Future[Any] = {
    log.info("[GATEWAY ACTOR] Received execute request!")
    val bundle :Bundle = request.PARAMS
    val appId = bundle.getData(BundleKeys.APP_ID)
    val taskId = bundle.getData(BundleKeys.TASK_ID)
    val seq = bundle.getData(BundleKeys.DATA_SEQ)

    log.info("[GATEWAY ACTOR] Execute request APP_ID:" + appId + " TASK_ID:" + taskId + " SEQ:" + seq)

    val head = mRefMap.get(appId)

    head match {
      case Some(ref) => {
        val bundle = new Bundle()
        bundle.putData(BundleKeys.APP_ID, appId)
        bundle.putData(BundleKeys.TASK_ID, taskId)
        bundle.putData(BundleKeys.DATA_SEQ, seq)
        val mes = new GhostRequest(GhostRequestTypes.EXECUTE, bundle)

        implicit val timeout = Timeout(10 seconds)
        val future = ref ? mes

        return future
      }
    }
  }

  override def onReceive(message: Any, sender: ActorRef): Unit = ???
}
