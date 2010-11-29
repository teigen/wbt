package wbt

object ProjectResources {
  val allow:PartialFunction[List[String], Boolean] = {
    case "wbt" :: "output.css" :: Nil => true
  }
}