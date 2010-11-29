package wbt

import xml.NodeSeq
import net.liftweb.http.{ListenerManager, CometActor, LiftSession}
import net.liftweb.common.{SimpleActor, Box}

trait InitCometActor extends CometActor {
  override def initCometActor(theSession: LiftSession,
                               theType: Box[String],
                               name: Box[String],
                               defaultXml: NodeSeq,
                               attributes: Map[String, String]) {
    super.initCometActor(theSession, theType, name, defaultXml, attributes)
  }
}

trait EnhancedListenerManager extends ListenerManager {
  self: SimpleActor[Any] =>

  private var tmp:Option[Any] = None
  protected final def createUpdate:Any = tmp.getOrElse(defaultMessage)
  protected def defaultMessage: Any

  protected def updateListeners(message:Any){
    tmp = Some(message)
    updateListeners()
    tmp = None
  }
}