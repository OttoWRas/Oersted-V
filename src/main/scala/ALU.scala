package core

import chisel3._
import chisel3.util._

object ALU {  
    def ALU_ADD:    UInt = 0.U(4.W)
    def ALU_SLL:    UInt = 1.U(4.W)
    def ALU_SEQ:    UInt = 2.U(4.W)
    def ALU_SNE:    UInt = 3.U(4.W)
    def ALU_XOR:    UInt = 4.U(4.W)
    def ALU_SRL:    UInt = 5.U(4.W)
    def ALU_OR:     UInt = 6.U(4.W)
    def ALU_AND:    UInt = 7.U(4.W)
    def ALU_COPY1:  UInt = 8.U(4.W)
    def ALU_COPY2:  UInt = 9.U(4.W)
    def ALU_SUB:    UInt = 10.U(4.W)
    def ALU_SRA:    UInt = 11.U(4.W)
    def ALU_SLT:    UInt = 12.U(4.W)
    def ALU_SGE:    UInt = 13.U(4.W)
    def ALU_SLTU:   UInt = 14.U(4.W)
    def ALU_SGEU:   UInt = 15.U(4.W)

    def isSub(op: UInt) : Bool = op(3)
    def isCmp(op: UInt) : Bool = op >= ALU_SLT
    def isCmpU(op: UInt) : Bool = op >= ALU_SLTU
    def isCmpI(op: UInt) : Bool = op(0)
    def isCmpEq(op: UInt) : Bool = !op(3)
}

import ALU._

class ALU extends Module {
    val io = IO {
        new Bundle {
            val opcode : UInt = Input(UInt(4.W))
            val data1 : UInt = Input(UInt(32.W))
            val data2 : UInt = Input(UInt(32.W))

            val    out : UInt = Output(UInt(32.W))
            val cmpOut : UInt = Output(Bool())
        }
    }

    val dp2Inv: UInt = Mux(isSub(io.opcode), ~io.data2, io.data2).asUInt()
    val sum: UInt = io.data1 + dp2Inv + isSub(io.opcode)

    val shamt: UInt = io.data2(4,0).asUInt
    val shin: UInt = Mux(io.opcode === ALU_SRA || io.opcode === ALU_SRL, io.data1, Reverse(io.data1))
    val shiftR : UInt = (Cat(isSub(io.opcode) & shin(31), shin).asSInt >> shamt)(31, 0)
    val shiftL : UInt = Reverse(shiftR)

    val slt: Bool = Mux(io.data1(31) === io.data2(31), sum(31), Mux(isCmpU(io.opcode), io.data2(31), io.data1(31)))
    val cmp: Bool = isCmpI(io.opcode) ^ Mux(isCmpEq(io.opcode), (io.data1 ^ io.data2) === 0.U, slt)

    val wOut = WireDefault(0.U)

    when (io.opcode === ALU_ADD || io.opcode === ALU_SUB) { wOut := sum }
    when (io.opcode === ALU_SLT || io.opcode === ALU_SLTU) { wOut := cmp }
    when (io.opcode === ALU_SRA || io.opcode === ALU_SRL) { wOut := shiftR }
    when (io.opcode === ALU_SLL) { wOut := shiftL }
    when (io.opcode === ALU_AND) { wOut := io.data1 & io.data2 }
    when (io.opcode === ALU_OR) { wOut := io.data1 | io.data2 }
    when (io.opcode === ALU_XOR) { wOut := io.data1 ^ io.data2 }
    when (io.opcode === ALU_COPY1) { wOut := io.data1 }
    when (io.opcode === ALU_COPY2) { wOut := io.data2 }

    io.out := wOut
    io.cmpOut := cmp
}

object ALUGen extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new ALU())
}