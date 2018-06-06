//
//  mbed_client.h
//  test1
//
//  Created by 节点加科技 on 2017/10/24.
//  Copyright © 2017年 节点加科技. All rights reserved.
//

#ifndef mbed_client_h
#define mbed_client_h

#include <stdio.h>

#endif /* mbed_client_h */
int connectDtls(char *address);
int readResponse(unsigned char * buf,size_t dataLength,char * address);
int sendRequest(const void * data, size_t dataLength,char * address);
void exitDtls(int ret);
void closeNotify(int ret);
