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
import akka.actor.{Props, Actor}
import akka.event.Logging
import jp.ac.keio.sfc.ht.memsys.ghost.cache.RemoteCacheContainer
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.data.OffloadableData
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.{GhostResponseTypes, GhostRequestTypes}
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests._
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.tasks.OffloadableTask
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.util.Util
import jp.ac.keio.sfc.ht.memsys.ghost.types.StatusTypes
import org.infinispan.manager.CacheContainer
import org.infinispan.client.hotrod.RemoteCache

/**
 * MemberActor
 * Created on 11/30/14.
 */
object MemberActor {
  def props(id: String): Props = Props(new MemberActor(id))

}

class MemberActor(AppId :String) extends Actor {
  val log = Logging(context.system, this)

  val ID = AppId
  val Status :StatusTypes = StatusTypes.STANDBY

  /*
   * Remote Cache
   */
  val cacheContainer = RemoteCacheContainer.getInstance()
  val mDataCache :RemoteCache[String, OffloadableData] = cacheContainer.getCache[String, OffloadableData](CacheKeys.DATA_CACHE)
  val mTaskCache :RemoteCache[String, OffloadableTask] = cacheContainer.getCache[String, OffloadableTask](CacheKeys.TASK_CACHE)
  val mResultCache :RemoteCache[String, OffloadableData] = cacheContainer.getCache[String, OffloadableData](CacheKeys.RESULT_CACHE)

  var currentTaskId :String = ""
  var currentTask :OffloadableTask = null

  override def receive: Receive = {
    case request :GhostRequest => {
      request.TYPE match {
        case GhostRequestTypes.EXECUTE => {
          val head = sender

          log.info("[MEMBER ACTOR] Received execute request")
          val bundle :Bundle = request.PARAMS

          val taskId = bundle.getData(BundleKeys.TASK_ID)
          val seq = bundle.getData(BundleKeys.DATA_SEQ)

          log.info("[MEMBER ACTOR] TaskID:" + taskId + " Seq:" seq)

          if (taskId != currentTaskId) {
            currentTask = mTaskCache.get(taskId)
            currentTaskId = taskId
          }

          val data :OffloadableData = mDataCache.get(Util.dataPathBuilder(currentTaskId, seq))

          if (data == null) {
            log.info("[MEMBER ACTOR] Error! Data is null!")
            head ! new GhostResponse(GhostResponseTypes.FAIL, currentTaskId, null)
          }

          val result :OffloadableData = currentTask.run(data)
          mResultCache.put(Util.dataPathBuilder(currentTaskId, seq), null)

          val resultBundle :Bundle = new Bundle()
          bundle.putData(BundleKeys.TASK_ID, currentTaskId)
          bundle.putData(BundleKeys.DATA_SEQ, seq)

          //TODO error handling

          head ! new GhostResponse(GhostResponseTypes.SUCCESS, currentTaskId, resultBundle)
        }
      }
    }
    case _ => {

    }
  }
}
