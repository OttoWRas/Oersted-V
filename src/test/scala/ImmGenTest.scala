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



class ImmBTypeTest (dut: ImmediateGen) extends PeekPokeTester(dut) {
   for (w <- 0 until 1000) {
      val r = new scala.util.Random

      val opcode     = OP_B.litValue()
      val rs1        = BigInt(r.nextInt(32) << 15)
      val rs2        = BigInt(r.nextInt(32) << 20)
      val imm        = BigInt(r.nextInt(4096) - 2048) // -2048
      val funct3s    = Array(0, 1, 4, 5) //, 6, 7 zero-extends.. 
      val funct3     = BigInt(funct3s(r.nextInt(4))) << 12 

      val mask12     = BigInt(1) << 12 
      val mask11     = BigInt(1) << 11
      val mask10to5  = BigInt(63) << 5
      val mask4to1   = BigInt(15) << 1
      
      val imm12      = (mask12 & imm) << 19 
      val imm11      = (mask11 & imm) >> 4
      val imm10to5   = (mask10to5 & imm) << 20
      val imm4to1    = (mask4to1 & imm) << 7

      var immExpect = (imm12 >> 19) | (imm11 << 4) | (imm10to5 >> 20) | (imm4to1 >> 7)

      if((imm12>>31) == BigInt(1)){
         immExpect = immExpect | 0xFFFFF000
      }
      val bitString = imm12 | imm10to5 | rs2 | rs1 | funct3 | imm4to1 | imm11 | opcode

      poke(dut.io.in, bitString)
      step(1)
      expect(dut.io.imm12, imm12>>31)
      expect(dut.io.imm11, imm11>>7)
      expect(dut.io.imm10to5, imm10to5>>25)
      expect(dut.io.imm4to1, imm4to1>>8)
      expect(dut.io.out, immExpect)
   }
}

class ImmJTypeTest (dut: ImmediateGen) extends PeekPokeTester(dut) {
   for (w <- 0 to 1000) {
      val r = new scala.util.Random

      val opcode      = OP_JAL.litValue()
      val rd          = BigInt(31) << 7 // BigInt(r.nextInt(32).toBinaryString, 2) << 7

      /* generate a large pos og neg number to test */
      val imm         = BigInt(r.nextInt(1048576)- 524288) << 12// -- 524288
     
      val mask20      = BigInt(1.toBinaryString, 2) << 31
      val mask19to12  = BigInt(255.toBinaryString, 2) << 12
      val mask11      = BigInt(1.toBinaryString, 2) << 20
      val mask10to1   = BigInt(1023.toBinaryString, 2) << 21

      
      val imm20       = imm & mask20
      val imm10to1    = imm & mask10to1
      val imm11       = imm & mask11
      val imm19to12   = imm & mask19to12

      /* put the immediates together the same way as it's done in the immediate generation according to specification */
      var immExpect = ((imm20 >> 11) | (imm19to12) | imm10to1 >> 20 | imm11 >> 9)
      
      if((imm20>>31) == BigInt(1)){
         immExpect = immExpect | 0xFFF00000
      }
      val bitString =  imm20 | imm10to1 | imm11 | imm19to12 | rd | opcode

      poke(dut.io.in, bitString)
      step(1)
      expect(dut.io.out, immExpect)
   }
}

class ImmSpec extends FlatSpec with Matchers {
//   "I regular type immediate generation" should "pass" in {
//     chisel3.iotesters.Driver(() => new ImmediateGen()) { c => new ImmITypeTest(c)} should be (true)
//   }
//    "I load type imm generation" should "pass" in {
//     chisel3.iotesters.Driver(() => new ImmediateGen()) { c => new ImmILTypeTest(c)} should be (true)
//   }
//   "S type imm generation" should "pass" in {
//     chisel3.iotesters.Driver(() => new ImmediateGen()) { c => new ImmSTypeTest(c)} should be (true)
//   }
  "B type imm generation" should "pass" in {
    chisel3.iotesters.Driver(() => new ImmediateGen()) { c => new ImmBTypeTest(c)} should be (true)
  }

// "U type imm generation" should "pass" in {
//     chisel3.iotesters.Driver(() => new ImmediateGen()) { c => new ImmUTypeTest(c)} should be (true)
//   }
//   "J type imm generation" should "pass" in {
//     chisel3.iotesters.Driver(() => new ImmediateGen()) { c => new ImmJTypeTest(c)} should be (true)
//   }
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

/*
 val rd          = BigInt(r.nextInt(32).toBinaryString, 2) << 7
      val imm         = (BigInt(r.nextInt(1048576).toBinaryString, 2)) //1048576 -- 524288

      val mask20      = BigInt((1).toBinaryString, 2) << 19
      val mask19to12  = BigInt((255<< 11).toBinaryString, 2)
      val mask11      = BigInt((1 << 10).toBinaryString, 2)
      val mask10to1   = BigInt((1023).toBinaryString, 2) << 1
   
      val imm20       = ((imm & mask20) >> 19) << 31 
      val imm10to1    = (imm & mask10to1) << 20
      val imm11       = ((imm & mask11) >> 10) << 19
      val imm19to12   = (imm & mask19to12) 

  
    val bitString =  imm20 | imm10to1 | imm11 | imm19to12 | rd | opcode

    poke(dut.io.in, bitString)
    step(1)
    expect(dut.io.imm20, imm20 >> 31)
    expect(dut.io.imm10to1, imm10to1 >> 21)
    expect(dut.io.imm11, imm11 >> 19)
   expect(dut.io.out, imm << 1)
   }
}
*/