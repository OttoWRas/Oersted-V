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
        }
    }
    
    val opcode  = io.in(6,0)
    val funct3  = io.in(14,12) // for I, S and B types we need to determine MSB or zero-extends
    val immTemp = WireDefault(0.U(32.W)) // temporary immediate, this is automatically 
    io.out      := immTemp.asSInt // default assignment

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
        
    }
   
 
    
}

object ImmGen extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new ImmediateGen())
}