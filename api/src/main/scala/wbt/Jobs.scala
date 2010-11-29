package wbt

import net.liftweb.util.Helpers
import net.liftweb.actor.LiftActor
import concurrent.ops._
import collection.mutable._
import net.liftweb.http._
import js.jquery.JqJE.{JqRemove, JqId}
import js.jquery.JqJsCmds.{FadeOut, FadeIn, AppendHtml, Hide}
import js.JsCmds._
import js.JE._
import xml.{Text}
import net.liftweb.common.{Full}
case class Job(description:JobDescription, f:() => Option[String])
case class JobDescription(id:String, name:String)

sealed trait JobStatus
case class JobQueue(queue:List[JobDescription]) extends JobStatus
case class JobStarting(description:JobDescription) extends JobStatus
case class JobDone(description:JobDescription, result:Option[String]) extends JobStatus

sealed trait JobControl
case class AddJob(job:Job) extends JobControl

object Job {
  def apply(name:String, f:() => Option[String]):Job = Job(JobDescription(Helpers.nextFuncName, name), f)
}

object Jobs extends LiftActor with EnhancedListenerManager {

  private val queue = new Queue[Job]
  private var running = false
  protected def defaultMessage = JobQueue(queue.toList.map(_.description))

  override def lowPriority = {

    case AddJob(job) =>
      queue.enqueue(job)
      updateListeners()
      if(!running)
        runJob

    case Done(job, result) =>
      running = false
      updateListeners(JobDone(job.description, result))
      if(!queue.isEmpty)
        runJob
  }

  private def runJob {
    running = true
    val job = queue.dequeue
    updateListeners()
    updateListeners(JobStarting(job.description))
    spawn {
      Jobs ! Done(job, job.f.apply)
    }
  }

  private case class Done(job:Job, result:Option[String])
}

object JobQueueComet {
   val cometCreation: LiftRules.CometCreationPF = {
    case CometCreationInfo(theType@"JobQueue", name, defaultXml, attributes, session) =>
      val comet = new JobQueueComet
      comet.initCometActor(session, Full(theType), name, defaultXml, attributes)
      comet
  }
}

class JobQueueComet extends InitCometActor with CometListener {
  private var jobs:List[JobDescription] = Nil

  protected def registerWith = Jobs

  def render = jobs.flatMap{ renderJob }

  def renderJob(description:JobDescription) =
          bind("job", defaultXml,
            "name" -> description.name,
            AttrBindParam("id", Text(description.id), "id"))

  import Helpers.TimeSpan

  override def lowPriority = {
    case JobQueue(queue) =>
      val add = (queue -- jobs).foldLeft(Noop){(a, e) => a & AppendHtml(uniqueId, renderJob(e)) & Hide(e.id) & FadeIn(e.id, TimeSpan(0), TimeSpan(500))}
      val remove = (jobs -- queue).foldLeft(Noop){(a, e) => a & FadeOut(e.id, TimeSpan(0), TimeSpan(500)) & After(TimeSpan(500), JqId(e.id) ~> JqRemove())}
      jobs = queue
      partialUpdate(add & remove)
    case _ =>
  }
}

