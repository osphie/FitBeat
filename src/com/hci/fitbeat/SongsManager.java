package com.hci.fitbeat;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.Track;
import com.echonest.api.v4.TrackAnalysis;

import android.os.AsyncTask;
import android.os.Environment;

public class SongsManager {
	
	
	//SDCard Path
	final String MEDIA_PATH = Environment.getExternalStorageDirectory()
            .getPath() + "/";
	final String DB = Environment.getExternalStorageDirectory()
            .getPath() + "/songlist.txt";
	private ArrayList<HashMap<String,String>> songsList = new ArrayList<HashMap<String, String>>();
	private ArrayList<HashMap<String,String>> songsListTempo = new ArrayList<HashMap<String, String>>();
	private boolean completedTask = false;
	//Constructor 
	public SongsManager() {
		new Connection().execute("");
	}
	
	/*
	 * Function to read all mp3 files from sdcard 
	 * and store the details in ArrayList
	 */
	public ArrayList<HashMap<String, String>> getPlayList() throws EchoNestException {
		while(!completedTask) {
			System.out.println("waiting on thread");
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//System.out.println("home found: " + home + " files: " + Environment.getExternalStorageDirectory().listFiles());

		EchoNestAPI en = new EchoNestAPI("YONAIFTTA0HFKM9J4" );
		File home = new File(MEDIA_PATH);
		if(home.listFiles(new FileExtensionFilter()) != null) {
			System.out.println("found files");
			if (home.listFiles(new FileExtensionFilter()).length > 0) {
				for( File file: home.listFiles(new FileExtensionFilter())) {
					HashMap<String, String> song = new HashMap<String, String>();
					song.put("songTitle", file.getName().substring(0, (file.getName().length()-4))); //-4 because .mp3
					song.put("songPath",  file.getPath());
					
					songsList.add(song);
				}
			}
		}
		System.out.println("size of songslist after SongsManager: " + songsList.size());
		return songsList;
	}
	
	/*
	 * Class to filter files which have .mp3 extension 
	 */
	
	private class FileExtensionFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) { //dir= where file was found; name
			return (name.endsWith(".mp3") || name.endsWith(".MP3"));
		}
		
	}
	
	private class Connection extends AsyncTask {

		@Override
		protected Object doInBackground(Object... params) {

			EchoNestAPI en = new EchoNestAPI("YONAIFTTA0HFKM9J4" );
			File home = new File(MEDIA_PATH);
		//	System.out.println("home found: " + home + " files: " + Environment.getExternalStorageDirectory().listFiles());
			if(home.listFiles(new FileExtensionFilter()) != null) {
	
				if (home.listFiles(new FileExtensionFilter()).length > 0) {
					for( File file: home.listFiles(new FileExtensionFilter())) {
						try {
				
							 File currFile = new File(file.getPath());
							 if(!currFile.exists()) {
								 System.err.println("Can't find in async " + file.getPath());
							 }
							 else {
								 //System.out.println("found file in async " + file.getPath());
				                Track track = en.uploadTrack(currFile, true);
				                track.waitForAnalysis(30000);
				                System.out.println("waiting for analysis " + file.getPath());
				                if (track.getStatus() == Track.AnalysisStatus.COMPLETE) {
				                    System.out.println("Tempo: " + track.getTempo());
									HashMap<String, String> song = new HashMap<String, String>();
									song.put("songPath",  file.getPath());
									
									int tempo = (int) track.getTempo();
									if(track.getTempo() >= 120 ) {
										song.put("tempo", "fast");
									}
									else if(tempo <= 90) {
										song.put("tempo", "slow");
									}
									else {
										song.put("tempo", "med");
									}
									
									songsListTempo.add(song);
				                } else {
				                    System.err.println("Trouble analysing track " + track.getStatus());
				                }
							 }
			            } catch (IOException e) {
			                System.err.println("Trouble uploading file");
			            } catch (EchoNestException e) {
							e.printStackTrace();
						}
					}
				}
			}
			completedTask = true;
			return null;
		}
		
	}
}

