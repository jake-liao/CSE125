/* 
 CSE125 Lab 5 booth.scala
 Professor Heiner Litz
 Jake Liao
 Mark Zakharov
*/
package dinocpu
import chisel3._
import chisel3.util._
/*
 * The Booth Multiplier
 * Input:  operation
 * Input:  Mc, the first input (e.g., reg1)
 * Input:  Mp, the second input (e.g., reg2)
 * Output: the result of the compuation
 */
class Booth extends Module {

  val io = IO(new Bundle {
    val operation = Input(UInt(1.W))
    val Mc        = Input(UInt(16.W))
    val Mp        = Input(UInt(16.W))
    val result    = Output(UInt(32.W))
    val done      = Output(UInt(1.W))
    val ready     = Output(UInt(1.W))
  })

  // ACCUMULATOR
  class Accumulator extends Bundle {
    val high_16   = UInt(16.W)
    val lower_17  = UInt(17.W)
  }
  val acc         = RegInit(0.U.asTypeOf(new Accumulator))
  
  // Encoding Addend
  val full          = Wire(UInt(33.W))
  val full_shift2   = Wire(UInt(33.W))
  val plus_mc       = Wire(UInt(16.W))
  val minus_mc      = Wire(UInt(16.W))
  val plus_two_mc   = Wire(UInt(16.W))
  val minus_two_mc  = Wire(UInt(16.W))
  full              := Cat(acc.high_16, acc.lower_17)
  full_shift2       := Cat(Fill(2, 0.U), acc.high_16, acc.lower_17(16,2))
  plus_mc           := io.Mc
  minus_mc          := (~io.Mc)+1.U
  plus_two_mc       := io.Mc*2.U
  minus_two_mc      := (~(io.Mc*2.U))+1.U

  // STATUS SIGNALS
  io.result         := DontCare
  io.done           := DontCare
  val cnt           = RegInit(8.U)
  io.ready          := RegInit(1.U)
  // ****************** DEBUG ****************** \\
  printf("------------------\n")  
  printf("cnt:\t%d\n", cnt)
  printf("acc:\t%b_%b\n", acc.high_16, acc.lower_17)
  // ******************************************* \\
  when (io.operation === 0.U){
    acc.high_16   := Fill(16,0.U)
    acc.lower_17  := Fill(17,0.U)
    io.result := 0.U
    cnt := 8.U
    io.ready := 1.U
  }.otherwise{
    
  when (cnt === 7.U){
    printf("\nFIRST CYCLE")
    io.result := 0.U
    acc.high_16 := 0.U
    cnt := cnt - 1.U
    io.done := 0.U
    io.ready := 0.U
    // acc_next := Cat(acc.high_16, acc.lower_17) >> 1
    switch (Cat(io.Mp(1,0), 0.U)) {
      is ("b000".U) {                                     // +0
        printf("b000\n")
        printf("+0:\t0\n")
        acc.high_16 := Fill(16,0.U)
        acc.lower_17 := Cat(Fill(2,0.U), io.Mp)
      }
      is ("b001".U) {                                     // +Mc
        printf("b001\n")
        printf("+mc:\t%b\n", (io.Mc))
        when (plus_mc(15)){
          acc.high_16 := Cat(1.U, plus_mc(15,1))
        } .otherwise {
          acc.high_16 := Cat(0.U, plus_mc(15,1))
        }
        acc.lower_17 := Cat(plus_mc(1,0),io.Mp)
      }
      is ("b010".U) {                                     // +Mc
        printf("b010\n")
        printf("+mc:\t%b\n", (io.Mc))
        when (plus_mc(15)){
          acc.high_16 := Cat(1.U, plus_mc(15,1))
        } .otherwise {
          acc.high_16 := Cat(0.U, plus_mc(15,1))
        }
        acc.lower_17 := Cat(plus_mc(0),io.Mp)
      }
      is ("b011".U) {                                     // + 2 * Mc
        printf("b011\n")
        printf("+ 2 * Mc:\t%b\n", (2.S*(io.Mc)).asUInt)
        when (plus_two_mc(15)){
          acc.high_16 := Cat(1.U, plus_two_mc(15,1))
        } .otherwise {
          acc.high_16 := Cat(0.U, plus_two_mc(15,1))
        }
        acc.lower_17 := Cat(plus_two_mc(0),io.Mp)
      }
      is ("b100".U) {                                     // - 2 * Mc
        printf("b100\n")
        printf("- 2 * Mc:\t%b\n", (-2.S*(io.Mc)).asUInt)
        when (minus_two_mc(15)){
          acc.high_16 := Cat(1.U, minus_two_mc(15,1))
        } .otherwise {
          acc.high_16 := Cat(0.U, minus_two_mc(15,1))
        }
        acc.lower_17 := Cat(minus_two_mc(0),io.Mp)
      }
      is ("b101".U) {                                     // -Mc
        printf("b101\n")
        printf("-mc:\t%b\n", (-1.S*(io.Mc)))
        when (minus_mc(15)){
          acc.high_16 := Cat(1.U, minus_mc(15,1))
        } .otherwise {
          acc.high_16 := Cat(0.U, minus_mc(15,1))
        }
        acc.lower_17 := Cat(minus_mc(0),io.Mp)
      }
      is ("b110".U) {                                     // -Mc
        printf("b110\n")
        printf("-mc:\t%b\n", (-1.S*(io.Mc)))
        when (minus_mc(15)){
          acc.high_16 := Cat(1.U, minus_mc(15,1))
        } .otherwise {
          acc.high_16 := Cat(0.U, minus_mc(15,1))
        }
        acc.lower_17 := Cat(minus_mc(0),io.Mp)
      }
      is ("b111".U) {                                     // -0
        printf("b111\n")
        printf("-0:\t0\n")
        acc.high_16 := Fill(16,0.U)
        acc.lower_17 := Cat(Fill(1,0.U), io.Mp)
      }
    }

  } .elsewhen (cnt === 0.U) { 
    cnt := 8.U
    io.done := 1.U
    io.ready := 0.U
    // io.result := Cat(acc.high_16, acc.lower_17(16,1))
    switch (Cat(acc.lower_17(3,1))) {
      is ("b000".U) {                                     // +0
        // printf("b000\n")
        printf("+0:\t0\n")
        when (acc.high_16(15)){
          printf("result:%b\n", ((Cat(Fill(2, 1.U), full(32, 2)))) >> 2)
          io.result   := (((Cat(Fill(2, 1.U), full(32, 2))).asSInt) >> 2).asUInt
        } .otherwise {
          io.result   := (((Cat(Fill(2, 0.U), full(32, 2))).asSInt) >> 2).asUInt
        }  
      }
      is ("b001".U) {                                     // +Mc
        // printf("b001\n")
        printf("+mc1:\t%b\n", plus_mc)
        switch (Cat(acc.high_16(15), plus_mc(15))){
          is("b00".U){
            io.result   := (((Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, plus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b01".U){
            io.result   := (((Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, plus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b10".U){
            io.result   := (((Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, plus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b11".U){
            io.result   := (((Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, plus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
        }                
      }
      is ("b010".U) {                                     // +Mc
        // printf("b010\n")
        printf("+mc2:\t%b\n", plus_mc)
        switch (Cat(acc.high_16(15), plus_mc(15))){
          is("b00".U){
            io.result   := (((Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, plus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b01".U){
            io.result   := (((Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, plus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b10".U){
            io.result   := (((Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, plus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b11".U){
            io.result   := (((Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, plus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
        }        
      }
      is ("b011".U) {                                     // + 2 * Mc
        // printf("b011\n")
        printf("+2*Mc:\t%b\n", Cat((2.S*(io.Mc))))
        switch (Cat(acc.high_16(15), plus_two_mc(15))){
          is("b00".U){
            io.result   := (((Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, plus_two_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b01".U){
            io.result   := (((Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, plus_two_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b10".U){
            io.result   := (((Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, plus_two_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b11".U){
            io.result   := (((Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, plus_two_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
        } 
      }
      is ("b100".U) {                                     // - 2 * Mc
        // printf("b100\n")
        printf("-2*Mc:\t%b\n", Cat((-2.S*(io.Mc))))
        switch (Cat(acc.high_16(15), minus_two_mc(15))){
          is("b00".U){
            io.result   := (((Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, minus_two_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b01".U){
            io.result   := (((Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, minus_two_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b10".U){
            io.result   := (((Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, minus_two_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b11".U){
            io.result   := (((Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, minus_two_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
        }   
      }
      is ("b101".U) {                                     // -Mc
        // printf("b101\n")
        printf("-Mc:\t%b\n", Cat((-1.S*(io.Mc))))
        switch (Cat(acc.high_16(15), minus_mc(15))){
          is("b00".U){
            io.result   := (((Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, minus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b01".U){
            io.result   := (((Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, minus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b10".U){
            io.result   := (((Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, minus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b11".U){
            io.result   := (((Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, minus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
        }    
      }
      is ("b110".U) {                                     // -Mc
        // printf("b110\n")
        printf("-Mc:\t%b\n", Cat((-1.S*(io.Mc))))
        switch (Cat(acc.high_16(15), minus_mc(15))){
          is("b00".U){
            io.result   := (((Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, minus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b01".U){
            io.result   := (((Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, minus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b10".U){
            io.result   := (((Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, minus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
          is("b11".U){
            io.result   := (((Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, minus_mc, Fill(16, 0.U))).asSInt) >> 2).asUInt
          }
        }   
      }
      is ("b111".U) {                                     // -0
        // printf("b111\n")
        printf("-0:\t0\n")
        when (acc.high_16(15)){
          io.result   := (((Cat(Fill(2, 1.U), full(32, 2))).asSInt) >> 2).asUInt
        } .otherwise {
          io.result   := (((Cat(Fill(2, 0.U), full(32, 2))).asSInt) >> 2).asUInt
        }  
      }
    }

  }.otherwise {
    when(cnt === 8.U){
      io.ready := 1.U
    } .otherwise{
      io.ready := 0.U
    }
    //io.ready := 0.U
    io.result := 0.U
    io.done := 0.U
    cnt := cnt - 1.U
    //io.ready := io.ready
    switch (Cat(acc.lower_17(3,1))) {
      is ("b000".U) {                                     // +0
        // printf("b000\n")
        printf("+0:\t0\n")
        when (acc.high_16(15)){
          acc.high_16 := Cat(Fill(2, 1.U), acc.high_16(15,2))
        } .otherwise {
          acc.high_16 := Cat(Fill(2, 0.U), acc.high_16(15,2))
        }  
        acc.lower_17 := Cat(acc.high_16(1,0), acc.lower_17(16,2)) 
      }
      is ("b001".U) {                                     // +Mc
        // printf("b001\n")
        printf("+mc1:\t%b\n", Cat(0.U, plus_mc))
        switch (Cat(acc.high_16(15), plus_mc(15))){
          is("b00".U){
            acc.high_16   := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, plus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, plus_mc, Fill(16, 0.U)))(16,0)
          }
          is("b01".U){
            acc.high_16   := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, plus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, plus_mc, Fill(16, 0.U)))(16,0)
          }
          is("b10".U){
            acc.high_16   := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, plus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, plus_mc, Fill(16, 0.U)))(16,0)
          }
          is("b11".U){
            acc.high_16   := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, plus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, plus_mc, Fill(16, 0.U)))(16,0)
          }
        }                
      }
      // 11111111110110001111111111001
      is ("b010".U) {                                     // +Mc
        // printf("b010\n")
        printf("+mc2:\t%b\n", plus_mc)
        printf("high_16:\t%b\n", acc.high_16)
        printf("full:      \t%b\n", full)
        printf("full_shift2:\t%b\n", full_shift2)
        // printf("full(32,2):\t%b\n", Cat(Fill(2, 1.U), full(32, 2)))
        // printf("shift:\t\t%b\n", Cat(Fill(3, 1.U),Fill(30, 0.U)) + full_shift2)
        // printf("addend:\t\t%b\n", Cat(0.U, plus_mc, Fill(16, 0.U)))
        printf("sum:\t%b\n", Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, plus_mc, Fill(16, 0.U)))
        switch (Cat(acc.high_16(15), plus_mc(15))){
          is("b00".U){
            printf("b00\n")
            acc.high_16   := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, plus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, plus_mc, Fill(16, 0.U)))(16,0)
          }
          is("b01".U){
            printf("b01\n")
            acc.high_16   := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, plus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, plus_mc, Fill(16, 0.U)))(16,0)
          }
          is("b10".U){
            printf("b10\n")
            acc.high_16   := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, plus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, plus_mc, Fill(16, 0.U)))(16,0)
          }
          is("b11".U){
            printf("b11\n")
            acc.high_16   := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, plus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, plus_mc, Fill(16, 0.U)))(16,0)
          }
        }     
      }
      is ("b011".U) {                                     // + 2 * Mc
        // printf("b011\n")
        printf("+2*Mc:\t%b\n", plus_two_mc)
        switch (Cat(acc.high_16(15), plus_two_mc(15))){
          is("b00".U){
            acc.high_16   := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, plus_two_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, plus_two_mc, Fill(16, 0.U)))(16,0)
          }
          is("b01".U){
            acc.high_16   := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, plus_two_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, plus_two_mc, Fill(16, 0.U)))(16,0)
          }
          is("b10".U){
            acc.high_16   := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, plus_two_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, plus_two_mc, Fill(16, 0.U)))(16,0)
          }
          is("b11".U){
            acc.high_16   := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, plus_two_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, plus_two_mc, Fill(16, 0.U)))(16,0)
          }
        } 
      }
      is ("b100".U) {                                     // - 2 * Mc
        // printf("b100\n")
        printf("-2*Mc:\t%b\n", minus_two_mc)
        switch (Cat(acc.high_16(15), minus_two_mc(15))){
          is("b00".U){
            acc.high_16   := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, minus_two_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, minus_two_mc, Fill(16, 0.U)))(16,0)
          }
          is("b01".U){
            acc.high_16   := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, minus_two_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, minus_two_mc, Fill(16, 0.U)))(16,0)
          }
          is("b10".U){
            acc.high_16   := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, minus_two_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, minus_two_mc, Fill(16, 0.U)))(16,0)
          }
          is("b11".U){
            acc.high_16   := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, minus_two_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, minus_two_mc, Fill(16, 0.U)))(16,0)
          }
        } 
      }
      is ("b101".U) {                                     // -Mc
        // printf("b101\n")
        printf("-Mc:\t%b\n", minus_mc)
        switch (Cat(acc.high_16(15), minus_mc(15))){
          is("b00".U){
            acc.high_16   := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, minus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, minus_mc, Fill(16, 0.U)))(16,0)
          }
          is("b01".U){
            acc.high_16   := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, minus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, minus_mc, Fill(16, 0.U)))(16,0)
          }
          is("b10".U){
            acc.high_16   := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, minus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, minus_mc, Fill(16, 0.U)))(16,0)
          }
          is("b11".U){
            acc.high_16   := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, minus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, minus_mc, Fill(16, 0.U)))(16,0)
          }
        } 
      }
      is ("b110".U) {                                     // -Mc
        // printf("b110\n")
        printf("-Mc:\t%b\n", minus_mc)
                switch (Cat(acc.high_16(15), minus_mc(15))){
          is("b00".U){
            acc.high_16   := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, minus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(0.U, minus_mc, Fill(16, 0.U)))(16,0)
          }
          is("b01".U){
            acc.high_16   := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, minus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 0.U), full(32, 2)) + Cat(1.U, minus_mc, Fill(16, 0.U)))(16,0)
          }
          is("b10".U){
            acc.high_16   := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, minus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(0.U, minus_mc, Fill(16, 0.U)))(16,0)
          }
          is("b11".U){
            acc.high_16   := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, minus_mc, Fill(16, 0.U)))(32,17)
            acc.lower_17  := (Cat(Fill(2, 1.U), full(32, 2)) + Cat(1.U, minus_mc, Fill(16, 0.U)))(16,0)
          }
        } 
      }
      is ("b111".U) {                                     // -0
        // printf("b111\n")
        printf("-0:\t0\n")
        when (acc.high_16(15)){
          acc.high_16 := Cat(Fill(2, 1.U), acc.high_16(15,2))
        } .otherwise {
          acc.high_16 := Cat(Fill(2, 0.U), acc.high_16(15,2))
        }  
        acc.lower_17 := Cat(acc.high_16(1,0), acc.lower_17(16,2)) 
      }
    }
  }
  }
}

