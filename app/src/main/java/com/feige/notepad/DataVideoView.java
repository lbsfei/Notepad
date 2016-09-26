package com.feige.notepad;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by feifei on 2016/9/24.
 */
public class DataVideoView extends VideoView{
    private String absolutePath;

    private Bitmap bitmap;

    public DataVideoView(Context context) {
        this(context, null);
    }

    public DataVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
