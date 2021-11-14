package consts

import chisel3._
import chisel3.util._
/* 
these should actually all be val's, since def's are functions that can/will be evaluated
*/
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
}

/* the numbers correspond to the opcodes matching the type of instruction */
object OP {
    val OP_R: UInt = 51.U(7.W)
    val OP_I: UInt = 19.U(7.W) // 'regular' I instructions
    val OP_IL: UInt = 3.U(7.W) // type I load instructions
    val OP_IE: UInt = 115.U(7.W) // type I environment call instructions 
    val OP_S: UInt = 32.U(7.W)
    val OP_B: UInt = 99.U(7.W)
    val OP_JAL: UInt = 111.U(7.W)
    val OP_JALR: UInt = 103.U(7.W)
    val OP_LUI: UInt = 55.U(7.W)
    val OP_AUIPC: UInt = "b0010111".U(7.W) 
}
/*
object Decoder {
    val DEC_R:      UInt = 0.U(3.W)
    val DEC_I:      UInt = 1.U(3.W)
    val DEC_S:      UInt = 2.U(3.W)
    val DEC_B:      UInt = 3.U(3.W)
    val DEC_U:      UInt = 4.U(3.W)
    val DEC_J:      UInt = 5.U(3.W)
}
*/