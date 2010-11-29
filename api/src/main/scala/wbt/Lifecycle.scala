package wbt

import net.liftweb.http.provider.HTTPResponse
import net.liftweb.http.{BasicResponse, Req}
import net.liftweb.common.Box

/*
 * This little gem only exists to delay the shutdown/reload to after a response
 * has been rendered. If shutdown/reload is executed right away, a response is
 * never sent and the browser will typically pop up an annoying little box saying
 * that it never got a response.
 */
object Lifecycle {

  // default noop impl.
  var hook:() => Unit = () => ()

  val afterSend:(BasicResponse, HTTPResponse, List[(String, String)], Box[Req]) => Any = (_, _, _, _) => hook()
}