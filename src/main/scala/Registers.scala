package core

import chisel3._
import chisel3.util._

class Registers extends Module {
    val io = IO(new Bundle {
        val rdAddr1     = Input(UInt(32.W))
        val rdAddr2     = Input(UInt(32.W))
        val rdAddr3     = Input(UInt(32.W))
        val wrEnable    = Input(Bool())
        val wrData      = Input(UInt(32.W))
      
        val wrAddr      = Input(UInt(5.W))

        val rdData1     = Output(UInt(32.W))
        val rdData2     = Output(UInt(32.W))
        val rdData3     = Output(UInt(32.W))
        val x17         = Output(UInt(32.W)) // to for a7 on ecall

        val regDebug    = Output(Vec(32, UInt(32.W)))
    })

    val registerFile = Reg(Vec (32, UInt (32.W)))

    io.rdData1 := WireDefault(0.U)
    io.rdData2 := WireDefault(0.U)
    io.rdData3 := WireDefault(0.U)
    io.rdData1 := registerFile(io.rdAddr1)
    io.rdData2 := registerFile(io.rdAddr2)
    io.rdData3 := registerFile(io.rdAddr3)

    when (io.wrEnable && io.wrAddr =/= 0.U) {
        when (io.wrAddr === io.rdAddr1) {
            io.rdData1 := io.wrData
        }
        when (io.wrAddr === io.rdAddr2) {
            io.rdData2 := io.wrData
        }
        when (io.wrAddr === io.rdAddr3) {
            io.rdData3 := io.wrData
        }
        registerFile(io.wrAddr) := io.wrData
    }

    
    io.x17 := registerFile(17.U)

   io.regDebug := registerFile
}