package core

import chisel3.iotesters._
import org.scalatest._
import chiseltest._
import chisel3._
import matchers._

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
    test(new SingleCycleRiscV(helperFunc.hexToFile("./testData/addneg.bin"))) { m=>
    val sb = new StringBuilder  
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

    for (w <- 0 to 8) {
      m.clock.step(1)
       // print(f"rdAddr: $pc%x => ")
       // print(f"rdData: $ins%8x\n \n\n")
        println()
        println()
  
        print(f"opcode = $op\nrd = $rd\nfunct3 = $funct3\nrs1 = $rs1\nrs2 = $rs2\nfunct7 = $funct7\nimm = $imm\nrd1 = $rd1\nrd2 = $rd2\naluCtrl = $aluCtrl\n\n")
        /*
        */
       // print(f"decoder: " + m.io.decDebug.peek())
      
  
  for(i <- 0 until 32){
    if(i != 0 && i % 8 == 0) { print(f"\n") } 
          var v = m.io.regDebug(i).peek().litValue() // peek(dut.io.regDebug(i))
          print(f"x$i%-2d ")
          print(f"$v%08x ")
    }
    
  }

  for(i <- 0 until 32){
    if(i != 0 && i % 8 == 0) { print(f"\n") } 
          var v = m.io.regDebug(i).peek().litValue() // peek(dut.io.regDebug(i))
          sb.append(f"$v%08x" + "\n")
  }

  sb.toString should be (helperFunc.hexToString("./testData/addneg.res"))
  
  
     
    //  for (i <- 0 until 31) {
    //   if(i != 0 && i % 8 == 0) { printf("\n") } 

    //   print(f"x$i%-2d ")
    //   val v = m.io.regDebug(2).peek().litValue // peek(dut.io.regDebug(i))
    //   print(f"$v%08x ")
    // }

    println(" ")

    }
  }
}



class R2Test extends FlatSpec with ChiselScalatestTester with Matchers {
  "MAIN tester" should "pass" in {
    test(new SingleCycleRiscV("./testData/instructions.hex.txt")) { m=>
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
    
    //m.clock.step(10)
    for(w <- 0 until 5){

    print(f"pc = $pc\nopcode = $op\nrd = $rd\nfunct3 = $funct3\nrs1 = $rs1\nrs2 = $rs2\nfunct7 = $funct7\nimm = $imm\nrd1 = $rd1\nrd2 = $rd2\naluCtrl = $aluCtrl\n\n")
        println()
      m.clock.step(1)
       for(i <- 0 until 32){
          if(i != 0 && i % 8 == 0) { print(f"\n") } 
          
          var v = m.io.regDebug(i).peek().litValue() // peek(dut.io.regDebug(i))
          print(f"x$i%-2d ")
          print(f"$v%08x ")
        }

        println()
        println()
        println()

      if(m.io.done.peek().litValue() == BigInt(1)){
    
       
      }
    }
    }
  }
}