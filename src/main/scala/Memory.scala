package core

import chisel3._
import chisel3.util._
import chisel3.util.experimental._

class Memory(fileToLoad: String = "") extends Module {
    val io = IO(new Bundle {
            val rdAddr   = Input(UInt(32.W))
            val wrEnable = Input(Bool())
            val wrData   = Input(UInt(32.W))
            val wrAddr   = Input(UInt(32.W))

            val rdData   = Output(UInt(32.W))
    })

    val mem = SyncReadMem(1792000, UInt(8.W))
    // 224MB of ram, similar to FPGA
   
    //  printf("rdAddr = %d\n \n", io.rdAddr)
    // printf("rdData = %d\n \n", mem.read(io.rdAddr))
    // printf("data pos 0 = %d", mem.read(0.U))
    when (io.wrEnable) {
        io.rdData := 0.U
        mem.write(io.wrAddr, io.wrData(7,0))
        mem.write(io.wrAddr+1.U, io.wrData(15,8))
        mem.write(io.wrAddr+2.U, io.wrData(23,16))
        mem.write(io.wrAddr+3.U, io.wrData(31,24))
    }.otherwise {
        io.rdData := Cat(mem.read(io.rdAddr+3.U), mem.read(io.rdAddr+2.U),
                         mem.read(io.rdAddr+1.U), mem.read(io.rdAddr))
    }
    if (!(fileToLoad == "")) {
        loadMemoryFromFile(mem, fileToLoad)
        
    }
     
    // printf("rdData = %d\n \n", mem.read(io.rdAddr))
}