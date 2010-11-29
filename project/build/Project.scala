import sbt._

class Project(info: ProjectInfo) extends ParentProject(info) {

  lazy val processor = project("processor", "processor", new ProcessorProject(_){
    val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"
    val jetty = "org.mortbay.jetty" % "jetty" % "6.1.22"
    override def mainResources = super.mainResources +++ descendents(mainSourcePath / "webapp" ##, "*")
  }, api, wbt_project)

  lazy val api = project("api", "api", new DefaultProject(_) {
    val lift_webkit = "net.liftweb" %% "lift-webkit" % "2.2-M1"
    val lift_widget = "net.liftweb" %% "lift-widgets" % "2.2-M1"
    val specs = "org.scala-tools.testing" % "specs" % "1.6.2.2" % "test"
    override def unmanagedClasspath = super.unmanagedClasspath +++ info.sbtClasspath
  })

  lazy val wbt_project = project("wbt-project", "project", new DefaultProject(_){
    override def unmanagedClasspath = super.unmanagedClasspath +++ info.sbtClasspath
  })
}
