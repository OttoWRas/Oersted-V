package core

import java.util.regex
import chisel3.iotesters._
import org.scalatest._
import chiseltest._
import chisel3._
import matchers._
import sbt._
import scala.util.control.Breaks._
import chiseltest.internal.WriteVcdAnnotation
import chiseltest.experimental.TestOptionBuilder._

import java.nio.file.{Files, Paths}
import java.io._
object helperFunc {

  def hexToFile(args: String): String = {
        val fileObject = new File(args + ".hex.txt")
        val printWriter = new PrintWriter(fileObject)
        val byteArray = Files.readAllBytes(Paths.get(args))
        val sb = new StringBuilder
        var i = 1
        var n = 0
        for (b <- byteArray) {
            sb.insert(0 + n*9, String.format("%02x", Byte.box(b)))
            if (i % 4 == 0) {
                sb.append("\n")
                n += 1
            }
            i += 1
        }
        printWriter.write(sb.toString)
        printWriter.close()
        return args + ".hex.txt"
    }

  def hexToString(args: String): String = {
    val byteArray = Files.readAllBytes(Paths.get(args))
    val sb = new StringBuilder
    var i = 1
    var n = 0
    for (b <- byteArray) {
        sb.insert(0 + n*9, String.format("%02x", Byte.box(b)))
        if (i % 4 == 0) {
            sb.append("\n")
            n += 1
        }
        i += 1
    }
    return sb.toString
  }
}

class RiscVSpecFull extends FlatSpec with ChiselScalatestTester with Matchers {
  
    val listOfFiles = new java.io.File("./testData/task4/").listFiles
      .map(_.getPath())

    val listOfBin: Array[String] = listOfFiles
      .filter(file => file.toString.endsWith(".bin"))
    
    val listOfRes: Array[String] = listOfBin
      .map[String, Array[String]](f => (f.slice(0,f.length - 3) + "res"))

    val listOfTest: Array[String] = listOfBin
      .map[String, Array[String]](f => (f.slice(17,f.length - 4) + " test"))

  for (n <- 0 to listOfBin.length-1) {
    listOfTest(n) should "pass" in { 
      test(new SingleCycleRiscV(helperFunc.hexToFile(listOfBin(n)))).withAnnotations(Seq(WriteVcdAnnotation)) { m=>
      
      val sb = new StringBuilder 

      for (w <- 0 to 512) {
        var pc = m.io.pcDebug.peek().litValue()
        var ins =  m.io.instrDebug.peek().litValue()
        var op = m.io.opcodeDebug.peek().litValue()
        var rd = m.io.rdDebug.peek().litValue()
        var funct3 = m.io.funct3Debug.peek().litValue()
        var funct7 = m.io.funct7Debug.peek().litValue()
        var rs1 = m.io.rs1Debug.peek().litValue()
        var rs2 = m.io.rs2Debug.peek().litValue()
        var imm = m.io.immDebug.peek().litValue()
        var rd1 = m.io.rd1Debug.peek().litValue()
        var rd2 = m.io.rd2Debug.peek().litValue()
        //var aluCtrl = m.io.aLUSrcDebug.peek().litValue()
        m.clock.step(1)
         // print(f"rdAddr: $pc%x => ")
         // print(f"rdData: $ins%8x\n \n\n")
      //println()
      //print(f"opcode = $op\nrd = $rd\nfunct3 = $funct3\nrs1 = $rs1\nrs2 = $rs2\nfunct7 = $funct7\nimm = $imm\nrd1 = $rd1\nrd2 = $rd2\naluCtrl = $aluCtrl\n\n")
          /*
          */
         // print(f"decoder: " + m.io.decDebug.peek())
      /*
      for(i <- 0 until 32){
        if(i != 0 && i % 8 == 0) { print(f"\n") } 
              var v = m.io.regDebug(i).peek().litValue() // peek(dut.io.regDebug(i))
              print(f"x$i%-2d ")
              print(f"$v%08x ")
        }
      */
      }

      for(i <- 0 until 32){
        if(i != 0 && i % 8 == 0) { print(f"\n") } 
              var v = m.io.regDebug(i).peek().litValue() // peek(dut.io.regDebug(i))
              sb.append(f"$v%08x" + "\n")
      }

      var dn = listOfBin(n) + " " + listOfRes(n)
      withClue(dn) { sb.toString should be (helperFunc.hexToString(listOfRes(n))) }
    }
    }
  }
}

class RiscVSpec extends FlatSpec with ChiselScalatestTester with Matchers {
  "MAIN tester" should "pass" in {
    test(new SingleCycleRiscV(helperFunc.hexToFile("./testData/task3/loop.bin"))).withAnnotations(Seq(WriteVcdAnnotation)) { m=>
    val sb = new StringBuilder  
   
  breakable  {
    for (w <- 0 to 512) {
      
    var pc = m.io.pcDebug.peek().litValue()
    var ins =  m.io.instrDebug.peek().litValue()
    var op = m.io.opcodeDebug.peek().litValue()
    var rd = m.io.rdDebug.peek().litValue()
    var funct3 = m.io.funct3Debug.peek().litValue()
    var funct7 = m.io.funct7Debug.peek().litValue()
    var rs1 = m.io.rs1Debug.peek().litValue()
    var rs2 = m.io.rs2Debug.peek().litValue()
    var imm = m.io.immDebug.peek().litValue()
    var rd1 = m.io.rd1Debug.peek().litValue()
    var rd2 = m.io.rd2Debug.peek().litValue()

    var decBuffD = m.io.decBuffD.peek().litValue()
    var immBuffD = m.io.immBuffD.peek().litValue()
    var aluBuffD = m.io.aluBuffD.peek().litValue()
    var opcBuffD = m.io.opcBuffD.peek().litValue()
    
    var memBuffD    = m.io.memBuffD.peek().litValue()
    var memAluBuffD = m.io.memAluBuffD.peek().litValue()
    //var memOpcBuffD = m.io.memOpcBuffD.peek().litValue()

    var wbMemBuffD = m.io.wbMemBuffD.peek().litValue()
    var wbAluBuffD = m.io.wbAluBuffD.peek().litValue()
    var wbOpcBuffD = m.io.wbOpcBuffD.peek().litValue()
    var hazard = m.io.hazardD.peek().litValue()

   // var aluCtrl = m.io.aLUSrcDebug.peek().litValue()
    var done = m.io.done.peek().litValue()
    
    //var pcJmpAddr = m.io.pcJmpAddrDebug.peek().litValue()
    //var ctrlBranch = m.io.ctrlBranchDebug.peek().litValue()

     
        println(f"\n dec:$decBuffD imm:$immBuffD alu:$aluBuffD opc:$opcBuffD \n\n mem:$memBuffD memAlu:$memAluBuffD memOpc: \n\n wbmem:$wbMemBuffD wbalu:$wbAluBuffD wbopc:$wbOpcBuffD \n\n")
        println(f"hazard: $hazard")
        print(f"instruction: $ins%08x at $pc - jmpaddr:  and ctrlBranch =")
        println()
        print(f"opcode = $op\nrd = $rd\nfunct3 = $funct3\nrs1 = $rs1\nrs2 = $rs2\nfunct7 = $funct7\nimm = $imm\nrd1 = $rd1\nrd2 = $rd2\naluCtrl = \n\n")
      
  
  for(i <- 0 until 32){
    if(i != 0 && i % 8 == 0) { print(f"\n") } 
          var v = m.io.regDebug(i).peek().litValue() // peek(dut.io.regDebug(i))
          print(f"x$i%-2d ")
          print(f"$v%08x ")
    }
      m.clock.step(1)
        if (done == BigInt(1)) break
        } 
  }

  for(i <- 0 until 32){
    if(i != 0 && i % 8 == 0) { print(f"\n") } 
          var v = m.io.regDebug(i).peek().litValue() // peek(dut.io.regDebug(i))
          sb.append(f"$v%08x" + "\n")
  }

  sb.toString should be (helperFunc.hexToString("./testData/task3/loop.res"))
  

    println(" ")

    }
  }
}
/*
   0x00200093, // addi x1 x0 2
    0x00300113, // addi x2 x0 3
    0x002081b3) // add x3 x1 x2
*/
// class RiscVSpec extends FlatSpec with Matchers {
//   "RiscV test" should "pass" in {
//     test(new SingleCycleRiscV).withAnnotations(Seq(WriteVcdAnnotation)) { c=>
//         c.io.fetch.poke(true.B)
//         c.clock.step(20)
//         c.io.instOut.expect(4369.U)
//         c.io.fetch.poke(false.B)
//         c.io.pcPlus.poke(true.B)
//         c.clock.step(1)
//         c.io.pcPlus.poke(false.B)
//         c.io.fetch.poke(true.B)
//         c.clock.step(5)
//         c.io.fetch.poke(false.B)
//         c.clock.step(5)
//     }
//   }
// }

