#pragma once
#include "pch.h"
#include "PCE/PceTypes.h"
#include "Shared/BaseControlDevice.h"

class PceControlManager
{
//protected:
//    vector<shared_ptr<BaseControlDevice>> _controlDevices;

private:
	PceControlManagerState _state = {};
//	PcEngineConfig _prevConfig = {};

public:
	PceControlManager();
	
	PceControlManagerState& GetState();

    vector<shared_ptr<BaseControlDevice>> _controlDevices;

    shared_ptr<BaseControlDevice> CreateControllerDevice(uint8_t port);
    shared_ptr<BaseControlDevice> GetControlDevice(uint8_t port, uint8_t subPort);

//	uint8_t CreateControllerDevice(u_int8_t type, uint8_t port);

	uint8_t ReadInputPort();
	void WriteInputPort(uint8_t value);
	void UpdateControlDevices();
    void reset(PceControlManager controlManager);

	// void Serialize(Serializer& s);
};