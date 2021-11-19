package core

import chisel3._
import chisel3.util._


class SingleCycleRiscV extends Module {
  val io = IO(new Bundle {
    val regDebug = Output(Vec(32, UInt(32.W))) // debug output for the tester
    val pcDebug  = Output(UInt(32.W))
    val instrDebug = Output(UInt(32.W))
    val done     = Output(Bool())
  })

    val pc      = Module(new ProgramCounter)
    val instr   = Module(new InstBuff)
    val reg     = Module(new Registers)
    val alu     = Module(new ALU)
    val decode  = Module(new Decoder)
    val mem     = Module(new Memory("./testData/instructions.hex.txt"))
    val wb      = Module(new WriteBack)
    val control = Module(new Control)


    instr.io.instIn := mem.io.rdData 
    pc.io.flagIn    := instr.io.flagOut
    pc.io.jmpAddr   := WireDefault(0.U) // no jump instr yet

    instr.io.flagIn := pc.io.flagOut
  
    control.io.in   := instr.io.instOut(6,0)
   
    mem.io.wrEnable := control.io.memWrite //io.wrEnableMem
    mem.io.wrData   := reg.io.rdData2
    mem.io.wrAddr   := alu.io.out
    mem.io.rdAddr   := alu.io.out // ?

    decode.io.in    := instr.io.instOut
    alu.io.opcode   := decode.io.aluOp

    wb.io.memData   := mem.io.wrData 
    wb.io.aluData   := alu.io.out
    wb.io.memSel    := control.io.memToReg
    
    // this seems a bit double, but is it for some hazard prevention later on?
    when(control.io.memToReg){
      wb.io.wrEnable := 1.U
    }.otherwise{
      wb.io.wrEnable := 0.U
    }

    reg.io.wrEnable := control.io.regWrite
    reg.io.wrData   := wb.io.wrBack
    reg.io.wrAddr   := decode.decoded.rd

    reg.io.rdAddr1   := decode.decoded.rs1
    reg.io.rdAddr2   := decode.decoded.rs2

    alu.io.data1    := reg.io.rdData1


    when(control.io.ALUSrc){
      alu.io.data2 := reg.io.rdData2 // this should actually be the immediate 
    }.otherwise {
      alu.io.data2 := reg.io.rdData2 // needs immediate handling
    }

    pc.io.pcPlus := true.B
    pc.io.wrEnable := true.B // ?? on all the time?w


  /* debugging connections o*/
    io.pcDebug := pc.io.pcAddr
    io.instrDebug := instr.io.instOut
    io.done := true.B

  /* fill up debugging reigster with actual registers */
    for(i <- 0 to 31){
      reg.io.rdAddr1 := i.asUInt
      io.regDebug(i) := reg.io.rdData1
    }
}

object CPU extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new SingleCycleRiscV)
}

