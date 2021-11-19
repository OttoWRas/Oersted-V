package core

import chisel3._
import chisel3.util._

class Registers extends Module {
    val io = IO(new Bundle {
            val rdData1 = Output(UInt(32.W))
            val rdData2 = Output(UInt(32.W))
            val rdAddr1 = Input(UInt(32.W))
            val rdAddr2 = Input(UInt(32.W))
            val wrEnable = Input(Bool())
            val wrData = Input(UInt(5.W))
            val wrAddr = Input(UInt(5.W))
    })

    val registerFile = Reg(Vec (32, UInt (32.W)))
    when (io.wrEnable) {
        registerFile(io.wrAddr) := io.wrData
    }
    when (!io.wrEnable) {
        io.rdData1 := registerFile(io.rdAddr1)
        io.rdData2 := registerFile(io.rdAddr2)
    }
}