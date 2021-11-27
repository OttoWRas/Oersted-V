package core

import chisel3._
import chisel3.util._
import OP._
class Control extends Module {
    val io = IO {
        new Bundle {
            val in          = Input(UInt(7.W))

            val branch      = Output(Bool())
            val memRead     = Output(Bool())
            val memToReg    = Output(Bool())
            val ALUOp       = Output(UInt(2.W))
            val memWrite    = Output(Bool())
            val ALUSrc      = Output(Bool())
            val regWrite    = Output(Bool())
            //val nextOp      = Output(Bool())
        }
    }
    io.ALUSrc    := WireDefault(false.B)
    io.memToReg  := WireDefault(false.B)
    io.regWrite  := WireDefault(false.B)
    io.memRead   := WireDefault(false.B)
    io.memWrite  := WireDefault(false.B)
    io.branch    := WireDefault(false.B)
    io.ALUOp     := WireDefault(0.U)
/*

   
   
    val OP_IE:    UInt = 115.U(7.W) // type I environment call instructions 

    //val OP_JAL:   UInt = 111.U(7.W) // J type
    //val OP_JALR:  UInt = 103.U(7.W) // I type
    val OP_LUI:   UInt = 55.U(7.W) // U type
    val OP_AUIPC: UInt = "b0010111".U(7.W) // U type 
*/

    switch(io.in){
        /* slightly unsure about these */
        is(OP_JAL, OP_JALR){
            io.ALUSrc    := true.B 
            io.memToReg  := true.B
            io.regWrite  := true.B 
           // io.memRead   := 
           // io.memWrite  := 
            io.branch    := true.B 
            io.ALUOp     := "b00".U 
            // 
        }
        is(OP_LUI,OP_AUIPC){
            io.ALUSrc    := true.B 
            io.memToReg  := false.B 
            io.regWrite  := true.B
            io.memRead   := false.B 
            io.memWrite  := false.B
            io.branch    := false.B 
            io.ALUOp     := "b00".U
        }
       
        is(OP_R){
            io.ALUSrc    := false.B 
            io.memToReg  := false.B
            io.regWrite  := true.B 
            io.memRead   := false.B
            io.memWrite  := false.B 
            io.branch    := false.B 
            io.ALUOp     := "b10".U
        }

        is(OP_IL){
            io.ALUSrc    := true.B 
            io.memToReg  := true.B 
            io.regWrite  := true.B
            io.memRead   := true.B 
            io.memWrite  := false.B 
            io.branch    := false.B 
            io.ALUOp     := "b00".U 
        }

         is(OP_S){
            io.ALUSrc    := true.B 
            // io.memToReg  := dontCare
            io.regWrite  :=  false.B 
            io.memRead   := false.B 
            io.memWrite  := true.B 
            io.branch    := false.B 
            io.ALUOp     := "b00".U
        }
        is(OP_B){
            io.ALUSrc    := false.B 
            // io.memToReg  := dontCare
            io.regWrite  :=  false.B 
            io.memRead   := false.B
            io.memWrite  := false.B 
            io.branch    := true.B 
            io.ALUOp     := "b10".U
        }
        is(OP_I, OP_IE){
            io.ALUSrc    := true.B 
            io.memToReg  := false.B
            io.regWrite  := true.B 
            io.memRead   := false.B
            io.memWrite  := false.B 
            io.branch    := false.B 
            io.ALUOp     := "b10".U
        }
    }

   
  

}

object ControlGen extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Control())
}

/*
io.ALUSrc    := 
io.memToReg  := 
io.regWrite  :=  
io.memRead   := 
io.memWrite  := 
io.branch    := 
io.ALUOp     := 
*/