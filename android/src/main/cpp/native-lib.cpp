#include <jni.h>
#include <string>
#include "native-lib.h"

#include "AudioEngine.h"
#include "PCE/PceVpc.h"
#include "PCE/PceConsole.h"

AudioEngine engine;
JavaVM *gJvm = nullptr;
jobject gClassLoader;
jmethodID gFindClassMethod;
PceConsole *pceConsole;
int vscreen[256*240];
SoundManager soundManager;
//int vscreen[682*242+1];
//int *vscreen = new int[256*240];
uint16_t *screen;

void initConsole(){
}

JNIEXPORT jint
JNI_OnLoad(JavaVM *vm, void *reserved) {
  gJvm = vm;
  JNIEnv* env;
  if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
    return -1;
  }
  jclass clsMain = env->FindClass("com/laconic/strifesdroid/app/MainActivity");
  jclass classClass = env->GetObjectClass(clsMain);
  jclass classLoaderClass = env->FindClass("java/lang/ClassLoader");
  jmethodID getClassLoaderMethod =
      env->GetMethodID(classClass, "getClassLoader", "()Ljava/lang/ClassLoader;");
  gClassLoader = env->NewGlobalRef(env->CallObjectMethod(clsMain, getClassLoaderMethod));
  gFindClassMethod =
      env->GetMethodID(classLoaderClass, "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");
  return JNI_VERSION_1_6;
}

std::vector<int> javaArrayToStdVector(JNIEnv *env, jintArray intArray) {
  std::vector<int> v;
  jsize length = env->GetArrayLength(intArray);
  if (length > 0) {
    jboolean isCopy;
    jint *elements = env->GetIntArrayElements(intArray, &isCopy);
    for (int i = 0; i < length; i++) {
      v.push_back(elements[i]);
    }
  }
  return v;
}

std::vector<uint8_t> javaArrayToStdCharVector(JNIEnv *env, jbyteArray intArray) {
    std::vector<uint8_t> v;
    jsize length = env->GetArrayLength(intArray);
    if (length > 0) {
        jboolean isCopy;
        jbyte *elements = env->GetByteArrayElements(intArray, &isCopy);
        for (int i = 0; i < length; i++) {
            v.push_back(elements[i]);
        }
    }
    return v;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_felipecsl_knes_app_MainActivity_stringFromJNI(
        JNIEnv *env,
jobject /* this */) {
    //okay, this works...
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
//JNIEXPORT jintArray JNICALL
JNIEXPORT void JNICALL
Java_com_laconic_strifesdroid_GLSprite_getVideoBuffer(
        JNIEnv *env,
        jobject glsprite /* this */
        ){
    int ARRAY_SIZE = 256 * 240;
    int offset = 16;

    jclass clazz = env->FindClass("com/laconic/strifesdroid/GLSprite");
    jmethodID mid = env->GetMethodID(clazz,"updateScreen","([I)V");

    screen = pceConsole->GetVpc()->_currentOutBuffer;
    jintArray intJavaArrayScreen = env->NewIntArray(ARRAY_SIZE);
    for(int i=0; i<ARRAY_SIZE; i++){
        vscreen[i] = screen[(48+((i/256)*682))+(i%256)];
    }
    //i=7550 is not zero, a lot more
//    env->SetIntArrayRegion(intJavaArray,0,ARRAY_SIZE,
//                           reinterpret_cast<const jint *>(pceConsole->GetVpc()->_currentOutBuffer));

    env->SetIntArrayRegion(intJavaArrayScreen,0,ARRAY_SIZE,vscreen);
    env->CallVoidMethod(glsprite,mid,intJavaArrayScreen);
}

extern "C"
JNIEXPORT void JNICALL
//Java_com_laconic_strifesdroid_app_MainActivity_loadROM(
Java_com_laconic_android_Emulator_loadROM(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray romData
        ){
//    jbyte *rom_data;
//    jbyteArray rom_data;
//    jsize size = env->GetArrayLength(romData);
//    rom_data = (jbyteArray) env->GetByteArrayElements(romData,NULL);
    soundManager = SoundManager();
    pceConsole = new PceConsole(&soundManager,&engine);
    pceConsole->LoadRom(javaArrayToStdCharVector(env,romData));

    //    env->ReleaseByteArrayElements(romData,(jbyte *)rom_data,JNI_ABORT);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_laconic_android_GamepadOverlay_pressButton(
        JNIEnv *env,
        jobject /* this */,
        jint button
        ){
    if(pceConsole != nullptr){
        pceConsole->SetControllerInput(button);//5);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_laconic_android_GamepadOverlay_releaseButton(
        JNIEnv *env,
        jobject /* this */,
        jint button
){
    if(pceConsole != nullptr){
        pceConsole->UnsetControllerInput(button);
    }
}

extern "C"
JNIEXPORT void JNICALL
//Java_com_laconic_strifesdroid_app_MainActivity_runFrame(
Java_com_laconic_android_Emulator_runFrame(
        JNIEnv *env,
        jobject /* this */
        ){
    pceConsole->RunFrame();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_laconic_strifesdroid_JniKt_startAudioEngine(
    JNIEnv *env,
    jobject instance,
    jintArray jCpuIds
) {
  std::vector<int> cpuIds = javaArrayToStdVector(env, jCpuIds);
  engine.start(cpuIds, gJvm, gClassLoader, gFindClassMethod, &soundManager);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_laconic_strifesdroid_JniKt_stopAudioEngine(JNIEnv *env, jobject instance) {
  engine.stop();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_laconic_strifesdroid_JniKt_pauseAudioEngine(JNIEnv *env, jobject instance) {
  engine.pause();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_laconic_strifesdroid_JniKt_resumeAudioEngine(JNIEnv *env, jobject instance) {
  engine.resume();
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_laconic_strifesdroid_app_MainActivity_00024Companion_getAudioBuffer(JNIEnv *env, jobject thiz) {

    int16_t* audioBuffer = pceConsole->GetAudioBuffer();
    int aBuffer[8000];
    for(int i=0; i<8000; i++){
        aBuffer[i] = audioBuffer[i];
    }
    jintArray intJavaArrayScreen = env->NewIntArray(8000);

    env->SetIntArrayRegion(intJavaArrayScreen,0,8000,aBuffer);
    return intJavaArrayScreen;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_laconic_android_Emulator_saveState(JNIEnv *env, jobject thiz) {
    // TODO: implement saveState()
}
extern "C"
JNIEXPORT void JNICALL
Java_com_laconic_android_Emulator_loadState(JNIEnv *env, jobject thiz) {
    // TODO: implement loadState()
}