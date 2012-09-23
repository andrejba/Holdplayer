package de.abmw.holdplayer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

public class Holdplayer extends Activity {

	private String YoutubeServiceUrl = "http://gdata.youtube.com/feeds/api/videos/";
	private int VideoPosition = 0;
	private String VideoURL = "";
	private ProgressBar progressBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_holdplayer);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		progressBar = (ProgressBar)findViewById(R.id.Progressbar);
		
		Bundle extras = getIntent().getExtras();
		
		if (extras != null) {
			VideoURL = "";
			VideoPosition = 0;
			ParseVideoIDFromUrl(extras.getString(Intent.EXTRA_TEXT));
		} else {
	        SharedPreferences prefs = getPreferences(0); 
	        VideoURL = prefs.getString("VideoURL", "");
	        VideoPosition = prefs.getInt("VideoPosition", 0);
	        
	        if (VideoURL.length() > 0) {
        		PlayVideo();
	        }
        }
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		VideoPosition = 0;
		
        VideoView vv = (VideoView) findViewById(R.id.VideoPlayer);
        VideoPosition =  vv.getCurrentPosition();
		
        SharedPreferences.Editor editor = getPreferences(0).edit();
        editor.putString("VideoURL", VideoURL);
        editor.putInt("VideoPosition", VideoPosition);
        editor.commit();
	}
	
	private void ParseVideoIDFromUrl(String url) {

		if (url != null && url.length() > 0) {

			String videoid = "";

			int pos = url.toLowerCase().indexOf("v=");

			if (pos > 0) {
				videoid = url.substring(pos + 2);
				if (videoid.indexOf("&") > 0) videoid = videoid.substring(0,videoid.indexOf("&"));

				if (videoid.length() > 0) {
					try {
						URL urlVideo = new URL(YoutubeServiceUrl + videoid);
						new PlayVideoByVideoID().execute(urlVideo);

					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				} else {
					TextView tv = (TextView) findViewById(R.id.txMain);
					tv.setText("Video ID not found!");
				}
			}
		}
	}

	private void PlayVideo() {
		if (VideoURL.length() > 0) {
			progressBar.setVisibility(View.VISIBLE);
			final VideoView vv = (VideoView) findViewById(R.id.VideoPlayer);
			vv.setVideoURI(Uri.parse(VideoURL));
			MediaController mc = new MediaController(this);
			vv.setMediaController(mc);
			vv.requestFocus();
			vv.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {
						@Override
						public void onSeekComplete(MediaPlayer mp) {
							mp.start();
							progressBar.setVisibility(View.GONE);
						}
					});
					if (VideoPosition > 0 )  {
						mp.start();
						mp.seekTo(VideoPosition);
						mp.pause();
					} else {
						mp.start();
						progressBar.setVisibility(View.GONE);
					}
				}
			});
			vv.start();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_holdplayer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		VideoView vv = (VideoView) findViewById(R.id.VideoPlayer);

		switch (item.getItemId()) {
		case R.id.menu_stopvideo:
			if (vv.isPlaying() == true) vv.pause();
			return true;
		case R.id.menu_playvideo:
			if (vv.isPlaying() == false) vv.start();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class PlayVideoByVideoID extends AsyncTask<URL, Integer, String> {

		protected String doInBackground(URL... urls) {

			try {
				URL url = urls[0];

				HttpGet httpGet = new HttpGet(url.toString());
				HttpClient httpclient = new DefaultHttpClient();
				HttpResponse response = httpclient.execute(httpGet);
				InputStream  content = response.getEntity().getContent();

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();

				Document dom = db.parse(content);      
				Element docEle = dom.getDocumentElement();

				NodeList nl = docEle.getElementsByTagName("media:content");
				if (nl != null && nl.getLength() > 0) {
					for (int i = 0 ; i < nl.getLength(); i++) {
						Element entry = (Element)nl.item(i);
						String format = entry.getAttribute("yt:format");

						if (format.equals("1")) {
							String vurl = entry.getAttribute("url");
							return vurl;
						}
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			finally {
			}

			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
		}

		protected void onPostExecute(String result) {
			VideoURL = result;
			VideoPosition = 0;
			PlayVideo();
		}
	}
}
