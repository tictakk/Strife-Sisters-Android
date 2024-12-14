#pragma once
#include "pch.h"
#include "PCE/PceTypes.h"

class PceControlManager
{
private:
	PceControlManagerState _state = {};
//	PcEngineConfig _prevConfig = {};

public:
	PceControlManager();
	
	PceControlManagerState& GetState();

	// shared_ptr<BaseControlDevice> CreateControllerDevice(ControllerType type, uint8_t port) override;
	uint8_t CreateControllerDevice(u_int8_t type, uint8_t port);

	uint8_t ReadInputPort();
	void WriteInputPort(uint8_t value);
	void UpdateControlDevices();
    void reset(PceControlManager controlManager);

	// void Serialize(Serializer& s);
};