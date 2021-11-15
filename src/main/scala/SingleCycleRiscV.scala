package core

import chisel3._
import chisel3.util._
/**
  * Author Martin Schoeberl (martin@jopdesign.com)
  *
  * A single-cycle implementation of RISC-V is practically an ISA simulator.
  */
class SingleCycleRiscV extends Module {
  val io = IO(new Bundle {
    val regDeb = Output(Vec(32, UInt(32.W))) // debug output for the tester
    val pc = Output(UInt(32.W))
    val done = Output(Bool())
  })

  /* set up modules needed */
  val decoder = Module(new Decoder)

  // TODO: the program should be read in from a file
  val program = Array[Int](
    0x00200093, // addi x1 x0 2
    0x00300113, // addi x2 x0 3
    0x002081b3) // add x3 x1 x2

  // A little bit of functional magic to convert the Scala Int Array to a Chisel Vec of UInt
  val imem = VecInit(program.map(_.U(32.W)))
  val pc = RegInit(0.U(32.W))

  /* set up 32 registers and init them all to 0 */
  val vec = Wire(Vec(32, UInt(32.W))) 
  for (i <- 0 until 32) vec(i) := 0.U
  
  val reg = RegInit(vec)
  val instr = imem(pc(31, 2)) // from 2nd bit since we know bit 0 and 1 are always 0.

  decoder.in := instr

 
  val opcode  = decoder.decoded.opcode  
  val rd      = decoder.decoded.rd       
  val rs1     = decoder.decoded.rs1
  val imm     = decoder.decoded.imm


  switch(opcode) {

    is(0x13.U) {

      reg(rd) := reg(rs1) + imm
    }
  }
  
  pc := pc + 4.U
io.pc := pc
  // done should be set when the program ends, and the tester shall stop
  io.done := true.B

  /* fill our debugging registers with the content of our actual registers */
  for (i <- 0 until 32) io.regDeb(i) := reg(i)
}


object CPU extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new SingleCycleRiscV)
}

