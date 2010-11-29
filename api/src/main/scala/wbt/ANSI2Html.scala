package wbt

import xml.{Node, Text}

trait ANSI2Html {
  private val ESCAPE = "\033"
  private val CODE_END = "m"
  private val NL = System.getProperty("line.separator")

  private def span(clazz: String) = (content: String) => <span class={clazz}>{content}</span>
  private def span_b(clazz: String) = (content: String) => <span class={clazz}><b>{content}</b></span>

  val BLACK = span("black")
  val BLACK_B = span_b("black")
  val BLINK = (s:String) => <blink>{s}</blink>
  val BLUE = span("blue")
  val BLUE_B = span_b("blue")
  val BOLD = ((s:String) => <b>{s}</b>)
  val CYAN = span("cyan")
  val CYAN_B = span_b("cyan")
  val GREEN = span("green")
  val GREEN_B = span_b("green")
  val INVISIBLE = span("invisible")
  val MAGENTA = span("magenta")
  val MAGENTA_B = span_b("magenta")
  val RED = span("red")
  val RED_B = span_b("red")
  val RESET:String => Node = Text(_:String)
  val UNDERLINED = (s:String) => <u>{s}</u>
  val WHITE = span("white")
  val WHITE_B = span_b("white")
  val YELLOW = span("yellow")
  val YELLOW_B = span_b("yellow")

  val NEWLINE = <br/>

  val Rules = List[(String,String => Node)](
    Console.BLACK -> BLACK,
    Console.BLACK_B -> BLACK_B,
    Console.BLINK -> BLINK,
    Console.BLUE -> BLUE,
    Console.BLUE_B -> BLUE_B,
    Console.BOLD -> BOLD,
    Console.CYAN -> CYAN,
    Console.CYAN_B -> CYAN_B,
    Console.GREEN -> GREEN,
    Console.GREEN_B -> GREEN_B,
    Console.INVISIBLE -> INVISIBLE,
    Console.MAGENTA -> MAGENTA,
    Console.MAGENTA_B -> MAGENTA_B,
    Console.RED -> RED,
    Console.RED_B -> RED_B,
    Console.RESET -> RESET,
    Console.UNDERLINED -> UNDERLINED,
    Console.WHITE -> WHITE,
    Console.WHITE_B -> WHITE_B,
    Console.YELLOW -> YELLOW,
    Console.YELLOW_B -> YELLOW_B)

  def apply(current:String => Node, ansi:String):(String => Node, List[Node]) = {

    // TODO, clean up fugly parsing!

    def newline(cur:String => Node, text:String):List[Node] = {
      if(text == "")
        Nil
      else {
        val index = text.indexOf(NL)
        if(index == -1)
          List(cur(text))
        else if(index == 0)
          NEWLINE :: newline(cur, text.substring(index + NL.length))
        else
          cur(text.substring(0, index)) :: NEWLINE :: newline(cur, text.substring(index + NL.length))
      }
    }

    val index = ansi.indexOf(ESCAPE)
    if(index == -1)
      current -> newline(current, ansi)
    else {
      val rest = ansi.substring(index)
      val first = newline(current, ansi.substring(0, index))
      Rules.find{ rest startsWith _._1 } match {
        case Some((esc, f)) =>
          val (cur, list) = apply(f, rest.substring(esc.length))
          cur -> (first ::: list)
        case _ => current -> (first ::: newline(current, rest))
      }
    }
  }
}