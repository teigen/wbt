package wbt

import sbt.{DefaultWebProject, ProjectInfo}

class WbtProject(info:ProjectInfo) extends DefaultWebProject(info){
  override def unmanagedClasspath = super.unmanagedClasspath +++ info.sbtClasspath
  val wbt_api = "wbt" % "api" % "0.1"
}