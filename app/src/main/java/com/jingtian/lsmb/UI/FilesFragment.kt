package com.jingtian.lsmb.UI

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import com.jingtian.lsmb.Base.BaseApp
import com.jingtian.lsmb.R
import com.jingtian.lsmb.service.DownloadService
import com.jingtian.lsmb.smb.ItemData
import com.jingtian.lsmb.smb.LSMB
import com.jingtian.lsmb.smb.OnOperationFileCallback
import com.jingtian.lsmb.smb.OnReadFileListNameCallback
import java.io.BufferedOutputStream
import java.util.*
import kotlin.concurrent.thread

class FilesFragment(val current_path:String, val last_frag:Fragment?): Fragment(), OnReadFileListNameCallback {
    companion object {
        val gridMod = 0
        val listMod = 1
    }

    var fileList:MutableList<FileIdBothDirectoryInformation>?  = null
    var mode = listMod
    var gv:GridView? = null
    override fun onSuccess(fileNameList: MutableList<FileIdBothDirectoryInformation>) {
        fileList = fileNameList
        fileList!!.removeAt(0)
        fileList!!.removeAt(0)
        activity!!.runOnUiThread {
            update_ui()
        }
    }


    fun update_ui() {
        if (mode == listMod) {
            val width = 1
        } else {
            val width = 3
        }
        gv!!.adapter = object : BaseAdapter() {
            override fun getCount(): Int = fileList!!.size

            override fun getItem(position: Int): Any = fileList!![position]

            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                var viewHolder:ViewHolder? = null
                var view:View? = convertView
                if(view == null) {
                    if (mode == listMod) {
                        view = View.inflate(context, R.layout.item_file_list,null)
                    } else {
                        view = View.inflate(context, R.layout.item_file_grid,null)
                    }
                    viewHolder = ViewHolder(view, mode)
                    view!!.tag = viewHolder
                } else {
                    viewHolder = view.tag as ViewHolder
                }
                val info = fileList!![position]
                var dab:Bitmap? = null
                if (info.allocationSize != 0L) {
                    dab = BitmapFactory.decodeResource(resources, R.drawable.file)
                } else {
                    dab = BitmapFactory.decodeResource(resources, R.drawable.file_directory)
                }
                var mat = Matrix()
                mat.postScale(0.2f,0.2f)
                var pic = Bitmap.createBitmap(dab, 0,0,dab.width, dab.height, mat,true)

                viewHolder.iv_icon!!.setImageBitmap(pic)
                viewHolder.tv_name!!.text = info.fileName
                if(info.allocationSize != 0L) {
                    viewHolder.tv_size!!.text = info.allocationSize.toString()
                } else {
                    viewHolder.tv_size!!.text = ""
                }
                return view
            }

        }
        gv!!.onItemClickListener = object :AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val menu = PopupMenu(context, view)
                menu.inflate(R.menu.menu_click_files)
                menu.setOnMenuItemClickListener {
                    if (it.itemId == R.id.open) {
                        if (fileList!![position].allocationSize == 0L) {
                            val path = current_path + "/" + fileList!![position].fileName
                            Toast.makeText(context, path, Toast.LENGTH_SHORT).show()
                            val frag = FilesFragment(path,this@FilesFragment)
                            val trans = activity!!.supportFragmentManager.beginTransaction()
                            trans.add(R.id.files_container, frag)
                            trans.show(frag)
                            trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            trans.hide(this@FilesFragment)
                            trans.commit()

                        } else {
                            val intent = Intent(this@FilesFragment.context, DownloadService::class.java)
//                            Toast.makeText(context, current_path + "/" + fileList!![position].fileName, Toast.LENGTH_SHORT).show()
                            intent.putExtra("save", current_path)
                            intent.putExtra("save_name", fileList!![position].fileName)
                            context!!.startService(intent)
                        }
                    } else {
                        Toast.makeText(context, "开发中...", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                menu.show()

            }

        }

    }
    fun data_inserted() {

    }
    fun data_deleted() {

    }
    override fun onFailure(message: String) {
        activity!!.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_files, container, false)
        gv = view!!.findViewById(R.id.gv_files)
        thread {
            BaseApp.mLSMB!!.listShareFileName(current_path, null ,this)
        }
        val bt_last = view!!.findViewById<Button>(R.id.bt_frag_files_last)
        if (last_frag != null) {
            bt_last!!.visibility = View.VISIBLE
            bt_last.setOnClickListener {
                val trans = activity!!.supportFragmentManager.beginTransaction()
                trans.remove(this)
                trans.show(last_frag)
                trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                trans.commit()
            }
        } else {
            bt_last!!.visibility = View.GONE
        }

        return view
    }

    class ViewHolder {
        public var iv_icon: ImageView? = null
        public var tv_name: TextView? = null
        public var tv_size: TextView? = null

        constructor(view:View, mode:Int) {
            if (mode == listMod) {
                iv_icon = view.findViewById(R.id.tv_item_list_ficon)
                tv_name = view.findViewById(R.id.tv_item_list_fname)
                tv_size = view.findViewById(R.id.tv_item_list_fsize)
            } else {

            }
        }

    }

}