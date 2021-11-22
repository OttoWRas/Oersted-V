package core

import org.scalatest._
import chiseltest._
import chisel3._

class RegisterTest extends FlatSpec with ChiselScalatestTester with Matchers {
  "Register read/write test" should "pass" in {
    test(new Registers()) { m=>
    
   for (w <- 0 to 1000) {
        val r = new scala.util.Random
        val addr = r.nextInt(32)
        val data = r.nextInt(2^32-1)
        
        m.io.wrEnable.poke(true.B)
        m.io.wrAddr.poke(addr.U)
        m.io.rdAddr1.poke(addr.U)
        m.io.rdAddr2.poke(addr.U)
        m.io.wrData.poke(data.U)
        m.clock.step(5)
        m.io.wrEnable.poke(false.B)
        m.clock.step(5)
        m.io.rdData1.expect(data.U)
        m.io.rdData2.expect(data.U)
        

      }
    }
  }
}