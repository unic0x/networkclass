package wrappernet;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.IOnActivityResult;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.BA.Author;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.ViewWrapper;

import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import com.facebook.network.connectionclass.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


@Author("Wrapped By Jamie John")
@Permissions(values={"android.permission.INTERNET"})
@Version(0.01f)
@Events(values={"state (response As String), error (response As String)"})
@ActivityObject
@ShortName("wrappernet")
@DependsOn(values={"jsr305-1.3.9"})

public class wrappernet {
	private static String eventName;
	private static BA ba;

	private ConnectionClassManager mConnectionClassManager;
	private DeviceBandwidthSampler mDeviceBandwidthSampler;
	private ConnectionChangedListener mListener;

	private String mURL = "https://get.google.com/apptips/images/google-play.png";
	private int mTries = 0;
	private ConnectionQuality mConnectionClass = ConnectionQuality.UNKNOWN;
  
 public void Initialize(final BA ba, String evname) {
	this.ba = ba;
	this.eventName = evname.toLowerCase(BA.cul);
	mConnectionClassManager = ConnectionClassManager.getInstance();
    mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance();
	BA.Log(mConnectionClassManager.getCurrentBandwidthQuality().toString());
	mListener = new ConnectionChangedListener();
  }

  /**
   * Listener to update the UI upon connectionclass change.
   */
   public void register() {
    mConnectionClassManager.register(mListener);
  }
  public void remove() {
    mConnectionClassManager.remove(mListener);
  }
  public void BandwidthQuality(){
	  BA.Log(mConnectionClassManager.getCurrentBandwidthQuality().toString());
  }
   public void downloadtest() {
	new DownloadImage().execute(mURL);
   }
  private class ConnectionChangedListener
      implements ConnectionClassManager.ConnectionClassStateChangeListener {

    @Override
    public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
      mConnectionClass = bandwidthState;
		  if(ba.subExists(wrappernet.eventName + "_state"))
            {
		ba.raiseEvent(this,wrappernet.eventName + "_state", mConnectionClass.toString());
			}
			else
			{
				BA.LogError("event sub does not exist: " + wrappernet.eventName);
			}
    }
  }
  /**
   * AsyncTask for handling downloading and making calls to the timer.
   */
   
  private class DownloadImage extends AsyncTask<String, Void, Void> {

    @Override
    protected void onPreExecute() {
      mDeviceBandwidthSampler.startSampling();
    }

    @Override
    protected Void doInBackground(String... url) {
      String imageURL = url[0];
      try {
        // Open a stream to download the image from our URL.
        URLConnection connection = new URL(imageURL).openConnection();
        connection.setUseCaches(false);
        connection.connect();
        InputStream input = connection.getInputStream();
        try {
          byte[] buffer = new byte[1024];

          // Do some busy waiting while the stream is open.
          while (input.read(buffer) != -1) {
          }
        } finally {
          input.close();
        }
      } catch (IOException e) {
		  if(ba.subExists(wrappernet.eventName + "_error"))
            {
		ba.raiseEvent(this,wrappernet.eventName + "_error", "Image not found");
		}
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void v) {
      mDeviceBandwidthSampler.stopSampling();
      // Retry for up to 10 times until we find a ConnectionClass.
      if (mConnectionClass == ConnectionQuality.UNKNOWN && mTries < 10) {
        mTries++;
        new DownloadImage().execute(mURL);
      }
      if (!mDeviceBandwidthSampler.isSampling()) {
		// BA.Log("Download View Gone Sampling");
      }
    }
  }
}
