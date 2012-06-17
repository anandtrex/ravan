package com.example.Raavan.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.example.Raavan.Constants;

import android.util.Log;

public class ServerDiscoveryDaemon implements Runnable {

	@Override
	public void run() {
		DatagramSocket socket;
		try {
			socket = new DatagramSocket(Constants.SERVER_DISCOVERY_PORT);

			while (true) {
				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				logInfo("waiting for client's discovery request...");
				socket.receive(packet);
				logInfo("got a client request!");
//				if (packet.getData().equals(Constants.DISCOVERY_REQUEST)) {
					DatagramPacket sendpacket = new DatagramPacket(Constants.DISCOVERY_RESPONSE.getBytes(), 
							Constants.DISCOVERY_RESPONSE.getBytes().length, InetAddress.getByName(Constants.CLIENT_IP),
							Constants.CLIENT_DISCOVERY_PORT_EXTERNAL);
					socket.send(sendpacket);
					logInfo("accepted client request for doing work.");
//				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void logInfo(String logMessage) {
		Log.i("Raavan_server_dd [" + Thread.currentThread().getId() + "]: ", logMessage);
	}

}
