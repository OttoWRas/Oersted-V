import chisel3.iotesters._
import org.scalatest._

import consts._

class RiscVSpec extends FlatSpec with Matchers {
  "RiscV" should "pass" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () => new SingleCycleRiscV()) { c =>
      new PeekPokeTester(c) {
        def dump() = {
          var str: String = "";
          for (i <- 0 until 32) {
             if(i != 0 && i % 8 == 0){
            printf("\n")
          }
          
            print(f"x$i%-2d ")
            //println(f"$name%s is $height%2.2f meters tall")
            val v = peek(dut.io.regDeb(i))
            //printf(p"${Hexadecimal(v)}")
            print(f"$v%08x ")

            //
          
        }
        }
  
         for (i <- 0 until 4) { // print until length of program
         
          val pc = peek(dut.io.pc)
          print(f"PC: $pc%x")
          println()
          dump()
          printf("\n")
          printf("\n\n\n\n\n")
          step(1)
        }
        
      }
    }
  }
}