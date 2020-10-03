// Tests for Lab 1. Feel free to modify and add more tests here.
// If you name your test class something that ends with "TesterLab1" it will
// automatically be run when you use `Lab1 / test` at the sbt prompt.

package dinocpu

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class MulEightCycleComponent(c: Booth) extends PeekPokeTester(c){
  private val mul = c

  // ********* TEST 1 ********* \\
  poke(mul.io.operation, 1.U)
  poke(mul.io.Mp, "0000000000000001".U)
  poke(mul.io.Mc, "0000000000000001".U)
  step(8)
  expect(mul.io.result, "00000000000000000000000000000001".U, "TEST 1 failed: 1 * 1")

  // ********* TEST 2 ********* \\
  poke(mul.io.operation, 0.U)
  step(1)
  expect(mul.io.result, "00000000000000000000000000000000".U, "TEST 2 failed: reset")

  // ********* TEST 3 ********* \\
  poke(mul.io.operation, 1.U)
  poke(mul.io.Mp, "0000000000000001".U)
  poke(mul.io.Mc, "0000000000011100".U)
  step(8)
  expect(mul.io.result, "00000000000000000000000000011100".U, "TEST 3 failed: 1 * 28")
  poke(mul.io.operation, 0.U)
  step(1)
  // ********* TEST 4 ********* \\
  poke(mul.io.operation, 1.U)
  poke(mul.io.Mp, (65509.U)(16.W))
  poke(mul.io.Mc, (65516.U)(16.W))
  step(8)
  expect(mul.io.result, "00000000000000000000001000011100".U, "TEST 4 failed: -27 * -20\n")
  poke(mul.io.operation, 0.U)
  step(1) 
  // ********* TEST 5 ********* \\
  poke(mul.io.operation, 1.U)
  poke(mul.io.Mp, (65509.U)(16.W))
  poke(mul.io.Mc, (36.U)(16.W))
  step(8)
  expect(mul.io.result, "11111111111111111111110000110100".U, "TEST 5 failed: -27 * 36\n")
  poke(mul.io.operation, 0.U)
  step(1) 
  // // ********* TEST 6 ********* \\
  poke(mul.io.operation, 1.U)
  poke(mul.io.Mp, (892.U)(16.W))
  poke(mul.io.Mc, (65436.U)(16.W))
  step(8)
  expect(mul.io.result, "11111111111111101010001110010000".U, "TEST 6 failed: 892 * -100\n")
  poke(mul.io.operation, 0.U)
  step(1) 
}
class MulEightCycle(c: PipelinedCPU) extends PeekPokeTester(c) {

  val inst_mem = Map(
    0  -> "b00000000001000000000000010010011".U, // R1 <- 2
    4  -> "b00000000010000000000000100010011".U, // R2 <- 4
    8  -> "b00000010001000001000000110110011".U, // R3 <- R1 * R2 
    12 -> "b00000000001100011010000000100011".U, // Store R3 to expose it to cpu io
    16 -> "b00000000000000000000000000000000".U, 
    20 -> "b00000000000000000000000000000000".U,
  )

  private val cpu = c
  
  for (cycles <- 0 to 13){
    val instruction = inst_mem(peek(cpu.io.imem.address).toInt)
    poke(cpu.io.imem.instruction, instruction)
    step(1)
  }
  expect(cpu.io.dmem.writedata, "b00000000000000000000000000001000".U, s"$cpu.io.dmem.writedata wrong (is not 8)")
}
class MulSingleCycle(c: PipelinedCPU) extends PeekPokeTester(c) {
  private val cpu = c

  poke(cpu.io.imem.instruction, "b00000000001000000000000010010011".U)
  step(1)
  poke(cpu.io.imem.instruction, "b00000000010000000000000100010011".U)
  step(1)
  poke(cpu.io.imem.instruction, "b00000010001000001000000110110011".U)
  step(1)
  poke(cpu.io.imem.instruction, "b00000000001100011010000000100011".U)
  step(1)
  poke(cpu.io.imem.instruction, "b00000000000000000000000000000000".U)
  step(2)
  expect(cpu.io.dmem.writedata, "b00000000000000000000000000001000".U, s"$cpu.io.dmem.writedata wrong (is not 8)")
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * Mult / testOnly dinocpu.ALUControlTesterLab1
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.ALUControlTesterLab1'
  * }}}
  */
class MulEightCycleTesterMult extends ChiselFlatSpec {
  behavior of "Top"
  val conf = new CPUConfig()
  conf.cpuType = "pipelined"
  // conf.isMulTest = true
  //conf.memFile = "/home/jesse/chisel/CSE125-S20-MUL/src/test/resources/raw/zero.hex"
  conf.debug = true // always run with debug print statements
  "MulEightCycle" should s"mult test" in {
    Driver(() => new PipelinedCPU()(conf)) {
      c => new MulEightCycle(c)
    } should be (true)
  }
}

class MulSingleCycleTesterMult extends ChiselFlatSpec {
  behavior of "Top"
  val conf = new CPUConfig()
  conf.cpuType = "pipelined"
  // conf.isMulTest = true
  //conf.memFile = "/home/jesse/chisel/CSE125-S20-MUL/src/test/resources/raw/zero.hex"
  conf.debug = true // always run with debug print statements
  "MulSingleCycle" should s"mult test" in {
    Driver(() => new PipelinedCPU()(conf)) {
      c => new MulSingleCycle(c)
    } should be (true)
  }
}

class MulEightCycleComponentTesterMult extends ChiselFlatSpec {
  "MulEightCycle" should s"match expectations" in {
    Driver(() => new Booth) {
      c => new MulEightCycleComponent(c)
    } should be (true)
  }
}

class RTypeTesterMult extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.rtype) {
    it should s"run R-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class ITypeTesterMult extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.itype) {
    it should s"run I-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class UTypeTesterMult extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.utype) {
    it should s"run U-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class MemoryTesterMult extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.memory) {
    it should s"run memory-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class RTypeMultiCycleTesterMult extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.rtypeMultiCycle) {
    it should s"run multi cycle R-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class ITypeMultiCycleTesterMult extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.itypeMultiCycle) {
    it should s"run multi cycle I-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class BranchTesterMult extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.branch) {
    it should s"run branch instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class JumpTesterMult extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.jump) {
    it should s"run jump instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}

class MemoryMultiCycleTesterMult extends CPUFlatSpec {
  behavior of "Pipelined CPU"
  for (test <- InstTests.memoryMultiCycle) {
    it should s"run multi cycle memory instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "pipelined") should be(true)
    }
  }
}
