//
// Created by Matthew Kersey on 12/13/24.
//
#pragma once
#include "PCE/PceTypes.h"

#ifndef HYPERXENGINE_EMULATORCONNECTOR_H
#define HYPERXENGINE_EMULATORCONNECTOR_H

class SoundManager{
private:
    //from sdlsoundmanager
    bool _needReset = false;
    uint16_t _previousLatency = 0;

    uint8_t* _buffer;// = nullptr;
    uint32_t _writePosition = 0;
    uint32_t _readPosition = 0;

    //from basesoundmanager
    bool _isStereo = true;
    uint32_t _sampleRate = 0;

    double _averageLatency = 0;
    uint32_t _bufferSize;// = 0x10000;
    uint32_t _bufferUnderrunEventCount = 0;

    int32_t _cursorGaps[60];
    int32_t _cursorGapIndex = 0;
    bool _cursorGapFilled = false;

public:
    SoundManager();
    ~SoundManager();

    void ReadFromBuffer(uint8_t* output, uint32_t len);
    void WriteToBuffer(uint8_t* output, uint32_t len);
};

#endif //HYPERXENGINE_EMULATORCONNECTOR_H
