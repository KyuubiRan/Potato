package me.kyuubiran.potato.hook

import android.app.Application
import com.github.kyuubiran.ezxhelper.AndroidLogger
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.LogExtensions.logexIfThrow
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import de.robv.android.xposed.XC_MethodHook.Unhook
import me.kyuubiran.potato.util.ClassMap
import me.kyuubiran.potato.util.MethodMap

// Example hook
object ApplicationHook : BaseHook() {
    override val name: String = "ApplicationHook"

    var versionCode = 0
        private set

    private fun initHooks(vararg hook: BaseHook) {
        hook.forEach {
            runCatching {
                if (it.isInit) return@forEach
                it.init()
                it.isInit = true
                Log.i("Inited hook: ${it.name}")
            }.logexIfThrow("Failed init hook: ${it.name}")
        }
    }

    private var unHook: Unhook? = null

    override fun init() {
        unHook = MethodFinder.fromClass(Application::class.java).filterByName("onCreate").first().createHook {
            after {
                unHook?.unhook()
                
                val ctx = it.thisObject as Application
                val info = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
                versionCode = info.versionCode

                EzXHelper.initAppContext(ctx, true)
                ClassMap.init(ctx)
                MethodMap.init(ctx)

                initHooks(PrivilegeManagerHook)

                AndroidLogger.toast("初始化完成")
            }
        }
    }
}