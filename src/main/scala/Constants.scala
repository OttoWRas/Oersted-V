package core

import chisel3._
import chisel3.util._

object Decoder {
    def DEC_R:      UInt = 0.U(3.W)
    def DEC_I:      UInt = 1.U(3.W)
    def DEC_S:      UInt = 2.U(3.W)
    def DEC_B:      UInt = 3.U(3.W)
    def DEC_U:      UInt = 4.U(3.W)
    def DEC_J:      UInt = 5.U(3.W)
}
