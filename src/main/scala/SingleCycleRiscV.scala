package core

import chisel3._
import chisel3.util._

class SingleCycleRiscV(program: String = "") extends Module {
  val io = IO(new Bundle {
    //val regDebug = Output(Vec(32, UInt(32.W))) // debug output for the tester
    val pcDebug  = Output(UInt(32.W))
    val instrDebug = Output(UInt(32.W))
  })

    val mem  = Module(new Memory(program))
    val pc  = Module(new ProgramCounter)
    val ins = Module(new InstBuff)

    pc.io.flagIn := ins.io.flagOut
    pc.io.pcPlus := true.B
    pc.io.jmpAddr := 0.U
    pc.io.wrEnable := false.B
    
    ins.io.flagIn := pc.io.flagOut
    ins.io.instIn := mem.io.rdData

    mem.io.wrEnable := false.B //control.io.memWrite //io.wrEnableMem
    mem.io.wrData   := 0.U //reg.io.rdData2
    mem.io.wrAddr   := 0.U // alu.io.out
    mem.io.rdAddr := pc.io.pcAddr>>2 // divide by 4





    /* debugging outputs */
    io.pcDebug := pc.io.pcAddr>>2
    io.instrDebug := ins.io.instOut // mem.io.rdData //instr.io.instOut
}

object CPU extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new SingleCycleRiscV)
}

