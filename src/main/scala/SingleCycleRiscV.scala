package core

import chisel3._
import chisel3.util._

class SingleCycleRiscV(program: String = "") extends Module {
  val io = IO(new Bundle {
    //val regDebug = Output(Vec(32, UInt(32.W))) // debug output for the tester
    val pcDebug  = Output(UInt(32.W))
    val instrDebug = Output(UInt(32.W))
    val rdAddr = Input(UInt(32.W))
    //val done     = Output(Bool())
  })

    val mem  = Module(new Memory(program))
    val pc = RegInit(0.U(32.W))

    mem.io.wrEnable := false.B //control.io.memWrite //io.wrEnableMem
    mem.io.wrData   := 0.U //reg.io.rdData2
    mem.io.wrAddr   := 0.U // alu.io.out

    mem.io.rdAddr := pc>>2

//   mem.io.wrEnable := false.B //control.io.memWrite //io.wrEnableMem
//   mem.io.wrData   := 0.U //reg.io.rdData2
//   mem.io.wrAddr   := 0.U // alu.io.out

//    mem.io.rdAddr := pc
//     // printf("mem.io.rdAddr = %d\n", mem.io.rdAddr)
//     // printf("mem.io.rdData = %d\n\n", mem.io.rdData)

  
pc := pc + 4.U
  


//     // mem.io.wrData := io.wrData
//   // mem.io.wrAddr := io.wrAddr     

//     // when (io.fetch) {
//     //     mem.io.rdAddr := pc.io.pcAddr
//     // }

//     // io.instOut := instr.io.instOut 







    /* debugging outputs */
    io.pcDebug := io.rdAddr
    io.instrDebug := mem.io.rdData //instr.io.instOut
}

object CPU extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new SingleCycleRiscV)
}

