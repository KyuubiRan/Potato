package me.kyuubiran.potato

import me.kyuubiran.potato.hook.BaseHook
import me.kyuubiran.potato.hook.ApplicationHook
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.LogExtensions.logexIfThrow
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.kyuubiran.potato.hook.PrivilegeManagerHook
import org.luckypray.dexkit.DexKitBridge

private const val PACKAGE_NAME_HOOKED = "com.dragon.read"
private const val TAG = "Potato"

class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit /* Optional */ {
    companion object {
        lateinit var dexKit: DexKitBridge
        private var refForDexKit = 2
        fun popDexKitRef() {
            if (--refForDexKit <= 0) {
                dexKit.close()
            }
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == PACKAGE_NAME_HOOKED && lpparam.packageName == lpparam.processName) {
            System.loadLibrary("dexkit")
            // Init EzXHelper
            EzXHelper.initHandleLoadPackage(lpparam)
            EzXHelper.setLogTag(TAG)
            EzXHelper.setToastTag(TAG)
            dexKit = DexKitBridge.create(lpparam.appInfo.sourceDir)!!
            // Init hooks
            initHooks(ApplicationHook)
            // Early init for events
            PrivilegeManagerHook
        }
    }

    // Optional
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelper.initZygote(startupParam)
    }

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
}