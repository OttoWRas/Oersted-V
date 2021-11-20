package core

import chisel3.iotesters._
import org.scalatest._
import chiseltest._
import chisel3._





class RiscVSpec extends FlatSpec with ChiselScalatestTester with Matchers {
  "MAIN tester" should "pass" in {
    test(new SingleCycleRiscV("./testData/instructions.hex.txt")) { m=>

    for (w <- 0 to 20) {
        m.io.rdAddr.poke((4*w).U)
        m.clock.step(1)
        print(f"data: " + m.io.instrDebug.peek())
        println()
        //m.io.rdData.expect(Integer.parseInt(a.slice(w,w+1), 16).U)
      }
    println(" ")
    }
  }
}


// class RiscVSpec extends FlatSpec with Matchers {
//   "RiscV test" should "pass" in {
//     test(new SingleCycleRiscV).withAnnotations(Seq(WriteVcdAnnotation)) { c=>
//         c.io.fetch.poke(true.B)
//         c.clock.step(20)
//         c.io.instOut.expect(4369.U)
//         c.io.fetch.poke(false.B)
//         c.io.pcPlus.poke(true.B)
//         c.clock.step(1)
//         c.io.pcPlus.poke(false.B)
//         c.io.fetch.poke(true.B)
//         c.clock.step(5)
//         c.io.fetch.poke(false.B)
//         c.clock.step(5)
//     }
//   }
// }


  // def dump() = {
  //   for (i <- 0 until 31) {
  //       if(i != 0 && i % 8 == 0){
  //     printf("\n")
  //   }

  //     print(f"x$i%-2d ")
    
  //     val v = peek(dut.io.regDebug(i))
 
  //     print(f"$v%08x ")
  //   }
  // }