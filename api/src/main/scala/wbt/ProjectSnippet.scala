package wbt

import sbt.Project
import xml.{NodeSeq, Text}
import net.liftweb.util.Helpers._
import net.liftweb.widgets.autocomplete.AutoComplete
import net.liftweb.http._
import js.JsCmds.Noop

object ProjectSnippet extends DispatchSnippet {

  object Current extends RequestVar[Project](Wbt.project)

  implicit def unit2Noop(unit:Unit) = Noop

  val dispatch:DispatchIt = {
    case "name" => _ => Text(Current.is.name)
    case "tasks" => tasks _
    case "output" => output _
    case "lifecycle" => lifecycle _
    case "jobqueue" => jobqueue _
  }

  def filter(t: List[(String, Project#ManagedTask)]): List[(String, Project#ManagedTask)] = t match {
    case (x@(name, _)) :: xs => x :: filter(xs.filter(_._1 != name))
    case Nil => Nil
  }

  def sortedTasks(project:Project) = filter(project.topologicalSort.flatMap(_.tasks)).sort(_._1 < _._1)

  def tasks(xhtml: NodeSeq): NodeSeq = {
    val project = Current.is
    val taskNames = sortedTasks(project).map{ _._1 }
    def options(current: String, limit: Int) = {
      taskNames.filter(_ startsWith current).take(limit)
    }
    def submit(task:String){ Jobs ! AddJob(Job(task, () => project.act(task))) }
    <lift:form>
      {AutoComplete("", options _, submit _, List("max" -> taskNames.size.toString) ,"class" -> "column span-10")}
    </lift:form>
  }

  def output(xhtml:NodeSeq):NodeSeq = <lift:comet type="ProjectOutput"/>

  def lifecycle(xhtml:NodeSeq):NodeSeq =
    bind("lifecycle", xhtml,
      "reload" -> SHtml.a(() => Lifecycle.hook = () => Wbt.reload, Text("Reload")),
      "quit" -> SHtml.a(() => Lifecycle.hook = () => Wbt.quit, Text("Quit")))

  def jobqueue(xhtml:NodeSeq):NodeSeq = <lift:comet type="JobQueue">{xhtml}</lift:comet>
}