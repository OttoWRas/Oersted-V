package core

import org.scalatest._
import chiseltest._
import chisel3._

class Memorytest extends FlatSpec with ChiselScalatestTester with Matchers {
  "Memory Write & Read test 1" should "pass" in {
    test(new Memory()) { m=>
    
    for (w <- 0 to 100) {
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

"Memory Write & Read test 2" should "pass" in {
    test(new Memory()) { m=>
    
    for (w <- 0 to 100) {
        val r = new scala.util.Random
        val a = r.nextInt(1000)
        val b = r.nextInt(100000)
        val c = r.nextInt(2000)
        val d = r.nextInt(100000)

        val e = r.nextInt(1)

        m.io.wrAddr.poke(a.U)
        m.io.wrData.poke(b.U)
        m.io.wrEnable.poke(true.B)
        m.clock.step(10)
        m.io.wrAddr.poke(c.U)
        m.io.wrData.poke(d.U)
        m.clock.step(10)
        m.io.wrEnable.poke(false.B)
        
        if (e == 1) {
            m.io.rdAddr.poke(a.U)
            m.clock.step(10)
            m.io.rdData.expect(b.U)
        } else {
            m.io.rdAddr.poke(c.U)
            m.clock.step(10)
            m.io.rdData.expect(d.U)
        }
      }
    }
  }

  "Memory deadBeef test" should "pass" in {
    test(new Memory("mem1.hex.txt")) { m=>

    val a = "deadbeef"
    print("Beeftest: ")

    for (w <- 0 to 7) {
        m.io.rdAddr.poke(w.U)
        m.clock.step(10)
        m.io.rdData.expect(Integer.parseInt(a.slice(w,w+1), 16).U)
      }
    println(" ")
    }
  }
}