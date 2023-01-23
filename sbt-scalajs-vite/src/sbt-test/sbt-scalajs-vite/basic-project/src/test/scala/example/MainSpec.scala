package example

import example.facade.LoremIpsum
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MainSpec extends AnyFreeSpec with Matchers {

  "Test" in {
    println("test print")
    println(LoremIpsum.loremIpsum())
    LoremIpsum.loremIpsum() should not be empty
    Main.printStuff()
  }
}
