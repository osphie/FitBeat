package com.hci.fitbeat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Track;

import java.io.FilenameFilter;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class AndroidBuildingMusicPlayerActivity extends Activity
implements OnCompletionListener, SeekBar.OnSeekBarChangeListener, SensorEventListener {

	private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    // Media Player
    private  MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();;
    //private SongsManager songManager;
    private Utilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> songsListEasy = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> songsListMed = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> songsListHard = new ArrayList<HashMap<String, String>>();
    private int genListSize;
    private int easyListSize;
    private int medListSize;
    private int hardListSize;
    protected boolean uploaded = false;
    
    //songmanager variables
    final String MEDIA_PATH = Environment.getExternalStorageDirectory()
            .getPath() + "/";
	final String DB = Environment.getExternalStorageDirectory()
            .getPath() + "/songlist.txt";
	//private ArrayList<HashMap<String,String>> songsList = new ArrayList<HashMap<String, String>>();
	private ArrayList<HashMap<String,String>> songsListTempo = new ArrayList<HashMap<String, String>>();
	private boolean completedTask = false;
 
    //sensor variables
	Sensor accelerometer;
	SensorManager sm;
	TextView acceleration;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);
 
        sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        
        acceleration=(TextView)findViewById(R.id.acceleration);
        
        // All player buttons
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
 
        // Mediaplayer
        mp = new MediaPlayer();
       // songManager = new SongsManager();
        utils = new Utilities();
        
        //Listeners
        songProgressBar.setOnSeekBarChangeListener(this);
        mp.setOnCompletionListener(this);
        
        Parse.initialize(this, "1HNsMPWDxmDo3SE6zwtsTqJMw8M63Ajw9yHUb88e", "vQ0lbV85eGTgp3d6PJ3rM82AuhsfpLqIsdKEstyy");
       /*
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.put("title", "bb");
        testObject.put("mode", "easy");
        testObject.saveInBackground();
        */
        //Getting all songs list
        try {
        	new Connection().execute("");
			songsList = getPlayList();
			genListSize = songsList.size();
		} catch (EchoNestException e) {
			e.printStackTrace();
		}
        
       if(genListSize > 0) {
	        //By default play first song
	        playSong(0);
	        /**
	         * Play button click event
	         * plays a song and changes button to pause image
	         * pauses a song and changes button to play image
	         * */
	        btnPlay.setOnClickListener(new View.OnClickListener() {
	 
	            @Override
	            public void onClick(View arg0) {
	                // check for already playing
	                if(mp.isPlaying()){
	                    if(mp!=null){
	                        mp.pause();
	                        // Changing button image to play button
	                        btnPlay.setImageResource(R.drawable.btn_play);
	                    }
	                }else{
	                    // Resume song
	                    if(mp!=null){
	                        mp.start();
	                        // Changing button image to pause button
	                        btnPlay.setImageResource(R.drawable.btn_pause);
	                    }
	                }
	 
	            }
	        });
	        
	        /*
	         *  Button Click event for Playlist click event
	         *  Launches list activity which displays list of songs
	         */
	        btnPlaylist.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
					startActivityForResult(i, 100);
					
				}
			});
	        
	        /**
	         * Forward button click event
	         * Forwards song specified seconds
	         * */
	        btnForward.setOnClickListener(new View.OnClickListener() {
	 
	            @Override
	            public void onClick(View arg0) {
	                // get current song position
	                int currentPosition = mp.getCurrentPosition();
	                // check if seekForward time is lesser than song duration
	                if(currentPosition + seekForwardTime <= mp.getDuration()){
	                    // forward song
	                    mp.seekTo(currentPosition + seekForwardTime);
	                }else{
	                    // forward to end position
	                    mp.seekTo(mp.getDuration());
	                }
	            }
	        });
	        
	        /**
	         * Backward button click event
	         * Backward song to specified seconds
	         * */
	        btnBackward.setOnClickListener(new View.OnClickListener() {
	 
	            @Override
	            public void onClick(View arg0) {
	                // get current song position
	                int currentPosition = mp.getCurrentPosition();
	                // check if seekBackward time is greater than 0 sec
	                if(currentPosition - seekBackwardTime >= 0){
	                    // forward song
	                    mp.seekTo(currentPosition - seekBackwardTime);
	                }else{
	                    // backward to starting position
	                    mp.seekTo(0);
	                }
	 
	            }
	        });
	        
	        /**
	         * Next button click event
	         * Plays next song by taking currentSongIndex + 1
	         * */
	        btnNext.setOnClickListener(new View.OnClickListener() {
	 
	            @Override
	            public void onClick(View arg0) {
	                // check if next song is there or not
	                if(currentSongIndex < (songsList.size() - 1)){
	                    playSong(currentSongIndex + 1);
	                    currentSongIndex = currentSongIndex + 1;
	                }else{
	                    // play first song
	                    playSong(0);
	                    currentSongIndex = 0;
	                }
	 
	            }
	        });
	        
	        /**
	         * Back button click event
	         * Plays previous song by currentSongIndex - 1
	         * */
	        btnPrevious.setOnClickListener(new View.OnClickListener() {
	 
	            @Override
	            public void onClick(View arg0) {
	                if(currentSongIndex > 0){
	                    playSong(currentSongIndex - 1);
	                    currentSongIndex = currentSongIndex - 1;
	                }else{
	                    // play last song
	                    playSong(songsList.size() - 1);
	                    currentSongIndex = songsList.size() - 1;
	                }
	 
	            }
	        });
	        
	        /**
	         * Button Click event for Repeat button
	         * Enables repeat flag to true
	         * */
	        btnRepeat.setOnClickListener(new View.OnClickListener() {
	 
	            @Override
	            public void onClick(View arg0) {
	                if(isRepeat){
	                    isRepeat = false;
	                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
	                    btnRepeat.setImageResource(R.drawable.btn_repeat);
	                }else{
	                    // make repeat to true
	                    isRepeat = true;
	                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
	                    // make shuffle to false
	                    isShuffle = false;
	                    btnRepeat.setImageResource(R.drawable.btn_repeat); //update to focused later
	                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
	                }
	            }
	        });
	        
	        /**
	         * Button Click event for Shuffle button
	         * Enables shuffle flag to true
	         * */
	        btnShuffle.setOnClickListener(new View.OnClickListener() {
	 
	            @Override
	            public void onClick(View arg0) {
	                if(isShuffle){
	                    isShuffle = false;
	                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
	                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
	                }else{
	                    // make repeat to true
	                    isShuffle= true;
	                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
	                    // make shuffle to false
	                    isRepeat = false;
	                    btnShuffle.setImageResource(R.drawable.btn_shuffle); //_focused);
	                    btnRepeat.setImageResource(R.drawable.btn_repeat);
	                }
	            }
	        });
        
       }
       else {
    	   songTitleLabel.setText("No Songs Found");
       }

    }

    /* 
     * Recieving song index from playlist view and play the song
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode == 100) {
    		currentSongIndex = data.getExtras().getInt("songIndex");
    		//play selected song
    		playSong(currentSongIndex);
    	}
    }
    
    /*
     * Function to play a song
     * @param songIndex - index of song
     */
    public void playSong(int songIndex) {
    	//Play song
    	try {
    		
    		mp.reset();
    		mp.setDataSource(songsList.get(songIndex).get("songPath"));
    		mp.prepare();
    		mp.start();
    		//Displaying Song Title
    		String songTitle = songsList.get(songIndex).get("songTitle");
    		songTitleLabel.setText(songTitle);
    		
    		//Changing Button Image to pause image
    		btnPlay.setImageResource(R.drawable.btn_pause);
    		
    		// set Progress bar values
    		songProgressBar.setProgress(0);
    		songProgressBar.setMax(100);
    		
    		//Updating progress bar
    		updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    


	/**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }   
 
    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
           public void run() {
               long totalDuration = mp.getDuration();
               long currentDuration = mp.getCurrentPosition();
 
               // Displaying Total Duration time
               songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
               // Displaying time completed playing
               songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
 
               // Updating progress bar
               int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
               //Log.d("Progress", ""+progress);
               songProgressBar.setProgress(progress);
 
               // Running this thread after 100 milliseconds
               mHandler.postDelayed(this, 100);
           }
        };
 
    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
 
    }
 
    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }
 
    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
 
        // forward or backward to certain seconds
        mp.seekTo(currentPosition);
 
        // update timer progress again
        updateProgressBar();
    }

    
    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     * */
    @Override
    public void onCompletion(MediaPlayer arg0) {
 
        // check for repeat is ON or OFF
        if(isRepeat){
            // repeat is on play same song again
            playSong(currentSongIndex);
        } else if(isShuffle){
            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            playSong(currentSongIndex);
        } else{
            // no repeat or shuffle ON - play next song
            if(currentSongIndex < (songsList.size() - 1)){
                playSong(currentSongIndex + 1);
                currentSongIndex = currentSongIndex + 1;
            }else{
                // play first song
                playSong(0);
                currentSongIndex = 0;
            }
        }
    }
    
    /*sensor functions */
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		// change value of text view to value on accelerometer 
		acceleration.setText("X: " + event.values[0] + "\nY: " +event.values[1] + "\nZ: " + event.values[2]);
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

		//EchoNestAPI en = new EchoNestAPI("YONAIFTTA0HFKM9J4" );
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
	
	int count = 0;
	private class Connection extends AsyncTask {

		@Override
		protected Object doInBackground(Object... params) {

			EchoNestAPI en = new EchoNestAPI("YONAIFTTA0HFKM9J4" );
			File home = new File(MEDIA_PATH);
			//File dbSongList = new File(DB);

				if(home.listFiles(new FileExtensionFilter()) != null) {
					
					if (home.listFiles(new FileExtensionFilter()).length > 0) {
						for( File file: home.listFiles(new FileExtensionFilter())) {
							try {
								
								 File currFile = new File(file.getPath());
								 if(!currFile.exists()) {
									 System.err.println("Can't find in async " + file.getPath());
								 }
								 else {
									 uploaded = false;
									// System.out.println("hi hi hi hi hi");
									// System.out.println("file name" + file.getName().substring(0, (file.getName().length()-4)) + " counter" + count);
									 ParseQuery<ParseObject> query = ParseQuery.getQuery("SongObject");
									 query.whereEqualTo("title", file.getName().substring(0, (file.getName().length()-4)) );
								       	try {
								            List<ParseObject> queryResult = query.find();
								            for(ParseObject so : queryResult) {
								                ParseObject songObj = new ParseObject("SongObject");
								                System.out.println("inside parse " + so.getString("title"));
								                uploaded = true;
								            }
								        }
								        catch(ParseException e) {
								            Log.d("mNameList", "Error: " + e.getMessage());
								        }
								        
									/* ParseQuery<ParseObject> query = ParseQuery.getQuery("SongObject");
									 query.whereEqualTo("title", file.getName().substring(0, (file.getName().length()-4)) );
									 query.findInBackground(new FindCallback<ParseObject>() {

										@Override
										public void done( List<ParseObject> songList,ParseException e) {
											if( e == null) {
												System.out.println("found music " + songList.size() );
												uploaded = true;
											} else {
												System.out.println("not in db");
											}
											
										}
									 }); 
									 */
									 
									 if(!uploaded) {
										// uploaded = false;
									    System.out.println("EchoNest API call");
						                Track track = en.uploadTrack(currFile, true);
						                track.waitForAnalysis(30000);
						                //System.out.println("waiting for analysis " + file.getPath() + " counter " + count + "what?");
						                count++;
						                if (track.getStatus() == Track.AnalysisStatus.COMPLETE) {
						                    System.out.println("Tempo: " + track.getTempo());
											HashMap<String, String> song = new HashMap<String, String>();
											song.put("songPath",  file.getPath());
											
											int tempo = (int) track.getTempo();
											String mode = "";
											if(track.getTempo() >= 120 ) {
												song.put("tempo", "fast");
												mode = "hard";
											}
											else if(tempo <= 90) {
												song.put("tempo", "slow");
												mode = "easy";
											}
											else {
												song.put("tempo", "med");
												mode ="med";
											}
											
											songsListTempo.add(song);
											
									        ParseObject songObject = new ParseObject("SongObject");
									        songObject.put("path", file.getPath());
									        songObject.put("title", file.getName().substring(0, (file.getName().length()-4)));
									        songObject.put("mode", mode);
									        songObject.saveInBackground();
						                } else {
						                    System.err.println("Trouble analysing track " + track.getStatus());
						                }
									 }
								  }
								 
								/* HashMap<String, String> song = new HashMap<String, String>();
								 song.put("songTitle", file.getName().substring(0, (file.getName().length()-4))); //-4 because .mp3
								 song.put("songPath",  file.getPath());	
								 songsList.add(song);*/
								 
				            } catch (IOException e) {
				                System.err.println("Trouble uploading file");
				            } catch (EchoNestException e) {
								e.printStackTrace();
							}
						}
					}
				}
				

		//	System.out.println("home found: " + home + " files: " + Environment.getExternalStorageDirectory().listFiles());

			completedTask = true;
			return null;
		}
		
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
