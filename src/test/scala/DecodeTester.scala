package core

import chisel3.iotesters._
import org.scalatest._



// class DecodeTester(dut: Decoder) extends PeekPokeTester(dut) {
//   poke(dut.in, 0x00200093) // addi x1 x0 2 
//   step(1)
//   expect(dut.decoded.opcode, OP.OP_I)
//   expect(dut.decoded.imm, 2)
//   println ("Immediate is " + peek(dut.decoded.imm).toString + "\n")
//   step(1)
//   println ("Opcode is " + peek(dut.decoded.opcode).toString)
  
//   poke(dut.in, 0x002081b3)
//   step(1)
//   expect(dut.decoded.opcode, OP.OP_R)
//   println ("Opcode is " + peek(dut.decoded.opcode).toString)

// }

// class DecoderSpec extends FlatSpec with Matchers {
//   "Decode Tester" should "pass" in {
//     chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeTester(c)} should be (true)
//   }
// }