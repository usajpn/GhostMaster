/*
 * Copyright (c) 2014. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

package jp.ac.keio.sfc.ht.memsys.ghost.types

/**
 * StatusTypes
 * Created on 11/30/14.
 */
object StatusTypes {
  case object STANDBY extends StatusTypes(0)
  case object RUNNING extends StatusTypes(1)

  val values = Array(STANDBY, RUNNING)

}

sealed abstract class StatusTypes(val code:Int) {
  val name = toString
}
