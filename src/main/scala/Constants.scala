package core

import chisel3._
import chisel3.util._

/* the numbers correspond to the opcodes matching the type of instruction */

/*
object Decoder {
    val DEC_R:      UInt = 0.U(3.W)
    val DEC_I:      UInt = 1.U(3.W)
    val DEC_S:      UInt = 2.U(3.W)
    val DEC_B:      UInt = 3.U(3.W)
    val DEC_U:      UInt = 4.U(3.W)
    val DEC_J:      UInt = 5.U(3.W)
}
*/