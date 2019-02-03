package edu.psu.ist.vaccine.geotxt.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileWriter {

	public static void writeFile(String value, String path) throws IOException {
		File file = new File(path);
		FileOutputStream fos = new FileOutputStream(file);
		if (!file.exists()) {
			file.createNewFile();
		}

		byte[] contentInBytes = value.getBytes();

		fos.write(contentInBytes);
		fos.flush();
		fos.close();
	}

}
