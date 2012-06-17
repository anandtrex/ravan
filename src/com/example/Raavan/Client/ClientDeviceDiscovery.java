package com.example.Raavan.Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

import com.example.Raavan.Constants;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class ClientDeviceDiscovery {

	/**
	 * Find devices and add to hashmap.
	 * 
	 * Discover servers running close-by by sending UDP broadcasts and
	 * see which one is free to do work. The server side discovery daemon is running 
	 * on {@link Constants#SERVER_DISCOVERY_PORT_EXTERNAL}.
	 * 
	 * @throws IOException 
	 */
	public static void findDevices(Context context, HashMap devices, int maxDevicesToDiscover) throws IOException {
		DatagramSocket socket = new DatagramSocket(Constants.CLIENT_DISCOVERY_PORT);
		socket.setBroadcast(true);
		
		// TODO : Replace server ip address with broadcast address when running on real phones!
		DatagramPacket sendpacket = new DatagramPacket(Constants.DISCOVERY_REQUEST.getBytes(), 
				Constants.DISCOVERY_REQUEST.length(),
				InetAddress.getByName(Constants.SERVER_IP), Constants.SERVER_DISCOVERY_PORT_EXTERNAL);
		socket.send(sendpacket);

		// TODO: timeout for finding servers
		//		socket.setSoTimeout(timeout);
		int numServers = maxDevicesToDiscover;
		while (numServers-- > 0) {
			byte[] buf = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			logInfo("Waiting to get response from server...");

			socket.receive(packet);
			logInfo("Got response: [" + new String(packet.getData()) + "]");
			if (new String(packet.getData()).indexOf(Constants.DISCOVERY_RESPONSE) > -1)
				devices.put(packet.getAddress(), packet.getPort());
			logInfo("Server found at IP: [" + packet.getAddress() +
					"] port: [" + packet.getPort() + "]");
		}
	}

	public static InetAddress getBroadcastAddress(Context context) throws IOException {
		logInfo("Trying to get the broadcast address...");
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		// handle null
		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		logInfo("Got broadcast address: [" +
				InetAddress.getByAddress(quads).getHostAddress() +"]");
		return InetAddress.getByAddress(quads);
	}
	
	private static void logInfo(String logMessage) {
		Log.i("Raavan_client_dd [" + Thread.currentThread().getId() + "]: ", logMessage);
	}

}
