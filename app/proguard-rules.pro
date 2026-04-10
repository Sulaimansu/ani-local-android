# Add project specific ProGuard rules here.

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.internal.**
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Apollo
-keep class com.sulaiman.anilocal.** { *; }

# Kotlinx Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandlerImpl {}

# Generic
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-dontnote kotlinx.coroutines.AnnotationsKt
