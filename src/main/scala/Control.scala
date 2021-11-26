package core

import chisel3._
import chisel3.util._

class Control extends Module {
    val io = IO {
        new Bundle {
            val in          = Input(UInt(8.W))

            val branch      = Output(Bool())
            val memRead     = Output(Bool())
            val memToReg    = Output(Bool())
            val ALUOp       = Output(Bool())
            val memWrite    = Output(Bool())
            val ALUSrc      = Output(Bool())
            val regWrite    = Output(Bool())
            val nextOp      = Output(Bool())
        }
    }
    
    io.nextOp    := io.in(7)
    io.branch    := io.in(6)
    io.memRead   := io.in(5)
    io.memToReg  := io.in(4)
    io.ALUOp     := io.in(3)
    io.memWrite  := io.in(2)
    io.ALUSrc    := io.in(1)
    io.regWrite  := io.in(0)
  

}

object ControlGen extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Control())
}