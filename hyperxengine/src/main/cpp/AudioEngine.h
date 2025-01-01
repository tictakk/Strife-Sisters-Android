#ifndef HYPERXENGINE_AUDIOENGINE_H
#define HYPERXENGINE_AUDIOENGINE_H

#include <oboe/Oboe.h>
#include <vector>
#include <jni.h>
#include <android/log.h>
#include <deque>
#include "SoundManager.h"

#ifndef MODULE_NAME
#define MODULE_NAME  "HYPERXENGINE"
#endif

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, MODULE_NAME, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, MODULE_NAME, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, MODULE_NAME, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, MODULE_NAME, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, MODULE_NAME, __VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL, MODULE_NAME, __VA_ARGS__)

using namespace oboe;

class AudioEngine : public AudioStreamCallback {

public:
    void start(
            std::vector<int> cpuIds,
            JavaVM *javaVm,
//            jobject classLoader,
//            jmethodID findClassMethod,
            SoundManager* sm
    );

    AudioStream *mStream = nullptr;

    void play(float data);//change back to int

    DataCallbackResult
    onAudioReady(AudioStream *oboeStream, void *audioData, int32_t numFrames) override;
    void stop() const;
    void pause() const;
    void resume() const;

    static void FillAudioBuffer(uint8_t *stream, int len); //this is the callback
    void writeAudioData(const int16_t* audioData, int32_t numFrames);
    ResultWithValue<int32_t> getFramesAvailable();

private:

//    AudioStream *mStream = nullptr; //moved this to public so I can shar <3
    JavaVM *mJavaVM = nullptr;
//    jobject mClassLoader;
//    jmethodID mFindClassMethod;
    std::vector<int> mCpuIds; // IDs of CPU cores which the audio callback should be bound to
    bool mIsThreadAffinitySet = false;
    jclass mMainActivityClass = nullptr;
    jmethodID mAudioBufferMethod = nullptr;
    SoundManager* soundManager = nullptr;

    void setThreadAffinity();
    JNIEnv *getEnv();
    jclass findClass(JNIEnv *env, const char *name);
    void maybeInitAudioBufferMethod(JNIEnv *jniEnv);
};


#endif //HYPERXENGINE_NATIVE_LIB_H