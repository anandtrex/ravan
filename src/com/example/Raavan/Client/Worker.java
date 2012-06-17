package com.example.Raavan.Client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.example.Raavan.Util;

import android.content.Context;
import android.util.Log;

public class Worker implements Runnable {

	private Context context;
	private byte[] binary;
	private ConcurrentLinkedQueue<Integer> dataFileIds;
	private final String serverip;
	private final int serverport;
	private final int chunkId;
	private final ConcurrentLinkedQueue<byte[]> results;

	public Worker(Context context, byte[] binary, ConcurrentLinkedQueue<Integer> dataFileIds, String serverip, int serverport, 
			int chunkId, ConcurrentLinkedQueue<byte[]> results) {

		super();
		this.context = context;
		this.serverip = serverip;
		this.serverport = serverport;
		this.binary = binary;
		this.dataFileIds = dataFileIds;
		this.chunkId = chunkId;
		this.results = results;
	}

	@Override
	public void run() {
		Socket socket = null;
		InputStream in = null;
		OutputStream out = null;
		try {
			socket = new Socket(this.serverip, this.serverport);
			logInfo("socket created.");
			out = socket.getOutputStream();
			in = socket.getInputStream();
		} catch (UnknownHostException e) {
			logInfo(e.getStackTrace().toString());
			throw new RuntimeException("failed!");
		} catch (IOException e) {
			logInfo(e.getStackTrace().toString());
			throw new RuntimeException("failed!");
		}

		try {
			logInfo("Sending apk file...");
			// send the apk file
			byte[] fileLength = (this.binary.length + "").getBytes();  
			int d = fileLength.length;
			for(int k = d-1; d < 12; d++){  
				out.write("0".getBytes());
			}  
			out.write(fileLength);  
			out.write(this.binary);
			out.flush();
			logInfo("File transfer complete.");

			//			int count = dataFileIds.length;
			while (true) {
				logInfo("Number of data CHUNKS left for processing = " + dataFileIds.size());

				int dataFileId = -1;
				try {
					dataFileId = dataFileIds.remove();
				} catch (NoSuchElementException e) {
					logInfo("Done with all the processing!");
					// write a zero length to out to indicate to server that no more chunks need to be processed.
					byte[] dataLength = (0 + "").getBytes();  
					d = dataLength.length;
					for(int k = d-1; d < 12; d++){  
						out.write("0".getBytes());
					}  
					out.write(dataLength);  
					out.flush();

					in.close();
					out.close();
					socket.close();
					return;
				}

				assert(dataFileId != -1);

				// send the data now
				byte[] data = Util.getFileContents(context, dataFileId);

				byte[] dataLength = (data.length + "").getBytes();  
				d = dataLength.length;
				for(int k = d-1; d < 12; d++){  
					out.write("0".getBytes());
				}  
				out.write(dataLength);  
				out.write(data, 0, data.length); 
				out.flush();

				data = null; // GC the object as soon as work is done.


				// Get back response from the remote server
				// read the response size (up to 12 decimal digits long)  
				byte[] buf = new byte[12];  
				int bytesRead = 0;  

				while(bytesRead < 12){  
					if(bytesRead > 0){  
						bytesRead += in.read(buf, bytesRead, 12 - bytesRead);  
					} else bytesRead += in.read(buf, 0, 12);  
				}

				int datalen= 0;  
				try{  
					datalen = new Integer(new String(buf));  
				} catch (NumberFormatException e ){  
					logInfo("Data length is not in correct numerical format.");
					throw new RuntimeException("failed!");
				}
				logInfo("Got length of response from server.");

				ByteArrayOutputStream responseBuf = new ByteArrayOutputStream();  
				byte[] tmpBuf = new byte[1024];
				bytesRead = 0;
				int c = 0;
				while(bytesRead < datalen){
					c = in.read(tmpBuf, 0, 1024); 
					responseBuf.write(tmpBuf, 0, c);
					bytesRead += c;
					//					if(bytesRead > 0){  
					//						bytesRead += in.read(tmpBuf, bytesRead - 1, datalen - bytesRead);  
					//					} else bytesRead += in.read(buf, 0, datalen);  
				}
				//				try {
				//					for (int c = in.read(tmpBuf); c > -1; c = in.read(tmpBuf)) {
				//						responseBuf.write(tmpBuf, 0, c);
				//					}
				//				} catch (IOException e) {
				//					logInfo(e.getStackTrace().toString());
				//					throw new RuntimeException("Failed to get back correct response from server!");
				//				}
				logInfo("Got back response from the server!");
				//				logInfo("Response: \n" + responseBuf.toString());

				results.add(responseBuf.toByteArray());
			} // for loop

		} catch (IOException e) {
			logInfo(e.getStackTrace().toString());
			throw new RuntimeException("failed!");
		} 
	}

	private void logInfo(String logMessage) {
		Log.i("Raavan_client_worker [" + Thread.currentThread().getId() + "]: ", logMessage);
	}

}
