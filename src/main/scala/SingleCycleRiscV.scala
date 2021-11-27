package core

import chisel3._
import chisel3.util._
import OP._

class SingleCycleRiscV(program: String = "") extends Module {
  val io = IO(new Bundle {
    val regDebug    = Output(Vec(32, UInt(32.W))) // debug output for the tester
    val pcDebug     = Output(SInt(32.W))
    val instrDebug  = Output(UInt(32.W))
    
    /* decoder debug */
    val opcodeDebug      = Output(UInt(7.W))
    val rdDebug          = Output(UInt(5.W))
    val funct3Debug      = Output(UInt(3.W))
    val rs1Debug         = Output(UInt(5.W))
    val rs2Debug         = Output(UInt(5.W))
    val rd1Debug         = Output(UInt(32.W))
    val rd2Debug         = Output(UInt(32.W))
    val aLUSrcDebug      = Output(Bool())
    val funct7Debug      = Output(UInt(7.W))
    val immDebug         = Output(SInt(32.W))

  /* alu debug */
    val aluOutDebug     = Output(UInt(32.W))
    val aluCmpOutDebug  = Output(Bool())

    val pcJmpAddrDebug = Output(SInt(32.W))

    val ctrlBranchDebug = Output(Bool())
    val done              = Output(Bool())
  })
    //val decDebug = IO(Output(new DecodeOut))
    

    val mem    = Module(new Memory(program))
    val pc     = Module(new ProgramCounter)
    val ins    = Module(new InstBuff)
    val ctrl   = Module(new Control)
    val reg    = Module(new Registers)
    val dec    = Module(new Decoder)
    val alu    = Module(new ALU)
    val wb     = Module(new WriteBack)
    val imm    = Module(new ImmediateGen)

    /* fetch / initialization */
    pc.io.flagIn    := ins.io.flagOut

    /* branching logics */
    pc.io.pcPlus    := true.B
    pc.io.jmpAddr   := 0.S
    pc.io.wrEnable  := false.B
    when(alu.io.cmpOut.asBool & ctrl.io.branch){
      pc.io.pcPlus    := false.B
      pc.io.wrEnable  := true.B
      
      pc.io.jmpAddr   := (pc.io.pcAddr + imm.io.out)
    }
    ctrl.io.in      := ins.io.instOut(6,0)  

    ins.io.flagIn   := pc.io.flagOut
    ins.io.instIn   := mem.io.rdData

    mem.io.wrEnable := ctrl.io.memWrite
    mem.io.wrData   := reg.io.rdData2
    mem.io.wrAddr   := alu.io.out
    mem.io.rdAddr   := (pc.io.pcAddr>>2).asUInt /* divide by four so we read addresses 1,2,3,4 instead of 0, 4, 8 etc */

    reg.io.wrEnable := ctrl.io.regWrite
    reg.io.wrData   := wb.io.wrBack 
    reg.io.wrAddr   := dec.out.rd
    reg.io.rdAddr1  := dec.out.rs1
    reg.io.rdAddr2  := dec.out.rs2

    io.rd1Debug := reg.io.rdData1
    io.rd2Debug := reg.io.rdData2

    /* decode */
    dec.io.in       := ins.io.instOut
    imm.io.in       := ins.io.instOut

    /* execute */
    alu.io.opcode   := dec.io.aluOp
    alu.io.data1    := reg.io.rdData1
    alu.io.data2    := WireDefault(0.U)
    /* ctrl.io.out.ALUSrc? */
    when(ctrl.io.ALUSrc){ 
      alu.io.data2 := imm.io.out.asUInt // needs immediate handling
    }.otherwise {
      alu.io.data2 := reg.io.rdData2 
    }

    io.aLUSrcDebug := ctrl.io.ALUSrc
    
    /* write back */
    wb.io.memData   := mem.io.rdData 
    wb.io.aluData   := alu.io.out
    wb.io.memToReg    := ctrl.io.memToReg //~
    wb.io.wrEnable  := true.B //ctrl.io.memToReg
    // //wb.io.wrEnable  := false.B 
    // when(ctrl.io.memToReg){
    //   wb.io.wrEnable := true.B
    // }.otherwise {
    //   wb.io.wrEnable := false.B 
    // }

  

    /* debugging outputs */
    io.pcDebug := pc.io.pcAddr
    io.instrDebug := ins.io.instOut // mem.io.rdData //instr.io.instOut

  /* decode debug */
    io.opcodeDebug := dec.out.opcode
    io.rdDebug := dec.out.rd
    io.funct3Debug := dec.out.funct3
    io.rs1Debug := dec.out.rs1
    io.rs2Debug := dec.out.rs2
    io.funct7Debug := dec.out.funct7
    io.immDebug := imm.io.out
    
    /* alu debug */
    io.aluCmpOutDebug := alu.io.cmpOut
    io.aluOutDebug := alu.io.out

    io.pcJmpAddrDebug := (pc.io.pcAddr + imm.io.out)

    io.ctrlBranchDebug := ctrl.io.branch
   // io.regDebug := reg.io.debugOut
  
    for(i <- 0 to 31){
      io.regDebug(i) := reg.io.regDebug(i)
    }
    io.done := false.B
    when((ins.io.instOut(6,0) === OP_IE) & (reg.io.x17 === 10.U)){
      io.done := true.B
    }
    
}


object CPU extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new SingleCycleRiscV)
}

/*
00150513
feb51ee3
feb548e3
fea5c6e3
00050613
00a00893
00000073

*/