package bootstrap.liftweb

import net.liftweb._
import sitemap.{SiteMap, Menu}
import widgets.autocomplete.AutoComplete
import http.{ResourceServer, LiftRules}
import http.js.JsCmds.Noop
import wbt._

class Boot {

  implicit def unit2Noop(unit:Unit) = Noop

  def boot {

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    LiftRules.setSiteMap(SiteMap(
      Menu("Home") / "index",
      Menu("Lifecycle") / "lifecycle",
      ProjectMenu))

    LiftRules.afterSend.prepend(Lifecycle.afterSend)

    LiftRules.snippetDispatch.prepend(Map(
      "project" -> ProjectSnippet
      ))

    LiftRules.cometCreation.prepend(ProjectOutputComet.cometCreation)
    LiftRules.cometCreation.prepend(JobQueueComet.cometCreation)

    ProjectOutputStream.capture
    LiftRules.unloadHooks.append(ProjectOutputStream.release _)

    ResourceServer.allow(ProjectResources.allow)

    AutoComplete.init
  }
}