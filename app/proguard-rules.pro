# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep AWS SDK classes
-keep class aws.sdk.** { *; }
-keep class aws.smithy.** { *; }

# Keep Room entities
-keep class com.nightsound.data.local.entities.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
