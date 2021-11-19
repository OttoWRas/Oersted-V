package core

import chisel3._
import chisel3.util._


class SingleCycleRiscV extends Module {
  val io = IO(new Bundle {
    val regDeb = Output(Vec(32, UInt(32.W))) // debug output for the tester
    val pc     = Output(UInt(32.W))
    val done   = Output(Bool())
  })

    val pc      = Module(new ProgramCounter)
    val instr   = Module(new InstBuff)
    val reg     = Module(new Registers)
    val alu     = Module(new ALU)
    val decode  = Module(new Decoder)
    val mem     = Module(new Memory("./testData/instructions.hex.txt"))
    val wb      = Module(new WriteBack)

    instr.io.instIn := mem.io.rdData
    pc.io.flagIn    := instr.io.flagOut
    instr.io.flagIn := pc.io.flagOut
  
    mem.io.wrEnable := io.wrEnableMem
    mem.io.wrData   := io.wrData
    mem.io.wrAddr   := io.wrAddr

    decode.io.in    := instr.io.instOut
    alu.io.opcode   := decode.io.aluOp

    wb.io.memData   := mem.io.wrData 
    wb.io.aluData   := alu.io.out
    reg.io.wrData   := wb.io.wrBack
    reg.io.wrAddr   := decode.decoded.rd

    reg.io.rdAddr1   := decode.decoded.rs1
    reg.io.rdAddr2   := decode.decoded.rs2

    alu.io.data1    := reg.io.rdData1
    alu.io.data2    := reg.io.rdData2 // needs immediate handling

    when(decoded.imm) {

    }

    when (io.fetch) {
        mem.io.rdAddr := pc.io.pcAddr
    } .otherwise {
        mem.io.rdAddr := alu.io.out
    }
  }
}

object CPU extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new SingleCycleRiscV)
}

