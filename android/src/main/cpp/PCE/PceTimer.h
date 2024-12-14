#pragma once
#include "pch.h"
#include "PCE/PceTypes.h"

class PceConsole;

class PceTimer final
{
private:
	PceTimerState _state = {};
	PceConsole* _console = nullptr;

public:
	PceTimer(PceConsole* console);

	PceTimerState& GetState() { return _state; }

	void Exec();

	void Write(uint16_t addr, uint8_t value);
	uint8_t Read(uint16_t addr);
//    void reset();
};
