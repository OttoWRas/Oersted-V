package core

import org.scalatest._
import chiseltest._
import chisel3._
import ALU._

class ALUtest extends FlatSpec with ChiselScalatestTester with Matchers {
  "ALU ADD & SUB test" should "pass" in {
    test(new ALU) { c=>

      for (w <- 0 to 1000) {
        val r = new scala.util.Random
        val a = r.nextInt(100000)
        val b = r.nextInt(100000)

        c.io.opcode.poke(ALU_ADD)
        c.io.data1.poke(a.U)
        c.io.data2.poke(b.U)
        c.clock.step(10)
        c.io.out.expect((a+b).U)
        c.io.opcode.poke(ALU_SUB)
        c.clock.step(10)
        if (a<b) {
          c.io.out.expect((4294967296l-((a-b).abs)).U)
        } else {
          c.io.out.expect((a-b).U)
        }
      }
    }
  }

  "ALU COPY test" should "pass" in {
    test(new ALU) { c=>

      for (w <- 0 to 1000) {
        val r = new scala.util.Random
        val a = r.nextInt(100000)
        val b = r.nextInt(100000)

        c.io.opcode.poke(ALU_COPY1)
        c.io.data1.poke(a.U)
        c.io.data2.poke(b.U)
        c.clock.step(10)
        c.io.out.expect(a.U)
        c.io.opcode.poke(ALU_COPY2)
        c.clock.step(10)
        c.io.out.expect(b.U)
      }
    }
  }

  "ALU SHIFT test" should "pass" in {
    test(new ALU) { c=>

      for (w <- 0 to 1000) {
        val r = new scala.util.Random
        val a = r.nextInt(100000)
        val b = r.nextInt(16)

        info("Tested numbers = " + a.toString + "," + b.toString)

        c.io.opcode.poke(ALU_SLL)
        c.io.data1.poke(a.U)
        c.io.data2.poke(b.U)
        c.clock.step(10)
        c.io.out.expect(("b" + (a << b).toBinaryString).U)
        c.io.opcode.poke(ALU_SRL)
        c.clock.step(15)
        c.io.out.expect(("b" + (a >> b).toBinaryString).U)
        c.io.opcode.poke(ALU_SRA)
        c.clock.step(10)
        c.io.out.expect(("b" + (a >>> b).toBinaryString).U)
      }
    }
  }

  "ALU LOGIC test" should "pass" in {
    test(new ALU) { c=>
      for (w <- 0 to 1000) {
        val r = new scala.util.Random
        val a = r.nextInt(1000000)
        val b = r.nextInt(1000000)

        c.io.opcode.poke(ALU_AND)
        c.io.data1.poke(a.U)
        c.io.data2.poke(b.U)
        c.io.out.expect((a & b).U)
        c.clock.step(10)
        c.io.opcode.poke(ALU_OR)
        c.clock.step(10)
        c.io.out.expect((a|b).U)
        c.io.opcode.poke(ALU_XOR)
        c.clock.step(10)
        c.io.out.expect((a ^ b).U)
      }
    }
  }
}