package me.kyuubiran.potato.hook

import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.MemberExtensions.isNative
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import me.kyuubiran.potato.util.ClassMap
import me.kyuubiran.potato.util.MethodMap

object PrivilegeManagerHook : BaseHook() {
    private const val CLASS_TAG = "PrivilegeManager"
    private const val NO_AD_METHOD_TAG = "PrivilegeManager_isNoAd"
    private const val HAS_DOWNLOAD_PRIV_METHOD_TAG = "PrivilegeManager_hasBookDownloadPrivilege"

    init {
        ClassMap.findClassEvent += CLASS_TAG to {
            val list = it.findClass {
                searchPackages = listOf("com.dragon.read")
                matcher {
                    usingStrings = listOf("PrivilegeManager", "kv_new_user_free_privilege")
                }
            }

            Log.d("Find class result: ${list.size}")

            val clz = list.first().getInstance(EzXHelper.classLoader)

            clz
        }

        MethodMap.findMethodEvent += NO_AD_METHOD_TAG to {
            val clz = ClassMap.getClass(CLASS_TAG)!!
            val m = MethodFinder.fromClass(clz).first {
                name == "isNoAd" && isNative
            }

            m
        }

        MethodMap.findMethodEvent += HAS_DOWNLOAD_PRIV_METHOD_TAG to {
            val clz = ClassMap.getClass(CLASS_TAG)!!
            val m = MethodFinder.fromClass(clz).first {
                name == "hasBookDownloadPrivilege"
            }

            m
        }
    }

    //    // Instance of PrivilegeManager
//    @Volatile
//    lateinit var Instance: Any
//        private set
//
    override fun init() {
//        ClassMap.getClass(CLASS_TAG)?.let {
//            FieldFinder.fromClass(it).first { type == it }.get(null)?.let { inst -> Instance = inst }
//                ?: run { Log.e("Cannot init PrivilegeManager instance, instance is null!") }
//        } ?: run { Log.e("Cannot init PrivilegeManager instance, class not found!") }

        MethodMap.getMethod(NO_AD_METHOD_TAG)?.createHook { returnConstant(true) }
            ?: run { Log.e("Cannot init AdHook, method not found!") }

        MethodMap.getMethod(HAS_DOWNLOAD_PRIV_METHOD_TAG)?.createHook { returnConstant(true) }
            ?: run { Log.e("Cannot init BookDownloadHook, method not found!") }
    }

    override val name: String = "PrivilegeManagerHook"
}