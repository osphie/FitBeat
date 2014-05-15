package com.hci.fitbeat;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.Track;
import com.echonest.api.v4.TrackAnalysis;

import android.os.Environment;

public class SongsManager {
	
	
	//SDCard Path
	final String MEDIA_PATH = Environment.getExternalStorageDirectory()
            .getPath() + "/"; 
	private ArrayList<HashMap<String,String>> songsList = new ArrayList<HashMap<String, String>>();
	
	//Constructor 
	public SongsManager() {
		
	}
	
	/*
	 * Function to read all mp3 files from sdcard 
	 * and store the details in ArrayList
	 */
	public ArrayList<HashMap<String, String>> getPlayList() throws EchoNestException {
		EchoNestAPI en = new EchoNestAPI("YONAIFTTA0HFKM9J4" );
		File home = new File(MEDIA_PATH);
		//System.out.println("home found: " + home + " files: " + Environment.getExternalStorageDirectory().listFiles());
		if(home.listFiles(new FileExtensionFilter()) != null) {
			System.out.println("found files");
			if (home.listFiles(new FileExtensionFilter()).length > 0) {
				for( File file: home.listFiles(new FileExtensionFilter())) {
					try {
						System.out.println("file: " + file.getPath());
						 File currFile = new File(file.getPath());
						 if(!currFile.exists()) {
							 System.err.println("Can't fine " + file.getPath());
						 }
						 else {
							 System.out.println("found file " + file.getPath());
			                Track track = en.uploadTrack(currFile, true);
			                track.waitForAnalysis(30000);
			               // TrackAnalysis a = track.getAnalysis();
			                //System.out.println("Tempo" + a.getTempo());
			                if (track.getStatus() == Track.AnalysisStatus.COMPLETE) {
			                    System.out.println("Tempo: " + track.getTempo());
			                    System.out.println("Loudness: " + track.getLoudness());
			                    System.out.println();
			                    System.out.println("Beat start times:");
			                    
			                    TrackAnalysis analysis = track.getAnalysis();
			                    for (TimedEvent beat : analysis.getBeats()) {
			                        System.out.println("beat " + beat.getStart());
			                    }
			                } else {
			                    System.err.println("Trouble analysing track " + track.getStatus());
			                }
						 }
		            } catch (IOException e) {
		                System.err.println("Trouble uploading file");
		            }
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
}
