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

    //val pcJmpAddrDebug = Output(SInt(32.W))

    //val ctrlBranchDebug = Output(Bool())
    val done              = Output(Bool())
  })
    //val decDebug = IO(Output(new DecodeOut))

    val mem    = Module(new Memory(program))
    val dec    = Module(new Decoder)
    val alu    = Module(new ALU)
    val imm    = Module(new ImmediateGen)
    val reg    = Module(new Registers)


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

    //io.pcJmpAddrDebug := 0.U
    io.done := false.B

    for(i <- 0 to 31){
      io.regDebug(i) := reg.io.regDebug(i)
    }
    
    mem.io.rdAddr   := 0.U
    mem.io.wrAddr   := 2.U
    mem.io.wrData   := 0.U
    mem.io.wrEnable := 0.U
    

    reg.io.wrEnable := false.B
    reg.io.rdAddr3  := 0.U


    val stop     = WireDefault(false.B)
    val hazard   = WireDefault(false.B)
    val branch   = WireDefault(false.B)

    val hBuff    = Reg(UInt(32.W))
    val hFlag  = Reg(Bool())
    val sFlag  = Reg(Bool())

    hFlag := false.B
    when (hazard) {
      hFlag := true.B
      when(~hFlag) {
        hBuff := mem.io.rdData
      }
    }

    sFlag := false.B
    when (stop) {
      sFlag := true.B
      hBuff := mem.io.rdData
    }
    
    val pc  = Reg(UInt(32.W))
    val pcL = Reg(UInt(32.W))
    
    when (~branch && ~stop && ~hazard) {
      pcL := pc
    }

    when (~stop && ~branch) {
      mem.io.rdAddr := pc/4.U
    }
    
    // MEM -> INSTBUFF STAGE
    val instBuff = Reg(UInt(32.W))
    val pcpBuff  = Reg(UInt(32.W))

    when (~stop && ~hazard) {
      when(hFlag || sFlag) {
        instBuff := hBuff
      }.otherwise{
        instBuff := mem.io.rdData
      }

      pcpBuff  := pcL
      
      when(~branch && ~stop) {
        pc := pc + 4.U
      }
    }

    when (branch) {
      instBuff := 0.U
      pcpBuff  := 0.U
    }

    dec.io.in := instBuff
    imm.io.in := instBuff
    
    // INSTBUFF -> DECODE STAGE
    val decBuff    = Reg(Output(new DecodeOut))
    val pcpBuffDec = Reg(UInt(32.W))
    val immBuff    = Reg(UInt(32.W))
    val iopBuff    = Reg(UInt(4.W))

    when(~stop && ~hazard) {
      decBuff    := dec.out // does this actually work? Check GTKWave.. 
      pcpBuffDec := pcpBuff
      iopBuff    := dec.io.aluOp
      immBuff    := imm.io.out.asUInt
    }

    when (branch) {
      decBuff    := 0.U.asTypeOf(new DecodeOut)
      pcpBuffDec := 0.U
      iopBuff    := 0.U
      immBuff    := 0.U
    }
    
    when(hazard) {
      decBuff    := 0.U.asTypeOf(new DecodeOut)
      pcpBuffDec := 0.U
      iopBuff    := 0.U
      immBuff    := 0.U
    }

    reg.io.rdAddr1 := decBuff.rs1
    reg.io.rdAddr2 := decBuff.rs2
    alu.io.data1   := reg.io.rdData1
    alu.io.opcode  := iopBuff

    when(immBuff =/= 0.U && decBuff.opcode =/= OP_B) { // this should be ctrl.io.ALUSrc.. seems dangerous to rely on this i think.. it is let me fix later
      alu.io.data2 := immBuff
    }.otherwise {
      alu.io.data2 := reg.io.rdData2 
    }

    // DECODE -> ALU STAGE
    val aluBuff    = Reg(UInt(32.W))
    val cmpBuff    = Reg(Bool())
    val pcpBuffAlu = Reg(UInt(32.W))
    val decBuffAlu = Reg(Output(new DecodeOut))
    val immBuffAlu = Reg(UInt(32.W))

    when (~stop) {
      aluBuff := alu.io.out
      cmpBuff := alu.io.cmpOut
      immBuffAlu := immBuff
      pcpBuffAlu := pcpBuffDec
      decBuffAlu := decBuff
    }

    when (branch) {
      aluBuff    := 0.U
      cmpBuff    := 0.U
      immBuffAlu := 0.U
      pcpBuffAlu := 0.U
      decBuffAlu := 0.U.asTypeOf(new DecodeOut)
    }

    when(((decBuffAlu.opcode === OP_B && cmpBuff) || decBuffAlu.opcode === OP_JAL || decBuffAlu.opcode === OP_JALR)) {
      branch        := true.B
      pc            := pcpBuffAlu + immBuffAlu + 4.U
      pcL           := pcpBuffAlu + immBuffAlu

      when(decBuffAlu.opcode =/= OP_JALR) {
        mem.io.rdAddr := (pcpBuffAlu + immBuffAlu)/4.U
      }.otherwise {
        reg.io.rdAddr3 := decBuffAlu.rs1 
        mem.io.rdAddr  := (pcpBuffAlu + immBuffAlu + reg.io.rdData3)/4.U
      }
    }

    when((decBuffAlu.opcode === OP_IL || decBuffAlu.opcode === OP_S) && ~sFlag) { // "b0000011".U - "b0100011".U
      stop := true.B
      when(decBuffAlu.opcode === OP_IL) {
        mem.io.rdAddr := aluBuff

        switch(decBuffAlu.funct3) {
          is(0.U) {mem.io.wrData   := reg.io.rdData3(7, 0)}
          is(2.U) {mem.io.wrData   := reg.io.rdData3}
        }
      }

      when(decBuffAlu.opcode === OP_S) { //"b0100011".U
        mem.io.wrEnable := true.B
        mem.io.wrAddr   := aluBuff
        reg.io.rdAddr3  := decBuffAlu.rd
        
        switch(decBuffAlu.funct3) {
          is(0.U) {mem.io.wrData   := reg.io.rdData3(7,0)}
          is(2.U) {mem.io.wrData   := reg.io.rdData3}
          is(4.U) {mem.io.wrData   := reg.io.rdData3(7,0)} //needs sign extend
        }
      }
    }

    // WB STAGE
    reg.io.wrAddr := 0.U
    reg.io.wrData := 0.U
    
    val aluBuffMem = Reg(UInt(32.W))
    val pcpBuffMem = Reg(UInt(32.W))
    val decBuffMem = Reg(Output(new DecodeOut))
    val memBuff    = Reg(UInt(32.W))

    when(~stop) {
      aluBuffMem := aluBuff
      pcpBuffMem := pcpBuffAlu
      decBuffMem := decBuffAlu
      memBuff    := mem.io.rdData 
    }

    when (~stop) {
      when(decBuffMem.opcode === OP_IL) {
        reg.io.wrEnable := true.B
        reg.io.wrAddr   := decBuffMem.rd
        reg.io.wrData   := memBuff
      }.elsewhen(decBuffMem.opcode === OP_S) {
        reg.io.wrEnable := false.B
      }.elsewhen(decBuffMem.opcode === OP_JAL || decBuffMem.opcode === OP_JALR) {
        reg.io.wrEnable := true.B
        reg.io.wrAddr   := decBuffMem.rd
        reg.io.wrData   := pcpBuffMem + 4.U
      }.elsewhen(decBuffMem.opcode =/= OP_B) {
        reg.io.wrEnable := true.B
        reg.io.wrAddr   := decBuffMem.rd
        reg.io.wrData   := aluBuffMem
      }
    }

    when(((dec.out.rs1 === decBuff.rd || dec.out.rs1 === decBuffAlu.rd || dec.out.rs1 === decBuffMem.rd) && dec.out.rs1 =/= 0.U) 
    || ((dec.out.rs2 === decBuff.rd || dec.out.rs2 === decBuffAlu.rd || dec.out.rs2 === decBuffMem.rd) && dec.out.rs2 =/= 0.U)) {
        hazard := true.B
    }

      /* intermidiate debug*/
    io.decBuffD := decBuff.rd
    io.immBuffD := immBuff
    io.aluBuffD := aluBuff
    
    io.opcBuffD := decBuff.opcode
    io.rs1Debug := dec.out.rs1
    io.rs2Debug := dec.out.rs2

    io.memBuffD    := 0.U   
    io.memAluBuffD := cmpBuff

    io.wbMemBuffD  := 0.U
    io.wbAluBuffD  := decBuff.opcode
    io.wbOpcBuffD  := 0.U
    io.hazardD     := branch 
    io.pcDebug     := 0.U
    io.instrDebug := 0.U // mem.io.rdData //instr.io.instOut
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