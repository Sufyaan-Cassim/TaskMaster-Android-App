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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Gson model classes and annotations
-keepattributes Signature, *Annotation*
-keep class com.google.gson.stream.** { *; }

# Keep app data models (used with Gson / Room / intents)
-keep class za.co.rosebankcollege.st10304152.taskmaster.data.** { *; }

# Room keep rules
-dontwarn androidx.room.**
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }

# Keep Kotlin data classes metadata
-keepclassmembers class ** {
    @kotlin.Metadata *;
}