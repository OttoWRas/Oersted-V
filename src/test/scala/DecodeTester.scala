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
        //val imm = BigInt(r.nextInt(4096)) << 20

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
        expect(dut.out.opcode, opcode)
        expect(dut.out.rd, rd >> 7 )
        expect(dut.out.rs1, rs1 >> 15 )
        expect(dut.out.rs2, rs2 >> 20)
    
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
        expect(dut.out.opcode, opcode)
        expect(dut.out.rd, rd >> 7 )
        expect(dut.out.imm, imm >> 12)
    
    }

}

class UTypeSpec extends FlatSpec with Matchers {
  "Instruction type U test" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeUType(c)} should be (true)
  }
}


class DecodeRALUTest (dut: Decoder) extends PeekPokeTester(dut) {
    for(i <- 0 to 1000) {
        val r = new scala.util.Random
        val opcode = OP_R.litValue()

        val funct3s = Array(0, 4, 6, 7, 1, 5, 2, 3)
        val funct7s = Array(0, 2)
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

class DecodeRALUSpec extends FlatSpec with Matchers {
  "R type instructions and ALU ops" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeRALUTest(c)} should be (true)
  }
}





class DecodeSTypeTest (dut: Decoder) extends PeekPokeTester(dut) {
    for(i <- 0 to 1000) {
       val r = new scala.util.Random
    //  val opcodes = Array(OP_LUI.litValue(), OP_AUIPC.litValue())
      val opcode    = OP_S.litValue()
      val rs1       = BigInt(r.nextInt(32) << 15)
      val rs2       = BigInt(r.nextInt(32) << 20)
      val imm       = BigInt(r.nextInt(4096)-2048)
      val imm4to0   = (BigInt(31) & imm) << 7 // 5 bits
      val imm11to5  = (BigInt(4094) & imm) << 20 // only shifted 20, since it's already shifted 5 because of the bitextraction
      val funct3    = BigInt(0) << 12

      val bitString = imm11to5 | rs2 | rs1 | funct3 | imm4to0 | opcode
      
      poke(dut.io.in, bitString)
      step(4)
      expect(dut.out.opcode, opcode)
      expect(dut.out.imm, imm)

      // m.io.in.poke(bitString.U)
      // m.clock.step(5)
      // m.out.opcode.expect(opcode.U)
      // m.out.imm.expect(imm.S)
    }

}

class DecodeSTypeSpec extends FlatSpec with Matchers {
  "S type instructions" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeSTypeTest(c)} should be (true)
  }
}







/*

class DecodeSTypeTest extends FlatSpec with ChiselScalatestTester with Matchers {
  "Decode S type instructions test" should "pass" in {
    test(new Decoder()) { m=>
    for(i <- 0 to 1000){
       val r = new scala.util.Random
    //  val opcodes = Array(OP_LUI.litValue(), OP_AUIPC.litValue())
      val opcode    = OP_S.litValue()
      val rs1       = BigInt(r.nextInt(32) << 15)
      val rs2       = BigInt(r.nextInt(32) << 20)
      val imm       = BigInt(r.nextInt(4096))
      val imm4to0   = (BigInt(31) & imm) << 7 // 5 bits
      val imm11to5  = (BigInt(4094) & imm) << 20 // only shifted 20, since it's already shifted 5 because of the bitextraction
      val funct3    = BigInt(0) << 12

      val bitString = imm11to5 | rs2 | rs1 | funct3 | imm4to0 | opcode

      m.io.in.poke(bitString.U)
      m.clock.step(5)
      m.out.opcode.expect(opcode.U)
      m.out.imm.expect(imm.S)
    }
      // expect(dut.out.rd, rd >> 7 )
      // expect(dut.out.imm, imm >> 12)   
    }
    }
  }




*/



















/* WIP */

class DecodeIALUTest (dut: Decoder) extends PeekPokeTester(dut) {
    for(i <- 0 to 1000) {
        val r = new scala.util.Random
        val opcode = OP_I.litValue()

        val funct3s = Array(0, 4, 6, 7, 1, 5, 2, 3)
        val funct7s = Array(0, 2)
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

// class DecodeIALUSpec extends FlatSpec with Matchers {
//   "I type instructions and ALU ops" should "pass" in {
//     chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeIALUTest(c)} should be (true)
//   }
// }






/* WIP
class DecodeJType (dut: Decoder) extends PeekPokeTester(dut) {
    for(i <- 0 to 100) {
        val r = new scala.util.Random
      
        val opcode = OP_JAL
        val rd  = BigInt(r.nextInt(32) << 7)
        val imm19to12 = BigInt(r.nextInt(2^8)) << 12 
        val imm11 = BigInt(r.nextInt(2)) << 20
        val imm10to1 = BigInt(r.nextInt(2^10)) << 21
        val imm20 = BigInt(r.nextInt(2)) << 31
      
       
       // val bitString = imm20 | imm10to1 | imm11 | imm19to12  | rd | opcode
        val bitString = imm20 | imm10to1 | imm11 | imm19to12 | rd | opcode
        //20 10:1 11 19:12
        val imm = (imm20 | imm19to12 | imm11 | imm10to1) << 12
        
        poke(dut.in, bitString)
        step(1)
        expect(dut.out.opcode, opcode)
        expect(dut.out.rd, rd >> 7 )
        expect(dut.out.imm, imm >> 12)
    
    }

}

class JTypeSpec extends FlatSpec with Matchers {
  "Instruction type J test (JAL instruction)" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeJType(c)} should be (true)
  }
}
 */