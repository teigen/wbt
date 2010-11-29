package wbt

import net.liftweb.common._
import net.liftweb.actor.LiftActor
import net.liftweb.http._
import js.JE._
import js.jquery.JqJE.{JqRemove, JqId, JqScrollToBottom}
import js.jquery.JqJsCmds.{JqOnLoad, AppendHtml}
import js.JsCmds._
import collection.mutable.ListBuffer
import xml.{NodeSeq}
import net.liftweb.util.{ActorPing, Helpers}

case class AddElement(elem:NodeSeq)
case class NewTask(id:String)
case class CurrentTask(id:String, elements:NodeSeq)

object ProjectOutputComet {
  val cometCreation: LiftRules.CometCreationPF = {
    case CometCreationInfo(theType@"ProjectOutput", name, defaultXml, attributes, session) =>
      val comet = new ProjectOutputComet
      comet.initCometActor(session, Full(theType), name, defaultXml, attributes)
      comet
  }
}

class ProjectOutputComet extends InitCometActor with CometListener {

  private val outputId = Helpers.nextFuncName
  private val content = new ListBuffer[NodeSeq]
  private var taskId = Helpers.nextFuncName

  def registerWith = ProjectOutput

  def render = {
      <div class="output" id={outputId}>
        <head>
          {Script(JqOnLoad(JqId(outputId) ~> JqScrollToBottom))}
        </head>
        <span id={taskId}>
          {content.flatMap(x => x)}
        </span>
      </div>
  }

  override def lowPriority = {
    case AddElement(element) =>
      content.append(element)
      partialUpdate(AppendHtml(taskId, element) & JqId(outputId) ~> JqScrollToBottom)

    case CurrentTask(id, elements) =>
      taskId = id
      content.clear
      content.append(elements)

    case NewTask(id) =>
      val removeOld = JqId(taskId) ~> JqRemove()
      taskId = id
      content.clear
      val addNew = AppendHtml(outputId, <span id={taskId}/>)
      partialUpdate(removeOld & addNew)

    case _ =>
  }
}

object ProjectOutput extends LiftActor with EnhancedListenerManager {
  private var unrendered = ""
  private val rendered = new ListBuffer[NodeSeq]
  private var currentId:String = _
  private val interval = Helpers.longToTimeSpan(500) // BUG!

  Jobs ! AddAListener(this, { case _ => true })

  ActorPing.schedule(this, Push, interval)

  object ANSI2Html extends ANSI2Html {

    private var current = RESET

    def next(s: String):Option[NodeSeq] = {
      val (c, r) = ANSI2Html.apply(current, s)
      current = c
      if(r.isEmpty) None else Some(r)
    }
  }

  override def lowPriority = {
    case message: String =>
      unrendered += message

    case JobStarting(JobDescription(id, _)) =>
      push
      currentId = id + "_output"
      rendered.clear()
      updateListeners(NewTask(currentId))

    case Push =>
      push
      ActorPing.schedule(this, Push, interval)

    case _ =>                          
  }

  def push  = ANSI2Html.next(unrendered).foreach {
    element =>
      updateListeners(AddElement(element))
      rendered.append(element)
      unrendered = ""
  }

  def defaultMessage = CurrentTask(currentId, rendered.flatMap(x => x))
  private case object Push
}

object ProjectOutputStream {
  import java.io.{PrintStream, OutputStream, ByteArrayOutputStream}
  private val out = System.out
  private val captured = new PrintStream(new OutputStream {

    val bytes = new ByteArrayOutputStream

    def push{
      ProjectOutput ! bytes.toString
      bytes.reset
    }

    override def flush = {
      bytes.flush
      out.flush
      push
    }
    override def write(b: Array[Byte]) = {
      bytes.write(b)
      out.write(b)
      push
    }
    override def write(b: Array[Byte], off: Int, len: Int) = {
      bytes.write(b, off, len)
      out.write(b, off, len)
      push
    }
    def write(b: Int) = {
      bytes.write(b)
      out.write(b)
      push
    }
  })

  def capture {
    System.setOut(captured)
  }

  def release {
    System.setOut(out)
  }
}