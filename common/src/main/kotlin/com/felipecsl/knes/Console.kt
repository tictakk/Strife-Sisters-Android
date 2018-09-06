package com.felipecsl.knes

internal class Console(
    val cpu: CPU,
    val apu: APU,
    val ppu: PPU,
    val cartridge: Cartridge,
    val controller1: Controller,
    val controller2: Controller,
    val mapper: Mapper,
    val sprite: Sprite,
    val ram: IntArray = IntArray(2048)
) {
  fun step(): Long {
    val cpuCycles = cpu.step()
    for (it in 0 until cpuCycles * 3) {
      if (!ppu.step()) {
        mapper.step()
      }
    }
    for (it in 0 until cpuCycles) {
      apu.step()
    }
    sprite.setImage(ppu.front)
    return cpuCycles
  }

  fun reset() {
    cpu.reset()
  }

  companion object {
    fun newConsole(
        cartridge: Cartridge,
        sprite: Sprite,
        bitmapFactory: (Int, Int) -> Bitmap,
        mapperCallback: MapperStepCallback? = null,
        cpuCallback: CPUStepCallback? = null,
        ppuCallback: PPUStepCallback? = null,
        ppu: PPU = PPU(bitmapFactory, ppuCallback),
        apu: APU = APU(),
        cpu: CPU = CPU(cpuCallback),
        mapper: Mapper = Mapper.newMapper(cartridge, cpu, mapperCallback)
    ): Console {
      val console = Console(cpu, apu, ppu, cartridge, Controller(), Controller(), mapper, sprite)
      ppu.console = console
      cpu.console = console
      return console
    }
  }
}
