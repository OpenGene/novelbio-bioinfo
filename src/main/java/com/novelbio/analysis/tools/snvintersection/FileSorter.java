package com.novelbio.analysis.tools.snvintersection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.Validate;

/**
 * @author novelbio
 *
 */
public class FileSorter {

	private SortStrategy sortStrategy;

	private File file;
	private File outputFile = null;
	private static final double constFileSize = 50 * Math.pow(2, 20);

	private FileSorter(File file) {
		this.file = file;
		this.outputFile = null;
		sortStrategy = new SortStrategy.DefaultSortStrategy();
	}

	private FileSorter(File file, SortStrategy sortStrategy) {
		Validate.notNull(file, "File can not be null!");
		Validate.notNull(sortStrategy, "SortStrategy can not be null!");
		this.file = file;
		this.sortStrategy = sortStrategy;
	}

	private void sortFile() throws IOException {
		File[] parts = SplitFile(file);
		mergeFile(file, parts);
		deleteTemporaryFiles(parts);
	}

	private void deleteTemporaryFiles(File[] parts) {
		for (int i = 0; i < parts.length; i++) {
			FileUtils.deleteQuietly(parts[i]);
		}
	}

	private long getTotalLineNumber(File file) {
		try {
			long lineNumber = 0;
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			while (bufferedReader.readLine() != null) {
				lineNumber++;
			}
			bufferedReader.close();
			return lineNumber;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private File[] SplitFile(File file) throws IOException {
		long totalSize = FileUtils.sizeOf(file);
		long totalLineNumber = getTotalLineNumber(file);
		LineIterator lineIterator = FileUtils.lineIterator(file);
		int fileNumber = (int) Math.ceil(totalSize / constFileSize);
		long partFileLineLineNumber = totalLineNumber / fileNumber;
		List<File> partFileList = new ArrayList<>();
		List<String> content = new ArrayList<>();
		for (int i = 0; i < fileNumber && lineIterator.hasNext(); i++) {
			content.clear();
			for (int j = 0; j < partFileLineLineNumber && lineIterator.hasNext(); j++) {
				content.add(lineIterator.next());
			}
			Collections.sort(content, sortStrategy);
			File partFile = new File(file.getParentFile(), file.getName() + ".part" + i);
			partFileList.add(partFile);
			FileUtils.writeLines(partFile, content);
		}
		File[] result = new File[partFileList.size()];
		return partFileList.toArray(result);
	}

	private void mergeFile(File file, File[] parts) throws IOException {
		outputFile = new File(file.getParent(), file.getName() + ".sort");
		BufferedWriter br = new BufferedWriter(new FileWriter(outputFile));
		UnsortedLine[] lineInfoArr = new UnsortedLine[parts.length];
		for (int i = 0; i < lineInfoArr.length; i++) {
			lineInfoArr[i] = new UnsortedLine(parts[i], sortStrategy);
		}
		UnsortedLine minLineInfo = null;
		while (!isFinish(lineInfoArr)) {
			minLineInfo = getMinLineInfo(lineInfoArr);
			br.write(minLineInfo.current() + "\n");
			minLineInfo.next();
		}
		br.close();
	}

	private UnsortedLine getMinLineInfo(UnsortedLine[] lineInfoArr) {
		UnsortedLine minLineInfo = null;
		for (int i = 0; i < lineInfoArr.length; i++) {
			if (lineInfoArr[i].isAvailable()) {
				if (minLineInfo == null) {
					minLineInfo = lineInfoArr[i];
				} else if (lineInfoArr[i].compareTo(minLineInfo) < 0) {
					minLineInfo = lineInfoArr[i];
				}
			}
		}
		return minLineInfo;
	}

	private boolean isFinish(UnsortedLine[] lineInfoArr) {
		for (int i = 0; i < lineInfoArr.length; i++) {
			if (lineInfoArr[i].isAvailable()) {
				return false;
			}
		}
		return true;
	}

	public static File sort(File file, SortStrategy sortStrategy) {
		try {
			FileSorter fileSorter = new FileSorter(file, sortStrategy);
			fileSorter.sortFile();
			return fileSorter.getSortedFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static File sort(File file) {
		try {
			FileSorter fileSorter = new FileSorter(file, new SortStrategy.DefaultSortStrategy());
			fileSorter.sortFile();
			return fileSorter.getSortedFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private File getSortedFile() {
		return outputFile;
	}
}

class UnsortedLine extends FileLineReader implements Comparable<UnsortedLine> {

	private SortStrategy sortStrategy = null;

	public UnsortedLine(File file, SortStrategy sortStrategy) throws IOException {
		super(file);
		this.sortStrategy = sortStrategy;
	}

	@Override
	public int compareTo(UnsortedLine other) {
		return sortStrategy.compare(this.current(), other.current());
	}
}
