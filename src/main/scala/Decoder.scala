package core

import chisel3._
import chisel3.util._

object OP {
    val OP_R:     UInt = 51.U(7.W)
    val OP_I:     UInt = 19.U(7.W) // 'regular' I instructions
    val OP_IL:    UInt = 3.U(7.W) // type I load instructions
    val OP_IE:    UInt = 115.U(7.W) // type I environment call instructions 
    val OP_S:     UInt = 32.U(7.W)
    val OP_B:     UInt = 99.U(7.W) 
    val OP_JAL:   UInt = 111.U(7.W) // J type
    val OP_JALR:  UInt = 103.U(7.W) // I type
    val OP_LUI:   UInt = 55.U(7.W) // U type
    val OP_AUIPC: UInt = "b0010111".U(7.W) // U type 
}

class DecodeOut extends Bundle {
    val opcode      = Output(UInt(7.W))
    val rd          = Output(UInt(5.W))
    val funct3      = Output(UInt(3.W))
    val rs1         = Output(UInt(5.W))
    val rs2         = Output(UInt(5.W))
    val funct7      = Output(UInt(7.W))
    val imm         = Output(SInt(32.W))
}

class RType extends Bundle {
    val funct7      = Output(UInt(7.W))
    val rs2         = Output(UInt(5.W))
    val rs1         = Output(UInt(5.W))
    val funct3      = Output(UInt(3.W))
    val rd          = Output(UInt(5.W))
    val opcode      = Output(UInt(7.W))
}

class IType extends Bundle {
    val imm11to0    = Output(UInt(12.W))
    val rs1         = Output(UInt(5.W))
    val funct3      = Output(UInt(3.W))
    val rd          = Output(UInt(5.W))
    val opcode      = Output(UInt(7.W))
}

class UType extends Bundle {
    val imm31to12   = Output(UInt(20.W))
    val rd          = Output(UInt(5.W))
    val opcode      = Output(UInt(7.W))
}


class BType extends Bundle { // skal være UInts (Bits)
    val imm12 = UInt(1.W)
    val imm10to5 = UInt(6.W)
    val rs2 = UInt(5.W)
    val rs1 = UInt(5.W)
    val funct3 = UInt(3.W)
    val imm4to1 = UInt(4.W)
    val imm11 = UInt(1.W)
    val opcode = UInt(7.W)
}
class SType extends Bundle { // skal være UInts (Bits)
    val imm11to5 = UInt(7.W)
    val rs2 = UInt(5.W)
    val rs1 = UInt(5.W)
    val funct3 = UInt(3.W)
    val imm4to0 = UInt(5.W)
    val opcode = UInt(7.W)
}

class JType extends Bundle {
    val imm20 = Output(UInt(1.W))
    val imm10to1 = Output(UInt(10.W))
    val imm11 = Output(UInt(1.W))
    val imm19to12 = Output(UInt(8.W))

    val rd = Output(UInt(5.W))
    val opcode = Output(UInt(7.W))
}

import OP._
import ALU._
class Decoder extends MultiIOModule {
    val io = IO(new Bundle {
        val in = Input(UInt(32.W))
        val aluOp = Output(UInt(4.W))
    })
    val out = IO(Output(new DecodeOut))

    val opcode = io.in(6,0)
    
    out.opcode   := opcode 

    out.rd       := WireDefault(0.U)
    out.funct3   := WireDefault(0.U)
    out.rs1      := WireDefault(0.U)
    out.rs2      := WireDefault(0.U)
    out.funct7   := WireDefault(0.U)
    out.imm      := WireDefault(0.S)
    io.aluOp     := WireDefault(0.U)


    switch(opcode) {
        is(OP.OP_B){
            val B = io.in.asTypeOf(new BType)

            out.funct3 := B.funct3
            out.rs2 := B.rs2
            out.rs1 := B.rs1

            /* equality alu op*/
            switch(B.funct3){
                is(0.U) { io.aluOp := ALU_SEQ }
                is(1.U) { io.aluOp := ALU_SNE }
                is(4.U) { io.aluOp := ALU_SLT }
                is(5.U) { io.aluOp := ALU_SGE }
                is(6.U) { io.aluOp := ALU_SLTU }
                is(7.U) { io.aluOp := ALU_SGEU }
                
            }
        }
        is(OP.OP_R){
            val R = io.in.asTypeOf(new RType)

            out.rd := R.rd
            out.funct3 := R.funct3
            out.rs1 := R.rs1
            out.rs2 := R.rs2
            out.funct7 := R.funct7
            
            switch(R.funct3){
                is(0.U) {
                    io.aluOp := ALU_ADD // funct7 === 0
        
                    when(R.funct7 === 32.U){
                        io.aluOp := ALU_SUB
                    }
                }
                is(4.U){ io.aluOp := ALU_XOR }
                is(6.U){ io.aluOp := ALU_OR }
                is(7.U){ io.aluOp := ALU_AND }
                is(1.U){ io.aluOp := ALU_SLL }
                is(5.U){
                    io.aluOp := ALU_SRL // funct7 === 0
                    
                    when(R.funct7 === 32.U){
                        io.aluOp := ALU_SRA
                    }
                }
                is(2.U){ io.aluOp := ALU_SLT }
                is(3.U){ io.aluOp := ALU_SLTU }
            }

        }
        is (OP.OP_I, OP_IL, OP_IE, OP_JALR){
            val I = io.in.asTypeOf(new IType)

            out.rd      := I.rd
            out.funct3  := I.funct3
            out.rs1     := I.rs1
            val immTemp = Wire(UInt(32.W))
            immTemp     := I.imm11to0
        
            /* sign extension of immediate */
            when(I.imm11to0(11) & true.B) { //check if sign bit is 1
                immTemp := I.imm11to0 | "hFFFFF000".U // extend with 1's
            }
            out.imm := immTemp.asSInt

            /* determine ALU operation */
            switch(I.funct3){
                is(0.U){ io.aluOp := ALU_ADD }
                is(4.U){ io.aluOp := ALU_XOR }
                is(6.U){ io.aluOp := ALU_OR  }
                is(7.U){ io.aluOp := ALU_AND }
                is(1.U){ io.aluOp := ALU_SLL }
                is(5.U){
                    io.aluOp := ALU_SRA //5
                    when(I.imm11to0(11,5) === 0.U){
                        io.aluOp := ALU_SRL //11
                    }
                }
                is(2.U){ io.aluOp := ALU_SLT }
                is(3.U){ io.aluOp := ALU_SLTU }
            }
        }
       
        // U type
        is(OP_LUI, OP_AUIPC){
            val U = io.in.asTypeOf(new UType)
            
            out.rd := U.rd
            out.imm := U.imm31to12.asSInt
        }

        is(OP.OP_S){
           val S = io.in.asTypeOf(new SType)
            
            out.funct3  := S.funct3
            out.rs1     := S.rs1
            out.rs2     := S.rs2

            val immTemp = Wire(UInt(32.W))
            immTemp := S.imm11to5 ## S.imm4to0 // ## 0.U(1.W)

            /* sign extension of immediate */
            when(S.imm11to5(6) & true.B) { //check if sign bit is 1
                immTemp := (S.imm11to5 ## S.imm4to0) | "hFFFFF000".U // extend with 1's //  ## 0.U(1.W
            }
              out.imm := immTemp.asSInt


         
        }
        // J type (WIP)
        is(OP_JAL){
            val J = io.in.asTypeOf(new JType)
            
            out.rd := J.rd
        }
       
    }
}