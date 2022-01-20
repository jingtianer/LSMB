package com.jingtian.lsmb.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.app.NotificationCompat
import com.jingtian.lsmb.Base.BaseApp
import com.jingtian.lsmb.Base.BaseApp.Companion.mLSMB
import com.jingtian.lsmb.R
import com.jingtian.lsmb.UI.MainActivity
import com.jingtian.lsmb.smb.LSMB
import com.jingtian.lsmb.smb.OnOperationFileCallback
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.File
import java.lang.Exception
import java.nio.Buffer
import kotlin.concurrent.thread

class DownloadService : Service(),OnOperationFileCallback {
    companion object {
        var service_count = 0
    }
    var id = 0
    fun start_notification(title:String, info:String, intent:PendingIntent) {
        id = service_count
        service_count++
        val nofication = NotificationCompat.Builder(applicationContext, BaseApp.chanelID)
                .setAutoCancel(true)
                .setSubText(info)
                .setContentIntent(intent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(info)
                .build()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(id, nofication)
    }
    fun stop_notification() {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(id)
        service_count--

    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var path = intent!!.getStringExtra("save")
        val save_name = intent.getStringExtra("save_name")
        path += "/" + save_name
        var path_save:BufferedOutputStream? = null
        val f = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) , save_name!!)
        f.createNewFile()
        path_save = BufferedOutputStream(f.outputStream())
        start_notification("正在下载", save_name,PendingIntent.getActivity(this,0, Intent(baseContext, MainActivity::class.java),0))
        thread {
            val mlsmb = mLSMB
            mlsmb!!.downloadFile(File(path!!),this, path_save)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                try{
                    val insert_uri = contentResolver.insert(uri, with(ContentValues()) {
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        put(MediaStore.Downloads.DISPLAY_NAME, save_name)
                        put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis()/1000)
                        this
                    })
                    Log.d("save uri", insert_uri.toString())
                    val os = contentResolver.openOutputStream(insert_uri!!)!!
                    try {
                        os.write(f.readBytes())
                    }finally {
                        os.close()
                        os.flush()
                        f.delete()
                    }
                }catch (e:Exception) {
                    e.printStackTrace()
                }
            } else {
                val os = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path, save_name).outputStream()
                try {
                    os.write(f.readBytes())
                }finally {
                    os.close()
                    os.flush()
                    f.delete()
                }
            }
            stop_notification()
        }
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onSuccess(message: String) {
        thread{
            Looper.prepare()
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            Looper.loop()
        }
    }

    override fun onFailure(message: String) {
        thread{
            Looper.prepare()
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            Looper.loop()
        }
    }
}