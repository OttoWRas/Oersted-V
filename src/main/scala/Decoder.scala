package core

import chisel3._
import chisel3.util._

object OP {
    val OP_R: UInt = 51.U(7.W)
    val OP_I: UInt = 19.U(7.W) // 'regular' I instructions
    val OP_IL: UInt = 3.U(7.W) // type I load instructions
    val OP_IE: UInt = 115.U(7.W) // type I environment call instructions 
    val OP_S: UInt = 32.U(7.W)
    val OP_B: UInt = 99.U(7.W) 
    val OP_JAL: UInt = 111.U(7.W) // J type
    val OP_JALR: UInt = 103.U(7.W) // I type
    val OP_LUI: UInt = 55.U(7.W) // U type
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
    val imm     = Output(SInt(12.W))
    val rs1     = Output(UInt(5.W))
    val funct3  = Output(UInt(3.W))
    val rd      = Output(UInt(5.W))
    val opcode  = Output(UInt(7.W))
}

class UType extends Bundle {
    val imm31to12 = Output(SInt(20.W))
    val rd = Output(UInt(5.W))
    val opcode = Output(UInt(7.W))
}



class SBType extends Bundle {
    val imm12 = SInt(1.W)
    val imm10to5 = SInt(6.W)
    val rs2 = UInt(5.W)
    val rs1 = UInt(5.W)
    val funct3 = UInt(3.W)
    val imm4to1 = SInt(5.W)
    val imm11 = SInt(1.W)
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
    val decoded = IO(Output(new DecodeOut))

    val opcode = io.in(6,0)
    
    decoded.opcode   := opcode 

    decoded.rd       := WireDefault(0.U)
    decoded.funct3   := WireDefault(0.U)
    decoded.rs1      := WireDefault(0.U)
    decoded.rs2      := WireDefault(0.U)
    decoded.funct7   := WireDefault(0.U)
    decoded.imm      := WireDefault(0.S)
    io.aluOp         := WireDefault(0.U)


    switch(opcode) {
        is(OP.OP_R){
            val R = io.in.asTypeOf(new RType)

            decoded.rd := R.rd
            decoded.funct3 := R.funct3
            decoded.rs1 := R.rs1
            decoded.rs2 := R.rs2
            decoded.funct7 := R.funct7
            
            switch(R.funct3){
                is(0.U) {
                    
                    when(R.funct7 === 0.U){
                        io.aluOp := ALU_ADD
                    }.elsewhen(R.funct7 === 2.U){
                        io.aluOp := ALU_SUB
                    }
                }
                is(4.U){
                    io.aluOp := ALU_XOR
                }
                is(6.U){
                    io.aluOp := ALU_OR
                }
                is(7.U) {
                    io.aluOp := ALU_AND
                }
                is(1.U){
                    io.aluOp := ALU_SLL
                }
                is(5.U){
                    when(R.funct7 === 0.U){
                        io.aluOp := ALU_SRL
                    }.elsewhen(R.funct7 === 2.U){
                        io.aluOp := ALU_SRA
                    }
                   
                }
                is(2.U){
                    io.aluOp := ALU_SLT
                }
                is(3.U){
                    io.aluOp := ALU_SLTU
                }
            }

        }
        is (OP.OP_I){
            val I = io.in.asTypeOf(new IType)

            decoded.rd      := I.rd
            decoded.funct3  := I.funct3
            decoded.rs1     := I.rs1

            /* sign extension of immediate */
            when(I.imm(11) & true.B) { //check if sign bit is 1
                decoded.imm := I.imm | "hFFFFF000".U.asSInt // extend with 1's
                
            }.otherwise {
                decoded.imm := I.imm | "h00000000".U.asSInt // otherwise, extend with alot of 0's.. 
                
            }

            /* determine ALU operation */
            switch(I.funct3){
                is(0.U){
                    io.aluOp := ALU_ADD // should be ADDI?
                }
                is(4.U){
                    io.aluOp := ALU_XOR // should be XORI?
                }
                is(6.U){
                    io.aluOp := ALU_OR // 
                }
                is(7.U){
                    io.aluOp := ALU_AND // 
                }
                is(1.U){
                    io.aluOp := ALU_SLL
                }
                is(5.U){
                    when(I.imm(11,5) === 0.U){
                        io.aluOp := ALU_SRL
                    }.elsewhen(I.imm(11,5) === 2.U){
                        io.aluOp := ALU_SRA
                    }
                }
                is(2.U){
                    io.aluOp := ALU_SLT
                }
                is(3.U){
                    io.aluOp := ALU_SLTU
                }
            }
        }
       
        // U type
        is(OP_LUI, OP_AUIPC){
            val U = io.in.asTypeOf(new UType)
            
            decoded.rd := U.rd
            decoded.imm := U.imm31to12 
        }

         /*
        is(OP.OP_B, OP.OP_S){
           val SB = in.asTypeOf(new SBType)
            
            decoded.funct3  := SB.funct3
            decoded.rs1     := SB.rs1
            decoded.rs2     := SB.rs2
            decoded.imm     := SB.imm12 ## SB.imm11 ## SB.imm10to5 ## SB.imm4to1 ## 0.U(1.W) // combining immediates for both S and B type

        }*/
        // J type (WIP)
        is(OP_JAL){
            val J = io.in.asTypeOf(new JType)
            
            decoded.rd := J.rd
            decoded.imm := (J.imm20 ## J.imm19to12 ## J.imm11 ## J.imm10to1).asSInt
        }
       
    }
}