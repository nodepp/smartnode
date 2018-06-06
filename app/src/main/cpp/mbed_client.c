//
//  mbed_client.c
//  test1
//
//  Created by 节点加科技 on 2017/10/24.
//  Copyright © 2017年 节点加科技. All rights reserved.
//


#if !defined(MBEDTLS_CONFIG_FILE)
#include "mbedtls/config.h"
#else
#include MBEDTLS_CONFIG_FILE
#endif

#if defined(MBEDTLS_PLATFORM_C)
#include "mbedtls/platform.h"
#else
#include <stdio.h>
#define mbedtls_printf     printf
#define mbedtls_fprintf    fprintf
#endif

#if !defined(MBEDTLS_SSL_CLI_C) || !defined(MBEDTLS_SSL_PROTO_DTLS) ||    \
!defined(MBEDTLS_NET_C)  || !defined(MBEDTLS_TIMING_C) ||             \
!defined(MBEDTLS_ENTROPY_C) || !defined(MBEDTLS_CTR_DRBG_C) ||        \
!defined(MBEDTLS_X509_CRT_PARSE_C) || !defined(MBEDTLS_RSA_C) ||      \
!defined(MBEDTLS_CERTS_C) || !defined(MBEDTLS_PEM_PARSE_C)
int main( void )
{
    mbedtls_printf( "MBEDTLS_SSL_CLI_C and/or MBEDTLS_SSL_PROTO_DTLS and/or "
                   "MBEDTLS_NET_C and/or MBEDTLS_TIMING_C and/or "
                   "MBEDTLS_ENTROPY_C and/or MBEDTLS_CTR_DRBG_C and/or "
                   "MBEDTLS_X509_CRT_PARSE_C and/or MBEDTLS_RSA_C and/or "
                   "MBEDTLS_CERTS_C and/or MBEDTLS_PEM_PARSE_C not defined.\n" );
    return( 0 );
}
#else

#include <string.h>

#include "mbedtls/net_sockets.h"
#include "mbedtls/debug.h"
#include "mbedtls/ssl.h"
#include "mbedtls/entropy.h"
#include "mbedtls/ctr_drbg.h"
#include "mbedtls/error.h"
#include "mbedtls/certs.h"
#include "mbedtls/timing.h"

#include <android/log.h>
#define LOG_TAG  "mbedtls"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#include "mbed_client.h"

#define SERVER_PORT "20443"
//#define SERVER_NAME "a.nodepp.com"
#define SERVER_ADDR "a.nodepp.com" /* forces IPv4 */
#define MESSAGE     "Echo"

#define READ_TIMEOUT_MS 1000
#define MAX_RETRY       5

#define DEBUG_LEVEL 0
typedef struct _MBEDTLS_SESSION {
    mbedtls_net_context server_fd;
    mbedtls_entropy_context entropy;
    mbedtls_ctr_drbg_context ctr_drbg;
    mbedtls_ssl_context ssl;
    mbedtls_ssl_config conf;
    mbedtls_x509_crt cacert;
    mbedtls_timing_delay_context timer;
} DTLSSession;
static DTLSSession dtlsSession;
static volatile int handshake =-1;//握手返回值，小于0代表不成功，大于等于0代表成功
static void my_debug( void *ctx, int level,
                     const char *file, int line,
                     const char *str )
{
    ((void) level);

    mbedtls_fprintf( (FILE *) ctx, "%s:%04d: %s", file, line, str );
    fflush(  (FILE *) ctx  );
}

int connectDtls(char *address)
{
    int ret;
    char *host;
    if(strlen(address) == 0){
        host = SERVER_ADDR;
    } else{
        host = address;
    }
//    LOGD( " connect host...%s",host);
    const char *pers = "dtls_client";
    /*
     * 0. Initialize the RNG and the session data
     */
    mbedtls_net_init(&(dtlsSession.server_fd));
    mbedtls_ssl_init(&(dtlsSession.ssl));
    mbedtls_ssl_config_init(&(dtlsSession.conf));
    mbedtls_x509_crt_init(&(dtlsSession.cacert));
    mbedtls_ctr_drbg_init(&(dtlsSession.ctr_drbg) );
//    mbedtls_printf( "\n  . Seeding the random number generator..." );
    fflush( stdout );
    mbedtls_entropy_init( (&dtlsSession.entropy) );
    if( ( ret = mbedtls_ctr_drbg_seed( &(dtlsSession.ctr_drbg), mbedtls_entropy_func,&(dtlsSession.entropy),
                                      (const unsigned char *) pers,
                                      strlen( pers ) ) ) != 0 )
    {
        LOGD( " failed\n  ! mbedtls_ctr_drbg_seed returned %d\n", ret );
        exitDtls(ret);
    }
    LOGD( " ok\n" );
    
    /*
     * 0. Load certificates
     */
//    mbedtls_printf( "  . Loading the CA root certificate ..." );
//    fflush( stdout );
    // ret = mbedtls_x509_crt_parse( &cacert, (const unsigned char *) mbedtls_test_cas_pem,
    //                            mbedtls_test_cas_pem_len );
    //if( ret < 0 )
    // {
    //    mbedtls_printf( " failed\n  !  mbedtls_x509_crt_parse returned -0x%x\n\n", -ret );
    //     goto exit;
    // }
    //    mbedtls_printf( " ok (%d skipped)\n", ret );
    
    /*
     * 1. Start the connection
     */
//    LOGD( "  . Connecting to udp/%s/%s...", host, SERVER_PORT );
    LOGD( "  . Connecting to server...");
    fflush( stdout );
    
    if( ( ret = mbedtls_net_connect( &(dtlsSession.server_fd), host,
                                    SERVER_PORT, MBEDTLS_NET_PROTO_UDP ) ) != 0 )
    {
        LOGD( " failed\n  ! mbedtls_net_connect returned %d\n\n", ret );
        exitDtls(ret);
        return -1;
    }
    LOGD( " ok\n" );
    
    /*
     * 2. Setup stuff
     */
    LOGD( "  . Setting up the DTLS structure..." );

    fflush( stdout );
    if( ( ret = mbedtls_ssl_config_defaults( (&dtlsSession.conf),
                                            MBEDTLS_SSL_IS_CLIENT,
                                            MBEDTLS_SSL_TRANSPORT_DATAGRAM,
                                            MBEDTLS_SSL_PRESET_DEFAULT ) ) != 0 )
    {
        LOGD( " failed\n  ! mbedtls_ssl_config_defaults returned %d\n\n", ret );
        exitDtls(ret);
        return -1;
    }
    mbedtls_ssl_conf_authmode(&(dtlsSession.conf), MBEDTLS_SSL_VERIFY_OPTIONAL );
    mbedtls_ssl_conf_ca_chain(&(dtlsSession.conf),&(dtlsSession.cacert), NULL );
    mbedtls_ssl_conf_rng(&(dtlsSession.conf), mbedtls_ctr_drbg_random, &(dtlsSession.ctr_drbg));
    mbedtls_ssl_conf_dbg(&(dtlsSession.conf), my_debug, stdout );
    mbedtls_ssl_conf_read_timeout(&(dtlsSession.conf),1000);
    if( ( ret = mbedtls_ssl_setup( &(dtlsSession.ssl), &(dtlsSession.conf) ) ) != 0 )
    {
        LOGD( " failed\n  ! mbedtls_ssl_setup returned %d\n\n", ret );
        exitDtls(ret);
        return -1;
    }
    
    if( ( ret = mbedtls_ssl_set_hostname(  &(dtlsSession.ssl), host) ) != 0 )
    {
        LOGD( " failed\n  ! mbedtls_ssl_set_hostname returned %d\n\n", ret );
        exitDtls(ret);
        return -1;
    }
    mbedtls_ssl_set_bio(&(dtlsSession.ssl),  &(dtlsSession.server_fd),
                        mbedtls_net_send, mbedtls_net_recv, mbedtls_net_recv_timeout );
    
    mbedtls_ssl_set_timer_cb( &(dtlsSession.ssl),  &(dtlsSession.timer), mbedtls_timing_set_delay,
                             mbedtls_timing_get_delay );
    LOGD( " ok\n" );
    /*
     * 4. Handshake
     */
    LOGD( "  . Performing the DTLS handshake..." );
    fflush( stdout );
    
    do ret = mbedtls_ssl_handshake(  &(dtlsSession.ssl) );
    while( ret == MBEDTLS_ERR_SSL_WANT_READ ||
          ret == MBEDTLS_ERR_SSL_WANT_WRITE );
    
    if( ret != 0 )
    {
        LOGD( " failed\n  ! mbedtls_ssl_handshake returned -0x%x\n\n", -ret );
        exitDtls(-ret);
    }else{
         handshake = 0;
    }
    LOGD( " ok\n" );
    //
    //    /*
    //     * 5. Verify the server certificate
    //     */
    //    mbedtls_printf( "  . Verifying peer X.509 certificate..." );
    //
    //    /* In real life, we would have used MBEDTLS_SSL_VERIFY_REQUIRED so that the
    //     * handshake would not succeed if the peer's cert is bad.  Even if we used
    //     * MBEDTLS_SSL_VERIFY_OPTIONAL, we would bail out here if ret != 0 */
    //    if( ( flags = mbedtls_ssl_get_verify_result(  &(dtlsSession.ssl) ) ) != 0 )
    //    {
    //        char vrfy_buf[512];
    //
    //        mbedtls_printf( " failed\n" );
    //
    //        mbedtls_x509_crt_verify_info( vrfy_buf, sizeof( vrfy_buf ), "  ! ", flags );
    //
    //        mbedtls_printf( "%s\n", vrfy_buf );
    //    }
    //    else
    //        mbedtls_printf( " ok\n" );
    
    
    /* Shell can not handle large exit numbers -> 1 for errors */
//    if( ret < 0 )
//        ret = 1;
    
    return( ret );
}
//int mbedtls_ssl_session_reset( mbedtls_ssl_context *ssl )
//{
//    return( ssl_session_reset_int( ssl, 0 ) );
//}
//void mbedtls_ssl_update_handshake_status( mbedtls_ssl_context *ssl )
int readResponse(unsigned char * buf,size_t dataLength,char * address){
    /*
     * 7. Read the response
     */
    int len,ret =-1;
    int retry_left = MAX_RETRY;
    if(handshake != 0) {
       handshake = connectDtls(address);
    }
    LOGD( "  < Read from server:" );
        fflush( stdout );
        memset( buf, 0,dataLength);
        
        do ret = mbedtls_ssl_read(&(dtlsSession.ssl), buf, dataLength);
        while( ret == MBEDTLS_ERR_SSL_WANT_READ ||ret == MBEDTLS_ERR_SSL_WANT_WRITE );
        if( ret <= 0 )
        {
            switch( ret )
            {
                case MBEDTLS_ERR_SSL_TIMEOUT:
                    LOGD( " timeout\n\n" );
                    break;
                case MBEDTLS_ERR_SSL_PEER_CLOSE_NOTIFY:
                    LOGD( " connection was closed gracefully\n" );
                    closeNotify(ret);
                    break;
                default:
                    LOGD( " mbedtls_ssl_read returned -0x%x\n\n", -ret );
                    exitDtls(ret);
            }
        }
        len = ret;
    return ret;
}
int sendRequest(const void * data, size_t dataLength,char * address){
    /*
     * 6. Write the echo request
     */
    int len,ret=-1;
    if(handshake != 0) {
        handshake = connectDtls(address);
    }
    LOGD( "  > Write to server:" );
    fflush( stdout );
    do ret = mbedtls_ssl_write(&(dtlsSession.ssl), data, dataLength );
    while( ret == MBEDTLS_ERR_SSL_WANT_READ ||
          ret == MBEDTLS_ERR_SSL_WANT_WRITE );
    if( ret < 0 )
    {
        LOGD( " failed\n  ! mbedtls_ssl_write returned %d\n\n", ret );
        exitDtls(ret);
    }
    len = ret;
    LOGD( " %d bytes written\n\n%s\n\n", len, MESSAGE );
    return ret;
}

void closeNotify(int ret){
    /*
     * 8. Done, cleanly close the connection
     */
    LOGD( "  . Closing the connection..." );
    /* No error checking, the connection might be closed already */
    do ret = mbedtls_ssl_close_notify( &(dtlsSession.ssl));
    while( ret == MBEDTLS_ERR_SSL_WANT_WRITE );
    ret = 0;
    LOGD( " done\n" );
}

void exitDtls(int ret){
    /*
     * 9. Final clean-ups and exit
     */
      handshake = ret;
#ifdef MBEDTLS_ERROR_C
    if( ret != 0 )
    {
        char error_buf[100];
        mbedtls_strerror( ret, error_buf, 100 );
        mbedtls_printf( "Last error was: %d - %s\n\n", ret, error_buf );
    }
#endif
    
    mbedtls_net_free(&(dtlsSession.server_fd));
    
    mbedtls_x509_crt_free(&(dtlsSession.cacert));
    mbedtls_ssl_free(&(dtlsSession.ssl));
    mbedtls_ssl_config_free(&(dtlsSession.conf));
    mbedtls_ctr_drbg_free(&(dtlsSession.ctr_drbg));
    mbedtls_entropy_free( &(dtlsSession.entropy));
#if defined(_WIN32)
    mbedtls_printf( "  + Press Enter to exit this program.\n" );
    fflush( stdout ); getchar();
#endif
    
}
#endif /* MBEDTLS_SSL_CLI_C && MBEDTLS_SSL_PROTO_DTLS && MBEDTLS_NET_C &&
MBEDTLD_TIMING_C && MBEDTLS_ENTROPY_C && MBEDTLS_CTR_DRBG_C &&
MBEDTLS_X509_CRT_PARSE_C && MBEDTLS_RSA_C && MBEDTLS_CERTS_C &&
MBEDTLS_PEM_PARSE_C */
