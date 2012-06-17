package com.example.Raavan;

import java.net.InetAddress;

public class Constants {

	public static final int APK_FILE_FD  = R.raw.imgproc;
	public static final String APK_CLASS  = "com.test.Image.ImageGrayscale";
	public static final String APK_CLASS_METHODNAME  = "processData";

	public static final int[] TEXT_FILE_FDS = {/*R.raw.exec, R.raw.fork, R.raw.ps, 
		R.raw.ls, R.raw.strace,*/ 
		R.raw.gcc, R.raw.gcc, R.raw.gcc, R.raw.gcc, R.raw.gcc,
		R.raw.gcc, R.raw.gcc, R.raw.gcc, R.raw.gcc, R.raw.gcc,
		R.raw.gcc, R.raw.gcc, R.raw.gcc, R.raw.gcc, R.raw.gcc,
		R.raw.gcc, R.raw.gcc, R.raw.gcc, R.raw.gcc, R.raw.gcc,
		R.raw.gcc, R.raw.gcc, R.raw.gcc, R.raw.gcc, R.raw.gcc,
	};

	/************* Execution phase ***************/

	// "redir add tcp:8080:8070" on emulator running the server
	public static final int SERVER_PORT = 8070;
	//	public static final int SERVER_EXTERNAL_PORT = 8070;

	// "redir add tcp:8070:8080" on emulator running the client
	//	public static final int CLIENT_PORT = 8050;
	//	public static final int CLIENT_EXTERNAL_PORT = 8060;



	/************* Device Discovery phase ********/
	public static final String SERVER_IP = "10.0.2.2";
	public static final String CLIENT_IP = "10.0.2.2";

	// port used by client on current device when looking for servers on other devices.
	// "redir add udp:9010:9000" on emulator running the client
	public static final int CLIENT_DISCOVERY_PORT = 9000;
	public static final int CLIENT_DISCOVERY_PORT_EXTERNAL = 9010;

	// "redir add udp:9030:9020" on emulator running the server
	public static final int SERVER_DISCOVERY_PORT = 9020;
	public static final int SERVER_DISCOVERY_PORT_EXTERNAL = 9030;

	public static final String DISCOVERY_REQUEST = "Raavan: ServiceRequest";
	public static final String DISCOVERY_RESPONSE = "Raavan: RequestAccepted";

	public static final String LOCALHOST_ADDRESS = "127.0.0.1";

	public static int MAX_DEVICES_TO_DISCOVER = 1;

}
