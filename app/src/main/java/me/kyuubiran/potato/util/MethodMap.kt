package me.kyuubiran.potato.util

import android.content.Context
import android.content.SharedPreferences
import com.github.kyuubiran.ezxhelper.DexDescriptor
import com.github.kyuubiran.ezxhelper.Log
import me.kyuubiran.potato.MainHook
import me.kyuubiran.potato.hook.ApplicationHook
import org.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Method

object MethodMap {
    private lateinit var sp: SharedPreferences
    private val map = hashMapOf<String, Method>()

    // return <TagName, FullMethodName>
    val findMethodEvent: HashSet<((DexKitBridge) -> Pair<String, Method>)> = HashSet()

    fun init(ctx: Context) {
        sp = ctx.getSharedPreferences("potato_methods_map", Context.MODE_PRIVATE)
//        sp.edit().clear().apply()

        sp.getInt("version_code", 0).let {
            Log.d("Cached vc(method)=$it, rel=${ApplicationHook.versionCode}")
            if (it != ApplicationHook.versionCode) {
                sp.edit().clear().putInt("version_code", ApplicationHook.versionCode).apply()
                initMethods()
            }
        }

        MainHook.popDexKitRef()
    }

    private fun initMethods() {
        val editor = sp.edit()

        findMethodEvent.map {
            try {
                it.invoke(MainHook.dexKit)
            } catch (e: Exception) {
                Log.e("Init methods failed! Cannot find method!")
                null
            }
        }.forEach { pair ->
            if (pair == null)
                return@forEach

            val (tag, method) = pair
            map[tag] = method
            Log.d("Put method: $tag=${method.name}")
            editor.putString(tag, DexDescriptor.getSignature(method))
        }

        editor.apply()
    }

    fun getMethod(methodTag: String): Method? {
        if (map.contains(methodTag)) {
            Log.d("Return memory cached class: $methodTag")
            return map[methodTag]
        }

        val signature = sp.getString(methodTag, null)
        if (signature == null) {
            Log.w("No such method cache found: $methodTag")
            return null
        }

        Log.d("Cached method found: $methodTag=$signature")

        val m = DexDescriptor.getMethod(signature)
        Log.d("Located method: $methodTag=${m.declaringClass.name}.${m.name}")
        map[methodTag] = m
        return m
    }
}