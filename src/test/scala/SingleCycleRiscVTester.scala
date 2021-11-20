package core

import chisel3.iotesters._
import org.scalatest._
import chiseltest._
import chisel3._



class RiscVTester(dut: SingleCycleRiscV) extends PeekPokeTester(dut) {
  def dump() = {
    for (i <- 0 until 31) {
        if(i != 0 && i % 8 == 0){
      printf("\n")
    }

      print(f"x$i%-2d ")
    
      val v = peek(dut.io.regDebug(i))
 
      print(f"$v%08x ")
    }
  }


  for (i <- 0 until 16) { // print until length of program
    val pc = peek(dut.io.pcDebug)
    val instruction = peek(dut.io.instrDebug)
    //val v = peek(dut.io.regDebug(6))
    print(f"PC: $pc%x \n")
    print(f"instr: $instruction")
    println()
    println()
    step(1)
  }

}

class RiscVSpec extends FlatSpec with Matchers {
  "RiscV main tester" should "pass" in {
    chisel3.iotesters.Driver(() => new SingleCycleRiscV()) { c => new RiscVTester(c)} should be (true)
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