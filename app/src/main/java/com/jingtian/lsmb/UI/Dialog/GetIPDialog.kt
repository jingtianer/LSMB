package com.jingtian.lsmb.UI.Dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.jingtian.lsmb.R


class GetIPDialog(private val callback:GetIPDialog.CallBack): DialogFragment() {

    interface CallBack {
        fun onPositive(mes:String, dic:String)
        fun onNegative(mes:String)
    }
    //初始化
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val window: Window? = dialog!!.window
        //不显示标题
        window!!.requestFeature(Window.FEATURE_NO_TITLE)
        val view = inflater.inflate(R.layout.dialog_get_ip, container)
        val posi = view.findViewById<Button>(R.id.bt_getip_posi)
        val neg = view.findViewById<Button>(R.id.bt_getip_neg)
        val ips = arrayListOf(
                view.findViewById<EditText>(R.id.et_getip_1),
                view.findViewById<EditText>(R.id.et_getip_2),
                view.findViewById<EditText>(R.id.et_getip_3),
                view.findViewById<EditText>(R.id.et_getip_4)
        )
        val et_dic = view.findViewById<EditText>(R.id.et_getip_dic)
        posi.setOnClickListener {
            var ip = ""
            for (et_ip in ips) {
                val text = et_ip.text.trim().toString().toIntOrNull()
                if (text == null || text < 0 || text > 255) {
                    callback.onNegative("ip地址无效")
                    return@setOnClickListener
                } else {
                    ip += text.toString() + "."
                }
            }
            if (et_dic.text.trim().isNullOrBlank()) {
                callback.onNegative("目录不可为空")
            }
            callback.onPositive(ip.substring(0, ip.length-1), et_dic.text.trim().toString())
            dismiss()
        }
        neg.setOnClickListener {
            callback.onNegative("取消")
            dismiss()
        }
        return view
    }
}