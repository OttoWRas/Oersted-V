
import chisel3._
import chisel3.util._
import consts._
/* wip - using bundles for the instructions.. supposed to be smart somehow */
class DecodeOut extends Bundle {
    val opcode      = Output(UInt(7.W))
    val rd          = Output(UInt(5.W))
    val funct3      = Output(UInt(3.W))
    val rs1         = Output(UInt(5.W))
    val rs2         = Output(UInt(5.W))
    val funct7      = Output(UInt(7.W))

}


class Decoder extends Module {
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
   
    /* default values */
    io.opcode   := io.instruction(6,0)
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
    }
    
}