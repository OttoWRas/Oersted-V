object ALU {  
    def ALU_ADD:    UInt = 0.U(4.W)
    def ALU_SLL:    UInt = 1.U(4.W)
    def ALU_SEQ:    UInt = 2.U(4.W)
    def ALU_SNE:    UInt = 3.U(4.W)
    def ALU_XOR:    UInt = 4.U(4.W)
    def ALU_SRL:    UInt = 5.U(4.W)
    def ALU_OR:     UInt = 6.U(4.W)
    def ALU_AND:    UInt = 7.U(4.W)
    def ALU_COPY1:  UInt = 8.U(4.W)
    def ALU_COPY2:  UInt = 9.U(4.W)
    def ALU_SUB:    UInt = 10.U(4.W)
    def ALU_SRA:    UInt = 11.U(4.W)
    def ALU_SLT:    UInt = 12.U(4.W)
    def ALU_SGE:    UInt = 13.U(4.W)
    def ALU_SLTU:   UInt = 14.U(4.W)
    def ALU_SGEU:   UInt = 15.U(4.W)
}

object Decoder {
    def DEC_R:      UInt = 0.U(3.W)
    def DEC_I:      UInt = 1.U(3.W)
    def DEC_S:      UInt = 2.U(3.W)
    def DEC_B:      UInt = 3.U(3.W)
    def DEC_U:      UInt = 4.U(3.W)
    def DEC_J:      UInt = 5.U(3.W)
}
