#include <android/log.h>
#include "com_nodepp_smartnode_utils_Utils.h"
#import "xxtea.h"
#import "mbed_client.h"
#define LOG_TAG  "jni"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

JNIEXPORT jbyteArray JNICALL
Java_com_nodepp_smartnode_utils_Utils_encrypt(JNIEnv *env, jobject instance, jbyteArray data_,
                                              jbyteArray key_) {
//    LOGD("--------_encrypt--------------");
    char *data = (char *) (*env)->GetByteArrayElements(env, data_, NULL);
    char *key = (char *) (*env)->GetByteArrayElements(env, key_, NULL);
    int in_len = (*env)->GetArrayLength(env, data_); // byte数组的长度
    int out_len = in_len+32;
    char *data_out = (char *)malloc(out_len* sizeof(char));
    int encryp_code = xxtea_encrypt(data, in_len, key, data_out, &out_len);
    jbyteArray result = (*env)->NewByteArray(env, out_len);
    (*env)->SetByteArrayRegion(env, result, 0, out_len, (jbyte *) data_out);
    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    free(data_out);
    return result;
}

JNIEXPORT jbyteArray JNICALL Java_com_nodepp_smartnode_utils_Utils_decrypt(JNIEnv *env,
                                                                           jobject instance,
                                                                           jbyteArray data_,
                                                                           jbyteArray key_) {
//    LOGD("--------decrypt--------------");
    char *data = (char *) (*env)->GetByteArrayElements(env, data_, NULL);
    char *key = (char *) (*env)->GetByteArrayElements(env, key_, NULL);
    int in_len = (*env)->GetArrayLength(env, data_); // byte数组的长度
    int out_len = in_len+32;
    char data_out[out_len];
    int decryp_code = xxtea_decrypt(data, in_len, key, data_out, &out_len);
    jbyteArray result = (*env)->NewByteArray(env, out_len);
    (*env)->SetByteArrayRegion(env, result, 0, out_len, (jbyte *) data_out);
    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    return result;

}

JNIEXPORT void JNICALL
Java_com_nodepp_smartnode_utils_Utils_disConnect(JNIEnv *env, jclass type) {
     exitDtls(-1);
    // TODO

}

JNIEXPORT void JNICALL
Java_com_nodepp_smartnode_utils_Utils_setConnectAddress(JNIEnv *env, jclass type,
                                                        jstring address_) {
    const char *address = (*env)->GetStringUTFChars(env, address_, 0);
    LOGD("address===========%s",address);
    (*env)->ReleaseStringUTFChars(env, address_, address);
}


JNIEXPORT jint JNICALL
Java_com_nodepp_smartnode_utils_Utils_connect(JNIEnv *env, jclass type, jstring address_) {
    const char *address = (*env)->GetStringUTFChars(env, address_, 0);
    jint result =connectDtls(address);
    (*env)->ReleaseStringUTFChars(env, address_, address);
    return result;
}

JNIEXPORT jbyteArray JNICALL
Java_com_nodepp_smartnode_utils_Utils_send(JNIEnv *env, jclass type, jstring address_,
                                           jbyteArray sendData_, jint dataLen, jint maxReadLen) {
    const char *address = (*env)->GetStringUTFChars(env, address_, 0);
    char *sendData = (char *) (*env)->GetByteArrayElements(env, sendData_, NULL);
    int ret = sendRequest(sendData,dataLen,address);
    LOGD("--------send-----ret===========%d",ret);
    (*env)->ReleaseByteArrayElements(env, sendData_, sendData, 0);
    if(ret < 0){
        ret = sendRequest(sendData,dataLen,address);//重试一次
    }
    if(ret < 0){
        exitDtls(-1);
        return NULL;
    } else{
        unsigned char buf[maxReadLen];
        for (int i = 0; i < 3; ++i) {//重试，最多3次
            ret = sendRequest(sendData,dataLen,address);//重试
            LOGD("--------retry-----=========%d", i);
            if (ret < 0){
                break;
            } else{
                ret = readResponse(buf,maxReadLen,address);
                if (ret > 0){
                    break;
                }
            }

        }
        (*env)->ReleaseStringUTFChars(env, address_, address);
        LOGD("--------read-----ret=========%d",ret);
        if (ret > 0){
            jbyteArray result = (*env)->NewByteArray(env, ret);
            //赋值
            (*env)->SetByteArrayRegion(env, result, 0, ret, (jbyte *) buf);
            return result;
        } else{
            exitDtls(-1);//结束会话
            return NULL;
        }

    }

}

JNIEXPORT jbyteArray JNICALL
Java_com_nodepp_smartnode_utils_Utils_receive(JNIEnv *env, jclass type, jstring address_,
                                              jint maxReadLen) {
    const char *address = (*env)->GetStringUTFChars(env, address_, 0);
    int ret = -1;
    unsigned char buf[maxReadLen];
    ret = readResponse(buf,maxReadLen,address);
    (*env)->ReleaseStringUTFChars(env, address_, address);
    if (ret > 0){
        jbyteArray result = (*env)->NewByteArray(env, ret);
        //赋值
        (*env)->SetByteArrayRegion(env, result, 0, ret, (jbyte *) buf);
        return result;
    } else{
        return NULL;
    }


}