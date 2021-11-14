import chisel3._
import chisel3.util._
import consts._

class DecodeOut extends Bundle {
    val opcode      = Output(UInt(7.W))
    val rd          = Output(UInt(5.W))
    val funct3      = Output(UInt(3.W))
    val rs1         = Output(UInt(5.W))
    val rs2         = Output(UInt(5.W))
    val funct7      = Output(UInt(7.W))
    val imm         = Output(UInt(32.W))
}

class SBType extends Bundle {
    val imm12 = UInt(1.W)
    val imm10to5 = UInt(6.W)
    val rs2 = UInt(5.W)
    val rs1 = UInt(5.W)
    val funct3 = UInt(3.W)
    val imm4to1 = UInt(5.W)
    val imm11 = UInt(1.W)
    val opcode = UInt(7.W)
}

class RType extends Bundle {
    val funct7      = Output(UInt(7.W))
    val rs2         = Output(UInt(5.W))
    val rs1         = Output(UInt(5.W))
    val funct3      = Output(UInt(3.W))
    val rd          = Output(UInt(5.W))
    val opcode      = Output(UInt(7.W))
}

class IType extends Bundle {
    val imm     = Output(UInt(12.W))
    val rs1     = Output(UInt(5.W))
    val funct3  = Output(UInt(3.W))
    val rd      = Output(UInt(5.W))
    val opcode  = Output(UInt(7.W))
}

class UType extends Bundle {
    val imm31to12 = Output(UInt(19.W))
    val rd = Output(UInt(5.W))
    val opcode = Output(UInt(7.W))
}

class JType extends Bundle {
    val imm20 = Output(UInt(1.W))
    val imm10to1 = Output(UInt(10.W))
    val imm1 = Output(UInt(1.W))
    val imm19to12 = Output(UInt(8.W))
    val rd = Output(UInt(5.W))
    val opcode = Output(UInt(7.W))
}

class Decoder extends MultiIOModule {
    val in = IO(Input(UInt(32.W)))
    val decoded = IO(Output(new DecodeOut))
    val opcode = in(6,0)

    // this feels like it could be smarter?
    decoded.opcode := opcode
    decoded.rd       := 0.U
    decoded.funct3   := 0.U
    decoded.rs1      := 0.U
    decoded.rs2      := 0.U
    decoded.funct7   := 0.U
    decoded.imm      := 0.U

    switch(opcode) {
        is(OP.OP_R){
            val R = in.asTypeOf(new RType)

            decoded.opcode := R.opcode
            decoded.rd := R.rd
            decoded.funct3 := R.funct3
            decoded.rs1 := R.rs1
            decoded.rs2 := R.rs2
            decoded.funct7 := R.funct7
            
        }
        is (OP.OP_I){
            print("OP I I I ")
            val I = in.asTypeOf(new IType)

            decoded.opcode := I.opcode
            decoded.rd := I.rd
            decoded.funct3 := I.funct3
            decoded.rs1 := I.rs1
            decoded.imm := 0.U(24.W) ## I.imm
            decoded.funct7 := 0.U
            decoded.rs2 := 0.U
        }

        is(OP.OP_B) {

        }
    }
}
  /*

val asSB = in.asTypeOf(new SBType)
val SBImm = asSB.imm12 ## asSB.imm11 ## asSB.imm10to5 ## asSB.imm4to1 ## 0.U(1.W) // ## = concat


    val io = IO(new Bundle {
        val instruction = Input(UInt(32.W))
        val opcode      = Output(UInt(7.W))
        val rd          = Output(UInt(5.W))
        val funct3      = Output(UInt(3.W))
        val rs1         = Output(UInt(5.W))
        val rs2         = Output(UInt(5.W))
        val funct7      = Output(UInt(7.W))
        val imm         = Output(UInt(32.W)) // for now 32 bit. needs to sign extended!!
        // val done        = Output(Bool()) // some kind of valid--ready ?
    })
   */

    /* default values */
    /*io.opcode   := io.instruction(6,0)
    io.rd       := 0.U
    io.funct3   := 0.U
    io.rs1      := 0.U
    io.rs2      := 0.U
    io.funct7   := 0.U
    io.imm      := 0.U

   switch(io.opcode){
        is(OP.OP_R){ // I can't manage to get simply OP_R to work?
            io.rd       := io.instruction(11,7) 
            io.funct3   := io.instruction(14,12)
            io.rs1      := io.instruction(19,15)
            io.rs2      := io.instruction(24,20)
            io.funct7   := io.instruction(31,25)
       
        }
        is(OP.OP_I){ // I
            io.rd       := io.instruction(11,7) 
            io.funct3   := io.instruction(14,12)
            io.rs1      := io.instruction(19,15)
            io.imm      := io.instruction(31,20)
        
        }
    }*/
    
