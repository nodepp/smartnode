#include "xxtea.h"
#include <time.h>
#include  <android/log.h>

#define MX (((z >> 5) ^ (y << 2)) + ((y >> 3) ^ (z << 4))) ^ ((sum ^ y) + (key[(p & 3) ^ e] ^ z))
#define DELTA 0x9e3779b9

#define LOG_TAG  "kk"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)



static int xxtea_uint_encrypt(uint32_t* data, int n, uint32_t* key) {
    if(data == NULL || n < 1 || key == NULL) {
        return -10;
    }
    uint32_t y, z, sum, p, rounds, e;

    rounds = 6 + 52/n;
    sum = 0;
    z = data[n-1];

    do {
        sum += DELTA;
        e = (sum >> 2)&3;
        for(p=0; p < n-1; p++) {
            y = data[p+1];
            z = data[p] += MX;
        }
        y = data[0];
        z = data[n-1] +=MX;
    }while (--rounds);

    return 0;
}

static int xxtea_uint_decrypt(uint32_t* data, int n, uint32_t* key) {
    if(data == NULL || n < 1 || key == NULL) {
        return -10;
    }
    uint32_t y, z, sum, p, rounds, e;

    rounds = 6+52/n;
    sum = rounds*DELTA;
    y = data[0];
    do {
        e = (sum >> 2)&3;

        for (p = n-1; p > 0; p--) {
            z = data[p - 1];
            y = data[p] -= MX;
        }

        z = data[n-1];
        y = data[0] -= MX;
        sum -= DELTA;
    }while (--rounds);

    return 0;
}

int xxtea_encrypt(const char* in, int in_len, const char *key, char* out, int* out_len) {
    if(in == NULL || out == NULL || out_len == NULL) {
        return -10;
    }
    int iPadLen, iTotalLen, i, j, ret;

    iTotalLen = 1+4+5+in_len;

    iPadLen = iTotalLen%8;
    if(iPadLen != 0) {
        iPadLen = 8-iPadLen;
    }

    if(*out_len < (iTotalLen + iPadLen)) {
        return -20;
    }
    *out_len = iTotalLen + iPadLen;

    srand(time(NULL));
    out[0] = (((uint8_t)rand()) & 0x0f8)| (uint8_t)iPadLen;
    i = 1;
    while(iPadLen--) {
        out[i++]=(uint8_t)rand(); /*Padding*/
    }

    *(uint32_t*)(&out[i]) = rand(); /*salt*/
    i += sizeof(uint32_t);

    memcpy(out+i, in, in_len);
    i += in_len;

    out[i++] = 0xaf;
    out[i++] = 0x5a;
    out[i++] = 0xf0;
    out[i++] = 0x05;
    out[i++] = 0xff;

    ret = xxtea_uint_encrypt((uint32_t*)out, (*out_len)/sizeof(uint32_t), (uint32_t*)key);
    if(ret != 0) {
        return -30;
    }

    return 0;
}



int xxtea_decrypt(const char* in, int in_len, const char *key, char* out, int* out_len) {
    if(in == NULL || out == NULL || out_len == NULL) {
        return -10;
    }
    int iPadLen, iHeadLen, iTailLen = 5, iDataLen, ret;

    if(in_len > *out_len) {
        return -20;
    }
    memcpy(out, in, in_len);
    *out_len = in_len;

    ret = xxtea_uint_decrypt((uint32_t*)out, (*out_len)/sizeof(uint32_t), (uint32_t*)key);
    if(ret != 0) {
        return -30;
    }

    iPadLen = out[0]& 0x07;
    iHeadLen = 1+iPadLen+4;
    iDataLen = in_len - iHeadLen - iTailLen;
    if(iDataLen <= 0) {
        return -40;
    }

    if(	out[*out_len-1] != 0xff ||
           out[*out_len-2] != 0x05 ||
           out[*out_len-3] != 0xf0 ||
           out[*out_len-4] != 0x5a ||
           out[*out_len-5] != 0xaf) {
        return -50;
    }

    memmove(out, out+iHeadLen, iDataLen);
    *out_len = iDataLen;
    return 0;
}


int encode() {
    char *text ="节点加科技欢迎你";
    const char *key = "abcdefghijk";
    char szEnData[512], szDeData[512], szDeDataB64[512], szEnDataB64[512];
    int iEnLen = sizeof(szEnData), iDeLen = sizeof(szDeData), iEnB64Len = sizeof(szEnDataB64), iDeB64Len=sizeof(szDeDataB64) ;
    int iRet;
    int len = strlen(text);
    LOGD("data len:%d", len);
    iRet = xxtea_encrypt(text, len, key, szEnData, &iEnLen);
    if(iRet != 0) {
        LOGD("iRet:%d\n", iRet);
        return 0;
    }
    LOGD("xxtea_encrypt text:%s", text);
    LOGD("xxtea_encrypt EnData:%s", szEnData);
    LOGD("xxtea_encrypt iEnLen:%d", iEnLen);
    iRet = xxtea_decrypt(szEnData, iEnLen, key, szDeData, &iDeLen);
    if(iRet != 0) {
        LOGD("iRet:%d\n", iRet);
        return 0;
    }
    LOGD("xxtea_decrypt EnData:%s", szEnData);
    LOGD("xxtea_decrypt DeData:%s", szDeData);
    LOGD("xxtea_decrypt iDeLen:%d\n", iDeLen);
//    char result[iDeLen];
//    memcpy(result, szDeData, iDeLen);
//    result[iDeLen]='\0';
//    LOGD("xxtea_encrypt EnData:%s", result);
    if (strncmp(text, szDeData, iDeLen) == 0) {
        LOGD("xxtea decrypt success!\n");
    }
    else {
        LOGD("xxtea decrypt fail!\n");
    }

    return 0;
}

