package com.jingtian.lsmb.Base

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import com.jingtian.lsmb.smb.LSMB
import com.jingtian.lsmb.util.SPValues
import java.util.*
import kotlin.properties.Delegates

class BaseApp: Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        sps = SPValues(this.applicationContext)
        // 创建必要实例
        if(!sps.createChannel) {
            val channel = NotificationChannel(chanelID, "LSMB Notification Chanel", NotificationManager.IMPORTANCE_HIGH)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
            sps.createChannel = true
            // 首次启动， 创建通知channel

        }
    }
    companion object {
        var instance:BaseApp by Delegates.notNull()
        var sps : SPValues by Delegates.notNull()
        // 相关常用数据
        var mLSMB:LSMB? = null
        val chanelID= "LSMB Notification Chanel ID"
    }
}