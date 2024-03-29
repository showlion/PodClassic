package com.example.podclassic.`object`

import android.text.TextUtils
import com.example.podclassic.util.Values


class Music {
    var name = ""
    set(value) {
        if (value.isNotBlank()) {
            field = value
        }
    }
    var album = Values.NULL
        set(value) {
            if (value.isNotBlank()) {
                field = value
            }
        }
    var singer = Values.NULL
        set(value) {
            if (value.isNotBlank()) {
                field = value
            }
        }
    var path : String = ""
    var id : Long = 0L

    constructor(name : String, album : String, singer : String, path : String, id : Long) {
        this.name = name
        this.album = album
        this.singer = singer
        this.path = path
        this.id = id
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }

    override fun toString(): String {
        return "name = $name , album = $album , singer = $singer , path = $path"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Music) {
            return false
        }
        if (other.path == path && !TextUtils.isEmpty(path)) {
            return true
        }
        return false
    }
}