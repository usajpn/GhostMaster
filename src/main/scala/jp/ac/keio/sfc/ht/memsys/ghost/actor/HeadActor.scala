/*
 * Copyright (c) 2014. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

package jp.ac.keio.sfc.ht.memsys.ghost.actor

import akka.actor.Actor.Receive
import akka.actor.Props
import akka.actor.Actor
import akka.event.Logging
import akka.routing.RoundRobinRouter
import akka.util.Timeout
import akka.pattern.ask

import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.{GhostResponseTypes, GhostRequestTypes}
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests.{GhostResponse, BundleKeys, Bundle, GhostRequest}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * HeadActor
 * Created on 11/27/14.
 */

object HeadActor {
  def props(id: String): Props = Props(new HeadActor(id))
}

class HeadActor(id: String) extends Actor {
  //TODO
  private val MAXACTORNUMS = 100
  // One ID, one head actor(router)
  val router = context.actorOf(MemberActor.props(id).withRouter(RoundRobinRouter(nrOfInstances = MAXACTORNUMS)))

  val log = Logging(context.system, this)
  log.info("Head Actor Parent is: " + context.parent.path.toString)

  override def receive: Receive = {
    case request: GhostRequest => {
      request.TYPE match {
        case GhostRequestTypes.REGISTERTASK => {
          //TODO LinkedList -> mutable.LinearSeq

          log.info("[HEAD ACTOR] Received register task")

          val bundle: Bundle = request.PARAMS
          val taskId: String = bundle.getData(BundleKeys.TASK_ID)

          log.info("[HEAD ACTOR] Task ID:" + taskId)

          val gateway = sender

          if (taskId != null) {
            val response = new GhostResponse(GhostResponseTypes.SUCCESS, "", null)
            gateway ! response
          }
          else {
            val response = new GhostResponse(GhostResponseTypes.FAIL, "", null)
            gateway ! response
          }
        }

        case GhostRequestTypes.EXECUTE => {
          log.info("[HEAD ACTOR] Received execute task")

          val bundle: Bundle = request.PARAMS
          val taskId: String = bundle.getData(BundleKeys.TASK_ID)
          val requestSeq: String = bundle.getData(BundleKeys.DATA_SEQ)

          log.info("[HEAD ACTOR] Task ID:" + taskId)

          val params = new Bundle()
          params.putData(BundleKeys.TASK_ID, taskId)
          params.putData(BundleKeys.DATA_SEQ, requestSeq)

          log.info("[HEAD ACTOR] Task execute request to child")

          implicit val timeout = Timeout(5 seconds)
          val result: Future[GhostResponse] = ask(router, new GhostRequest(GhostRequestTypes.EXECUTE, params)).mapTo[GhostResponse]

          val parent = sender

          result onComplete {
            case Success(response) => {
              response.STATUS match {
                case GhostResponseTypes.SUCCESS => {
                  log.info("[HEAD ACTOR] APP_ID:" + id + " Task_ID:" + taskId + " has finished!")
                  parent ! new GhostResponse(GhostResponseTypes.SUCCESS, requestSeq, bundle)
                }
                case GhostResponseTypes.FAIL => {
                  log.info("[HEAD ACTOR] APP_ID:" + id + " Task_ID:" + taskId + " has failed!")
                  val bundle = new Bundle()
                  bundle.putData(BundleKeys.MESSAGE, "Task execution failure!!!")
                  parent ! new GhostResponse(GhostResponseTypes.FAIL, requestSeq, bundle)
                }
              }
            }
            case Failure(t) => {
              val bundle = new Bundle()
              bundle.putData(BundleKeys.MESSAGE, "[HEAD ACTOR] Task execution failed! Waiting error!")
              parent ! new GhostResponse(GhostResponseTypes.FAIL, requestSeq, bundle)
            }
          }
        }

        case GhostRequestTypes.HEALTH => {

        }

        case GhostRequestTypes.SHUTDOWN => {

        }

        case _ => {
          sender ! new GhostResponse(GhostResponseTypes.UNKNOWN, "", null)
        }
      }
    }
    case response: GhostResponse => {

    }

    case _ => {
      log.info(id + "[HEAD ACTOR] Unhandled message!")
    }
  }
}
