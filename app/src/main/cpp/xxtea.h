

#ifndef __XXTEA_H__
#define __XXTEA_H__

#include <stdint.h>
#include <stdlib.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief 
 *
 * @param in
 * @param in_len
 * @param key
 * @param out
 * @param out_len
 *
 * @return 
 */
int xxtea_encrypt(const char* in, int in_len, const char *key, char* out, int* out_len);
/**
 * @brief 
 *
 * @param in
 * @param in_len
 * @param key
 * @param out
 * @param out_len
 *
 * @return 
 */
int xxtea_decrypt(const char* in, int in_len, const char *key, char* out, int* out_len);
int encode();
#ifdef __cplusplus
}
#endif

#endif
