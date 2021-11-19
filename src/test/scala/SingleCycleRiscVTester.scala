package core

import chisel3.iotesters._
import org.scalatest._


class RiscVSpec extends FlatSpec with Matchers {
  "RiscV test" should "pass" in {
    test(new SingleCycleRiscV).withAnnotations(Seq(WriteVcdAnnotation)) { c=>
        c.io.fetch.poke(true.B)
        c.clock.step(20)
        c.io.instOut.expect(4369.U)
        c.io.fetch.poke(false.B)
        c.io.pcPlus.poke(true.B)
        c.clock.step(1)
        c.io.pcPlus.poke(false.B)
        c.io.fetch.poke(true.B)
        c.clock.step(5)
        c.io.fetch.poke(false.B)
        c.clock.step(5)
    }
  }
}