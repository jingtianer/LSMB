package com.jingtian.lsmb.UI

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.transition.Slide
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.jingtian.lsmb.Base.BaseApp
import com.jingtian.lsmb.R
import com.jingtian.lsmb.UI.Dialog.GetIPDialog
import com.jingtian.lsmb.smb.LSMB
import com.jingtian.lsmb.smb.OnCreateCallback
import com.jingtian.lsmb.smb.OnReadFileListNameCallback
import kotlin.concurrent.thread

class MainActivity: AppCompatActivity() , OnCreateCallback {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==1) {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, BaseApp.chanelID)
            startActivity(intent)
        }
    }
    var msmb: LSMB? = null
    fun setSubtitle(mes:String) {

        var toolbar = findViewById<Toolbar>(R.id.toolbar)

        toolbar.subtitle = mes
    }
    fun open_ip_dialog() {
        GetIPDialog(object : GetIPDialog.CallBack {
            override fun onPositive(mes: String, dic:String) {
                connect(mes,dic)
            }

            override fun onNegative(mes: String) {
                Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            }

        }).show(supportFragmentManager, "")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "LSMB"
        toolbar.subtitle = "无目标主机"
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.setServer_ip) {
                open_ip_dialog()
            } else if (it.itemId == R.id.more) {
                Toast.makeText(this, "更多", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
            }
            true
        }
        if(PackageManager.PERMISSION_GRANTED != checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE))) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE),0)
        }
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, getApplicationInfo().uid)
            startActivityForResult(intent, 1)
        }
        if (BaseApp.sps.ip_valid) {
            connect(BaseApp.sps.server_ip, BaseApp.sps.dic)
        }else {
            open_ip_dialog()
        }
    }

    fun connect(ip:String, dic:String) {
        BaseApp.sps.server_ip = ip
        thread {
            BaseApp.mLSMB = LSMB.with().setConfig(ip, "", "", dic).build(this)
        }
        BaseApp.sps.dic = dic
    }

    override fun onSuccess(message: String) {
        runOnUiThread {
            val frag = FilesFragment("",null)
            var trans = supportFragmentManager.beginTransaction()
            val frags = supportFragmentManager.fragments
            for (f in frags) {
                trans.remove(f)
            }
            trans.commit()
            trans = supportFragmentManager.beginTransaction()
            trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            trans.add(R.id.files_container,frag)
            trans.commit()
            setSubtitle(BaseApp.sps.server_ip)
        }
        BaseApp.sps.ip_valid = true
    }

    override fun onFailure(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }

        BaseApp.sps.ip_valid = false
    }

    override fun onDestroy() {
        super.onDestroy()
        thread {
            BaseApp.mLSMB?.finalize()
        }
        BaseApp.mLSMB = null
    }

}