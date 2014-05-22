package com.hci.fitbeat;

import java.util.ArrayList;
import java.util.HashMap;

import com.echonest.api.v4.EchoNestException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.parse.Parse;
import com.parse.ParseAnalytics;
/*
 *  Class will display list of songs in list layout by using SongsManager.java class
 */
public class PlayListActivity extends ListActivity {
	// Songs list
	public ArrayList<HashMap<String,String>> songsList = new ArrayList<HashMap<String, String>>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);
		
		ArrayList<HashMap<String, String>> songsListData = new ArrayList<HashMap<String, String>>();
		
		SongsManager plm = new SongsManager();
		// gets all songs from sdcard
		try {
			this.songsList = plm.getPlayList();
		} catch (EchoNestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// looping through playlist
		for(int i = 0; i < songsList.size(); i++) {
			//creating new HashMap
			HashMap<String, String> song = songsList.get(i);
			
			// adding HashList to ArrayList
			songsListData.add(song);
		}
		
		// Adding menuItems to ListView
		ListAdapter adapter = new SimpleAdapter(this, songsListData, 
				R.layout.playlist_item, new String[] {"songTitle"}, new int[] {R.id.songTitle});
		
		setListAdapter(adapter); // cursor for the listview
		
		// selecting single ListView item
		ListView lv = getListView();
		//listening to single listitem click
		lv.setOnItemClickListener(new OnItemClickListener() {
 
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
				// getting listitem index
				int songIndex = position;
				
				//starting new intent
				Intent in = new Intent(getApplicationContext(), AndroidBuildingMusicPlayerActivity.class);
				//sending songIndex to PlayerActivity
				in.putExtra("songIndex", songIndex);
				setResult(100, in);
				//Closing PlayListView
				finish();
			}
		
		});
	}
}
