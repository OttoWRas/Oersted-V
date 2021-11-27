package core

import chisel3.iotesters._
import org.scalatest._
import chiseltest._
import chisel3._

import OP._
import ALU._

class DecodeIType (dut: Decoder) extends PeekPokeTester(dut) {
    for(i <- 0 to 100) {
        val r = new scala.util.Random
        val opcode = OP_I.litValue() // litValue converts from UInt to BigInt 
        val rd  = BigInt(r.nextInt(32) << 7)
        val rs1 = BigInt(r.nextInt(32) << 15)
        val funct3 = 0
        val funct7 = 0 
        val imm = BigInt(r.nextInt(4096) - 2048) << 20

        val bitString = rd | opcode | rs1 | imm 

        poke(dut.io.in, bitString)
        step(1)
        expect(dut.out.opcode, opcode)
        expect(dut.out.rd, rd >> 7 )
        expect(dut.out.rs1, rs1 >> 15 )
        expect(dut.out.imm, imm >> 20)
     
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
        expect(dut.out.opcode, opcode)
        expect(dut.out.rd, rd >> 7 )
        expect(dut.out.rs1, rs1 >> 15 )
        expect(dut.out.rs2, rs2 >> 20)
    
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
        expect(dut.out.opcode, opcode)
        expect(dut.out.rd, rd >> 7 )
        expect(dut.out.imm, imm)
    
    }

}



/* R TYPE AND ALU OPCODE TEST */
class DecodeRALUTest (dut: Decoder) extends PeekPokeTester(dut) {
    for(i <- 0 to 1000) {
        val r = new scala.util.Random
        val opcode = OP_R.litValue()

        val funct3s = Array(0, 4, 6, 7, 1, 5, 2, 3)
        val funct7s = Array(0, 32)
        val funct3 = BigInt(funct3s(r.nextInt(8))) << 12
        val funct7 = BigInt(funct7s(r.nextInt(2))) << 25
        val rd  = BigInt(r.nextInt(32) << 7)
        val rs1 = BigInt(r.nextInt(32) << 15)
        val rs2 = BigInt(r.nextInt(32) << 20)

        val bitString = (funct7) | rs2 | rs1 | (funct3) | rd | opcode

        /* this is fcuking retarded */ 
        val Zero  = BigInt(0)
        val One   = BigInt(1)
        val Two   = BigInt(2)
        val Three = BigInt(3)
        val Four  = BigInt(4)
        val Five  = BigInt(5)
        val Six   = BigInt(6)
        val Seven = BigInt(7)

        val negOne = BigInt(-1)
       
        val aluOp = (funct3 >> 12) match  {
          case Zero   => if(funct7 >> 25 == Zero) { ALU_ADD.litValue() } else { ALU_SUB.litValue() }
          case Four   => ALU_XOR.litValue()
          case Six    => ALU_OR.litValue()
          case Seven  => ALU_AND.litValue()
          case One    => ALU_SLL.litValue()
          case Five   => if((funct7 >> 25) == Zero) { ALU_SRL.litValue() } else { ALU_SRA.litValue() }
          case Two    => ALU_SLT.litValue()
          case Three  => ALU_SLTU.litValue()

          case _ => negOne
        }
     
        poke(dut.io.in, bitString)
        step(10)
        expect(dut.out.opcode, opcode)
        expect(dut.out.rd, rd >> 7 )
        expect(dut.out.rs1, rs1 >> 15)
        expect(dut.out.rs2, rs2 >> 20)
        expect(dut.out.funct3, funct3 >> 12)
        expect(dut.out.funct7, funct7 >> 25)
        expect(dut.io.aluOp, aluOp)
    }

}



class DecodeSTypeTest (dut: Decoder) extends PeekPokeTester(dut) {
    for(i <- 0 to 1000) {
       val r = new scala.util.Random
      val opcode    = OP_S.litValue()
      val rs1       = BigInt(r.nextInt(32) << 15)
      val rs2       = BigInt(r.nextInt(32) << 20)
      val imm       = BigInt(r.nextInt(4096)-2048)
      val imm4to0   = (BigInt(31) & imm) << 7 // 5 bits
      /* we need to shift the 11to5 >> 5 first, and then << 25 otherwise it will clash with rs2 */
      val imm11to5  = ((BigInt(4094) & imm) >> 5) << 25
      val funct3    = BigInt(r.nextInt(3)) << 12

      val bitString = imm11to5 | rs2 | rs1 | funct3 | imm4to0 | opcode
      
      poke(dut.io.in, bitString)
      step(4)
      expect(dut.out.opcode, opcode)
      expect(dut.out.imm, imm)
      expect(dut.out.rs1, rs1>>15)
      expect(dut.out.rs2, rs2>>20)
      expect(dut.out.funct3, funct3>>12)
    }

}

/* I TYPE AND ALU OPCODE TEST */

class DecodeIALUTest (dut: Decoder) extends PeekPokeTester(dut) {
    for(i <- 0 to 1000) {
        val r = new scala.util.Random
        val opcode = OP_I.litValue()

        val funct3s = Array(0, 4, 6, 7, 1, 5, 2, 3)
        val funct3 = BigInt(funct3s(r.nextInt(8))) << 12
        val rd  = BigInt(r.nextInt(32) << 7)
        val rs1 = BigInt(r.nextInt(32) << 15)
        val imm = BigInt(r.nextInt(4096) - 2048) << 20 // 
        
        val bitString = imm | rs1 | funct3 | rd | opcode

        /* this is fcuking retarded */ 
        val Zero  = BigInt(0)
        val One   = BigInt(1)
        val Two   = BigInt(2)
        val Three = BigInt(3)
        val Four  = BigInt(4)
        val Five  = BigInt(5)
        val Six   = BigInt(6)
        val Seven = BigInt(7)
        val ThirtyTwo = BigInt(32)
        val imm11to5 = ((BigInt(4094) & (imm>>20)) >> 5)
        val negOne = BigInt(-1)
       
        val aluOp = (funct3 >> 12) match  {
          case Zero   => ALU_ADD.litValue() 
          case Four   => ALU_XOR.litValue()
          case Six    => ALU_OR.litValue()
          case Seven  => ALU_AND.litValue()
          case One    => ALU_SLL.litValue()
          case Five   => if(imm11to5 == Zero) { ALU_SRL.litValue() } else {  ALU_SRA.litValue() }
          case Two    => ALU_SLT.litValue()
          case Three  => ALU_SLTU.litValue()

          case _ => negOne
        }
     
        poke(dut.io.in, bitString)
        step(10)
        expect(dut.out.opcode, opcode)
        expect(dut.out.rd, rd >> 7 )
        expect(dut.out.rs1, rs1 >> 15)
        expect(dut.out.imm, imm >> 20)
        expect(dut.out.funct3, funct3 >> 12)
   
        expect(dut.io.aluOp, aluOp)
    }

}

class DecodeSpec extends FlatSpec with Matchers {
  "I type instructions and ALU ops" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeIALUTest(c)} should be (true)
  }
  "S type instructions" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeSTypeTest(c)} should be (true)
  }
  "R type instructions and ALU ops" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeRALUTest(c)} should be (true)
  }
   "Instruction type I test" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeIType(c)} should be (true)
  }
  "Instruction type R test" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeRType(c)} should be (true)
  }
"Instruction type U test" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeUType(c)} should be (true)
  }
}

