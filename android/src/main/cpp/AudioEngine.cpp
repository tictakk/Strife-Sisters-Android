#include <memory>
#include "AudioEngine.h"
#include "native-lib.h"
//#include "PCE/PceConsole.h"

void AudioEngine::start(
        std::vector<int> cpuIds,
        JavaVM *javaVm,
        jobject classLoader,
        jmethodID findClassMethod,
        SoundManager* sm
) {
    LOGD("AudioEngine start()");
    mCpuIds = cpuIds;
    mJavaVM = javaVm;
    mClassLoader = classLoader;
    mFindClassMethod = findClassMethod;
    AudioStreamBuilder builder;
//    builder.setCallback(this);
    builder.setPerformanceMode(PerformanceMode::LowLatency);
    builder.setUsage(Usage::Game);
//  builder.setFormat(AudioFormat::Float);
    builder.setFormat(AudioFormat::I16);
    builder.setChannelCount(ChannelCount::Stereo);
    builder.setSharingMode(SharingMode::Exclusive);
    builder.setSampleRate(96000);//96000?
    Result result = builder.openStream(&mStream);
    soundManager = sm;

    if (result != Result::OK) {
        LOGE("Failed to open stream. Error: %s", convertToText(result));
        return;
    }
    mStream->requestStart();
}

void AudioEngine::stop() const {
    LOGD("AudioEngine stop()");
    if (mStream != nullptr) {
        mStream->close();
    }
}

void AudioEngine::pause() const {
//    mStream->getAvailableFrames().value();
    LOGD("AudioEngine pause()");
    if (mStream != nullptr) {
        mStream->pause();
    }
}

void AudioEngine::resume() const {
    LOGD("AudioEngine resume()");
    if (mStream != nullptr) {
        mStream->start();
    }
}

void AudioEngine::writeAudioData(const int16_t * audioData, int32_t numFrames){
    if(!mStream) return;
//    LOGI("audio frames: %i",numFrames);
    auto result = mStream->write(audioData,numFrames,0);
    if(result.value() < 0){
        LOGE("Some error");
    }
//    LOGI("XRun cnt: %i",mStream->getXRunCount().value());
}

JNIEnv *AudioEngine::getEnv() {
    JNIEnv *env;
    int status = mJavaVM->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        status = mJavaVM->AttachCurrentThread(&env, nullptr);
        if (status < 0) {
            return nullptr;
        }
    }
    return env;
}

jclass AudioEngine::findClass(JNIEnv *env, const char *name) {
    return static_cast<jclass>(
            env->CallObjectMethod(mClassLoader, mFindClassMethod, env->NewStringUTF(name)));
}

void AudioEngine::maybeInitAudioBufferMethod(JNIEnv *jniEnv) {
    if (mMainActivityClass == nullptr) {
        mMainActivityClass = findClass(jniEnv, "com/felipecsl/knes/app/MainActivity");
        mAudioBufferMethod = jniEnv->GetStaticMethodID(mMainActivityClass, "audioBuffer", "()[F");
    }
}

DataCallbackResult AudioEngine::onAudioReady(
        AudioStream *oboeStream,
        void *audioData,
        int32_t numFrames
) {
    JNIEnv *jniEnv = getEnv();
    if (!mIsThreadAffinitySet) setThreadAffinity();
    maybeInitAudioBufferMethod(jniEnv);
    jboolean isCopy;

//    elements = SoundManager::GetAudioBuffer(numFrames);
//    int length = elements.size();

    auto *floatAudioData = static_cast<int16_t *>(audioData);
    soundManager->ReadFromBuffer((uint8_t *)floatAudioData,numFrames);

//    for (int i = 0; i < numFrames; i++) {
//        floatAudioData[i] = elements[i];//(i >= length)? 0 :  elements[i] * 10;
//    }

    for (int i = numFrames - 1; i >= 0; --i) {
        floatAudioData[i * oboe::ChannelCount::Stereo] = floatAudioData[i];
        floatAudioData[i * oboe::ChannelCount::Stereo + 1] = floatAudioData[i];
    }

    return DataCallbackResult::Continue;
}

/**
 * Set the thread affinity for the current thread to mCpuIds. This can be useful to call on the
 * audio thread to avoid underruns caused by CPU core migrations to slower CPU cores.
 */
void AudioEngine::setThreadAffinity() {
    pid_t current_thread_id = gettid();
    cpu_set_t cpu_set;
    CPU_ZERO(&cpu_set);

    // If the callback cpu ids aren't specified then bind to the current cpu
    if (mCpuIds.empty()) {
        int current_cpu_id = sched_getcpu();
        LOGV("Current CPU ID is %d", current_cpu_id);
        CPU_SET(current_cpu_id, &cpu_set);
    } else {

        for (int cpu_id : mCpuIds) {
            LOGV("CPU ID %d added to cores set", cpu_id);
            CPU_SET(cpu_id, &cpu_set);
        }
    }

    int result = sched_setaffinity(current_thread_id, sizeof(cpu_set_t), &cpu_set);
    if (result == 0) {
        LOGV("Thread affinity set");
    } else {
        LOGW("Error setting thread affinity. Error no: %d", result);
    }

    mIsThreadAffinitySet = true;
}
