package com.novelbio.analysis.seq.fasta;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.hadoop.fs.FSDataInputStream;

import com.novelbio.base.fileOperate.FileHadoop;

public interface RandomChrFileInt {
	public void seek(long site) throws IOException;
	
	public int read(byte[] byteinfo) throws IOException;
	
	public void close() throws IOException;
		
	public static class RandomChrFileFactory {
		/** 根据是fileHadoop还是本地文件，初始化相应的对象 */
		public static RandomChrFileInt createInstance(String fileName) {
			try {
				if (FileHadoop.isHdfs(fileName)) {
					return new RandomFileHdfs(fileName);
				} else {
					return new RandomFileLocal(fileName);
				}
			} catch (Exception e) {
				
			}
		
			return null;
		}
	}
}

class RandomFileLocal extends RandomAccessFile implements RandomChrFileInt {
	public RandomFileLocal(String file) throws FileNotFoundException {
		super(file, "r");
	}
}

class RandomFileHdfs implements RandomChrFileInt {
	FSDataInputStream fsDataInputStream;
	
	public RandomFileHdfs(String file) throws IOException {
		FileHadoop fileHadoop = new FileHadoop(file);
		fsDataInputStream = fileHadoop.getInputStream();
	}
	
	@Override
	public void seek(long site) throws IOException {
		fsDataInputStream.seek(site);
	}

	@Override
	public int read(byte[] byteinfo) throws IOException {
		return fsDataInputStream.read(byteinfo);
	}

	@Override
	public void close() throws IOException {
		fsDataInputStream.close();
	}
	
}

