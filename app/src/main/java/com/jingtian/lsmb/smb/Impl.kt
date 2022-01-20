package com.jingtian.lsmb.smb

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation

interface OnOperationFileCallback {

    fun onSuccess(message:String)
    fun onFailure(message: String)
}

interface OnReadFileListNameCallback {

    fun onSuccess(fileNameList: MutableList<FileIdBothDirectoryInformation>)
    fun onFailure(message: String)
}

interface OnCreateCallback {

    fun onSuccess(message: String)
    fun onFailure(message: String)
}