package com.jingtian.lsmb.smb

class ItemData(val type:Int, val name:String, val size:String) {
    companion object {
        val unknown = 0
        val directory = 1
        val pic = 2
        val otherfile = 3
    }

}