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


@Author("NetworkClass wrapped By J")
@Permissions(values={"android.permission.INTERNET"})
@Version(0.02f)
@Events(values={"state (response As String), error (response As String)"})
@ActivityObject
@ShortName("networkclass")
@DependsOn(values={"jsr305-1.3.9"})

public class networkclass {
	private static String eventName;
	private static BA ba;

	private ConnectionClassManager mConnectionClassManager;
	private DeviceBandwidthSampler mDeviceBandwidthSampler;
	private ConnectionChangedListener mListener;

	private String mURL = "https://eoimages.gsfc.nasa.gov/images/imagerecords/73000/73751/world.topo.bathy.200407.3x5400x2700.jpg";
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
	   //BA.Log("Registered");
    mConnectionClassManager.register(mListener);
  }
  public void remove() {
	  //BA.Log("Removed");
    mConnectionClassManager.remove(mListener);
  }
  public void BandwidthQuality(){
	  //BA.Log(mConnectionClassManager.getCurrentBandwidthQuality().toString());
	  		  if(ba.subExists(networkclass.eventName + "_state"))
            {
		ba.raiseEvent(this,networkclass.eventName + "_state", mConnectionClassManager.getCurrentBandwidthQuality().toString());
			}
			else
			{
				BA.LogError("event sub does not exist: " + networkclass.eventName);
			}
  }
   public void startSampling() {
	mDeviceBandwidthSampler.startSampling();
   }
   public void stopSampling() {
	mDeviceBandwidthSampler.stopSampling();
   }
   public void downloadtest() {
	new DownloadImage().execute(mURL);
   }
  private class ConnectionChangedListener
      implements ConnectionClassManager.ConnectionClassStateChangeListener {

    @Override
    public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
		//BA.Log("BandwidthStateChanged");
      mConnectionClass = bandwidthState;
		  if(ba.subExists(networkclass.eventName + "_state"))
            {
		ba.raiseEvent(this,networkclass.eventName + "_state", mConnectionClass.toString());
			}
			else
			{
				BA.LogError("event sub does not exist: " + networkclass.eventName);
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
	  //BA.Log("Started Sampling");
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
		  if(ba.subExists(networkclass.eventName + "_error"))
            {
		ba.raiseEvent(this,networkclass.eventName + "_error", "Nothing to sample");
		}
      }
      return null;
    }
    @Override
    protected void onPostExecute(Void v) {
		//BA.Log("Post");
      mDeviceBandwidthSampler.stopSampling();
      //Retry for up to 10 times until we find a ConnectionClass.
      if (mConnectionClass == ConnectionQuality.UNKNOWN && mTries < 10) {
        mTries++;
        new DownloadImage().execute(mURL);
      }
      if (!mDeviceBandwidthSampler.isSampling()) {
		//BA.Log("Gone");
      }
    }
  }
}
