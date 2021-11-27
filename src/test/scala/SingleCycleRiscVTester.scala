package core

import chisel3.iotesters._
import org.scalatest._
import chiseltest._
import chisel3._
import matchers._
import scala.util.control.Breaks._

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

  import helperFunc._

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

class RiscVSpec extends FlatSpec with ChiselScalatestTester with Matchers {
  "MAIN tester" should "pass" in {
    test(new SingleCycleRiscV(helperFunc.hexToFile("./testData/task1/shift2.bin"))) { m=>
    val sb = new StringBuilder  
   
  breakable  {
    for (w <- 0 to 100) {
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
    var aluCtrl = m.io.aLUSrcDebug.peek().litValue()
    var done = m.io.done.peek().litValue()
     
     
        println()
        println()
        print(f"instruction: $ins%08x")
        println()
        print(f"opcode = $op\nrd = $rd\nfunct3 = $funct3\nrs1 = $rs1\nrs2 = $rs2\nfunct7 = $funct7\nimm = $imm\nrd1 = $rd1\nrd2 = $rd2\naluCtrl = $aluCtrl\n\n")
      
  
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

  sb.toString should be (helperFunc.hexToString("./testData/task1/shift2.res"))
  

    println(" ")

    }
  }
}

class R2Test extends FlatSpec with ChiselScalatestTester with Matchers {
  "MAIN tester" should "pass" in {
    test(new SingleCycleRiscV("./testData/instructions.hex.txt")) { m=>
    
      breakable { 
        for (i <- 0 to 20) {
          
          var done = m.io.done.peek().litValue()
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
          var aluCtrl = m.io.aLUSrcDebug.peek().litValue()
          var jmpAddr = m.io.pcJmpAddrDebug.peek().litValue()
          var aluOut = m.io.aluOutDebug.peek().litValue()
          var aluCmpOut = m.io.aluCmpOutDebug.peek().litValue()

          
          println()
          print("\n-----------------------------------\n")
          print(f"instruction: $ins%08x and PC = $pc & jmpAddr = $jmpAddr")
          println()
          print(f"aluOut = $aluOut | aluCmpOut = $aluCmpOut")
          println()
          print(f"opcode = $op\nrd = $rd\nfunct3 = $funct3\nrs1 = $rs1\nrs2 = $rs2\nfunct7 = $funct7\nimm = $imm\nrd1 = $rd1\nrd2 = $rd2\naluCtrl = $aluCtrl\n\n")
          println()
          /* print regs */
          for(i <- 0 until 32){
            if(i != 0 && i % 8 == 0) { print(f"\n") } 
                
            var v = m.io.regDebug(i).peek().litValue() // peek(dut.io.regDebug(i))
            print(f"x$i%-2d ")
            print(f"$v%08x ")
          }

          print("\n-----------------------------------\n")
          println()
          println()
          println()
        m.clock.step(1)
        if (done == BigInt(1)) break
        } 
      }



      
    }
  }

}

/*

    for(w <- 0 until 15){
      
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
      var aluCtrl = m.io.aLUSrcDebug.peek().litValue()
     
      var done = m.io.done.peek().litValue()
      if(done == BigInt(1)){
       print("DONEONEOND")
        
      } else {
        m.clock.step(1)
      }
    print(f"instruction: $ins%08x and PC: $pc")
        println()
        print(f"opcode = $op\nrd = $rd\nfunct3 = $funct3\nrs1 = $rs1\nrs2 = $rs2\nfunct7 = $funct7\nimm = $imm\nrd1 = $rd1\nrd2 = $rd2\naluCtrl = $aluCtrl\n\n")
        println()
        for(i <- 0 until 32){
    if(i != 0 && i % 8 == 0) { print(f"\n") } 
          var v = m.io.regDebug(i).peek().litValue() // peek(dut.io.regDebug(i))
          print(f"x$i%-2d ")
          print(f"$v%08x ")
    }
    println()
        println()
      
      
      */