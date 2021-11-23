package core

import chisel3.iotesters._
import org.scalatest._
import chiseltest._
import chisel3._

import OP._
import ALU._


class ImmITypeTest (dut: ImmediateGen) extends PeekPokeTester(dut) {
   for (w <- 0 to 1000) {
        val r = new scala.util.Random
    
        val opcode = OP_I.litValue()

        val funct3s     = Array(0, 4, 6, 7, 1, 5, 2, 3) 
        val funct3      = BigInt(funct3s(r.nextInt(8))) << 12 
        val rd          = BigInt(r.nextInt(32) << 7)
        val rs1         = BigInt(r.nextInt(32) << 15)
        var imm         = BigInt(r.nextInt(4096) - 2048) << 20
        var immExpect   = imm >> 20 // in the regular case we expect whatever random number we came up with
        /* but in the case of SLTU (set less than UNsigned) we need to zero-extend our expected number for the test to work. */
        if((funct3 >> 12) == BigInt(3)){
            immExpect = (imm >> 20) & 0x00000FFF // we force all the bits except (12,0) to be set to 0.
        }
        val bitString = imm | rs1 | funct3 | rd | opcode

        poke(dut.io.in, bitString)
        step(1)
        expect(dut.io.out, immExpect)
   }
}

class ImmILTypeTest (dut: ImmediateGen) extends PeekPokeTester(dut) {
   for (w <- 0 to 1000) {
        val r = new scala.util.Random
    
        val opcode = OP_IL.litValue()

        val funct3s     = Array(0, 1, 2, 4, 5) 
        val funct3      = BigInt(funct3s(r.nextInt(5))) << 12 
        val rd          = BigInt(r.nextInt(32) << 7)
        val rs1         = BigInt(r.nextInt(32) << 15)
        var imm         = BigInt(r.nextInt(4096) - 2048) << 20
        var immExpect   = imm >> 20 
        if(((funct3 >> 12) == BigInt(4)) | ((funct3 >> 12) == BigInt(5)) ){
            immExpect = (imm >> 20) & 0x00000FFF // we force all the bits except (12,0) to be set to 0.
        }
        val bitString = imm | rs1 | funct3 | rd | opcode

        poke(dut.io.in, bitString)
        step(1)
        expect(dut.io.out, immExpect)
   }
}

class ImmSTypeTest (dut: ImmediateGen) extends PeekPokeTester(dut) {
   for (w <- 0 to 1000) {
        val r = new scala.util.Random
    
        val opcode = OP_S.litValue()

    val rs1       = BigInt(r.nextInt(32) << 15)
      val rs2       = BigInt(r.nextInt(32) << 20)
      val imm       = BigInt(r.nextInt(4096)-2048)
      val imm4to0   = (BigInt(31) & imm) << 7 // 5 bits
      /* we need to shift the 11to5 >> 5 first, and then << 25 otherwise it will clash with rs2 */
      val imm11to5  = ((BigInt(4094) & imm) >> 5) << 25
      val funct3    = BigInt(r.nextInt(3)) << 12

      val bitString = imm11to5 | rs2 | rs1 | funct3 | imm4to0 | opcode
    


        // var imm         = BigInt(r.nextInt(4096) - 2048) << 20
        // var immExpect   = imm >> 20 
        // if(((funct3 >> 12) == BigInt(4)) | ((funct3 >> 12) == BigInt(5)) ){
        //     immExpect = (imm >> 20) & 0x00000FFF // we force all the bits except (12,0) to be set to 0.
        // }
        // val bitString = imm | rs1 | funct3 | rd | opcode

        poke(dut.io.in, bitString)
        step(1)
        expect(dut.io.out, imm)
   }
}

class ImmSpec extends FlatSpec with Matchers {
  "I regular type immediate generation" should "pass" in {
    chisel3.iotesters.Driver(() => new ImmediateGen()) { c => new ImmITypeTest(c)} should be (true)
  }
   "I load type imm generation" should "pass" in {
    chisel3.iotesters.Driver(() => new ImmediateGen()) { c => new ImmILTypeTest(c)} should be (true)
  }
  "S type imm generation" should "pass" in {
    chisel3.iotesters.Driver(() => new ImmediateGen()) { c => new ImmSTypeTest(c)} should be (true)
  }
}
