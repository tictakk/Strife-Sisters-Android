#pragma once
#include "pch.h"
#include "PCE/PceConstants.h"
#include "PCE/PceTypes.h"
#include "PCE/PcePsgChannel.h"

class PceConsole;
// class SoundMixer;
struct blip_t;

class PcePsg final
{
private:
	static constexpr int MaxSamples = 4000;
	static constexpr int SampleRate = 96000;
	static constexpr int PsgFrequency = PceConstants::MasterClockRate / 6;

	PceConsole* _console = nullptr;
	// SoundMixer* _soundMixer = nullptr;
	PcePsgState _state = {};
	PcePsgChannel _channels[6] = {};
	uint64_t _lastClock = 0;

	int16_t* _soundBuffer = nullptr;
	blip_t* _leftChannel = nullptr;
	blip_t* _rightChannel = nullptr;
	int16_t _prevLeftOutput = 0;
	int16_t _prevRightOutput = 0;

	uint32_t _clockCounter = 0;
	
	void UpdateSoundOffset();

public:
	PcePsg(PceConsole* console);
	~PcePsg();

	bool IsLfoEnabled();
	uint16_t GetLfoFrequency();
	uint32_t GetLfoCh1PeriodOffset();

	PcePsgState& GetState() { return _state; }
	PcePsgChannelState& GetChannelState(uint8_t ch) { return _channels[ch].GetState(); }

	void Write(uint16_t addr, uint8_t value);
	void Run();

	void PlayQueuedAudio();
    int16_t* GetAudioBuffer();
};