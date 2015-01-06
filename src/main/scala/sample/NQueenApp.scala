/*
 * Copyright (c) 2014. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

package sample

import akka.util.Timeout
import jp.ac.keio.sfc.ht.memsys.ghost.actor.Gateway
import jp.ac.keio.sfc.ht.memsys.ghost.cache.RemoteCacheContainer
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.data.OffloadableData
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.GhostRequestTypes
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests._
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.tasks.OffloadableTask
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.util.Util
import jp.ac.keio.sfc.ht.memsys.ghost.nqueen.{NQueenUtil, NQueenTaskImpl, NQueenTaskKeys}
import org.infinispan.client.hotrod.RemoteCache

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
 * NQueenApp
 * Created on 11/30/14.
 *
 * Demonstration of NQueen (All is done LOCALLY)
 */
class NQueenApp(_gateway: Gateway, app_name: String) {
  val gateway = _gateway

  val cacheContainer = RemoteCacheContainer.getInstance()
  val mDataCache :RemoteCache[String, OffloadableData] = cacheContainer.getCache[String, OffloadableData](CacheKeys.DATA_CACHE)
  val mTaskCache :RemoteCache[String, OffloadableTask] = cacheContainer.getCache[String, OffloadableTask](CacheKeys.TASK_CACHE)
  val mResultCache :RemoteCache[String, OffloadableData] = cacheContainer.getCache[String, OffloadableData](CacheKeys.RESULT_CACHE)

  val APP_NAME = app_name
  val TASK_NAME = "nqueen_task"

  def runApp(num :Int): Unit = {

    println("Start App")

    /*
     * 1. Register Application
     * Gateway returns APP_ID when registered by name
     */
    val APP_ID = gateway.registerApplication(APP_NAME)
    println("[App] APP ID: " + APP_ID)

    /*
     * 2. Register task in cache
     */
    val TASK_ID = Util.taskPathBuilder(APP_ID, TASK_NAME)
    mTaskCache.put(TASK_ID, new NQueenTaskImpl())

    /*
     * 3. Register task in gateway
     */
    val bundle: Bundle = new Bundle()
    bundle.putData(BundleKeys.APP_ID, APP_ID)
    bundle.putData(BundleKeys.TASK_ID, TASK_ID)

    val request: GhostRequest = new GhostRequest(GhostRequestTypes.REGISTERTASK, bundle)
    val futureTask: Future[Any] = gateway.registerTask(request)

    implicit val timeout = Timeout(10 seconds)
    // waiting for task to be registered...
    val result = Await.result(futureTask, timeout.duration).asInstanceOf[GhostResponse]
    println("[App] Register task done")

    /*
     * 4. Execute Heap Sort Task 1000 times
     */
//    for (i <- 0 until 1000) {
//      var seq: String = i.toString()
      var seq: String = "0"

      // Offload data
      println("[App] Offload data")
      val data: OffloadableData = NQueenUtil.genData(TASK_ID, seq, num)
      data.putData(NQueenTaskKeys.DEBUG, null)

      val path: String = Util.dataPathBuilder(TASK_ID, seq)
      mDataCache.put(path, data)

      println("[App] Generated data path akka://" + path)

      val eBundle: Bundle = new Bundle()
      eBundle.putData(BundleKeys.APP_ID, APP_ID)
      eBundle.putData(BundleKeys.TASK_ID, TASK_ID)
      eBundle.putData(BundleKeys.DATA_SEQ, seq)

      val eRequest: GhostRequest = new GhostRequest(GhostRequestTypes.EXECUTE, eBundle)

      val res :Future[Any] = gateway.executeTask(eRequest)
      implicit val timeout2 = Timeout(30 seconds)
      val result2 = Await.result(res, timeout2.duration).asInstanceOf[GhostResponse]
      val resultpath = Util.dataPathBuilder(TASK_ID, seq)
      val offloadableData: OffloadableData = mResultCache.get(resultpath)
      println(offloadableData.getData("result_data"))

//    }

  }

}

