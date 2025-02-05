#include "pch.h"
#include "PCE/PceControlManager.h"
#include "PCE/Input/PceController.h"
#include "Utilities/Serializer.h"
//#include "PCE/Input/PceTurboTap.h"
//#include "PCE/Input/PceAvenuePad6.h"

PceControlManager::PceControlManager()
{
    _controlDevices.push_back(CreateControllerDevice(0));
}

PceControlManagerState& PceControlManager::GetState()
{
	return _state;
}

// shared_ptr<BaseControlDevice> PceControlManager::CreateControllerDevice(ControllerType type, uint8_t port)
shared_ptr<BaseControlDevice> PceControlManager::CreateControllerDevice(uint8_t port)
{
	// PcEngineConfig& cfg = _emu->GetSettings()->GetPcEngineConfig();
    shared_ptr<BaseControlDevice> device;

	device.reset(new PceController(port,(new PcEngineConfig)->Port1.Keys));// cfg.Port1.Keys));
	// switch(type) {
	// 	default:
	// 	case ControllerType::None: break;

	// 	case ControllerType::PceController: device.reset(new PceController(_emu, port, cfg.Port1.Keys)); break;
	// 	case ControllerType::PceAvenuePad6: device.reset(new PceAvenuePad6(_emu, port, cfg.Port1.Keys)); break;

	// 	case ControllerType::PceTurboTap: {
	// 		ControllerConfig controllers[5];
	// 		std::copy(cfg.Port1SubPorts, cfg.Port1SubPorts + 5, controllers);
	// 		controllers[0].Keys = cfg.Port1.Keys;
	// 		device.reset(new PceTurboTap(_emu, port, controllers));
	// 		break;
	// 	}
	// }

    return device;
//	return 0;
}

shared_ptr<BaseControlDevice> PceControlManager::GetControlDevice(uint8_t port, uint8_t subPort){
    return _controlDevices.at(port);
}


uint8_t PceControlManager::ReadInputPort()
{
//	 SetInputReadFlag();

	 uint8_t result = 0;
	 bool hasController = false;
	 for(shared_ptr<BaseControlDevice>& device : _controlDevices) {
	 	if(device->IsConnected()) {
	 		result |= device->ReadRam(0);
	 		hasController |= device->GetPort() == 0;
	 	}
	 }

	 if(!hasController) {
	 	//When no controller is connected, bottom 4 bits will be open bus
	 	result |= 0x0F;
	 }

	 return result;
//	return 0;
}

void PceControlManager::WriteInputPort(uint8_t value)
{
	 for(shared_ptr<BaseControlDevice>& device : _controlDevices) {
	 	if(device->IsConnected()) {
	 		device->WriteRam(0, value);
	 	}
	 }
}

void PceControlManager::UpdateControlDevices()
{
	// PcEngineConfig& cfg = _emu->GetSettings()->GetPcEngineConfig();
	// if(_emu->GetSettings()->IsEqual(_prevConfig, cfg) && _controlDevices.size() > 0) {
	// 	//Do nothing if configuration is unchanged
	// 	return;
	// }

	// auto lock = _deviceLock.AcquireSafe();

	// ClearDevices();

	// shared_ptr<BaseControlDevice> device(CreateControllerDevice(cfg.Port1.Type, 0));
	// if(device) {
	// 	RegisterControlDevice(device);
	// }
}

 void PceControlManager::Serialize(Serializer& s)
 {
	 if(!s.IsSaving()) {
	 	UpdateControlDevices();
	 }

//	 BaseControlManager::Serialize(s);
	 for(int i = 0; i < (int)_controlDevices.size(); i++) {
	 	SVI(_controlDevices[i]);
	 }
 }
