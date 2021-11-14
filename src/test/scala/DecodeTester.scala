import chisel3.iotesters._
import org.scalatest._
import consts._



class DecodeTester(dut: Decoder) extends PeekPokeTester(dut) {
  poke(dut.io.instruction, 0x00200093) // addi x1 x0 2 
  step(1)
  expect(dut.io.opcode, OP.OP_I)
  expect(dut.io.imm, 2)
  println ("Immediate is " + peek(dut.io.imm).toString + "\n")
  step(1)
  println ("Opcode is " + peek(dut.io.opcode).toString)
  poke(dut.io.instruction, 0x002081b3)
  step(1)
  expect(dut.io.opcode, OP.OP_R)
  println ("Opcode is " + peek(dut.io.opcode).toString)

}

class DecoderSpec extends FlatSpec with Matchers {
  "Decode Tester" should "pass" in {
    chisel3.iotesters.Driver(() => new Decoder()) { c => new DecodeTester(c)} should be (true)
  }
}