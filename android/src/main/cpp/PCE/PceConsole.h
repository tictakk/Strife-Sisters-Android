#pragma once
#include "pch.h"
#include "SoundManager.h"
//#include "AudioEngine.h"
//#include <memory>

class PceCpu;
class PceVdc;
class PceVpc;
class PceVce;
class PcePsg;
class PceTimer;
class PceCdRom;
class PceMemoryManager;
class PceControlManager;
class IPceMapper;
enum class PceConsoleType;

enum class RomFormat
{
    Unknown,

    Sfc,
    Spc,

    Gb,
    Gbs,

    iNes,
    Unif,
    Fds,
    VsSystem,
    VsDualSystem,
    Nsf,
    StudyBox,

    Pce,
    PceCdRom,
    PceHes,

    Sms,
    GameGear,
    Sg,
    ColecoVision,

    Gba,

    Ws,
};

class PceConsole
{
private:
    unique_ptr<PceCpu> _cpu;
    unique_ptr<PceVdc> _vdc;
    unique_ptr<PceVdc> _vdc2;
    unique_ptr<PceVpc> _vpc;
    unique_ptr<PceVce> _vce;
    unique_ptr<PcePsg> _psg;
    unique_ptr<PceTimer> _timer;
    unique_ptr<PceMemoryManager> _memoryManager;
    unique_ptr<PceControlManager> _controlManager;
//    unique_ptr<PceCdRom> _cdrom;
    unique_ptr<IPceMapper> _mapper;
//    unique_ptr<HesFileData> _hesData;
    RomFormat _romFormat = RomFormat::Pce;
//    AudioEngine _engine;

    static bool IsPopulousCard(uint32_t crc32);
    static bool IsSuperGrafxCard(uint32_t crc32);

//    bool LoadHesFile(VirtualFile& hesFile);
//    bool LoadFirmware(DiscInfo& disc, vector<uint8_t>& romData);

public:
    PceConsole(SoundManager *sm);
//    unique_ptr<AudioEngine> _engine;
    SoundManager* _soundManager;
    static vector<string> GetSupportedExtensions() { return { ".pce", ".cue", ".sgx", ".hes" }; }
    static vector<string> GetSupportedSignatures() { return { "HESM" }; }

    // void Serialize(Serializer& s) override;

    void InitializeRam(void* data, uint32_t length);

    void Reset();

    bool LoadRom(vector<uint8_t> romData);

    void RunFrame();

    void ProcessEndOfFrame();

    void SaveBattery();

    PceControlManager* GetControlManager();
    PceCpu* GetCpu();
    PceVdc* GetVdc();
    PceVce* GetVce();
    PceVpc* GetVpc();
    PcePsg* GetPsg();
    PceMemoryManager* GetMemoryManager();

    int16_t* GetAudioBuffer();

    bool IsSuperGrafx() { return _vdc2 != nullptr; }

    uint64_t GetMasterClock();
    uint32_t GetMasterClockRate();
    double GetFps();

    // BaseVideoFilter* GetVideoFilter(bool getDefaultFilter) override;

    // PpuFrameInfo GetPpuFrame();
    uint8_t* GetPpuFrame();
    RomFormat GetRomFormat();

    void InitHesPlayback(uint8_t selectedTrack);
//    AudioTrackInfo GetAudioTrackInfo();
    // void ProcessAudioPlayerAction(AudioPlayerActionParams p);

    // AddressInfo GetAbsoluteAddress(AddressInfo& relAddress) override;
    // AddressInfo GetRelativeAddress(AddressInfo& absAddress, CpuType cpuType) override;

//    PceVideoState GetVideoState();
//    void SetVideoState(PceVideoState& state);
//    void GetConsoleState(BaseState& state, ConsoleType consoleType);
};