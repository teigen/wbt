package wbt

import net.liftweb.sitemap.Loc.Link
import net.liftweb.sitemap.{Menu, Loc, ConvertableToMenu}
import Loc.Template
import net.liftweb.http.TemplateFinder
import sbt.Project

object ProjectMenu extends ConvertableToMenu {
  lazy val toMenu = menu(List("project"), Wbt.project)

  def menu(path: List[String], project: Project): Menu = {
    val template = Template(() => {
      ProjectSnippet.Current.set(project)
      TemplateFinder.findAnyTemplate(List("templates-hidden", "project")) openOr <div>Not found</div>
    })
    val newPath = project.name :: path
    Menu(Loc(project.name, new Link(newPath.reverse), project.name, template), project.subProjects.values.toList.map(menu(newPath, _)): _*)
  }
}