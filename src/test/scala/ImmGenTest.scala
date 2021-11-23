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

    val funct3s     = Array(0,1,2) 
    val funct3      = BigInt(funct3s(r.nextInt(3))) << 12 


    val bitString = imm11to5 | rs2 | rs1 | funct3 | imm4to0 | opcode
    
    poke(dut.io.in, bitString)
    step(1)
    expect(dut.io.out, imm)
   }
}

class ImmBTypeTest (dut: ImmediateGen) extends PeekPokeTester(dut) {
   for (w <- 0 until 10) {
    val r = new scala.util.Random

    val opcode   = OP_B.litValue()
    val rs1      = BigInt(r.nextInt(32) << 15)
    val rs2      = BigInt(r.nextInt(32) << 20)
    val imm      = BigInt(r.nextInt(4096)) // -2048
    val funct3s  = Array(0, 1, 4, 5) //, 6, 7 s
    val funct3   = BigInt(funct3s(r.nextInt(4))) << 12 
    val imm12    = (((BigInt(2048)) & imm) >> 11) << 31
    val imm11    = (((BigInt(1024)) & imm) >> 10) << 7
    val imm10to5 = (((BigInt(992)) & imm) >> 5) << 25
    val imm4to1  = (((BigInt(30)) & imm) >> 1) << 8

    val immExpect = ((imm12 >> 20) | (imm11 << 3) | (imm10to5 >> 20) | (imm4to1 >> 8)) << 1

    val bitString = imm12 | rs2 | rs1 | funct3 | imm11 | opcode
    //val bitString = imm12 | imm10to5 | rs2 | rs1 | funct3 | imm4to1 | imm11 | opcode
    
    poke(dut.io.in, bitString)
    step(1)
    expect(dut.io.out, immExpect)
   }
}
class ImmUTypeTest (dut: ImmediateGen) extends PeekPokeTester(dut) {
   for (w <- 0 to 1000) {
    val r = new scala.util.Random

    val opcodes = Array(OP_LUI.litValue(), OP_AUIPC.litValue)
    val opcode = opcodes(r.nextInt(2))
 
    val rd          = BigInt(r.nextInt(32) << 7)
    val imm       = BigInt(r.nextInt(524288) - 262144) << 12

    val bitString =  imm | rd | opcode
    
    poke(dut.io.in, bitString)
    step(1)
    expect(dut.io.out, imm>>12)
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
//   "B type imm generation" should "pass" in {
//     chisel3.iotesters.Driver(() => new ImmediateGen()) { c => new ImmBTypeTest(c)} should be (true)
//   }

"U type imm generation" should "pass" in {
    chisel3.iotesters.Driver(() => new ImmediateGen()) { c => new ImmUTypeTest(c)} should be (true)
  }
}

/*
val r = new scala.util.Random

val opcode = OP.OP_I // convert this to binary like below...

val rd  = r.nextInt(32).toBinaryString
val rs1 = r.nextInt(32).toBinaryString
val rs2 = r.nextInt(32).toBinaryString

val funct3 = 7.toBinaryString // or in binary string directly like:
val funct7 = "0100000"

// Concatenate the strings (prepending "b" and convert to Chisel UInt)
val bitString = ("b" + funct7 + funct3 + rs2 + rs1 + rd + opcode).U
*/