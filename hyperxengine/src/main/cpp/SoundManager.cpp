//
// Created by Matthew Kersey on 12/13/24.
//

#include "SoundManager.h"
#include <android/log.h>
#include <oboe/Oboe.h>

using namespace std;

#ifndef MODULE_NAME
#define MODULE_NAME  "HYPERXENGINE"
#endif

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, MODULE_NAME, __VA_ARGS__)

//int16_t *SoundManager::soundBuffer = new int16_t[8000 * 10];//uh..whatever I guess?
//int32_t bufferPos=0;

SoundManager::SoundManager() {
    _bufferSize = 0x10000;
    //sdlmanager
    int bytesPerSample = 2 * (_isStereo? 2 : 1);//isStereo
    int32_t requestedByteLatency = (int32_t)((float)(48000 * 60) / 1000.0f * bytesPerSample);
    _bufferSize = (int32_t)std::ceil((double)requestedByteLatency * 2 / 0x10000) * 0x10000;
    _buffer = new uint8_t[_bufferSize];
    memset(_buffer, 0, _bufferSize);

    _writePosition = 0;
    _readPosition = 0;

    _needReset = false;
}

SoundManager::~SoundManager() {}

void SoundManager::ReadFromBuffer(uint8_t* output, uint32_t len) {
    if(_readPosition + len < _bufferSize) {//There's enough bytes available to read: mk
        memcpy(output, _buffer+_readPosition, len);
        _readPosition += len;
    } else {//There's not enough bytes in the buffer to reach
        int remainingBytes = (_bufferSize - _readPosition);
        memcpy(output, _buffer+_readPosition, remainingBytes);
        memcpy(output+remainingBytes, _buffer, len - remainingBytes);
        _readPosition = len - remainingBytes;
    }

    //doesn't actually do anything right now
    if(_readPosition >= _writePosition && _readPosition - _writePosition < _bufferSize / 2) {
        _bufferUnderrunEventCount++;
    }
}

void SoundManager::WriteToBuffer(uint8_t* input, uint32_t len) {
    if(_writePosition + len < _bufferSize) {
        memcpy(_buffer+_writePosition, input, len);
        _writePosition += len;
    } else {
        int remainingBytes = _bufferSize - _writePosition;
        memcpy(_buffer+_writePosition, input, remainingBytes);
        memcpy(_buffer, ((uint8_t*)input)+remainingBytes, len - remainingBytes);
        _writePosition = len - remainingBytes;
    }
}