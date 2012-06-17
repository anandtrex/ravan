package com.example.Raavan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.Raavan.Client.ClientDevice;
import com.example.Raavan.Server.ServerDevice;

public class Raavan extends Activity {

	public enum Mode {
		CLIENT, 
		SERVER
	};

	Mode mode = Mode.CLIENT;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mode == Mode.CLIENT){
//			byte[][] input = new byte[Constants.TEXT_FILE_FDS.length][];
//			for (int i = 0; i < Constants.TEXT_FILE_FDS.length; i++)
//				input[i] = Util.getFileContents(this.getApplicationContext(), Constants.TEXT_FILE_FDS[i]);
			startClient(this.getApplicationContext(), Constants.APK_FILE_FD,
					Constants.TEXT_FILE_FDS);
		} else {
			startServer();
		}
	}

	private void startServer() {
		Intent intent = new Intent(this, ServerDevice.class);
		this.startActivity(intent);		
	}

	private void startClient(Context context, int apkFileFd, int[] fileIds) {
		ClientDevice client = new ClientDevice(context);
		client.getWorkDone(Util.getFileContents(this.getApplicationContext(), apkFileFd), fileIds);
	}
}