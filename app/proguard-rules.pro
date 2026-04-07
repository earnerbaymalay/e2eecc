# ──────────────────────────────────────────────
# Cypherchat ProGuard/R8 Rules
# Release build optimization + obfuscation
# ──────────────────────────────────────────────

# ── General ──────────────────────────────────
-keepattributes Signature, InnerClasses, *Annotation*, EnclosingMethod
-dontnote okhttp3.**
-dontnote okio.**
-dontwarn javax.annotation.**

# ── Kotlin ───────────────────────────────────
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ── Kotlin Coroutines ────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ── Jetpack Compose ──────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class kotlin.reflect.jvm.internal.** { *; }

# ── Koin (DI) ────────────────────────────────
-keep class org.koin.** { *; }
-keepclassmembers class org.koin.** { *; }
-keep class com.cypherchat.** { *; }
-keep class * implements org.koin.core.module.Module

# ── Room ─────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class *
-keep class androidx.room.** { *; }

# ── SQLCipher ────────────────────────────────
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.* { *; }
-dontwarn net.sqlcipher.**
-dontwarn net.sqlcipher.database.**

# ── Android Keystore / Crypto ────────────────
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
-keep class android.security.keystore.** { *; }
-dontwarn java.security.**
-dontwarn javax.crypto.**

# ── Preserve crypto module internals ────────
# These contain critical crypto logic that must not be obfuscated
-keep class com.cypherchat.core.crypto.** { *; }
-keepclassmembers class com.cypherchat.core.crypto.** { *; }

# ── Preserve database entities ───────────────
-keep class com.cypherchat.core.database.entity.** { *; }
-keepclassmembers class com.cypherchat.core.database.entity.** { *; }

# ── Preserve network types ───────────────────
-keep class com.cypherchat.core.network.** { *; }

# ── Enum classes ─────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Remove logging in release ────────────────
-assumenosideeffects class com.cypherchat.core.common.Logger {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ── Native libraries ─────────────────────────
-keep class com.cypherchat.** { *; }
