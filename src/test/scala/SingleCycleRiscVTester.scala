package core

import chisel3.iotesters._
import org.scalatest._
import chiseltest._
import chisel3._

class RiscVSpec extends FlatSpec with ChiselScalatestTester with Matchers {
  "MAIN tester" should "pass" in {
    test(new SingleCycleRiscV("./testData/instructions.hex.txt")) { m=>
    for (w <- 0 to 3) {
        m.clock.step(1)
       var pc = m.io.pcDebug.peek().litValue()
       var ins =  m.io.instrDebug.peek().litValue()
       var op = m.io.opcodeDebug.peek().litValue()
       var rd = m.io.rdDebug.peek().litValue()
       var funct3 = m.io.funct3Debug.peek().litValue()
       var funct7 = m.io.funct7Debug.peek().litValue()
       var rs1 = m.io.rs1Debug.peek().litValue()
       var rs2 = m.io.rs2Debug.peek().litValue()
       var imm = m.io.immDebug.peek().litValue()


       // print(f"rdAddr: $pc%x => ")
       // print(f"rdData: $ins%8x\n \n\n")
        println()
        print(f"opcode = $op\nrd = $rd\nfunct3 = $funct3\nrs1 = $rs1\nrs2 = $rs2\nfunct7 = $funct7\nimm = $imm\n\n")
        /*
        */
       // print(f"decoder: " + m.io.decDebug.peek())
 for(i <- 0 until 3){
          var v = m.io.regDebug(i).peek().litValue() // peek(dut.io.regDebug(i))
          print(f"x$i%-2d ")
           print(f"$v \n")
          //print(f"$v%08x \n")
        }
      }
     
    //  for (i <- 0 until 31) {
    //   if(i != 0 && i % 8 == 0) { printf("\n") } 

    //   print(f"x$i%-2d ")
    //   val v = m.io.regDebug(2).peek().litValue // peek(dut.io.regDebug(i))
    //   print(f"$v%08x ")
    // }

    println(" ")
    }
  }
}

/*
   0x00200093, // addi x1 x0 2
    0x00300113, // addi x2 x0 3
    0x002081b3) // add x3 x1 x2
*/
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


