# Keep third-party libraries unchanged. Only InstantSnap's own classes are
# candidates for renaming.
-keep class javafx.** { *; }
-keep class com.sun.** { *; }
-keep class com.github.kwhat.** { *; }
-keep class org.slf4j.** { *; }
-keep class ch.qos.logback.** { *; }

# Entry points used by jpackage and JavaFX reflection.
-keep class com.github.rebi0395.instantsnap.app.Main {
    public static void main(java.lang.String[]);
}
-keep class com.github.rebi0395.instantsnap.app.InstantSnapApp { *; }

# FXMLLoader injects and invokes these members by their original names.
-keepclassmembers class * {
    @javafx.fxml.FXML <fields>;
    @javafx.fxml.FXML <methods>;
}

# Class.getEnumConstants() resolves these standard enum methods by name.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# JNA maps Java method and Structure field names to native declarations.
-keepclassmembers interface * extends com.sun.jna.Library {
    <methods>;
}
-keepclassmembers class * extends com.sun.jna.Structure {
    <fields>;
}

# Preserve metadata required by JavaFX, JNA and generic/reflection code.
-keepattributes Exceptions,InnerClasses,EnclosingMethod,Signature,*Annotation*

# Keep the first rollout conservative. Renaming provides the protection while
# avoiding shrink/optimization surprises in reflection-heavy desktop code.
-dontshrink
-dontoptimize
-dontwarn

# Do not leak original Java file names or line-number tables.
-renamesourcefileattribute SourceFile
