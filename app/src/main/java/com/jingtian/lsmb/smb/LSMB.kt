package com.jingtian.lsmb.smb

import android.util.Log
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.common.SmbPath
import com.hierynomus.smbj.common.SmbPath.*
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.share.DiskShare
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class LSMB(private val builder:Builder) {
    private var connection:Connection? = null
    private var connectShare:DiskShare? = null
    fun finalize() {
        connection?.close()
        connectShare?.close()
        connection = null
        connectShare = null
    }
    private fun init(callback: OnCreateCallback?) {
        val config = SmbConfig.builder()
            // 设置读取超时
            .withTimeout(builder.readTimeOut, TimeUnit.SECONDS)
            // 设置写入超时
            .withWriteTimeout(builder.writeTimeOut, TimeUnit.SECONDS)
            // 设置Socket链接超时
            .withSoTimeout(builder.soTimeOut, TimeUnit.SECONDS)
            .build()

        val client = SMBClient(config)
        try {
            connection = client.connect(builder.ip)
            val authContext = AuthenticationContext(builder.username, builder.password.toCharArray(), null)
            val session = connection?.authenticate(authContext)

            connectShare = session?.connectShare(builder.folderName) as DiskShare?
            if (connectShare == null) callback?.onFailure("请检查文件夹名称")
            callback?.onSuccess("成功")
        }catch (e:java.lang.Exception) {
            callback?.onFailure(e.toString())
        }
    }

    /**
     * 向共享文件里写文件
     */
    fun writeToFile(inputFile: File?, callback: OnOperationFileCallback) {
        if (connectShare == null) {
            callback.onFailure("配置错误")
            return
        }
        if (inputFile == null || !inputFile.exists()) {
            callback.onFailure("文件不存在")
            return
        }
        var inputStream: BufferedInputStream? = null
        var outputStream: BufferedOutputStream? = null
        var openFile: com.hierynomus.smbj.share.File? = null
        try {
            inputStream = BufferedInputStream(FileInputStream(inputFile))
            openFile = connectShare!!.openFile(
                inputFile.name,
                EnumSet.of(AccessMask.GENERIC_WRITE), null,
                SMB2ShareAccess.ALL,
                // FILE_OVERWRITE_IF 可覆盖；FILE_CREATE 只能新建
                SMB2CreateDisposition.FILE_OVERWRITE_IF, null
            )
            outputStream = BufferedOutputStream(openFile.outputStream)

            val buffer = ByteArray(1024)
            var len: Int
            while (true) {
                // 读取长度
                len = inputStream.read(buffer, 0, buffer.size)
                if (len != -1) {
                    outputStream.write(buffer, 0, len)
                } else {
                    break
                }
            }
            callback.onSuccess("文件上传成功！ \n" + inputFile.name)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFailure(e.message ?: "上传失败")
        } finally {
            try {
                outputStream?.flush()
                inputStream?.close()
                // 需要调用close，不然删除会失效
                openFile?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 下载文件
     * */
    fun downloadFile(file: File?, callback: OnOperationFileCallback, outputStream:BufferedOutputStream? ) {
        if (connectShare == null) {
            callback.onFailure("配置错误")
            return
        }
        if (file == null) {
            callback.onFailure("文件不存在")
            return
        }
        var inputStream: BufferedInputStream? = null
        var openFile: com.hierynomus.smbj.share.File? = null
        try {
            openFile = connectShare!!.openFile(
                file.toString(),
                EnumSet.of(AccessMask.GENERIC_READ), null,
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN, null
            )
            inputStream = BufferedInputStream(openFile.inputStream)
            val buffer = ByteArray(1024)
            var len: Int
            while (true) {
                // 读取长度
                len = inputStream.read(buffer, 0, buffer.size)
                Log.d("size", len.toString())
                if (len != -1) {
                    outputStream!!.write(buffer, 0, len)
                } else {
                    break
                }
            }
            outputStream!!.flush()
            callback.onSuccess("文件保存成功！\n" + file.name)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFailure(e.message ?: "下载失败")
        } finally {
            try {
                outputStream!!.flush()
                inputStream?.close()
                // 需要调用close，不然删除会失效
                openFile?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取connectShare可以自己操作
     * */
    fun getConnectShare(): DiskShare? {
        return connectShare
    }

    /**
     * 获取当前根目录下的所有文件名
     */
    fun listShareFileName(callback: OnReadFileListNameCallback) {
        listShareFileName("", null, callback)
    }

    /**
     * 文件列表
     * @param path          路径 默认""则在当前的根目录下
     * @param searchPattern 文件显示规则 默认null当前目录下的所有文件 示例："*.TXT"
     */
    fun listShareFileName(path: String = "", searchPattern: String? = null, callback: OnReadFileListNameCallback) {
        if (connectShare == null) {
            callback.onFailure("配置错误")
            return
        }
        try {
            val list = connectShare!!.list(path, searchPattern)
            callback.onSuccess(list)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFailure(e.message ?: "获取文件名失败")
        }

    }

    /**
     * 删除文件
     * @param path 文件名全路径，在根目录直接传文件名
     * */
    fun deleteFile(path: String, callback: OnOperationFileCallback) {
        if (connectShare == null) {
            callback.onFailure("配置错误")
            return
        }
        try {
            connectShare!!.rm(path)
            callback.onSuccess("文件删除成功 \n" + path)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFailure(e.message ?: "删除失败")
        } finally {
            try {
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        class Builder {
            var readTimeOut: Long = 60L
            var writeTimeOut: Long = 60L
            var soTimeOut: Long = 0L
            var ip: String = ""
            var username: String = ""
            var password: String = ""
            var folderName: String = ""


            /**@param readTimeOut 读取时间，默认60秒*/
            fun setReadTimeOut(readTimeOut: Long): Builder {
                this.readTimeOut = readTimeOut
                return this
            }

            /**@param writeTimeOut 写入时间，单位秒 默认60秒*/
            fun setWriteTimeOut(writeTimeOut: Long): Builder {
                this.writeTimeOut = writeTimeOut
                return this
            }

            /**@param soTimeOut Socket超时时间，单位秒 默认0秒*/
            fun setSoTimeOut(soTimeOut: Long): Builder {
                this.soTimeOut = soTimeOut
                return this
            }

            fun setConfig(ip: String, username: String, password: String, folderName: String): Builder {
                this.ip = ip
                this.username = username
                this.password = password
                this.folderName = folderName
                return this
            }

            fun build(callback: OnCreateCallback?): LSMB {
                val lSMB = LSMB(this)
                lSMB.init(callback)
                return lSMB
            }
        }
        fun with(): Builder {
            return Builder()
        }
    }
}