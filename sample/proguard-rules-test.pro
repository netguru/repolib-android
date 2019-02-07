# Additional proguard rules for instrumentation testing

-keep class rx.plugins.** { *; }
-keep class org.junit.** { *; }
-keep class com.netguru.android.testcommons.** { *; }
-keep class android.support.test.espresso.** { *; }
-dontwarn org.hamcrest.**
