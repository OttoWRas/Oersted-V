package core

import chisel3._
import chisel3.util._

class SingleCycleRiscV(program: String = "") extends Module {
  val io = IO(new Bundle {
    val regDebug    = Output(Vec(32, UInt(32.W))) // debug output for the tester
    val pcDebug     = Output(UInt(32.W))
    val instrDebug  = Output(UInt(32.W))
   // val done        = Output(Bool())
  })

    val mem   = Module(new Memory(program))
    val pc    = Module(new ProgramCounter)
    val ins   = Module(new InstBuff)
    val ctrl  = Module(new Control)
    val reg   = Module(new Registers)
    val dec   = Module(new Decoder)
    val alu   = Module(new ALU)
    val wb    = Module(new WriteBack)

    /* fetch / initialization */
    pc.io.flagIn    := ins.io.flagOut
    pc.io.pcPlus    := true.B
    pc.io.jmpAddr   := 0.U
    pc.io.wrEnable  := false.B
    
    ins.io.flagIn   := pc.io.flagOut
    ins.io.instIn   := mem.io.rdData

    mem.io.wrEnable := ctrl.io.memWrite
    mem.io.wrData   := reg.io.rdData2
    mem.io.wrAddr   := alu.io.out
    mem.io.rdAddr   := pc.io.pcAddr>>2 /* divide by four so we read addresses 1,2,3,4 instead of 0, 4, 8 etc */

    ctrl.io.in      := ins.io.instOut

    reg.io.wrEnable := ctrl.io.regWrite
    reg.io.wrData   := wb.io.wrBack
    reg.io.wrAddr   := dec.out.rd
    reg.io.rdAddr1  := dec.out.rs1
    reg.io.rdAddr2  := dec.out.rs2

    /* decode */
    dec.io.in       := ins.io.instOut

    /* execute */
    alu.io.opcode   := dec.io.aluOp
    alu.io.data1    := reg.io.rdData1
    alu.io.data2    := WireDefault(0.U)
    when(ctrl.io.ALUSrc){
      alu.io.data2 := dec.out.imm.asUInt // needs immediate handling
    }.otherwise {
      alu.io.data2 := reg.io.rdData2 // this should actually be the immediate 
    }
    
    /* write back */
    wb.io.memData   := mem.io.wrData 
    wb.io.aluData   := alu.io.out
    wb.io.memSel    := ctrl.io.memToReg
    wb.io.wrEnable  := false.B 
    when(ctrl.io.memToReg){
      wb.io.wrEnable := true.B
    }.otherwise {
      wb.io.wrEnable := false.B 
    }


    /* debugging outputs */
    io.pcDebug := pc.io.pcAddr>>2
    io.instrDebug := ins.io.instOut // mem.io.rdData //instr.io.instOut

    for(i <- 0 to 31){
      reg.io.wrEnable := false.B 

      reg.io.rdAddr1 := i.asUInt
      io.regDebug(i) := reg.io.rdData1
    }
}

object CPU extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new SingleCycleRiscV)
}

