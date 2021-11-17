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
        //val imm = BigInt(r.nextInt(4096)) << 20

        val imm = BigInt(r.nextInt(4096) - 2048) << 20

        val bitString = rd | opcode | rs1 | imm 

        poke(dut.io.in, bitString)
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

        poke(dut.io.in, bitString)
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

/* is(OP.OP_LUI, OP.OP_AUIPC, OP.OP_JAL){ */
class DecodeUType (dut: Decoder) extends PeekPokeTester(dut) {
    for(i <- 0 to 100) {
        val r = new scala.util.Random
        val opcodes = Array(OP_LUI.litValue(), OP_AUIPC.litValue())
        val opcode = opcodes(r.nextInt(1))
        val rd  = BigInt(r.nextInt(32) << 7)
        val imm = BigInt(r.nextInt(1048576) - 524288) << 12
       

        val bitString = imm | rd | opcode

        poke(dut.io.in, bitString)
        step(1)
        expect(dut.decoded.opcode, opcode)
        expect(dut.decoded.rd, rd >> 7 )
        expect(dut.decoded.imm, imm >> 12)
    
    }

}

class UTypeSpec extends FlatSpec with Matchers {
  "Instruction type U test" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeUType(c)} should be (true)
  }
}


/* WIP
class DecodeJType (dut: Decoder) extends PeekPokeTester(dut) {
    for(i <- 0 to 100) {
        val r = new scala.util.Random
      
        val opcode = OP_JAL
        val rd  = BigInt(r.nextInt(32) << 7)
        val imm19to12 = BigInt(r.nextInt(2^8)) << 12 
        val imm11 = BigInt(r.nextInt(1)) << 20
        val imm10to1 = BigInt(r.nextInt(2^10)) << 21
        val imm20 = BigInt(r.nextInt(1)) << 31
      
       
       // val bitString = imm20 | imm10to1 | imm11 | imm19to12  | rd | opcode
        val bitString = imm20 | imm10to1 | imm11 | imm19to12 | rd | opcode
        //20 10:1 11 19:12
        val imm = (imm20 | imm19to12 | imm11 | imm10to1) << 12
        
        poke(dut.in, bitString)
        step(1)
        expect(dut.decoded.opcode, opcode)
        expect(dut.decoded.rd, rd >> 7 )
        expect(dut.decoded.imm, imm >> 12)
    
    }

}

class JTypeSpec extends FlatSpec with Matchers {
  "Instruction type J test (JAL instruction)" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeJType(c)} should be (true)
  }
}
 */