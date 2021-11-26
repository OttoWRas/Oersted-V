package core

import chisel3._
import chisel3.util._

import OP._
import ALU._

/*
    1) takes in instruction
    2) extract opcode and decide what type of instruction it is 
    3) handle immediate according to funct3, funct7, opcode depending on the type of instruction
    4) output signed 32 bit immediate
*/

class ImmediateGen extends Module {
    val io = IO {
        new Bundle {
            val in   = Input(UInt(32.W))
            val out  = Output(SInt(32.W))   
            val imm12     = Output(UInt(1.W))
            val imm10to5  = Output(UInt(6.W))
            val imm4to1     = Output(UInt(4.W))
            val imm11 = Output(UInt(1.W))
        }
    }
    
    val opcode  = io.in(6,0)
    val funct3  = io.in(14,12) // for I, S and B types we need to determine MSB or zero-extends
    val immTemp = WireDefault(0.U(32.W)) // temporary immediate, this is automatically 
    io.out      := immTemp.asSInt // default assignment
    io.imm12    := WireDefault(0.U(1.W))
    io.imm10to5 := WireDefault(0.U(6.W))
    io.imm4to1 := WireDefault(0.U(4.W))
    io.imm11 := WireDefault(0.U(1.W))


    switch(opcode){
        
        is(OP_I){ 
            val imm11to0 = io.in(31,20)
            immTemp := imm11to0
           
            when((imm11to0(11) & true.B) & (funct3 =/= 3.U)){ // & funct3 =/= 3.U
                immTemp := imm11to0 | "hFFFFF000".U
            }
        
            io.out := immTemp.asSInt
        }
        is(OP_IL){ // i type LOAD, where we have to zero extend LBU and LHU
              

            val imm11to0 = io.in(31,20)
            immTemp := imm11to0
           
            when((imm11to0(11) & true.B) & (funct3 =/= 4.U) & (funct3 =/= 5.U)){ // & funct3 =/= 3.U
                immTemp := imm11to0 | "hFFFFF000".U
            }
        
            io.out := immTemp.asSInt
        }
        /* in case of S type we need to be a bit more delicate as the immediates are spread out in the instruction */
        is(OP_S){
            val imm11to5 = io.in(31,25)
            val imm4to0 = io.in(11,7)
            immTemp := imm11to5 ## imm4to0 
            when(imm11to5(6) & true.B){
                immTemp := (imm11to5 ## imm4to0) | "hFFFFF000".U 
            }

            io.out := immTemp.asSInt
        }
            /* zero extend in case of funct3 == 6 | funct3 == 7 */
        is(OP_B){
            val imm12    = io.in(31)
            val imm10to5 = io.in(30,25)
            val imm4to1  = io.in(11,8)
            val imm11    = io.in(7)

            /* notice the extra 0 added as LSB. branch instructions will only branch to multiples of 16 bits, ie. no uneven numbers */
            immTemp := imm12 ## imm11 ## imm10to5 ## imm4to1 ## 0.U(1.W) // ## 0.U(1.W) 
            when((imm12 & true.B) ){ // & (funct3 =/= 6.U) & (funct3 =/= 7.U)
                immTemp := (imm12 ## imm11 ## imm10to5 ## imm4to1 ## 0.U(1.W)) | "hFFFFF000".U 
            }

            io.imm12 := imm12
            io.imm10to5 := imm10to5
            io.imm4to1 := imm4to1
            io.imm11 := imm11

            io.out := immTemp.asSInt
        }
        
        /* U type */
        is(OP_LUI, OP_AUIPC){ //, OP_AUIPC
            val imm = io.in(31,12)
            immTemp := imm
            when(imm(19) & true.B){
                immTemp := imm | "hFFF00000".U 
            }

            io.out := immTemp.asSInt
        }
        
        /* J type */
        is(OP_JAL) {
            val imm20     = io.in(31)
            val imm10to1  = io.in(30,21)
            val imm11     = io.in(20)
            val imm19to12 = io.in(19, 12)

            immTemp := imm20 ## imm19to12 ## imm11 ## imm10to1 ## 0.U(1.W)
            when(imm20 & true.B){
                immTemp := (imm20 ## imm19to12 ## imm11 ## imm10to1 ## 0.U(1.W)) | "hFFF00000".U 
            }

            io.out := immTemp.asSInt
        }

    }

    
}

object ImmGen extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new ImmediateGen())
}