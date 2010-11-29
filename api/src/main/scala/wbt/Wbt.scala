package wbt

import concurrent.Channel
import sbt.Project
import sbt.processor.{ProcessorResult, Success, Reload}

object Wbt {
  private [wbt] val _init = new Channel[(String, Project, Option[String], String)]
  private [wbt] val _result = new Channel[ProcessorResult]

  lazy val (label, project, onFail, args) = _init.read
  def result(r:ProcessorResult) = _result.write(r)

  def quit = result(new Success(project, onFail))
  def reload = result(new Reload(label))
}