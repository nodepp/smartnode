# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
# 压缩级别
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-dontwarn
-dontskipnonpubliclibraryclassmembers
-ignorewarnings
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*


 -keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

# 保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留Parcelable序列化类不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}

# 保留自定义控件（继承自View）不被混淆
-keep class com.nodepp.smartnode.view.**{*;}
# 保留esptouch
#-keep class com.nodepp.smartnode.esptouch.**{*;}
#第三方包
-dontwarn com.tencent.**
-keep class com.tencent.**{*;}

-dontwarn com.iflytek.**
-keep class com.iflytek.**{*;}
-dontwarn com.lidroid.xutils.**
-keep class com.lidroid.xutils.**{*;}
-keep class com.google.zxing.**{*;}
-keep class com.amap.api.**{*;}
-keep class com.loc.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}
-keep class com.qq.**{*;}
-keep class tencent.tls.**{*;}
-keep class org.bouncycastle.**{*;}


-keep class com.nodepp.smartnode.model.**{*;}
-keep class com.nodepp.smartnode.dtls.**{*;}
-keep class com.nodepp.smartnode.esptouch.**{*;}
-keep class nodepp.**{*;}
-keep class outnodepp.**{*;}
-keep class dsig.**{*;}
-keep class msig.**{*;}
#native 方法不被混淆
-keep class com.nodepp.smartnode.utils.Utils  {
    public native static byte[] encrypt(...);
    public native static byte[] decrypt(...);
}
# 泛型与反射
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes *Annotation*
-dontoptimize
-dontpreverify
# 极光推送
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.helpers.JPushMessageReceiver { *; }

-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }