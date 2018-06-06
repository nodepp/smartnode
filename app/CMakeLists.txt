#指定需要CMAKE的最小版本
cmake_minimum_required(VERSION 3.4.1)


#C 的编译选项是 CMAKE_C_FLAGS
# 指定编译参数，可选
#SET(CMAKE_CXX_FLAGS "-Wno-error=format-security -Wno-error=pointer-sign")

#设置生成的so动态库最后输出的路径
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

#调用指定目录下到cmakelist
add_subdirectory(src/main/cpp/mbedtls)

#设置需要引用的库的类型（动／静态库），库的地址,名称
#add_library(lib SHARED/STATIC IMPORTED)
#set_target_properties(lib PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/lib.a)


#设置头文件搜索路径（和此txt同个路径的头文件无需设置），可选
INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/mbedtls/include)

#指定用到的系统库或者NDK库或者第三方库的搜索路径，可选。
#LINK_DIRECTORIES(/usr/local/lib)


add_library( nodepp
             SHARED
             src/main/cpp/utils.c
             src/main/cpp/xxtea.c
             src/main/cpp/mbed_client.c)

target_link_libraries( nodepp
                       mbedcrypto
                       mbedx509
                       mbedtls
                       log )