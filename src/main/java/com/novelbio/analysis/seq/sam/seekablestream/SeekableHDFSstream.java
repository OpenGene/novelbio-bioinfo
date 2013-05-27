package com.novelbio.analysis.seq.sam.seekablestream;

import java.io.IOException;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.seekablestream.SeekableStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.aspectj.apache.bcel.generic.RET;

import com.novelbio.base.fileOperate.FileHadoop;

/**
 * 将HDFS的流包装成Samtools识别的Seekable流
 * @author zong0jie
 *
 */
public class SeekableHDFSstream extends SeekableStream {
	public static void main(String[] args) throws IOException {
		Configuration conf = new Configuration();
		// 在你的文件地址前自动添加：hdfs://192.168.0.188:9000/
		conf.set("fs.default.name", "hdfs://192.168.0.188:9000/");
		// 指定用户名
		conf.set("hadoop.job.user", "novelbio");
		
		String hdfsFileName = "/user/aaa.bam";
		String hdfsFileIndex = "/user/aaa.bam.bai";
		
		FileSystem hdfs = FileSystem.get(conf);
		
		String fileName = "/home/zong0jie/Desktop/paper/aaa.bam";
		String fileIndex = "/home/zong0jie/Desktop/paper/aaa.bam.bai";
		
		FileHadoop fileHadoop = new FileHadoop(hdfs, hdfsFileName);
		
		System.out.println(fileHadoop.getFileNameHdfs());
		
	}
	
	
	private static final Logger logger = Logger.getLogger(SeekableHDFSstream.class);
	
	FileHadoop fileHadoop;
	FSDataInputStream fsDataInputStream;
	long fileLength;
	
	public SeekableHDFSstream(FileHadoop fileHadoop) {
		this.fsDataInputStream = fileHadoop.getInputStream();
		fileLength = fileHadoop.getContentSummary().getLength();
	}
	
	@Override
	public long length() {
		return fileHadoop.getContentSummary().getLength();
	}

	@Override
	public long position() throws IOException {
		return fsDataInputStream.getPos();
	}

	@Override
	public void seek(long position) throws IOException {
		logger.error("seek " + position);
		fsDataInputStream.seek(position);
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		return fsDataInputStream.read(buffer, offset, length);
	}

	@Override
	public void close() throws IOException {
		fsDataInputStream.close();
	}

	@Override
	public boolean eof() throws IOException {
		return fileLength == fsDataInputStream.getPos();
	}
	
	/**
	 * 在sam-1.87版本中，仅用来显示报错信息
	 */
	@Override
	public String getSource() {
		return fileHadoop.getFileNameHdfs();
	}

	@Override
	public int read() throws IOException {
		return fsDataInputStream.read();
	}

}
