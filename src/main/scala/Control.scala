package core

import chisel3._
import chisel3.util._

class Control extends Module {
    val io = IO {
        new Bundle {
            val in = Input(UInt(7.W))

            val Branch = Output(Bool())
            val MemRead = Output(Bool())
            val MemtoReg = Output(Bool())
            val ALUOp = Output(Bool())
            val MemWrite = Output(Bool())
            val ALUSrc = Output(Bool())
            val RegWrite = Output(Bool())
        }
    }
    
   io.Branch := io.in(1,0)
   io.MemRead := io.in(2,1)
   io.MemtoReg := io.in(3,2)
   io.ALUOp := io.in(4,3)
   io.MemWrite := io.in(5,4)
   io.ALUSrc := io.in(6,5)
   io.RegWrite := io.in(7,6)

}

object ControlGen extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Control())
}