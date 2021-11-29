package core

import chisel3._
import chisel3.util._
import OP._

class SingleCycleRiscV(program: String = "") extends Module {
  val io = IO(new Bundle {
    val regDebug    = Output(Vec(32, UInt(32.W))) // debug output for the tester
    val pcDebug     = Output(UInt(32.W))
    val instrDebug  = Output(UInt(32.W))
    
    /* decoder debug */
    val opcodeDebug      = Output(UInt(7.W))
    val rdDebug          = Output(UInt(5.W))
    val funct3Debug      = Output(UInt(3.W))
    val rs1Debug         = Output(UInt(5.W))
    val rs2Debug         = Output(UInt(5.W))
    val rd1Debug         = Output(UInt(32.W))
    val rd2Debug         = Output(UInt(32.W))
  // val aLUSrcDebug      = Output(Bool())
    val funct7Debug      = Output(UInt(7.W))
    val immDebug         = Output(SInt(32.W))

  /* intermidiate debug*/
    val hazardD          = Output(Bool())

    val decBuffD         = Output(UInt(32.W))
    val immBuffD         = Output(UInt(32.W))
    val aluBuffD         = Output(UInt(32.W))
    val opcBuffD         = Output(UInt(32.W))

    val memBuffD         = Output(UInt(32.W))
    val memAluBuffD      = Output(UInt(32.W))

    val wbMemBuffD       = Output(UInt(32.W))
    val wbAluBuffD       = Output(UInt(32.W))
    val wbOpcBuffD       = Output(UInt(32.W))

  /* alu debug */
    val aluOutDebug     = Output(UInt(32.W))
    val aluCmpOutDebug  = Output(Bool())

    val pcJmpAddrDebug = Output(SInt(32.W))

    //val ctrlBranchDebug = Output(Bool())
    val done              = Output(Bool())
  })
    //val decDebug = IO(Output(new DecodeOut))

    val mem    = Module(new Memory(program))
    val pc     = Module(new ProgramCounter)
    val dec    = Module(new Decoder)
    val alu    = Module(new ALU)
    val imm    = Module(new ImmediateGen)
    val reg    = Module(new Registers)
    val instBuff = Reg(UInt(32.W))


    io.rd1Debug := reg.io.rdData1
    io.rd2Debug := reg.io.rdData2

    /* debugging outputs */
 

  /* decode debug */
    io.opcodeDebug := dec.out.opcode
    io.rdDebug := dec.out.rd
    io.funct3Debug := dec.out.funct3
    io.funct7Debug := dec.out.funct7
    io.immDebug := imm.io.out
  

    /* alu debug */
    io.aluCmpOutDebug := alu.io.cmpOut
    io.aluOutDebug := alu.io.out

    io.pcJmpAddrDebug := (pc.io.pcAddr + imm.io.out)
    io.done := false.B

    for(i <- 0 to 31){
      io.regDebug(i) := reg.io.regDebug(i)
    }

    val stop     = WireDefault(false.B)
    val hazard   = WireDefault(false.B)
    val branch   = WireDefault(false.B)
    val inst     = Wire(UInt(32.W))
    val pck = RegInit(0.U(32.W))

    pc.io.wrEnable := true.B
    pc.io.flagIn := true.B
    pc.io.pcPlus := true.B
    mem.io.rdAddr := pck/4.U
    pc.io.jmpAddr := 0.S
    
    val hazardFlag = Reg(Bool())
    val pckBuff = Reg(UInt(32.W))
    val aluPckBuff = Reg(UInt(32.W))
    
    when(hazard && ~hazardFlag && ~branch) {
      pck := pck - 4.U
      //pckBuff := pckBuff + 4.U
      //aluPckBuff := aluPckBuff + 4.U
      hazardFlag := true.B
    }

    when (~stop && ~hazard) {
      instBuff := mem.io.rdData
      pc.io.pcPlus := false.B
      hazardFlag := false.B
      when (~branch) {
        //mem.io.rdAddr := alu.io.out
        pck := pck + 4.U
        pc.io.jmpAddr := mem.io.rdData.asSInt
      }.otherwise{
        //pck := pck - 4.U
      }
    }

    dec.io.in := inst
    imm.io.in := inst
    
    val decBuff = Reg(Output(new DecodeOut))
    val opBuff  = Reg(UInt(4.W))
    val immBuff = Reg(UInt(32.W))
    decBuff := 0.U.asTypeOf(new DecodeOut)

    when(~stop && ~hazard) {
      pckBuff := pck - 4.U
      decBuff := dec.out // does this actually work? Check GTKWave.. 
      opBuff  := dec.io.aluOp
      immBuff := imm.io.out.asUInt
    }.elsewhen(hazard) {
      decBuff := 0.U.asTypeOf(new DecodeOut) // does this actually work? Check GTKWave.. 
      opBuff  := 0.U
      immBuff := 0.U
    }

    when (branch) {
      decBuff := 0.U.asTypeOf(new DecodeOut)
      immBuff := 0.U
    }

    reg.io.rdAddr1 := decBuff.rs1
    reg.io.rdAddr2 := decBuff.rs2
    alu.io.data1   := reg.io.rdData1
    alu.io.opcode  := opBuff

    when(immBuff =/= 0.U && decBuff.opcode =/= OP_B) { // this should be ctrl.io.ALUSrc.. seems dangerous to rely on this i think.. 
      alu.io.data2 := immBuff
    }.otherwise {
      alu.io.data2 := reg.io.rdData2 
    }
    /*
    when (decBuff.opcode === OP_B & alu.io.cmpOut.asBool) { //"b1100011".U
        branch := true.B
    }.elsewhen ( decBuff.opcode === OP_JALR // "b1100111".U
      ||  decBuff.opcode === OP_JAL) { // "b1101111".U
        branch := true.B
    }*/

    val aluBuff = Reg(UInt(32.W))
    val cmpBuff = Reg(Bool())
    val opcBuff = Reg(UInt(20.W))
    val aluImmBuff = Reg(UInt(32.W))


    when (~stop) {
      aluBuff := alu.io.out
      cmpBuff := alu.io.cmpOut
      aluImmBuff := immBuff
      aluPckBuff := pckBuff
      opcBuff := Cat(decBuff.opcode, decBuff.funct3, decBuff.rs2, decBuff.rd)
    }

    when (branch) {
      aluBuff := 0.U
      cmpBuff := 0.U
      aluImmBuff := 0.U
      opcBuff := 0.U
    }

    val memBuff = Reg(UInt(32.W))

    mem.io.wrAddr := WireDefault(0.U)
    reg.io.wrAddr := WireDefault(0.U)
    reg.io.rdAddr3 := WireDefault(0.U)
    mem.io.wrEnable := false.B
    mem.io.wrData := WireDefault(0.U)

    when(opcBuff(19,13) === OP_B && cmpBuff) {
      branch := true.B
    }

    when(opcBuff(6,0) === OP_IL || opcBuff(6,0) === OP_S) { // "b0000011".U - "b0100011".U
      stop := true.B
      when(opcBuff(6,0) === OP_IL) {
        //mem.io.rdAddr := aluBuff/4.U
        memBuff       := mem.io.rdData

        switch(opcBuff(8, 7)) {
          is(0.U) {mem.io.wrData   := reg.io.rdData3(7, 0)}
          is(2.U) {mem.io.wrData   := reg.io.rdData3}
        }
      }

      when(opcBuff(6,0) === OP_S) { //"b0100011".U
        mem.io.wrEnable := true.B
        mem.io.wrAddr   := aluBuff
        reg.io.rdAddr3  := opcBuff(14,9)
        
        switch(opcBuff(8,7)) {
          is(0.U) {mem.io.wrData   := reg.io.rdData3(7,0)}
          is(2.U) {mem.io.wrData   := reg.io.rdData3}
          is(4.U) {mem.io.wrData   := reg.io.rdData3(7,0)} //needs sign extend
        }
      }
    }
    
    val wbMemBuff = Reg(UInt(32.W))
    val wbAluBuff = Reg(UInt(32.W))
    val wbOpcBuff = Reg(UInt(20.W))
    val wbPckBuff = Reg(UInt(32.W))

    when (~stop) {
      wbPckBuff := aluPckBuff
      wbMemBuff := memBuff
      wbAluBuff := aluBuff
      wbOpcBuff := opcBuff
    }

    reg.io.wrEnable := true.B
    reg.io.wrData   := wbAluBuff
    reg.io.wrAddr   := wbOpcBuff(4,0)
    /*when (~(wbOpcBuff(6,0) === "b0000011".U)) {
        reg.io.wrEnable := true.B
        reg.io.wrAddr   := wbOpcBuff(19,15)
      when (wbOpcBuff(6,0) === "b0100011"U) {
        reg.io.wrData   := wbMemBuff
      }.otherwise {
    }
  }*/

    when(((dec.out.rs1 === decBuff.rd || dec.out.rs1 === opcBuff(4,0) || dec.out.rs1 === wbOpcBuff(4,0)) && dec.out.rs1 =/= 0.U) 
    || ((dec.out.rs2 === decBuff.rd || dec.out.rs2 === opcBuff(4,0) || dec.out.rs2 === wbOpcBuff(4,0)) && dec.out.rs2 =/= 0.U)) {
        hazard := true.B
      }

      /* intermidiate debug*/
    io.decBuffD := decBuff.rd
    io.immBuffD := immBuff
    io.aluBuffD := aluBuff
    
    io.opcBuffD := decBuff.opcode
    io.rs1Debug := dec.out.rs1
    io.rs2Debug := dec.out.rs2

    io.memBuffD    := opcBuff(19,13)   
    io.memAluBuffD := cmpBuff

    io.wbMemBuffD  := wbOpcBuff(4,0)
    io.wbAluBuffD  := decBuff.opcode
    io.wbOpcBuffD  := wbOpcBuff(6,0)
    io.hazardD     := branch 
    io.pcDebug     := pck
    io.instrDebug := inst // mem.io.rdData //instr.io.instOut

    val branchFlag = Reg(Bool())
    val branchFlag2 = Reg(Bool())

    when(branch) {
      inst := 0.U
      mem.io.rdAddr := (wbPckBuff + aluImmBuff)
      reg.io.rdAddr3  := opcBuff(6,0)
      pck := (wbPckBuff + aluImmBuff)
      hazardFlag := false.B
      branchFlag := true.B
      branchFlag2 := true.B
    }.elsewhen(branchFlag) {
      inst := 0.U
      branchFlag := false.B
    }.elsewhen(~branchFlag && branchFlag2) {
      //pckBuff := pckBuff - 4.U
      //aluPckBuff := aluPckBuff - 4.U
      inst := 0.U
      branchFlag2 := false.B
    }.otherwise {
      inst := instBuff
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