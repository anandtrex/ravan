package com.example.Raavan;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

public class Util {

	public static byte[] getFileContents(Context context, int fileFd) {
		InputStream fis = null;
		try {
			fis = context.getResources().openRawResource(fileFd);
		} catch (NotFoundException e) {
			throw new RuntimeException("File not found!");
		}

		return getFileContentsAsByteArray(fis);
	}

	public static byte[] getFileContents(Context context, String filePath) {
		InputStream fis = null;
		try {
			fis = context.openFileInput(filePath);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found!");
		}

		return getFileContentsAsByteArray(fis);
	}
	
	private static byte[] getFileContentsAsByteArray(InputStream fis) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();  
		byte[] tmpBuf = new byte[1024];
		try {
			for (int c = fis.read(tmpBuf); c > -1; c = fis.read(tmpBuf)) {
				buf.write(tmpBuf, 0, c);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to read the file!");
		}
		
		return buf.toByteArray();
	}
}
