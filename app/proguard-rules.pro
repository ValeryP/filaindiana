# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Crashlytics keep classes
-keepattributes *Annotation*
-keep public class * extends java.lang.Exception

# Crashlytics exclude for faster builds
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# JodaTime: https://github.com/dlew/joda-time-android/issues/206#issuecomment-576666450
-keep class net.danlew.android.joda.R$raw { *; }

# Gson annotations: https://stackoverflow.com/a/57904190/1012234
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
  }
-keep,allowobfuscation @interface com.google.gson.annotations.SerializedName