package com.novelbio.analysis.tools.snvintersection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileLineReader implements ReadFirstIterator<String> {
	private String line;
	private BufferedReader bufferedReader = null;
	private boolean isAvailable = false;
	private File file;

	public FileLineReader(File file) throws IOException {
		this.file = file;
		reset();
	}

	public void close() {
		if (bufferedReader != null) {
			try {
				bufferedReader.close();
			} catch (IOException ioe) {
			}
		}
	}

	@Override
	public void next() {
		try {
			String readLine = bufferedReader.readLine();
			if (readLine != null) {
				line = readLine;
				isAvailable = true;
			} else {
				line = null;
				isAvailable = false;
				close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void reset() {
		close();
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		next();
	}

	@Override
	public boolean isAvailable() {
		return isAvailable;
	}

	@Override
	public String current() {
		if (!isAvailable) {
			throw new RuntimeException("The current content does not exist");
		}
		return line;
	}

}
