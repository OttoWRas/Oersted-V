package core

import chisel3.iotesters._
import org.scalatest._
import chisel3._
import chisel3.util._
import OP._

class DecodeIType (dut: Decoder) extends PeekPokeTester(dut) {
    for(i <- 0 to 100) {
        val r = new scala.util.Random
        val opcode = OP_I.litValue() // litValue converts from UInt to BigInt 
        val rd  = BigInt(r.nextInt(32) << 7)
        val rs1 = BigInt(r.nextInt(32) << 15)
        val funct3 = 0
        val funct7 = 0 
        val imm = BigInt(r.nextInt(4096)) << 20

        val bitString = rd | opcode | rs1 | imm 

        poke(dut.in, bitString)
        step(1)
        expect(dut.decoded.opcode, opcode)
        expect(dut.decoded.rd, rd >> 7 )
        expect(dut.decoded.rs1, rs1 >> 15 )
        expect(dut.decoded.imm, imm >> 20)
    
    }

}

class ITypeSpec extends FlatSpec with Matchers {
  "Instruction type I test" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeIType(c)} should be (true)
  }
}





class DecodeRType (dut: Decoder) extends PeekPokeTester(dut) {
    for(i <- 0 to 100) {
        val r = new scala.util.Random
        val opcode = OP_R.litValue()
        val rd  = BigInt(r.nextInt(32) << 7)
        val rs1 = BigInt(r.nextInt(32) << 15)
        val rs2 = BigInt(r.nextInt(32) << 20)
        val funct3 = 0 // for RType can be quite a few different things. testing for this will be implemented when decoder and ALU communicates better. 
        val funct7 = 0 // for Rtype can either be 0x00 or 0x20
      

        val bitString = funct7 | rs2 | rs1 | funct3 | rd | opcode 

        poke(dut.in, bitString)
        step(1)
        expect(dut.decoded.opcode, opcode)
        expect(dut.decoded.rd, rd >> 7 )
        expect(dut.decoded.rs1, rs1 >> 15 )
        expect(dut.decoded.rs2, rs2 >> 20)
    
    }

}

class RTypeSpec extends FlatSpec with Matchers {
  "Instruction type R test" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeRType(c)} should be (true)
  }
}




class DecodeTester(dut: Decoder) extends PeekPokeTester(dut) {



/*
  poke(dut.in, 0x00200093) // 
  step(1)
  expect(dut.decoded.opcode, OP.OP_I)
  expect(dut.decoded.imm, 2)
  println ("Immediate is " + peek(dut.decoded.imm).toString + "\n")
  println ("Opcode is " + peek(dut.decoded.opcode).toString)
  println("funct3: " + peek(dut.decoded.funct3).toString)
  println("funct7: " + peek(dut.decoded.funct7).toString)
  
  poke(dut.in, 0x002081b3)
  step(1)
  expect(dut.decoded.opcode, OP.OP_R)
  println ("Opcode is " + peek(dut.decoded.opcode).toString)
*/
}

class DecoderSpec extends FlatSpec with Matchers {
  "Decode Tester" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeTester(c)} should be (true)
  }
}
