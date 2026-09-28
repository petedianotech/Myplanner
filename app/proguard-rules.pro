# MyPlanner ProGuard rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences
-dontwarn androidx.compose.**
