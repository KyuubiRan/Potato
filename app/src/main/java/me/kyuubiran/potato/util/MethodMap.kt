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
    private val map = hashMapOf<String, Method?>()

    // return <TagName, FullMethodName>
    val findMethodEvent: HashMap<String, ((DexKitBridge) -> Method)> = HashMap()

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

        findMethodEvent.map { (tag, func) ->
            try {
                tag to func.invoke(MainHook.dexKit)
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

    private fun tryFindAndCacheMethod(tagName: String): Method? {
        if (!ClassMap.findClassEvent.contains(tagName)) {
            Log.e("No such tag found: $tagName")
            return null
        }

        try {
            val m = findMethodEvent[tagName]!!.invoke(MainHook.dexKit)
            Log.d("Located class: $tagName=${m.name}")
            map[tagName] = m
            sp.edit().putString(tagName, m.name).apply()

            return m
        } catch (e: Exception) {
            Log.e("Failed find class: $tagName")
        }

        return null
    }

    fun getMethod(methodTag: String): Method? {
        if (map.contains(methodTag)) {
            Log.d("Return memory cached method: $methodTag")
            return map[methodTag]
        }

        val signature = sp.getString(methodTag, null) ?: return tryFindAndCacheMethod(methodTag)

        Log.d("Cached method found: $methodTag=$signature")

        val m = try {
            DexDescriptor.getMethod(signature).also { m -> Log.d("Located method: $methodTag=${m.declaringClass.name}.${m.name}") }
        } catch (e: Exception) {
            Log.e("Cannot find method: $methodTag")
            null
        }

        map[methodTag] = m
        return m
    }
}