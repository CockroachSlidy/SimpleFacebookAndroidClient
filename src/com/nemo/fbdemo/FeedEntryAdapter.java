package com.nemo.fbdemo;

import java.util.ArrayList;

import com.nemo.fbdemo.model.FeedEntry;
import com.nemo.fbdemo.model.FeedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FeedEntryAdapter extends BaseAdapter {

	ArrayList<FeedEntry> data;
	
	public FeedEntryAdapter(FeedList data){
		super();
		
		this.data = data.getFeed();
	}
	
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int arg0) {
		return data.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View oldView, ViewGroup parent) {
		ViewHolder viewHolder;
		View view;
		if(oldView == null){
			//creating new view and viewHolder
			LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.feed_entry, null);
			
			viewHolder = new ViewHolder();
			
			viewHolder.userName           = (TextView)view.findViewById(R.id.feed_userName);
			viewHolder.feedMessage        = (TextView)view.findViewById(R.id.feed_message);
			viewHolder.userPhoto          = (ImageView)view.findViewById(R.id.feed_userPic);
			viewHolder.feedPhoto          = (ImageView)view.findViewById(R.id.feed_pic);
			viewHolder.userProgressBar    = (ProgressBar)view.findViewById(R.id.feed_userPic_progressBar);
			viewHolder.pictureProgressBar = (ProgressBar)view.findViewById(R.id.feed_pic_progressBar);
			
			view.setTag(viewHolder);
		}
		else{
			//reusing old view
			view = oldView;
			viewHolder = (ViewHolder)view.getTag();
			cleanView(viewHolder);
		}
		
		FeedEntry entry = (FeedEntry)getItem(position);
		
		String userName = entry.getUser().getName();
		Bitmap pic      = entry.getUser().getPicture();
		
		viewHolder.userName.setText(userName);
		if(pic != null){
			viewHolder.userPhoto.setImageBitmap(pic);
			viewHolder.userProgressBar.setVisibility(ProgressBar.INVISIBLE);
		} else viewHolder.userProgressBar.setVisibility(ProgressBar.VISIBLE);
		
		String feedMessage;
		
		switch (entry.getType()) {
		case Status:
			feedMessage = entry.getMessage();
			break;
		case Photo:
			feedMessage = entry.getMessage();
			if(entry.getPicture() != null) viewHolder.feedPhoto.setImageBitmap(entry.getPicture());
			else viewHolder.pictureProgressBar.setVisibility(ProgressBar.VISIBLE);
			break;
		case Video:
			feedMessage = entry.getDescription();
			if(entry.getPicture() != null) {
				viewHolder.feedPhoto.setImageBitmap(entry.getPicture());
			} else viewHolder.pictureProgressBar.setVisibility(ProgressBar.VISIBLE);
			break;
		case Link:
			feedMessage = entry.getLink();
			break;

		default:
			feedMessage = "unsupported type";
			break;
		}
		
		viewHolder.feedMessage.setText(feedMessage);
		
		return view;
	}

	
	private void cleanView(ViewHolder viewHolder){	
		viewHolder.feedMessage.setText("");
		viewHolder.feedPhoto.setImageBitmap(null);
		viewHolder.pictureProgressBar.setVisibility(ProgressBar.GONE);
	}
	
	private static class ViewHolder{
		public TextView userName;
		public TextView feedMessage;
		public ImageView userPhoto;
		public ImageView feedPhoto;
		public ProgressBar userProgressBar;
		public ProgressBar pictureProgressBar;
	}

}
