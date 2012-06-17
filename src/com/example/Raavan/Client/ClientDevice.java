package com.example.Raavan.Client;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import android.content.Context;
import android.util.Log;

import com.example.Raavan.Constants;
import com.example.Raavan.R;
import com.example.Raavan.Util;
import com.example.Raavan.Server.ServerDevice;

import dalvik.system.DexClassLoader;

public class ClientDevice {

	private Context context;	
	ConcurrentLinkedQueue<Integer> input = null;
	ConcurrentLinkedQueue<byte[]> results = null;

	public ClientDevice(Context context) {
		this.context = context; 
	}

	private static int[][] divideDataFiles(int numDevices, int[] dataFiles) {
		int perDevice = dataFiles.length / numDevices;
		int[][] data = new int[numDevices][];
		int count = 0;
		for (int i = 0; i < numDevices - 1; i ++) {
			data[i] = new int[perDevice]; 
			for (int j = 0; j < perDevice; j++) {
				data[i][j] = dataFiles[count++];
			}
		}
		data[numDevices - 1] = new int[dataFiles.length - count]; 
		int remaining = dataFiles.length - count;
		for (int j = 0; j < remaining; j++) {
			data[numDevices - 1][j] = dataFiles[count++];
		}
		return data;
	}
	
	public Object getWorkDone(byte[] binaryFile, int[] dataFiles) {
		input = new ConcurrentLinkedQueue<Integer>();
		results = new ConcurrentLinkedQueue<byte[]>();
		
		// Some numbers to keep track of time taken for different processing
		long startDiscovery = 0, endDiscovery = 0;
		long startRemoteProcessing = 0, endRemoteProcesing = 0;
		long startLocalProcessing = 0, endLocalProcessing = 0;
		long startTotalProcessing = 0, endTotalProcessing = 0;
		
		startDiscovery = System.currentTimeMillis();
//		HashMap<String, Integer> devices = new HashMap<String, Integer>();
//		try {
//			ClientDeviceDiscovery.findDevices(context, devices, Constants.MAX_DEVICES_TO_DISCOVER);
//		} catch (IOException e) {
//			logInfo(e.getStackTrace().toString());
//			return null;(
//		}(
		endDiscovery = System.currentTimeMillis();

		
//		/* TESTING */ devices.put("192.168.1.106", 8080);devices.put("192.168.1.104", 8080);  
		String[] deviceip = {/*"10.0.2.2", "10.0.2.2", "10.0.2.2", "10.0.2.2"*/};
		int[] deviceports = {8000, 8020, 8040, 8060};
//		for (int i = 0; i < 1 ; i++) 
			input.add(R.raw.rose100);
		
		
		Thread[] clientThreads = new Thread[deviceip.length];
		logInfo("Creating Threads for remote work...");
		startRemoteProcessing = System.currentTimeMillis();
		startTotalProcessing =  System.currentTimeMillis();
		for (int i = 0; i < deviceip.length; i++ ){
			clientThreads[i] = new Thread(
					new Worker(context, binaryFile, input, deviceip[i], deviceports[i], i, results),
					"ClientThread_" + i);
			logInfo("Starting thread " + i +"...");
			clientThreads[i].start();
		}

		logInfo("Starting work locally...");		
		startLocalProcessing = System.currentTimeMillis();
		while (true) {
			try {
				int dataFileId = input.remove();
				results.add(doWork(binaryFile, dataFileId));
			} catch (NoSuchElementException e) {
				logInfo("Done with all the processing locally!");
				break;
			}
		}
		endLocalProcessing = System.currentTimeMillis();
		
		logInfo("Waiting for remote work to finish...");
		for (int i = 0; i < deviceip.length; i++) {
			try {
				clientThreads[i].join();
			} catch (InterruptedException e) {
				logInfo(e.getStackTrace().toString());
			}
		}
		endRemoteProcesing = System.currentTimeMillis();
		endTotalProcessing =  System.currentTimeMillis();

		logInfo("ALL DONE!\n\n" + "TotalProcessingTime = " + (endTotalProcessing - startTotalProcessing) +
				"\nTOTAL_NUM_RESULTS = " + results.size() + "\n\n");

		return results;
	}


	private byte[] doWork(byte[] file, int dataFileId) {

		String fileName = "local_binary.apk";
		try {
			FileOutputStream fout = context.openFileOutput(fileName, Context.MODE_WORLD_WRITEABLE);

			fout.write(file);  
			fout.close(); //close file

			logInfo("Internal files stored at: [" + context.getFilesDir().getAbsolutePath() + "]");
			logInfo("APK file at: [" + context.getFilesDir().getAbsolutePath() + "/" + fileName + "]");
			DexClassLoader dexClassLoader = new DexClassLoader(context.getFilesDir().getAbsolutePath() + "/" + fileName, 
					context.getFilesDir().getAbsolutePath(), 
					null , 
					ServerDevice.class.getClassLoader());

			Class<?> dc = dexClassLoader.loadClass(Constants.APK_CLASS);
			Object dl = dc.newInstance();

			logInfo("Invoking class method [" + Constants.APK_CLASS_METHODNAME + "]" /*+ " with data: [" + new String(data) + "]"*/);
			Method[] methods = dc.getMethods();
			Object result = null;
			
			byte[] data = Util.getFileContents(context, dataFileId);
			
			for (int i = 0; i < methods.length; i++) {
				// TODO: This does not handle the case where method is overloaded.
				if (methods[i].getName().equals(Constants.APK_CLASS_METHODNAME)) {
					result = methods[i].invoke(dl, data);
					break;
				}
			}
			data = null;
			
//			Object obj = dc.getMethod(Constants.APK_CLASS_METHODNAME, null).invoke(dl, new String(data));
//			logInfo(result.toString());
			logInfo("DONE!");
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);   
			out.writeObject(result);
			return bos.toByteArray();

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
			throw new RuntimeException("invocation target exception!");
//		} catch (NoSuchMethodException e) {
//			logInfo(e.getStackTrace().toString());
//			throw new RuntimeException("no such method exception!");
		} catch (IOException e) {
			logInfo(e.getStackTrace().toString()); 
			throw new RuntimeException("got IO exception!");
		}
	}
	
	private void logInfo(String logMessage) {
		Log.i("Raavan_client [" + Thread.currentThread().getId() + "]: ", logMessage);
	}
}
