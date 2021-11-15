package core

import org.scalatest._
import chiseltest._
import chisel3._

class Memorytest extends FlatSpec with ChiselScalatestTester with Matchers {
  "Memory Write & Read test" should "pass" in {
    test(new Memory) { m=>

        val r = new scala.util.Random
        val a = r.nextInt(1000)
        val b = r.nextInt(100000)

        m.io.wrAddr.poke(a.U)
        m.io.wrData.poke(b.U)
        m.io.wrEnable.poke(true.B)
        m.clock.step(10)
        m.io.wrEnable.poke(false.B)
        m.io.rdAddr.poke(a.U)
        m.clock.step(10)
        m.io.rdData.expect(b.U)
    }
  }
}