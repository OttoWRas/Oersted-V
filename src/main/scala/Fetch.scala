package core

import chisel3._
import chisel3.util._

class ProgramCounter extends Module {
    val io = IO(new Bundle {
            val pcPlus   = Input(Bool())
            val jmpAddr  = Input(SInt(32.W)) // x
            val wrEnable = Input(Bool())
            val flagIn   = Input(Bool())

            val flagOut  = Output(Bool()) // x
            val pcAddr   = Output(SInt(32.W))
    })

    val pc = RegInit(0.S(32.W))
    val flag = RegInit(true.B)

    when (io.wrEnable) {
        pc := io.jmpAddr
        flag := true.B
    }

    when (io.pcPlus) {
        pc := pc + 4.S
        flag := true.B 
    }

    when (io.flagIn) {
        flag := false.B
    }
    io.pcAddr := pc
    io.flagOut := flag
}

class InstBuff extends Module {
    val io = IO(new Bundle {
            val instIn   = Input(UInt(32.W))
            val flagIn   = Input(Bool())
            
            val instOut  = Output(UInt(32.W))
            val flagOut  = Output(Bool())
    })

    val flag = RegInit(false.B)

    val instructionBuffer = RegInit(0.U(32.W))
    when ((io.instIn =/= instructionBuffer) && (io.instIn =/= 0.U) && io.flagIn) {
        instructionBuffer := io.instIn
        flag := false.B
    }
    
    io.flagOut := flag
    io.instOut := instructionBuffer
}