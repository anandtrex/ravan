package com.example.Raavan.Server;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

import com.example.Raavan.Constants;
import com.example.Raavan.Util;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import dalvik.system.DexClassLoader;

public class ServerDevice extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		try {
			logInfo("Starting device discovery daemon...");
			Thread deviceDiscoveryDaemon = new Thread(new ServerDiscoveryDaemon(), 
			"ServerDeviceDiscoveryDaemon");
			deviceDiscoveryDaemon.start();

			logInfo("Starting server...");
			startServer(); // does not return!
			deviceDiscoveryDaemon.join();
		} catch (IOException e) {
			logInfo(e.getStackTrace().toString());
		} catch (InterruptedException e) {
			logInfo(e.getStackTrace().toString());
		}
	}

	private void startServer() throws IOException {

		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(Constants.SERVER_PORT);
		} catch (IOException e) {
			logInfo("Could not listen on port: " + Constants.SERVER_PORT);
			return;
		}
		logInfo("bind to port " + Constants.SERVER_PORT + " successful.");

		// Listen to client connections.
		// Process results and send them back.
		while (true) {
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				logInfo("Accept failed.");
				throw new RuntimeException("socket accept failed!");
			}

			logInfo("client connected!");
			InputStream in = clientSocket.getInputStream();
			OutputStream out = clientSocket.getOutputStream();


			// ********************************************
			// Receive APK File.
			// ********************************************		  
			String apkFileName = "client_binary.apk";
			FileOutputStream fout = openFileOutput(apkFileName, Context.MODE_WORLD_WRITEABLE);

			// read the file size (up to 12 decimal digits long)  
			byte[] buf0 = new byte[12];  
			int bytesRead = 0;  

			while(bytesRead < 12){  
				if(bytesRead > 0){  
					bytesRead += in.read(buf0, bytesRead, 12 - bytesRead);  
				} else bytesRead += in.read(buf0, 0, 12);  
			}  

			int fileLength = 0;  
			try{  
				fileLength = new Integer(new String(buf0));  
			} catch (NumberFormatException e ){  
				logInfo("File length is not in correct numerical format.");
				return;
			}

			logInfo("got file length from client [" + fileLength + "].");

			// read upto fileLength bytes from the 'in' and close  
			bytesRead = 0;  
			byte[] buf = new byte[1024];  

			int c;  
			while(bytesRead  < fileLength){  
				if(fileLength - bytesRead > 1024){  
					c = in.read(buf, 0, 1024);  
				} else{  
					c = in.read(buf, 0, fileLength - bytesRead);  
				}  
				bytesRead += c;  
				fout.write(buf, 0, c);  
			}         
			fout.close(); //close file

			logInfo("got the APK file from the client!");


			// ********************************************
			// Receive Data.
			// ********************************************

			while (true) {

				logInfo("waiting for data from client...");
				
				String dataFileName = "client_data.data";
				FileOutputStream ofout = openFileOutput(dataFileName, Context.MODE_WORLD_WRITEABLE);

				// read the data size (up to 12 decimal digits long)  
				buf0 = new byte[12];  
				bytesRead = 0;  

				while(bytesRead < 12){  
					if(bytesRead > 0){  
						bytesRead += in.read(buf0, bytesRead, 12 - bytesRead);  
					} else bytesRead += in.read(buf0, 0, 12);  
				}

				int objectLength = 0;  
				try{  
					objectLength = new Integer(new String(buf0));  
				} catch (NumberFormatException e ){  
					logInfo("Data length is not in correct numerical format.");
					return;
				}

				if (objectLength == 0) {
					logInfo("DONE processing data! Closing client connection...");
					break;
				}

				logInfo("got data length from client.");

				// read upto fileLength bytes from the 'in' and close  
				bytesRead = 0;  
				buf = new byte[1024];  

				while(bytesRead  < objectLength){  
					if(objectLength - bytesRead > 1024){  
						c = in.read(buf, 0, 1024);  
					} else{  
						c = in.read(buf, 0, objectLength - bytesRead);  
					}  
					bytesRead += c;  
					ofout.write(buf, 0, c);  
				}   
				ofout.close(); //close file
				logInfo("got the data from client!");


				logInfo("Internal files stored at: [" + getFilesDir().getAbsolutePath() + "]");
				logInfo("APK file at: [" + getFilesDir().getAbsolutePath() + "/" + apkFileName + "]");
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				try {
					DexClassLoader dexClassLoader = new DexClassLoader(getFilesDir().getAbsolutePath() + "/" + apkFileName, 
							getFilesDir().getAbsolutePath(), 
							null , 
							ServerDevice.class.getClassLoader());

					Class<?> dc = dexClassLoader.loadClass(Constants.APK_CLASS);
					Object dl = dc.newInstance();

					logInfo("Invoking class method [" + Constants.APK_CLASS_METHODNAME + "]...");
					Method[] methods = dc.getMethods();
					Object result = null;
					for (int i = 0; i < methods.length; i++) {
						// TODO: This does not handle the case where method is overloaded.
						if (methods[i].getName().equals(Constants.APK_CLASS_METHODNAME)) {
							result = methods[i].invoke(dl, Util.getFileContents(getApplicationContext(), dataFileName));
							break;
						}
					}
					logInfo(result.toString());
					logInfo("binary invocation done!");
					new ObjectOutputStream(bos).writeObject(result);   

				} catch (ClassNotFoundException e) {
					logInfo(e.getStackTrace().toString());
					throw new RuntimeException("class not found!");
				} catch (IllegalAccessException e) {
					logInfo(e.getStackTrace().toString());
					throw new RuntimeException("illegal access!");
				} catch (InstantiationException e) {
					logInfo(e.getStackTrace().toString());
					throw new RuntimeException("instantiation exception!");
				} catch (IllegalArgumentException e) {
					logInfo(e.getStackTrace().toString());
					throw new RuntimeException("illegal argument!");
				} catch (SecurityException e) {
					logInfo(e.getStackTrace().toString());
					throw new RuntimeException("security exception!");
				} catch (InvocationTargetException e) {
					logInfo(e.getStackTrace().toString());
					throw new RuntimeException("invocation target exception");
				}

				// ********************************************
				// Send back the response to the client.
				// ********************************************
				logInfo("publishing back the results...");
				byte[] response = bos.toByteArray();
				byte[] responseLength = (response.length + "").getBytes();  
				int d = responseLength.length;
				for(int k = d-1; d < 12; d++){  
					out.write("0".getBytes());
				}  
				out.write(responseLength);  
				out.write(response, 0, response.length); 
				out.flush();
				logInfo("Sent the results back to the client!");
			}

			// Close the connection.
			try {
				in.close();
				out.close();
				clientSocket.close();
			} catch (IOException e) {
				Log.e("SERVER: ", e.getStackTrace().toString());
				throw new RuntimeException("io exception!");		
			}			
		} // while

		// serverSocket.close();
	}

	private void logInfo(String logMessage) {
		Log.i("Raavan_server [" + Thread.currentThread().getId() + "]: ", logMessage);
	}
}
