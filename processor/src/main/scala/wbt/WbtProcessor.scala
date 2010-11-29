package wbt

import sbt._
import processor.{ProcessorResult, Processor}
import org.mortbay.jetty.Server
import org.mortbay.jetty.webapp.WebAppContext
import org.mortbay.resource.{Resource, ResourceCollection}
import org.mortbay.jetty.nio.SelectChannelConnector
import collection.mutable.ArrayBuffer
import java.net.URLClassLoader

class WbtProcessor extends Processor {
  def apply(label: String, project: Project, onFailure: Option[String], args: String): ProcessorResult = {

    val wbtProject = {
      def wbt(project: Project): Option[WbtProject] = project match {
        case p: WbtProject => Some(p)
        case _ => project.subProjects.values.toList.flatMap(wbt(_).toList).firstOption
      }
      wbt(project)
    }

    Wbt._init.write((label, project, onFailure, args))

    val wbtConfig = wbtProject.flatMap(_.jettyConfiguration match {
      case d: DefaultJettyConfiguration => Some(d)
      case _ => None
    })   

    val thisJar = classOf[WbtProcessor].getProtectionDomain.getCodeSource.getLocation
    val server = new Server
    val scc = new SelectChannelConnector
    scc.setPort(9999)
    server.setConnectors(Array(scc))
    val webapp = new WebAppContext
    webapp.setContextPath("/")

    val resources = new ArrayBuffer[Resource]

    wbtConfig.foreach{ config:DefaultJettyConfiguration =>
      resources += Resource.newResource(config.war.asURL)
      webapp.setClassLoader(new URLClassLoader(config.classpath.getURLs))
    }

    resources += Resource.newResource("jar:" + thisJar + "!/")

    webapp.setBaseResource(new ResourceCollection(resources.toArray))



    webapp.setServer(server)
    server.addHandler(webapp)


    try {
      server.start
      Wbt._result.read
    } finally {
      server.stop
      server.join
    }
  }
}