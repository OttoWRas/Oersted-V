package core

import chisel3._
import chisel3.util._
import chisel3.util.experimental._

class Memory(fileToLoad: String = "") extends Module {
    val io = IO(new Bundle {
            val rdAddr   = Input(UInt(32.W))
            val wrEnable = Input(Bool())
            val wrData   = Input(UInt(32.W))
            val wrAddr   = Input(UInt(16.W))

            val rdData   = Output(UInt(32.W))
    })

    val mem = SyncReadMem(1792000, UInt(32.W)) // 224MB of ram, similar to FPGA

   
    //  printf("rdAddr = %d\n \n", io.rdAddr)
    // printf("rdData = %d\n \n", mem.read(io.rdAddr))
    // printf("data pos 0 = %d", mem.read(0.U))
    when (io.wrEnable) {
        mem.write(io.wrAddr, io.wrData)
    }
    if (!(fileToLoad == "")) {
        loadMemoryFromFile(mem, fileToLoad)
        
    }
     io.rdData := mem.read(io.rdAddr)
    // printf("rdData = %d\n \n", mem.read(io.rdAddr))
}