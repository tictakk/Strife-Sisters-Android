#include "pch.h"
#include "PCE/PceConsole.h"
#include "PCE/PceMemoryManager.h"
#include "PCE/PceVpc.h"
#include "PCE/PceVce.h"
#include "PCE/PceTimer.h"
#include "PCE/PcePsg.h"
#include "PCE/PceCpu.h"
#include "PCE/PceControlManager.h"
//#include "PCE/CdRom/PceCdRom.h"

PceMemoryManager::PceMemoryManager(PceConsole* console, PceVpc* vpc, PceVce* vce, PceControlManager* controlManager, PcePsg* psg, PceTimer* timer, IPceMapper* mapper, vector<uint8_t>& romData, uint32_t cardRamSize, bool cdromUnitEnabled)
{
	_console = console;
	_vpc = vpc;
	_vce = vce;
	_psg = psg;
	_timer = timer;
	_mapper = mapper;
	_controlManager = controlManager;
	_prgRomSize = (uint32_t)romData.size();
	_prgRom = new uint8_t[_prgRomSize];
	_workRamSize = console->IsSuperGrafx() ? 0x8000 : 0x2000;
	_workRam = new uint8_t[_workRamSize];

	_unmappedBank = new uint8_t[0x2000];
	memset(_unmappedBank, 0xFF, 0x2000);

	memcpy(_prgRom, romData.data(), _prgRomSize);

	_console->InitializeRam(_workRam, _workRamSize);

	_cdromUnitEnabled = cdromUnitEnabled;

	if(cardRamSize > 0) {
		if(cardRamSize == 0x30000) {
			//Super/Arcade CD-ROM
			_cardRamStartBank = 0x68;
			_cardRamEndBank = 0x7F;
		} else if(cardRamSize == 0x8000) {
			//Populous
			_cardRamStartBank = 0x40;
			_cardRamEndBank = 0x5F;
		}

		_cardRamSize = cardRamSize;
		_cardRam = new uint8_t[cardRamSize];
		_console->InitializeRam(_cardRam, _cardRamSize);
//		_emu->RegisterMemory(MemoryType::PceCardRam, _cardRam, _cardRamSize);
	}

//	_emu->RegisterMemory(MemoryType::PcePrgRom, _prgRom, _prgRomSize);
//	_emu->RegisterMemory(MemoryType::PceWorkRam, _workRam, _workRamSize);

//	_cdromRam = (uint8_t*)_emu->GetMemory(MemoryType::PceCdromRam).Memory;
//	_saveRam = (uint8_t*)_emu->GetMemory(MemoryType::PceSaveRam).Memory;

	//CPU boots up in slow speed
	_state.FastCpuSpeed = false;
	UpdateExecCallback();

	//Set to 0 on power on
	_state.DisabledIrqs = 0;

	//MPR 7 is set to 0 on power on
	_state.Mpr[7] = 0;

//	if(_emu->GetSettings()->GetPcEngineConfig().EnableRandomPowerOnState) {
//		//Other MPR registers are random
//		//Note this appears to break some games?
//		for(int i = 0; i < 7; i++) {
//			_state.Mpr[i] = RandomHelper::GetValue(0, 255);
//		}
//	}

	for(int i = 0; i <= 0xFF; i++) {
		_bankMask[i] = 0x1FFF;
	}
	
	//Save RAM is 2KB, not 8KB
	_bankMask[0xF7] = 0x7FF;

	//Map ROM to all 128 banks
	uint32_t bankCount = _prgRomSize / 0x2000;

	if(bankCount == 0x30) {
		//384KB games - Mirrored as 256x2, 128x4
		uint32_t bankOffsets[8] = { 0, 0x10, 0, 0x10, 0x20, 0x20, 0x20, 0x20 };
		UpdateMappings(bankOffsets);
	} else if(bankCount == 0x40) {
		//512KB games - Mirrored as either 256x2 + 256x2, or 512x2
		//Mirror them as 256 + 256x3 for compatibility
		uint32_t bankOffsets[8] = { 0, 0x10, 0x20, 0x30, 0x20, 0x30, 0x20, 0x30 };
		UpdateMappings(bankOffsets);
	} else if(bankCount == 0x60) {
		//768KB games - Mirrored as 512x1 + 256x2
		uint32_t bankOffsets[8] = { 0, 0x10, 0x20, 0x30, 0x40, 0x50, 0x40, 0x50 };
		UpdateMappings(bankOffsets);
	} else {
		//Use default mirroring for anything else
		uint32_t bankOffsets[8] = { 0, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70 };
		UpdateMappings(bankOffsets);
	}
}

PceMemoryManager::~PceMemoryManager()
{
	delete[] _prgRom;
	delete[] _workRam;
	delete[] _cardRam;
	delete[] _unmappedBank;
}

void PceMemoryManager::SetSpeed(bool slow)
{
	_state.FastCpuSpeed = !slow;
	UpdateExecCallback();
}

void PceMemoryManager::UpdateMappings(uint32_t bankOffsets[8])
{
	int bankCount = _prgRomSize / 0x2000;
	for(int i = 0; i <= 0x7F; i++) {
		//00 - 7F
		int bank = (bankOffsets[i >> 4] + (i & 0x0F)) % bankCount;
		_readBanks[i] = _prgRom + (bank * 0x2000);
		_bankMemType[i] = MemoryType::PcePrgRom;
	}

	for(int i = 0x80; i <= 0xFF; i++) {
		//80 - FF
		_readBanks[i] = _unmappedBank;
		_bankMemType[i] = MemoryType::None;
	}

	for(int i = 0; i < 4; i++) {
		//F8 - FB
		_readBanks[0xF8 + i] = _workRam + ((i * 0x2000) % _workRamSize);
		_writeBanks[0xF8 + i] = _workRam + ((i * 0x2000) % _workRamSize);
		_bankMemType[0xF8 + i] = MemoryType::PceWorkRam;
	}

	if(_cardRam) {
		for(uint32_t i = _cardRamStartBank; i <= _cardRamEndBank; i++) {
			//68-7F (cdrom) or 40-5F (populous)
			_readBanks[i] = _cardRam + (((i - _cardRamStartBank) * 0x2000) % _cardRamSize);
			_writeBanks[i] = _cardRam + (((i - _cardRamStartBank) * 0x2000) % _cardRamSize);
			_bankMemType[i] = MemoryType::PceCardRam;
		}
	}

	UpdateCdRomBanks();
}

void PceMemoryManager::UpdateCdRomBanks()
{
//	if(_cdrom) {
//		_cdrom->InitMemoryBanks(_readBanks, _writeBanks, _bankMemType, _unmappedBank);
//	}
}

void PceMemoryManager::UpdateExecCallback()
{
//	if(_cdromUnitEnabled) {
//		if(_console->IsSuperGrafx()) {
//			_exec = &PceMemoryManager::ExecTemplate<true, true>;
//		} else {
//			_exec = &PceMemoryManager::ExecTemplate<true, false>;
//		}
//	} else {
		if(_console->IsSuperGrafx()) {
			_exec = &PceMemoryManager::ExecTemplate<false, true>;
		} else {
			_exec = &PceMemoryManager::ExecTemplate<false, false>;
		}
//	}

	_fastExec = _exec;
	if(!_state.FastCpuSpeed) {
		_exec = &PceMemoryManager::ExecSlow;
	}
}

template<bool hasCdRom, bool isSuperGrafx>
void PceMemoryManager::ExecTemplate()
{
	_state.CycleCount += 3;
	_timer->Exec();

	if constexpr(isSuperGrafx) {
		_vpc->ExecSuperGrafx();
	} else {
		_vpc->Exec();
	}

//	if constexpr(hasCdRom) {
//		_cdrom->Exec();
//	}
}

void PceMemoryManager::ExecSlow()
{
	(this->*_fastExec)();
	(this->*_fastExec)();
	(this->*_fastExec)();
	(this->*_fastExec)();
}

uint8_t PceMemoryManager::ReadRegister(uint16_t addr)
{
	if(addr <= 0x3FF) {
		//VDC
		Exec(); //CPU is delayed by 1 CPU cycle when reading/writing to VDC/VCE
		return _vpc->Read(addr);
	} else if(addr <= 0x7FF) {
		//VCE
		Exec(); //CPU is delayed by 1 CPU cycle when reading/writing to VDC/VCE
		_vpc->DrawScanline();
		return _vce->Read(addr);
	} else if(addr <= 0xBFF) {
		//PSG
		return _state.IoBuffer;
	} else if(addr <= 0xFFF) {
		//Timer
		_state.IoBuffer = (_state.IoBuffer & 0x80) | (_timer->Read(addr) & 0x7F);
		return _state.IoBuffer;
	} else if(addr <= 0x13FF) {
		//IO			

		//Some NA games crash is bit 6 is not set (region locking) - e.g Order of the Griffon
		//When in Auto mode, pretend this is a turbografx console for compatibilty
//		PcEngineConfig& cfg = _emu->GetSettings()->GetPcEngineConfig();
//		PceConsoleType consoleType = cfg.ConsoleType;
//		bool isTurboGrafx = consoleType == PceConsoleType::Auto || consoleType == PceConsoleType::TurboGrafx;

		//Some games use the CDROM addon's save ram to save data and will check bit 7 for presence of the CDROM
		//Unless the UI option to disable the save ram for hucard games is enabled, always report that the CDROM is connected
		bool cdromConnected = false;//_cdrom != nullptr;

		_state.IoBuffer = (
			(cdromConnected ? 0 : 0x80) |
			(true ? 0 : 0x40) |
			0x30 |
			_controlManager->ReadInputPort()
			);

		return _state.IoBuffer;
	} else if(addr <= 0x17FF) {
		//IRQ
		switch(addr & 0x03) {
			case 0:
			case 1:
				break;

			case 2: _state.IoBuffer = (_state.IoBuffer & 0xF8) | (_state.DisabledIrqs & 0x07); break;
			case 3: _state.IoBuffer = (_state.IoBuffer & 0xF8) | (_state.ActiveIrqs & 0x07); break;
		}
		return _state.IoBuffer;
	} else if(addr <= 0x1BFF) {
		return 0xFF;//return _cdrom ? _cdrom->Read(addr) : 0xFF;
	} else if(_console->GetRomFormat() == RomFormat::PceHes && addr <= 0x1C01) {
		//Infinite BRA loop
		return addr & 0x01 ? 0xFE : 0x80;
	}

//	LogDebug("[Debug] Read - missing handler: $" + HexUtilities::ToHex(addr));
	return 0xFF;
}

void PceMemoryManager::WriteRegister(uint16_t addr, uint8_t value)
{
	if(addr <= 0x3FF) {
		_console->GetCpu()->RunIdleCpuCycle(); //CPU is delayed by 1 CPU cycle when reading/writing to VDC/VCE
		_vpc->Write(addr, value);
	} else if(addr <= 0x7FF) {
		_console->GetCpu()->RunIdleCpuCycle(); //CPU is delayed by 1 CPU cycle when reading/writing to VDC/VCE
		_vpc->DrawScanline();
		_vce->Write(addr, value);
	} else if(addr <= 0xBFF) {
		//PSG
		_psg->Write(addr, value);
		_state.IoBuffer = value;
	} else if(addr <= 0xFFF) {
		//Timer
		_timer->Write(addr, value);
		_state.IoBuffer = value;
	} else if(addr <= 0x13FF) {
		//IO
		_controlManager->WriteInputPort(value);
		_state.IoBuffer = value;
	} else if(addr <= 0x17FF) {
		//IRQ
		switch(addr & 0x03) {
			case 0:
			case 1:
//				LogDebug("[Debug] Write - missing handler: $" + HexUtilities::ToHex(addr) + " = " + HexUtilities::ToHex(value));
				break;

			case 2: _state.DisabledIrqs = value & 0x07; break;
			case 3: ClearIrqSource(PceIrqSource::TimerIrq); break;
		}
		_state.IoBuffer = value;
	} else if(addr <= 0x1BFF) {
//		if(_cdrom) {
//			_cdrom->Write(addr, value);
//		}
	} else {
//		LogDebug("[Debug] Write - missing handler: $" + HexUtilities::ToHex(addr) + " = " + HexUtilities::ToHex(value));
	}
}

void PceMemoryManager::WriteVdc(uint16_t addr, uint8_t value)
{
//	if(_emu->ProcessMemoryWrite<CpuType::Pce>(addr, value, MemoryOperationType::Write)) {
		_console->GetCpu()->RunIdleCpuCycle(); //CPU is delayed by 1 CPU cycle when reading/writing to VDC/VCE
		_vpc->StVdcWrite(addr, value);
//	}
}

uint8_t PceMemoryManager::DebugRead(uint16_t addr)
{
	uint8_t bank = _state.Mpr[(addr & 0xE000) >> 13];
	if(bank != 0xFF) {
		return _readBanks[bank][addr & _bankMask[bank]];
	} else {
		//TODO read registers without side effects
		if(_console->GetRomFormat() == RomFormat::PceHes && addr >= 0x1C00 && addr <= 0x1C01) {
			//Patch to fix trace logger for HES files
			return addr & 0x01 ? 0xFE : 0x80;
		}
	}
	return 0xFF;
}

void PceMemoryManager::DebugWrite(uint16_t addr, uint8_t value)
{
	uint8_t bank = _state.Mpr[(addr & 0xE000) >> 13];
	if(bank != 0xFF) {
		uint8_t* data = _writeBanks[bank];
		if(data) {
			data[addr & _bankMask[bank]] = value;
		}
	} else {
		//TODO write registers
	}
}

void PceMemoryManager::SetMprValue(uint8_t regSelect, uint8_t value)
{
	if(regSelect == 0) {
		return;
	}

	_state.MprReadBuffer = value;

	for(int i = 0; i < 8; i++) {
		if(regSelect & (1 << i)) {
			_state.Mpr[i] = value;
		}
	}
}

uint8_t PceMemoryManager::GetMprValue(uint8_t regSelect)
{
	if(regSelect == 0) {
		//"If an operand of $00 is used, the accumulator is loaded with the last value that was written with TAM"
		return _state.MprReadBuffer;
	}

	uint8_t value = 0;
	for(int i = 0; i < 8; i++) {
		//"If multiple bits are set in the operand to TMA, the values from several MPRs are combined togetherand returned."
		if(regSelect & (1 << i)) {
			value |= _state.Mpr[i];
		}
	}

	_state.MprReadBuffer = value;
	return value;
}

AddressInfo PceMemoryManager::GetAbsoluteAddress(uint32_t relAddr)
{
	uint8_t bank = _state.Mpr[(relAddr & 0xE000) >> 13];
	uint32_t absAddr;
	uint16_t mask = _bankMask[bank];
	switch(_bankMemType[bank]) {
		case MemoryType::PcePrgRom: absAddr = (uint32_t)(_readBanks[bank] - _prgRom) + (relAddr & mask); break;
		case MemoryType::PceWorkRam: absAddr = (uint32_t)(_readBanks[bank] - _workRam) + (relAddr & mask); break;
		case MemoryType::PceSaveRam: absAddr = (uint32_t)(_readBanks[bank] - _saveRam) + (relAddr & mask); break;
//		case MemoryType::PceCdromRam: absAddr = (uint32_t)(_readBanks[bank] - _cdromRam) + (relAddr & mask); break;
		case MemoryType::PceCardRam: absAddr = (uint32_t)(_readBanks[bank] - _cardRam) + (relAddr & mask); break;
		default: return { -1, MemoryType::None };
	}
	return { (int32_t)absAddr, _bankMemType[bank] };
}

AddressInfo PceMemoryManager::GetRelativeAddress(AddressInfo absAddr)
{
	for(int i = 0; i < 8; i++) {
		AddressInfo bankStart = GetAbsoluteAddress(i * 0x2000);
		if(bankStart.Type == absAddr.Type && bankStart.Address == (absAddr.Address & ~0x1FFF)) {
			return { (i << 13) | (absAddr.Address & 0x1FFF), MemoryType::PceMemory };
		}
	}

	return { -1, MemoryType::None };
}
