package com.xx.dynamicload;

import java.io.Serializable;

/**
 * Created by wxx
 * on 2016/12/9.
 */

public class ItemModel implements Serializable {

    private String title;
    private int iconResId;
    private String iconResName;//图片资源名称


    public ItemModel(String title, int iconResId) {
        this.title = title;
        this.iconResId = iconResId;
    }

    public ItemModel(String title, String iconResName) {
        this.title = title;
        this.iconResName = iconResName;
    }

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }


    public String getIconResName() {
        return iconResName;
    }


    public void setIconResName(String iconResName) {
        this.iconResName = iconResName;
    }
}
