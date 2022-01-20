package com.jingtian.lsmb.util

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

//工具类， 变量的持久化保存
class SP<T>(val context: Context, val name:String, val default:T) :
    ReadWriteProperty<Any?, T> {
    val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("default", Context.MODE_PRIVATE)
    }


    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return findPreference(name, default)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPreference(name, value)
    }

    private fun findPreference(name:String, default: T) : T  = with(prefs){
        return when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw IllegalArgumentException("Unsupport type")
        } as T
    }

    private fun <T> putPreference(name:String, value:T) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("Unsupport type")
        }.apply()
        //commit和apply都表示提交
        //应该是对when-else语句的返回值调用apply方法
    }

}

class SPValues(val context: Context) {
    var server_ip:String by SP(context, "server_ip", "0.0.0.0")
    var dic:String by SP(context, "dic", "")
    var ip_valid:Boolean by SP(context, "ip_valid", false)
    var createChannel:Boolean by SP(context, "createChannel", false)
}