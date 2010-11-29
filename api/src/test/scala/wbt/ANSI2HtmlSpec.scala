package wbt

import org.specs.Specification
import Console._
import io.Source
import xml.{Node, NodeSeq, Text}

class ANSI2HtmlSpec extends Specification {
  "ansi2html" should {
    object ANSI2Html extends ANSI2Html

    "convert ansi codes to html" in {
      ANSI2Html(ANSI2Html.RESET, "Hello " + YELLOW + "World" + GREEN + "!")._2 must_==
        List(Text("Hello "), <span class="yellow">World</span>, <span class="green">!</span>)
    }

    "yield empty nodeseq for empty string" in {
      ANSI2Html(ANSI2Html.RESET, "")._2 must_== Nil
    }

    "Packaging complete" in {
      ANSI2Html(ANSI2Html.RESET, "Packaging complete.")._2 must_== List(Text("Packaging complete."))
    }
  }
}