/*
 * Copyright (c) 2014. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

package jp.ac.keio.sfc.ht.memsys.ghost.actor

import akka.actor.TypedActor.Receiver
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests.GhostRequest

import scala.concurrent.Future

/**
 * Gateway
 * Created on 11/27/14.
 *
 * This actor bridges the outside(Main method) with the inside(Worker System)
 */
trait Gateway extends Receiver {
  def registerApplication(APPNAME :String) :String

  def registerTask(request :GhostRequest) :Future[Any]

  //TODO def registerTaskPipeline(request :GhostRequest) :Future[GhostResponse]

  def checkApplicationHealth(request :GhostRequest) :Future[Any]

  def executeTask(request :GhostRequest) :Future[Any]

  //TODO def stopTask(request :GhostRequest) :Future[GhostResponse]

  def removeApplication(request :GhostRequest) :Future[Any]
}
