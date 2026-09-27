package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CommandHistoryEntity::class, SavedScriptEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandHistoryDao(): CommandHistoryDao
    abstract fun savedScriptDao(): SavedScriptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "adb_ai_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialScripts(database.savedScriptDao())
                    }
                }
            }

            suspend fun populateInitialScripts(dao: SavedScriptDao) {
                val defaultScripts = listOf(
                    SavedScriptEntity(
                        title = "تثبيت معدل التحديث 90Hz (Oppo Reno 5)",
                        description = "إجبار شاشة AMOLED على العمل بتردد 90Hz دائم في جميع التطبيقات وإلغاء خنق التردد",
                        commands = "settings put system peak_refresh_rate 90\nsettings put system min_refresh_rate 90\nsettings put global oneplus_screen_refresh_rate 2",
                        category = "Display & 90Hz",
                        isBuiltIn = true
                    ),
                    SavedScriptEntity(
                        title = "تسريع واجهة ColorOS 13 (حركات 0.5x)",
                        description = "تقليل زمن الرسوم المتحركة للنوافذ والانتقالات لتسريع استجابة الهاتف بنسبة 50%",
                        commands = "settings put global window_animation_scale 0.5\nsettings put global transition_animation_scale 0.5\nsettings put global animator_duration_scale 0.5",
                        category = "Performance",
                        isBuiltIn = true
                    ),
                    SavedScriptEntity(
                        title = "حذف تطبيقات ColorOS الإعلانية غير الضرورية",
                        description = "إلغاء تثبيت متجر التطبيقات ومساعد HeyTap والمتصفح الافتراضي لتقليل استهلاك الرام",
                        commands = "pm uninstall -k --user 0 com.heytap.browser\npm uninstall -k --user 0 com.oppo.market\npm uninstall -k --user 0 com.heytap.habit.analysis\npm uninstall -k --user 0 com.coloros.gamespace",
                        category = "Debloat",
                        isBuiltIn = true
                    ),
                    SavedScriptEntity(
                        title = "منح صلاحيات Write Secure Settings لتطبيق Shizuku",
                        description = "منح الأذونات المتقدمة للأدوات الخارجية للتحكم في الإعدادات المحمية بدون روت",
                        commands = "pm grant moe.shizuku.privileged.api android.permission.WRITE_SECURE_SETTINGS\npm grant moe.shizuku.privileged.api android.permission.DUMP",
                        category = "Permissions",
                        isBuiltIn = true
                    ),
                    SavedScriptEntity(
                        title = "استعلام معلومات Oppo Reno 5 4G ومعالج Snapdragon 720G",
                        description = "قراءة مواصفات العتاد ودرجة حرارة البطارية والتردد الحالي للمعالج",
                        commands = "getprop ro.product.model\ngetprop ro.build.version.oplusrom\ndumpsys battery\ncat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq",
                        category = "Oppo Reno 5 & ColorOS",
                        isBuiltIn = true
                    ),
                    SavedScriptEntity(
                        title = "استخراج لقطة شاشة وحفظها برمجياً",
                        description = "التقاط صورة لشاشة الجهاز وحفظها في مجلد الصور بدون ظهور واجهة المستخدم",
                        commands = "screencap -p /sdcard/Pictures/adb_screenshot.png\nam broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/adb_screenshot.png",
                        category = "Oppo Reno 5 & ColorOS",
                        isBuiltIn = true
                    ),
                    SavedScriptEntity(
                        title = "تجاوز قيود قتل التطبيقات في الخلفية (ColorOS Battery Whitelist)",
                        description = "إضافة التطبيق إلى القائمة البيضاء لتفادي إغلاقه التلقائي من نظام توفير الطاقة",
                        commands = "dumpsys deviceidle whitelist +com.aistudio.adbai.termx\ncmd appops set com.aistudio.adbai.termx RUN_IN_BACKGROUND allow",
                        category = "Battery & Doze",
                        isBuiltIn = true
                    )
                )
                dao.insertAll(defaultScripts)
            }
        }
    }
}
