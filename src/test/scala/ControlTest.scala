package core

import chisel3.iotesters._
import org.scalatest._
import OP._


class ControlTester(dut: Control) extends PeekPokeTester(dut) {
    for(i <- 0 to 100){
    val r = scala.util.Random
    
    val opcodes  = Array(OP_R, OP_I, OP_IL, OP_IE, OP_S, OP_B, OP_JAL, OP_JALR, OP_LUI, OP_AUIPC)
    val opPoke   = opcodes(r.nextInt(10))
    
    val opLit    = opPoke.litValue()  
    val regWrite = opLit & 1
    val ALUSrc   = opLit & (1<<1)
    val memWrite = opLit & (1<<2)
    val ALUOp    = opLit & (1<<3)
    val memToReg = opLit & (1<<4)
    val memRead  = opLit & (1<<5)
    val branch   = opLit & (1<<6)

    poke(dut.io.in, opPoke) // addi x1 x0 2 
    step(1)
    expect(dut.io.branch, branch>>6)
    expect(dut.io.memRead, memRead>>5)
    expect(dut.io.memToReg, memToReg>>4)
    expect(dut.io.ALUOp, ALUOp>>3)
    expect(dut.io.memWrite, memWrite>>2)
    expect(dut.io.ALUSrc, ALUSrc>>1)
    expect(dut.io.regWrite, regWrite)
}
}

class ControllerSpec extends FlatSpec with Matchers {
  "Control unit test" should "pass" in {
    chisel3.iotesters.Driver(() => new Control()) { c => new ControlTester(c)} should be (true)
  }
}