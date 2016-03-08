package com.novelbio.analysis.tools.snvintersection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;


/**
 * 有序文件交集类
 * 
 * @author novelbio
 *
 */
public class FileIntersection {
	private File[] fileArray;
	private File outputFile;
	
	private EqualStrategy equalStrategy;
	private SortStrategy sortStrategy;

	public static File intersection(File[] fileArray) {
		try {
			FileIntersection fileIntersection = new FileIntersection(fileArray,
					new EqualStrategy.DefaultEqualStrategy(), new SortStrategy.DefaultSortStrategy());
			fileIntersection.intersectionFiles();
			return fileIntersection.getOutputFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static File intersection(File[] fileArray, EqualStrategy equalStrategy, SortStrategy sortStrategy) {
		try {
			FileIntersection fileIntersection = new FileIntersection(fileArray, equalStrategy, sortStrategy);
			fileIntersection.intersectionFiles();
			return fileIntersection.getOutputFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private FileIntersection(File[] fileArray, EqualStrategy equalStrategy, SortStrategy sortStrategy) {
		Validate.notNull(fileArray, "FileArray can not be null");
		Validate.notNull(fileArray, "EqualStrategy can not be null");
		Validate.notNull(fileArray, "SortStrategy can not be null");
		this.fileArray = fileArray;
		this.equalStrategy = equalStrategy;
		this.sortStrategy = sortStrategy;
	}

	private File getOutputFile() {
		return outputFile;
	}

	private void intersectionFiles() throws IOException {
		File[] sortedFileArray = getSortFileArray();
		IntersectionLine[] lineInfoArray = getLineInfoArray(sortedFileArray);
		analysis(lineInfoArray);
		for (int i = 0; i < lineInfoArray.length; i++) {
			lineInfoArray[i].close();
		}
		removeSortedFileArray(sortedFileArray);
	}

	private void removeSortedFileArray(File[] sortedFileArray) {
		for (int i = 0; i < sortedFileArray.length; i++) {
			FileUtils.deleteQuietly(sortedFileArray[i]);
		}
	}

	private IntersectionLine[] getLineInfoArray(File[] sortedFileArray) throws IOException {
		IntersectionLine[] lineInfoArray = new IntersectionLine[sortedFileArray.length];
		for (int i = 0; i < lineInfoArray.length; i++) {
			lineInfoArray[i] = new IntersectionLine(sortedFileArray[i], equalStrategy, sortStrategy);
		}
		return lineInfoArray;
	}

	private File[] getSortFileArray() {
		File[] sortedFileArray = new File[fileArray.length];
		for (int i = 0; i < fileArray.length; i++) {
			sortedFileArray[i] = FileSorter.sort(fileArray[i], sortStrategy);
		}
		return sortedFileArray;
	}

	/**
	 * 文件交集分析算法
	 * 
	 * @param bw
	 * @param iterArray
	 * @param lineInfoArray
	 * @throws IOException
	 */
	private void analysis(IntersectionLine[] lineInfoArray) throws IOException {
		Validate.isTrue(lineInfoArray.length > 1, "file number can not be little than 1");
		initOutputFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		int maxIndex = -1;
		while (isAvailable(lineInfoArray)) {
			if (isEqual(lineInfoArray)) {
				bw.write(getComparedInfo(lineInfoArray));
			}
			maxIndex = getMinIndex(lineInfoArray);
			lineInfoArray[maxIndex].next();
		}
		bw.close();
	}

	/**
	 * 结果文件名称生成方法
	 * 
	 * @param fileArray
	 * @return
	 */
	private void initOutputFile() {
		String[] subFileName = new String[fileArray.length];
		for (int i = 0; i < fileArray.length; i++) {
			subFileName[i] = fileArray[i].getName();
		}
		String outputFileName = StringUtils.join(subFileName, "-") + ".intersection";
		outputFile = new File(fileArray[0].getParentFile(), outputFileName);

	}

	private boolean isAvailable(IntersectionLine[] lineInfoArray) {
		for (IntersectionLine lineInfo : lineInfoArray) {
			if (!lineInfo.isAvailable()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * lineInfoArray结果是否相同
	 * 
	 * @param lineInfoArray
	 * @return
	 */
	private boolean isEqual(IntersectionLine[] lineInfoArray) {
		for (int i = 1; i < lineInfoArray.length; i++) {
			if (!lineInfoArray[0].equals(lineInfoArray[i])) {
				return false;
			}
		}
		return true;
	}

	private String getComparedInfo(IntersectionLine[] lineInfoArray) {
		String result = "";
		for (IntersectionLine line : lineInfoArray) {
			result += line.current() + "\n";
		}
		return result;
	}

	private int getMinIndex(IntersectionLine[] lineInfoArray) {
		int minIndex = 0;
		for (int i = 1; i < lineInfoArray.length; i++) {
			if (lineInfoArray[i].compareTo(lineInfoArray[minIndex]) < 0) {
				minIndex = i;
			}
		}
		return minIndex;
	}
}

class IntersectionLine extends FileLineReader implements Comparable<IntersectionLine> {
	private SortStrategy sortStrategy = null;
	private EqualStrategy equalStrategy = null;

	public IntersectionLine(File file, EqualStrategy equalStrategy, SortStrategy sortStrategy) throws IOException {
		super(file);
		Validate.notNull(file, "file can not be null");
		Validate.notNull(equalStrategy, "equalStrategy can not be null");
		Validate.notNull(sortStrategy, "sortStrategy can not be null");
		this.equalStrategy = equalStrategy;
		this.sortStrategy = sortStrategy;
	}

	@Override
	public boolean equals(Object obj) {
		IntersectionLine other = (IntersectionLine) obj;
		if (this == other) {
			return true;
		}
		return equalStrategy.equals(this.current(), other.current());
	}

	@Override
	public int compareTo(IntersectionLine other) {
		return sortStrategy.compare(this.current(), other.current());
	}

}