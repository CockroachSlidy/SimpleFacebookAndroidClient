package com.nemo.fbdemo;

import com.facebook.GraphUser;
import com.facebook.ProfilePictureView;
import com.facebook.Request;
import com.facebook.Session;
import com.facebook.Response;
import com.nemo.fbdemo.model.FeedEntry;
import com.nemo.fbdemo.model.FeedEntryType;
import com.nemo.fbdemo.model.FeedList;
import com.nemo.fbdemo.model.UsersList;
import com.nemo.fbdemo.network.FeedDownloader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SelectionFragment extends Fragment {
	
	private ProfilePictureView profilePictureView;
	private TextView userNameView;
	private PullToRefresh feedListView;
	private ProgressBar feedProgressBar;
	
	private FeedEntryAdapter feedAdapter;
	private UsersList users;
	private static FeedList feedList;
	private static String userName = "Loading...";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
	    super.onCreateView(inflater, container, savedInstanceState);
	    
	    View view = inflater.inflate(R.layout.selection, container, false);

	    profilePictureView = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic);
	    profilePictureView.setCropped(true);
	    userNameView = (TextView) view.findViewById(R.id.selection_user_name);
	    feedProgressBar = (ProgressBar)view.findViewById(R.id.feed_progressBar);
	    
	    //adding callback to write post button
	    Button writeButton = (Button)view.findViewById(R.id.write_post_button);
	    writeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new StatusUpdateDialog().show(getFragmentManager(), "dialog"); 
			}
		});
	    
	    //adding callback to feed listView
	    feedListView = (PullToRefresh) view.findViewById(R.id.feed);
	    feedListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
				FeedEntry entry = feedList.getFeed().get((int)id);
				if(entry.getType() == FeedEntryType.Link ||
				   entry.getType() == FeedEntryType.Photo ||
				   entry.getType() == FeedEntryType.Video){
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getLink()));
					startActivity(browserIntent);
				}
			}
	    	
		});
	    feedListView.setOnRefreshListener(new PullToRefresh.OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				loadOlderFeed();
			}
		});
	    
	    //adding callback to userList. It will be called when user pictures loaded.
		users = new UsersList();
		users.setCallback(new UsersList.Callback() {
			
			@Override
			public void userDataLoaded() {
				if(feedAdapter != null && getActivity() != null){
					
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							feedAdapter.notifyDataSetChanged();
						}
					});
				}
			}
			
		});
		
		loadUser();
	    
	    return view;
	}
	
	@Override 
	public void onResume(){
		super.onResume();
		loadFeed();
	}
	
	private void loadUser(){
		if(userName.equals("Loading...")){
		    final Session session = Session.getActiveSession();
		    if (session != null && session.isOpened()) {
		        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
		        	
		            @Override
		            public void onCompleted(GraphUser user, Response response) {
		                if (session == Session.getActiveSession()) {
		                    if (user != null) {
		                        profilePictureView.setUserId(user.getId());
		                        userName = user.getName();
		                        userNameView.setText(userName);
		                    }   
		                }   
		            }
		            
		        });
		        
		        Request.executeBatchAsync(request);
		    }  
		}
		userNameView.setText(userName);
	}
	
	private void loadOlderFeed(){
		Bundle bundle = new Bundle();
		bundle.putString("limit", "10");
		bundle.putString("until", feedList.getNextPageUrl());
		FeedDownloader.Download(users, false, bundle, new FeedDownloader.Callback() {
			
			@Override
			public void Error() {
				Toast.makeText(getActivity(), "Error", Toast.LENGTH_LONG).show();
				feedListView.onRefreshComplete();
			}
			
			@Override
			public void Downloaded(FeedList feed) {
				if(feed != null) feedList.add(feed, true);
				else Toast.makeText(getActivity(), "No more feeds", Toast.LENGTH_SHORT).show();
				feedListView.onRefreshComplete();
			}
		});
	}
	
 	private void loadFeed(){
		
 		if(feedList != null && feedList.getFeed().size() > 0){
 			feedAdapter = new FeedEntryAdapter(feedList);
			feedListView.setAdapter(feedAdapter);
			return;
 		}
		
		feedProgressBar.setVisibility(ProgressBar.VISIBLE);
		Bundle params = new Bundle();
		params.putString("limit", "10");
		
		FeedDownloader.Download(users, true, params, new FeedDownloader.Callback() {
			
			@Override
			public void Downloaded(FeedList feed) {
				feedList = feed;
				feedAdapter = new FeedEntryAdapter(feedList);
				feedListView.setAdapter(feedAdapter);	
				feedProgressBar.setVisibility(ProgressBar.INVISIBLE);
				
				//adding feedList callback. will be called when pictures loaded.
				feedList.setCallback(new FeedList.Callback() {
					
				@Override
				public void dataChanged() {
						if(feedAdapter != null && getActivity() != null){
							getActivity().runOnUiThread(new Runnable() {
								public void run() {
									feedAdapter.notifyDataSetChanged();
								}
							});
						}
					}
				});
			}

			@Override
			public void Error() {
				Toast.makeText(getActivity(), getString(R.string.feed_download_error), Toast.LENGTH_LONG).show();
				feedList = null;
			}
		});
	}
	
	
}
