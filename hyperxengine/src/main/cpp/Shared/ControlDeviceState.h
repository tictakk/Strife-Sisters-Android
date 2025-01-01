#pragma once
#include "pch.h"
#include <cstring>
#include "SettingsType.h"
//
// Created by Matthew Kersey on 12/16/24.
//

#ifndef KTNES_CONTROLDEVICESTATE_H
#define KTNES_CONTROLDEVICESTATE_H

struct ControlDeviceState
{
    vector<uint8_t> State;

    bool operator!=(ControlDeviceState &other)
    {
        return State.size() != other.State.size() || memcmp(State.data(), other.State.data(), State.size()) != 0;
    }
};

struct ControllerData
{
    ControllerType Type;
    ControlDeviceState State;
    uint8_t Port;
};


#endif //KTNES_CONTROLDEVICESTATE_H
