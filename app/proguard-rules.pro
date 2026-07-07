# Add project specific ProGuard rules here.
#<<<<<<< HEAD
# By default, the flags in this file are appended to flags specified
# in C:\tools\adt-bundle-windows-x86_64-20131030\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
##=======
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#>>>>>>> 6195274 (新增路径管理功能：保存用户添加过的路径，一键选择下载路径)
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

#<<<<<<< HEAD
# Add any project specific keep options here:

#=======
#>>>>>>> 6195274 (新增路径管理功能：保存用户添加过的路径，一键选择下载路径)
# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#<<<<<<< HEAD
-dontnote **
-dontwarn **

#-packageobfuscationdictionary ConfusionDictionary.txt
#-classobfuscationdictionary ConfusionDictionary.txt
#-obfuscationdictionary ConfusionDictionary.txt
##=======
# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#>>>>>>> 6195274 (新增路径管理功能：保存用户添加过的路径，一键选择下载路径)
# ========== 保留 Gson 泛型支持 ==========

# app/proguard-rules.pro

# ========== 基础保留 ==========

# 保留行号信息（方便调试）
-keepattributes SourceFile,LineNumberTable

# 保留注解
-keepattributes *Annotation*

# 保留泛型签名（Gson 需要）
-keepattributes Signature

# ========== Gson 相关 ==========

-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * implements com.google.gson.TypeAdapterFactory { *; }

# ========== 你的实体类 ==========

# 替换 com.example.chinesetoy.entity 为你的实际包名
-keep class com.example.chinesetoy.** { *; }
-keep class com.example.chinesetoy.model.** { *; }

# 特别保留 UserPaths（如果它在其他包中）
-keep class com.example.chinesetoy.UserPaths { *; }

# ========== TinyPinyin 相关 ==========

-keep class com.github.promeg.pinyinhelper.** { *; }
-keep class com.github.promeg.tinypinyin.** { *; }

# ========== 保留所有实现了 Serializable 的类 ==========

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ========== 忽略警告 ==========

-dontwarn com.google.gson.**
-dontwarn okhttp3.**
-dontwarn okio.**
