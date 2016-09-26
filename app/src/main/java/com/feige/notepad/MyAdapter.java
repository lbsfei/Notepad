package com.feige.notepad;



import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.annotation.SuppressLint;
import android.provider.MediaStore;

import java.util.List;


public class MyAdapter extends BaseAdapter{

	private Context context;
	private Cursor cursor;

	private LinearLayout layout;
	

	public MyAdapter(Context context,Cursor cursor){
		this.context = context;
		this.cursor  = cursor;
	}
	

	@Override
	public int getCount() {
		return cursor.getCount();
	}

	@Override
	public Object getItem(int position) {
		return cursor.getPosition();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		layout = (LinearLayout) inflater.inflate(R.layout.listview_cell, null);

		TextView title_tv = (TextView) layout.findViewById(R.id.title);
		TextView time_tv = (TextView) layout.findViewById(R.id.time);
		ImageView img_iv = (ImageView) layout.findViewById(R.id.cell_img);
		ImageView video_iv = (ImageView) layout.findViewById(R.id.cell_video);
		
		cursor.moveToPosition(position);
		String title = cursor.getString(cursor.getColumnIndex("title"));
		String time = cursor.getString(cursor.getColumnIndex("time"));

		String urls = cursor.getString(cursor.getColumnIndex("imagepaths"));
        String new_url = urls.replace("[","");
        new_url = new_url.replace("]","");
        String[] url = new_url.split(",");


		String url_videos = cursor.getString(cursor.getColumnIndex("videopaths"));
        String new_url_video = url_videos.replace("[","");
        new_url_video = new_url_video.replace("]","");
        String[] url_video = new_url_video.split(",");

		title_tv.setText(title);
		time_tv.setText(time);
		img_iv.setImageBitmap(getImageThumbnail(url[0], 200, 200));
		video_iv.setImageBitmap(getVideoThumbnail(url_video[0], 200, 200,
				MediaStore.Images.Thumbnails.MICRO_KIND));
		
		
		return layout;
	}
	

	public Bitmap getImageThumbnail(String uri, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeFile(uri, options);
		options.inJustDecodeBounds = false;
		int beWidth = options.outWidth / width;
		int beHeight = options.outHeight / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(uri, options);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
	

	private Bitmap getVideoThumbnail(String uri, int width, int height, int kind) {
		Bitmap bitmap = null;
		bitmap = ThumbnailUtils.createVideoThumbnail(uri, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

}
