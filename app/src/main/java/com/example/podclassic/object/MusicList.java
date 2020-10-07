package com.example.podclassic.object;

import com.example.podclassic.storage.SaveMusics;
import com.example.podclassic.util.MediaUtil;

import java.util.ArrayList;



public class MusicList {
    public String name;
    public int size;
    public int type = 0;

    public static final int TYPE_SINGER = 1;
    public static final int TYPE_ALBUM = 2;

    private ArrayList<Music> list;

    public ArrayList<Music> getList() {
        if (list == null) {
            if (type == TYPE_SINGER) {
                list = MediaUtil.INSTANCE.searchMusic(MediaUtil.NAME + " like? or " + MediaUtil.SINGER + "=?", new String[] {"%" + name + "%", name});
            } else if (type == TYPE_ALBUM) {
                list = MediaUtil.INSTANCE.searchMusic(MediaUtil.ALBUM + "=?", new String[] {name});
            }
        }
        return list;
    }

    public void setList(ArrayList<Music> list) {
        this.list = list;
    }

    public MusicList() { }

    public MusicList(String name, ArrayList<Music> list) {
        this.list = list;
        this.name = name;
    }

    public int getSize() {
        if (list != null && list.size() != 0) {
            return list.size();
        }
        return size;
    }

    @Override
    public String toString() {
        return name + size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MusicList)) {
            return false;
        }
        MusicList musicList = (MusicList) obj;
        try {
            return name.equals(musicList.name);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
