package core

import chisel3._
import chisel3.util._

class WriteBack extends Module {
    val io = IO(new Bundle {
        val wrEnable = Input(Bool())
        val memToReg = Input(Bool())
        val memData  = Input(UInt(32.W))
        val aluData  = Input(UInt(32.W))
        
        val wrBack   = Output(UInt(32.W))
        
    })

    io.wrBack := WireDefault(0.U)

    when (io.wrEnable) {
        when (io.memToReg) {
            io.wrBack := io.memData
        }.otherwise {
            io.wrBack := io.aluData
        }
    }


}