package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chiseltest._
import chiseltest.internal.WriteVcdAnnotation
import chiseltest.experimental.TestOptionBuilder._

class FetchTestModule extends Module {
    val io = IO(new Bundle {
      val pcPlus   = Input(Bool())
      val jmpAddr  = Input(UInt(32.W))
      val wrEnablePC = Input(Bool())
      val wrEnableMem = Input(Bool())
      val wrData = Input(UInt(32.W))
      val wrAddr = Input(UInt(16.W))
      val instOut  = Output(UInt(32.W))
      val fetch  = Input(Bool())
  })

    val pc = Module(new ProgramCounter)
    val mem = Module(new Memory("./testData/fetchTest.hex.txt"))
    val instr = Module(new InstBuff)

    pc.io.pcPlus := io.pcPlus
    pc.io.jmpAddr := io.jmpAddr
    pc.io.wrEnable := io.wrEnablePC

    instr.io.instIn := mem.io.rdData
    pc.io.flagIn := instr.io.flagOut
    instr.io.flagIn := pc.io.flagOut

    mem.io.rdAddr := 0.U
    mem.io.wrEnable := io.wrEnableMem
    mem.io.wrData := io.wrData
    mem.io.wrAddr := io.wrAddr

    when (io.fetch) {
        mem.io.rdAddr := pc.io.pcAddr
    }

    io.instOut := instr.io.instOut 
}

class FetchTest extends FlatSpec with ChiselScalatestTester with Matchers {
  "Fetch test" should "pass" in {
    test(new FetchTestModule).withAnnotations(Seq(WriteVcdAnnotation)) { f=>
        f.io.fetch.poke(true.B)
        f.clock.step(20)
        f.io.instOut.expect(4369.U)
        print("Instr: " + f.io.instOut.peek().litValue())
        println()
        f.io.fetch.poke(false.B)
        f.io.pcPlus.poke(true.B)
        f.clock.step(1)
        print("Instr: " + f.io.instOut.peek().litValue())
        println()
        f.io.pcPlus.poke(false.B)
        f.io.fetch.poke(true.B)
        f.clock.step(5)
        f.io.fetch.poke(false.B)
        f.clock.step(5)
    }
  }
}



class InstrFetchModule extends Module {
    val io = IO(new Bundle {
      val pcPlus   = Input(Bool())
      val jmpAddr  = Input(UInt(32.W))
      val wrEnablePC = Input(Bool())
      val wrEnableMem = Input(Bool())
      val wrData = Input(UInt(32.W))
      val wrAddr = Input(UInt(16.W))
      val instOut  = Output(UInt(32.W))
      val pcOut = Output(UInt(32.W))
      val fetch  = Input(Bool())
  })

    val pc = Module(new ProgramCounter)
    val mem = Module(new Memory("./testData/instructions.hex.txt"))
    val instr = Module(new InstBuff)

    pc.io.pcPlus := io.pcPlus
    pc.io.jmpAddr := io.jmpAddr
    pc.io.wrEnable := io.wrEnablePC

    instr.io.instIn := mem.io.rdData
    pc.io.flagIn := instr.io.flagOut
    instr.io.flagIn := pc.io.flagOut

    mem.io.rdAddr := 0.U
    mem.io.wrEnable := io.wrEnableMem
    mem.io.wrData := io.wrData
    mem.io.wrAddr := io.wrAddr

    when (io.fetch) {
        mem.io.rdAddr := pc.io.pcAddr
    }

    io.instOut := instr.io.instOut 
    io.pcOut := pc.io.pcAddr
}

class FetchInstrTest extends FlatSpec with ChiselScalatestTester with Matchers {
  "Instruction fetch test" should "pass" in {
    test(new InstrFetchModule).withAnnotations(Seq(WriteVcdAnnotation)) { f=>
      
        f.io.pcPlus.poke(false.B)
        f.io.fetch.poke(true.B)
        f.clock.step(5)
        print("Instr: " + f.io.instOut.peek().litValue() + " PC: " + f.io.pcOut.peek().litValue())
        println()
        f.io.fetch.poke(false.B)
        f.io.pcPlus.poke(true.B)
        f.clock.step(5)
        f.io.pcPlus.poke(false.B)
        f.io.fetch.poke(true.B)
        print("Instr: " + f.io.instOut.peek().litValue() + " PC: " + f.io.pcOut.peek().litValue())
        println()
       
       
        // f.clock.step(1)
        
       
        // f.io.pcPlus.poke(false.B)
        // f.io.fetch.poke(true.B)
        // f.clock.step(5)
        // print("Instr: " + f.io.instOut.peek().litValue() + " PC: " + f.io.pcOut.peek().litValue())
        // println()
        // f.io.fetch.poke(false.B)
        // f.clock.step(5)
        // print("Instr: " + f.io.instOut.peek().litValue() + " PC: " + f.io.pcOut.peek().litValue())
        // println()
        // f.clock.step(20)
        // 
        // f.io.instOut.expect(4369.U)
        // 
        // f.io.pcPlus.poke(true.B)
        // f.clock.step(1)
        // f.io.pcPlus.poke(false.B)
        // f.io.fetch.poke(true.B)
        // f.clock.step(5)
        // f.io.fetch.poke(false.B)
        // f.clock.step(5)
    }
  }
}
