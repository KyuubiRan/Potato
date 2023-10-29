package me.kyuubiran.potato.util

import android.content.Context
import android.content.SharedPreferences
import com.github.kyuubiran.ezxhelper.ClassUtils
import com.github.kyuubiran.ezxhelper.Log
import me.kyuubiran.potato.MainHook
import me.kyuubiran.potato.hook.ApplicationHook
import org.luckypray.dexkit.DexKitBridge

object ClassMap {

    private val map = hashMapOf<String, Class<*>?>()

    // <TagName, Finder>
    val findClassEvent: HashMap<String, ((DexKitBridge) -> Class<*>)> = HashMap()

    private lateinit var sp: SharedPreferences

    fun init(ctx: Context) {
        sp = ctx.getSharedPreferences("potato_classes_map", Context.MODE_PRIVATE)
//        sp.edit().clear().apply()

        sp.getInt("version_code", 0).let {
            Log.d("Cached vc(class)=$it, rel=${ApplicationHook.versionCode}")

            if (it != ApplicationHook.versionCode) {
                sp.edit().clear().putInt("version_code", ApplicationHook.versionCode).apply()
                initClasses()
            }
        }

        MainHook.popDexKitRef()
    }

    private fun initClasses() {
        val editor = sp.edit()

        findClassEvent.map { (tag, func) ->
            try {
                tag to func.invoke(MainHook.dexKit)
            } catch (e: Exception) {
                Log.e("Init class failed! Cannot find class!")
                null
            }
        }.forEach { pair ->
            if (pair == null)
                return@forEach

            val (tag, clazz) = pair
            Log.d("Put class: $tag=${clazz.name}")
            map[tag] = clazz
            editor.putString(tag, clazz.name)
        }

        editor.apply()
    }

    private fun tryFindAndCacheClass(tagName: String): Class<*>? {
        if (!findClassEvent.contains(tagName)) {
            Log.e("No such tag found: $tagName")
            return null
        }

        try {
            val c = findClassEvent[tagName]!!.invoke(MainHook.dexKit)
            Log.d("Located class: $tagName=${c.name}")
            map[tagName] = c
            sp.edit().putString(tagName, c.name).apply()

            return c
        } catch (e: Exception) {
            Log.e("Failed find class: $tagName")
        }


        return null
    }

    fun getClass(classTag: String): Class<*>? {
        if (map.contains(classTag)) {
            Log.d("Return memory cached class: $classTag")
            return map[classTag]
        }

        val className = sp.getString(classTag, null) ?: return tryFindAndCacheClass(classTag)

        Log.d("Cached class found: $classTag=$className")

        val clz = ClassUtils.loadClassOrNull(className)?.also { Log.d("Located class: $classTag=${it.name}") }
        map[classTag] = clz
        return clz
    }

}