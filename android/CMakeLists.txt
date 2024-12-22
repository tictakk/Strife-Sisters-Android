project(ktnes)

cmake_minimum_required(VERSION 3.4.1)

add_library( # Specifies the name of the library.
        ktnes-audio
        SHARED
        src/main/cpp/native-lib.cpp
        src/main/cpp/AudioEngine.cpp
        src/main/cpp/pch.cpp
        src/main/cpp/PCE/PceConsole.cpp
        src/main/cpp/PCE/PceTimer.cpp
        src/main/cpp/PCE/PceMemoryManager.cpp
        src/main/cpp/PCE/PceControlManager.cpp
        src/main/cpp/PCE/PceVce.cpp
        src/main/cpp/PCE/PceVpc.cpp
        src/main/cpp/PCE/PcePsg.cpp
        src/main/cpp/PCE/Timer.cpp
        src/main/cpp/PCE/PceVdc.cpp
        src/main/cpp/PCE/PceCpu.cpp
        src/main/cpp/PCE/PcePsgChannel.cpp
        src/main/cpp/PCE/PceCpu.Instructions.cpp
        src/main/cpp/PCE/blip_buf.cpp
        src/main/cpp/SoundManager.cpp
        src/main/cpp/Shared/SettingsType.cpp
        src/main/cpp/Shared/ControlDeviceState.cpp
        src/main/cpp/Shared/BaseControlDevice.cpp
        src/main/cpp/PCE/PceControlManager.cpp
)
#        src/main/cpp/Shared/Audio/SoundMixer.cpp

find_package (oboe REQUIRED CONFIG)

target_link_libraries(ktnes-audio log oboe::oboe)

include_directories("src/main/cpp", "src/main/cpp/PCE", .)