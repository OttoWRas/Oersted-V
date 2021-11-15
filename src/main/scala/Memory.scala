package core

import chisel3._
import chisel3.util._

class Memory extends Module {
    val io = IO(new Bundle {
            val rdAddr = Input(UInt(16.W))
            val rdData = Output(UInt(32.W))
            val wrEnable = Input(Bool())
            val wrData = Input(UInt(32.W))
            val wrAddr = Input(UInt(16.W))
    })

    val mem = SyncReadMem(1792000, UInt(32.W)) // 224MB of ram, similar to FPGA

    io.rdData := mem.read(io.rdAddr)

    when (io.wrEnable) {
        mem.write(io.wrAddr, io.wrData)
    }
}