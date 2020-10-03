// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu

import chisel3._
import chisel3.util._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.6 of Patterson and Hennessy
 * This follows figure 4.49
 */
class PipelinedCPU(implicit val conf: CPUConfig) extends Module {
  val io = IO(new CoreIO)
  // Everything in the register between IF and ID stages
  class IFIDBundle extends Bundle {
    val instruction = UInt(32.W)
    val pc          = UInt(32.W)
    val pcplusfour  = UInt(32.W)
  }
  // Control signals used in EX stage
  class EXControl extends Bundle {
    val add       = Bool()
    val immediate = Bool()
    val alusrc1   = UInt(2.W)
    val branch    = Bool()
    val jump      = UInt(2.W)
  }
  // Control signals used in MEM stage
  class MControl extends Bundle {
    val memread = Bool()
    val memwrite = Bool()
    val exmemtaken = Bool() //Hazard Control Signal
  }
  // Control signals used in EB stage
  class WBControl extends Bundle {
    val toreg     = UInt(2.W)
    val regwrite  = Bool()
  }
  // Everything in the register between ID and EX stages
  class IDEXBundle extends Bundle {
    val rs1 = UInt(5.W)
    val rs2 = UInt(5.W)
    val immediate = UInt(32.W)
    val pc = UInt(32.W)
    val pcplusfour = UInt(32.W)
    val instruction = UInt(32.W)
    val readdata1 = UInt(32.W)
    val readdata2 = UInt(32.W)
    val excontrol = new EXControl
    val mcontrol  = new MControl
    val wbcontrol = new WBControl
  }
  // Everything in the register between ID and EX stages
  class EXMEMBundle extends Bundle {
    val pcplusfour = UInt(32.W)
    val next_pc = UInt(32.W)
    val instruction = UInt(32.W)
    val address = UInt(32.W)
    val writedata = UInt(32.W)
    val exmem_taken = Bool()  //hazard control, might not be a Bool() if jump is 2-bit
    val mcontrol  = new MControl
    val wbcontrol = new WBControl
  }
  // Everything in the register between ID and EX stages
  class MEMWBBundle extends Bundle {
    val address = UInt(32.W)
    val instruction = UInt(32.W)
    val readdata = UInt(32.W) 
    val pcplusfour = UInt(32.W)
    val wbcontrol = new WBControl
  }

  // All of the structures required
  val pc         = RegInit(0.U)
  val control    = Module(new Control())
  val branchCtrl = Module(new BranchControl())
  val registers  = Module(new RegisterFile())
  val aluControl = Module(new ALUControl())
  val alu        = Module(new ALU())
  val immGen     = Module(new ImmediateGenerator())
  val pcPlusFour = Module(new Adder())
  val branchAdd  = Module(new Adder())
  val forwarding = Module(new ForwardingUnit())  //pipelined only
  val hazard     = Module(new HazardUnit())      //pipelined only
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  val if_id      = RegInit(0.U.asTypeOf(new IFIDBundle))
  val id_ex      = RegInit(0.U.asTypeOf(new IDEXBundle))
  val ex_mem     = RegInit(0.U.asTypeOf(new EXMEMBundle))
  val mem_wb     = RegInit(0.U.asTypeOf(new MEMWBBundle))

  printf("Cycle=%d ", cycleCount)

  // From memory back to fetch. Since we don't decide whether to take a branch or not until the memory stage.
  val next_pc = Wire(UInt(32.W))
  // For wb back to other stages
  val write_data = Wire(UInt(32.W))

  /////////////////////////////////////////////////////////////////////////////
  // FETCH STAGE

  // Note: This comes from the memory stage!
  // Only update the pc if the pcwrite flag is enabled
  next_pc := DontCare
  switch(hazard.io.pcwrite) { //mux that controls next pc
    is(0.U) { next_pc := pcPlusFour.io.result }
    is(1.U) { next_pc := ex_mem.next_pc }
    is(2.U) { next_pc := pc }
  }

  // Send the PC to the instruction memory port to get the instruction
  io.imem.address := pc

  // Get the PC + 4
  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  // Fill the IF/ID register if we are not bubbling IF/ID
  // otherwise, leave the IF/ID register *unchanged*
  when (hazard.io.ifid_bubble) {  //only bubbles the pipeline stage, feeds back old value to hold onto them for stall
    if_id.instruction := if_id.instruction
    if_id.pc := if_id.pc
    if_id.pcplusfour := if_id.pcplusfour
  } .elsewhen (hazard.io.ifid_flush) {  //nops instruction as branch occured and instruction is bad
    if_id.instruction := 19.U   //nop, addi 0
    if_id.pc := 0.U
    if_id.pcplusfour := 0.U
  } .otherwise {
    if_id.instruction := io.imem.instruction
    if_id.pc          := pc
    if_id.pcplusfour  := pcPlusFour.io.result
  }
  printf(p"IF/ID: $if_id\n")

  /////////////////////////////////////////////////////////////////////////////
  // ID STAGE
  /////////////////////////////////////////////////////////////////////////////
  
  
  val rs1 = if_id.instruction(19,15)
  val rs2 = if_id.instruction(24,20)
  val opcode = if_id.instruction(6,0)
  printf(p"REG1 NUMBER: $rs1 \n")
  printf(p"REG2 NUMBER: $rs2 \n")

  hazard.io.rs1         := rs1
  hazard.io.rs2         := rs2
  control.io.opcode     := opcode
  registers.io.readreg1 := rs1
  registers.io.readreg2 := rs2
  immGen.io.instruction := if_id.instruction
  id_ex.pcplusfour      := if_id.pcplusfour
  id_ex.pc              := if_id.pc

  when (hazard.io.idex_bubble) {
    id_ex.excontrol.add       := false.B
    id_ex.excontrol.branch    := false.B
    id_ex.excontrol.immediate := false.B
    id_ex.excontrol.alusrc1   := 0.U
    id_ex.excontrol.jump      := 0.U
    id_ex.mcontrol.memread    := false.B
    id_ex.mcontrol.memwrite   := false.B
    id_ex.wbcontrol.toreg     := 0.U
    id_ex.wbcontrol.regwrite  := false.B
    id_ex.instruction         := 19.U
    id_ex.immediate           := 0.U
    id_ex.readdata1           := 0.U
    id_ex.readdata2           := 0.U
    id_ex.rs1                 := 0.U
    id_ex.rs2                 := 0.U
  } .otherwise {
    id_ex.excontrol.add       := control.io.add
    id_ex.excontrol.branch    := control.io.branch
    id_ex.excontrol.immediate := control.io.immediate
    id_ex.excontrol.alusrc1   := control.io.alusrc1
    id_ex.excontrol.jump      := control.io.jump
    id_ex.mcontrol.memread    := control.io.memread
    id_ex.mcontrol.memwrite   := control.io.memwrite
    id_ex.wbcontrol.toreg     := control.io.toreg
    id_ex.wbcontrol.regwrite  := control.io.regwrite
    id_ex.instruction         := if_id.instruction
    id_ex.immediate           := immGen.io.sextImm
    id_ex.readdata1           := registers.io.readdata1
    id_ex.readdata2           := registers.io.readdata2
    id_ex.rs1                 := rs1
    id_ex.rs2                 := rs2
  }

  // printf("DASM(%x)\n", if_id.instruction)
  printf(p"ID/EX: $id_ex\n")
  printf("opcode: %b\n", opcode)
  printf("control.io.add: %x\n", control.io.add)
  printf("id_ex.excontrol.add: %x\n", id_ex.excontrol.add)
  /////////////////////////////////////////////////////////////////////////////
  // EX STAGE
  /////////////////////////////////////////////////////////////////////////////

  val idex_rd = id_ex.instruction(11,7)
  val control_funct7 = id_ex.instruction(31,25)
  val control_funct3 = id_ex.instruction(14,12)
  val forwardinputxmux = Wire(UInt())
  val alu_inputx = Wire(UInt())
  val forwardinputymux = Wire(UInt())

  hazard.io.idex_rd       := idex_rd
  hazard.io.idex_memread  := id_ex.mcontrol.memread

  forwarding.io.rs1       := id_ex.rs1
  forwarding.io.rs2       := id_ex.rs2

  aluControl.io.add       := id_ex.excontrol.add
  aluControl.io.immediate := id_ex.excontrol.immediate
  aluControl.io.funct7    := control_funct7
  aluControl.io.funct3    := control_funct3

  forwardinputxmux := DontCare
  switch(forwarding.io.forwardA) { 
    is(0.U) { forwardinputxmux := id_ex.readdata1 }
    is(1.U) { forwardinputxmux := ex_mem.address  }
    is(2.U) { forwardinputxmux := write_data }
  }

  alu_inputx := DontCare
  switch(id_ex.excontrol.alusrc1) {  //controlled by current instruction stored in id_ex reg
    is(0.U) { alu_inputx := forwardinputxmux } //forward mux A
    is(1.U) { alu_inputx := 0.U }
    is(2.U) { alu_inputx := id_ex.pc}   //takes pc from current instruction, not current pc
  }

  forwardinputymux := DontCare
  switch(forwarding.io.forwardB) {
    is(0.U) { forwardinputymux := id_ex.readdata2 }
    is(1.U) { forwardinputymux := ex_mem.address }
    is(2.U) { forwardinputymux := write_data }
  }

  val alu_inputy = Mux(id_ex.excontrol.immediate,id_ex.immediate, forwardinputymux)

  branchCtrl.io.branch := id_ex.excontrol.branch
  branchCtrl.io.funct3 := control_funct3
  branchCtrl.io.inputx := forwardinputxmux
  branchCtrl.io.inputy := forwardinputymux

  alu.io.operation    := aluControl.io.operation
  alu.io.inputx       := alu_inputx
  alu.io.inputy       := alu_inputy

  branchAdd.io.inputx := id_ex.pc
  branchAdd.io.inputy := id_ex.immediate

  ex_mem.pcplusfour := id_ex.pcplusfour

  when (hazard.io.exmem_bubble) {
    ex_mem.mcontrol.memread   := false.B
    ex_mem.mcontrol.memwrite  := false.B
    ex_mem.exmem_taken        := false.B
    ex_mem.wbcontrol.toreg    := 0.U
    ex_mem.wbcontrol.regwrite := false.B
    ex_mem.address            := 0.U
    ex_mem.writedata          := 0.U
    ex_mem.instruction        := 19.U
  } .otherwise {
    ex_mem.mcontrol.memread   := id_ex.mcontrol.memread
    ex_mem.mcontrol.memwrite  := id_ex.mcontrol.memwrite
    ex_mem.exmem_taken := id_ex.excontrol.jump(1) || branchCtrl.io.taken  
    ex_mem.wbcontrol.toreg    := id_ex.wbcontrol.toreg
    ex_mem.wbcontrol.regwrite := id_ex.wbcontrol.regwrite
    ex_mem.address            := alu.io.result
    ex_mem.writedata          := forwardinputymux
    ex_mem.instruction        := id_ex.instruction
  }
  
  when (branchCtrl.io.taken || id_ex.excontrol.jump === 2.U) {
    ex_mem.next_pc := branchAdd.io.result
  } .elsewhen (id_ex.excontrol.jump === 3.U) {
    ex_mem.next_pc := alu.io.result & Cat(Fill(31,1.U), 0.U)
  } 
  printf(p"ALU INPUT Y: $alu_inputy \n") 
  printf(p"ALU INPUT X: $alu_inputx \n")
  printf(p"EX/MEM: $ex_mem\n")

  /////////////////////////////////////////////////////////////////////////////
  // MEM STAGE
  /////////////////////////////////////////////////////////////////////////////

  val rd = ex_mem.instruction(11,7)
  val maskmode = ex_mem.instruction(13,12)
  val sext = ex_mem.instruction(14)
  io.dmem.address   := ex_mem.address
  io.dmem.writedata := ex_mem.writedata
  io.dmem.memread   := ex_mem.mcontrol.memread
  io.dmem.memwrite  := ex_mem.mcontrol.memwrite
  io.dmem.maskmode  := maskmode
  io.dmem.sext      := ~sext

  pc := next_pc

  hazard.io.exmem_taken := ex_mem.exmem_taken

  forwarding.io.exmemrd := rd
  forwarding.io.exmemrw := ex_mem.wbcontrol.regwrite

  mem_wb.pcplusfour     := ex_mem.pcplusfour
  mem_wb.address        := ex_mem.address
  mem_wb.instruction    := ex_mem.instruction
  mem_wb.readdata       := io.dmem.readdata

  mem_wb.wbcontrol.toreg    := ex_mem.wbcontrol.toreg
  mem_wb.wbcontrol.regwrite := ex_mem.wbcontrol.regwrite

  // printf(p"MEM/WB: $mem_wb\n")

  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////

  when (mem_wb.wbcontrol.toreg === 1.U) {
    write_data := mem_wb.readdata
  } .elsewhen (mem_wb.wbcontrol.toreg === 2.U) {
    write_data := mem_wb.pcplusfour
  } .otherwise {
    write_data := mem_wb.address
  }

  val writereg = mem_wb.instruction(11,7)
  when (writereg === 0.U) {  
    registers.io.writedata  := 0.U
  } .otherwise {
    registers.io.writedata  := write_data
  }
  registers.io.wen          := mem_wb.wbcontrol.regwrite
  registers.io.writereg     := writereg

  forwarding.io.memwbrd     := writereg
  forwarding.io.memwbrw     := mem_wb.wbcontrol.regwrite

  // printf("aluControl.io.operation:%b\n", aluControl.io.operation)
  // print Hazard
  // printf("pcwrite:\t%b\n", hazard.io.pcwrite)
  // printf("ifid_flush: \t%b\n", hazard.io.ifid_flush)
  // printf("ifid_bubble: \t%b\n", hazard.io.ifid_bubble)
  // printf("idex_memread: \t%b\n", hazard.io.idex_memread)
  // printf("exmem_taken: \t%b\n", hazard.io.exmem_taken)
  // printf("exmem_bubble: \t%b\n", hazard.io.exmem_bubble)
  // printf("idex_bubble: \t%b\n", hazard.io.idex_bubble)

  printf("---------------------------------------------\n")
}
