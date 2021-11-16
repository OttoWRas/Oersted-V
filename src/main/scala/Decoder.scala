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
    val OP_JAL: UInt = 111.U(7.W)
    val OP_JALR: UInt = 103.U(7.W)
    val OP_LUI: UInt = 55.U(7.W)
    val OP_AUIPC: UInt = "b0010111".U(7.W) 
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

class UJType extends Bundle {
    val imm31to12 = Output(SInt(20.W))
    val imm20 = Output(SInt(1.W))
    val imm10to1 = Output(SInt(10.W))
    val imm11 = Output(SInt(1.W))
    val imm19to12 = Output(SInt(8.W))

    val rd = Output(UInt(5.W))
    val opcode = Output(UInt(7.W))
}


class Decoder extends MultiIOModule {
    val in = IO(Input(UInt(32.W)))
    val decoded = IO(Output(new DecodeOut))
    val opcode = in(6,0)
    // this feels like it could be smarter? if no default values are given it won't compile
    decoded.opcode   := opcode // same for all instr

    decoded.rd       := WireDefault(0.U)
    decoded.funct3   := WireDefault(0.U)
    decoded.rs1      := WireDefault(0.U)
    decoded.rs2      := WireDefault(0.U)
    decoded.funct7   := WireDefault(0.U)
    decoded.imm      := WireDefault(0.S)

    switch(opcode) {
        is(OP.OP_R){
            val R = in.asTypeOf(new RType)

            decoded.rd := R.rd
            decoded.funct3 := R.funct3
            decoded.rs1 := R.rs1
            decoded.rs2 := R.rs2
            decoded.funct7 := R.funct7
            
        }
        is (OP.OP_I){
            val I = in.asTypeOf(new IType)

            decoded.rd      := I.rd
            decoded.funct3  := I.funct3
            decoded.rs1     := I.rs1
           
            /* sign extension for immediate ? */
            //decoded.imm := I.imm 
            
            when(I.imm(11) & true.B) { //( check if sign bit is 1)
                decoded.imm := I.imm | "hFFFFF000".U.asSInt // extend with 1's
            }.otherwise {
                decoded.imm := I.imm | "h00000000".U.asSInt // otherwise, extend with alot of 0's.. 
            }

            decoded.funct7  := 0.U
            decoded.rs2     := 0.U
        }
        /*
        is(OP.OP_B, OP.OP_S){
           val SB = in.asTypeOf(new SBType)
            
            decoded.funct3  := SB.funct3
            decoded.rs1     := SB.rs1
            decoded.rs2     := SB.rs2
            decoded.imm     := SB.imm12 ## SB.imm11 ## SB.imm10to5 ## SB.imm4to1 ## 0.U(1.W) // combining immediates for both S and B type

        }*/

        is(OP.OP_LUI, OP.OP_AUIPC, OP.OP_JAL){
            val UJ = in.asTypeOf(new UJType)

            decoded.rd := UJ.rd
            decoded.imm := UJ.imm31to12 
        }
     
    }
}
  /*


31:20 for load (type I)
31:25 and 11:17 for store (type S) (6 + 6)
31, 7, 30:25 and 11:8 for conditional (type B) (1 + 1 + 6 + 4 = 12 bits)

opcodes can be used to figure out what to sign extend

opcode bit 6: 0 for data transfer (load/store)
opcode bit 6: 1 for conditional branches
opcode bit 5: 0 for load
opcode bit 5: 1 for store





Elaboration: The immediate generation logic must choose between 
sign-extending
a 12-bit field in instruction bits 31:20 for load instructions, 
bits 31:25 and 11:7 for store instructions, 

or bits 31, 7, 30:25, and 11:8 for the conditional branch. Since
the input is all 32 bits of the instruction, it can use the opcode bits of the instruction
to select the proper field. RISC-V opcode bit 6 happens to be 0 for data transfer
instructions and 1 for conditional branches, and RISC-V opcode bit 5 happens to be 0
for load instructions and 1 for store instructions. Thus, bits 5 and 6 can control a 3:1
multiplexor inside the immediate generation logic that selects the appropriate 12-bit
field for load, store, and conditional branch instructions.




val asSB = in.asTypeOf(new SBType)
val SBImm = asSB.imm12 ## asSB.imm11 ## asSB.imm10to5 ## asSB.imm4to1 ## 0.U(1.W) // ## = concat


    val io = IO(new Bundle {
        val instruction = Input(UInt(32.W))
        val opcode      = Output(UInt(7.W))
        val rd          = Output(UInt(5.W))
        val funct3      = Output(UInt(3.W))
        val rs1         = Output(UInt(5.W))
        val rs2         = Output(UInt(5.W))
        val funct7      = Output(UInt(7.W))
        val imm         = Output(UInt(32.W)) // for now 32 bit. needs to sign extended!!
        // val done        = Output(Bool()) // some kind of valid--ready ?
    })
   */

    /* default values */
    /*io.opcode   := io.instruction(6,0)
    io.rd       := 0.U
    io.funct3   := 0.U
    io.rs1      := 0.U
    io.rs2      := 0.U
    io.funct7   := 0.U
    io.imm      := 0.U

   switch(io.opcode){
        is(OP.OP_R){ // I can't manage to get simply OP_R to work?
            io.rd       := io.instruction(11,7) 
            io.funct3   := io.instruction(14,12)
            io.rs1      := io.instruction(19,15)
            io.rs2      := io.instruction(24,20)
            io.funct7   := io.instruction(31,25)
       
        }
        is(OP.OP_I){ // I
            io.rd       := io.instruction(11,7) 
            io.funct3   := io.instruction(14,12)
            io.rs1      := io.instruction(19,15)
            io.imm      := io.instruction(31,20)
        
        }
    }*/
    
