# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Gson-related classes
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Room entities
-keep class com.daygrid.app.data.db.entity.** { *; }

# Keep export/import model
-keep class com.daygrid.app.domain.usecase.ExportRecord { *; }

# Keep domain models (used in serialization)
-keep class com.daygrid.app.domain.model.** { *; }

# Keep Hilt-generated code
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Glance widget receivers
-keep class com.daygrid.app.widget.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
