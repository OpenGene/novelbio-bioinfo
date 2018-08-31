package com.novelbio.bioinfo.wig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.IntCmdSoft;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.sam.SamFile;
import com.novelbio.bioinfo.sam.SamRecord;

/** 将sam文件转化为bigwig文件 */
public class SamFileToWig implements IntCmdSoft {
	private static final Logger loggger = Logger.getLogger(SamFileToWig.class);
	
	public static void main(String[] args) {
		SamFile samFile = new SamFile("/hdfs:/nbCloud/public/customerData/Projects/DN14001/bam/IonXpress_013.bam");
		DateUtil dateUtil = new DateUtil();
		dateUtil.setStartTime();
		SamFileToWig samFileToWig = new SamFileToWig();
		samFileToWig.setSamFile(samFile);
		samFileToWig.setNormalizedByReadsNum(false);
		samFileToWig.setInterval(6);
		samFileToWig.calculate();
		System.out.println(dateUtil.getElapseTime());
	}
	/** 每次读取的份数 */
	int chunkSizeRaw = 10_000_000;
	int chunkSize = 10_000_000;
	int interval = 5;
	List<SamFile> lsSamFiles;
	String prefix;
	String outPath;
	String outBigWigFile;
	 
	boolean normalizedByReadsNum;
	/** Specified wigsum. 1,000,000,000 equals to coverage of 10 million 100nt reads. Ignore this option to disable normalization */
	long normalizeNum = 1000000000;
	long readsBpNum = 0;
	
	List<String> lsCmd = new ArrayList<>();

	
	public void setInterval(int interval) {
		this.interval = interval;
		chunkSize = chunkSizeRaw /interval * interval;
	}
	
	/**
	 * 必须输入排序并index的sam文件
	 * @param samFilePath
	 */
	public void setLsSamFile(String prefix, List<SamFile> lsSamFiles) {
		if (lsSamFiles == null || lsSamFiles.size() == 0) {
			throw new ExceptionNullParam("No Sam File");
		}
		//检测sam文件是否mapping至同一个reference
		if (lsSamFiles.size() > 1) {
			Map<String, Long> mapChrId2Len = lsSamFiles.get(0).getMapChrID2Length();
			for (int i = 1; i < lsSamFiles.size(); i++) {
				Map<String, Long> mapChrId2LenTmp = lsSamFiles.get(i).getMapChrID2Length();
				if (!mapChrId2Len.equals(mapChrId2LenTmp)) {
					throw new ExceptionNullParam("Sam Files Are Not From Same Reference");
				}
			}
		}
		
		this.lsSamFiles = lsSamFiles;
		this.prefix = prefix;
	}
	
	public void setSamFile(SamFile samFile) {
		prefix = null;
		if (samFile == null) {
			throw new ExceptionNullParam("No Sam File");
		}
		
		lsSamFiles = new ArrayList<>();
		lsSamFiles.add(samFile);
	}
	/**
	 * 设定输出路径，默认输出在第一个sam文件夹下
	 * @param outputFile
	 */
	public void setOutPath(String outPath) {
		if (!StringOperate.isRealNull(outPath)) {
			this.outPath = FileOperate.addSep(outPath);
		}
	}
	
	/** 获得转换好的bigwig文件名 */
	public String getOutBigWigFile() {
		return outBigWigFile;
	}
	
	/** 是否用readsNum进行标准化 */
	public void setNormalizedByReadsNum(boolean normalizedByReadsNum) {
		this.normalizedByReadsNum = normalizedByReadsNum;
	}
	
	public void calculate() {
		try {
			run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private final void run() throws IOException {
		setOutPathAndPrefix();
		String wigFile = PathDetail.getTmpPathWithSep() + prefix + DateUtil.getDateAndRandom() + ".wig";
		outBigWigFile = outPath + prefix + ".bw";
		String txtMapChrId2Len = outPath + prefix + DateUtil.getDateAndRandom();
		Map<String, Long> mapChrId2Len = lsSamFiles.get(0).getMapChrID2Length();
		writeMapChrId2Len(txtMapChrId2Len, mapChrId2Len);
		if (!FileOperate.isFileExistAndBigThanSize(wigFile, 0)) {
			for (SamFile samFile : lsSamFiles) {
				samFile.indexMake();
			}
			getNormalizedReadsNum();
			writeWigFile(mapChrId2Len, wigFile);
		}
		
		WigToBigWig wigToBigWig = new WigToBigWig();
		wigToBigWig.setWigFile(wigFile);
		wigToBigWig.setTxtMapChrId2Len(txtMapChrId2Len);
		wigToBigWig.setBigWigFile(outBigWigFile);
		lsCmd = wigToBigWig.getCmdExeStr();
		wigToBigWig.convert();
	}
	
	/** 设定输出文件夹等信息 */
	private void setOutPathAndPrefix() {
		if (outPath == null) outPath = FileOperate.getPathName(lsSamFiles.get(0).getFileName());
		if (prefix == null) {
			if (lsSamFiles.size() == 1) {
				prefix = FileOperate.getFileName(lsSamFiles.get(0).getFileName());
			} else {
				prefix = FileOperate.getFileName(lsSamFiles.get(0).getFileName() + "_merge");
			}
		}
	}
	
	/** 获取标准化所需要的信息 */
	private void getNormalizedReadsNum() {
		if (normalizedByReadsNum && readsBpNum <= 0) {
			readsBpNum = 0;
			for (SamFile samFile : lsSamFiles) {
				for (SamRecord samRecord : samFile.readLines()) {
					if (samRecord.getMappedReadsWeight() > 1 && samRecord.getMapIndexNum() != 1) {
						continue;
					}
					for (Align align : samRecord.getAlignmentBlocks()) {
						readsBpNum += align.getLength();
					}
				}
			}
		}
	}
	
	private void writeMapChrId2Len(String outFileName, Map<String, Long> mapChrId2Len) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFileName, true);
		for (String chrId : mapChrId2Len.keySet()) {
			txtWrite.writefileln(new String[]{chrId, mapChrId2Len.get(chrId) + ""});
		}
		txtWrite.close();
	}
	
	private void writeWigFile(Map<String, Long> mapChrId2Len, String wigFile) throws IOException {
		TrackHeader header = TrackHeader.newWiggle();
		String wigFileTmp = FileOperate.changeFileSuffix(wigFile, "_tmp", null);
		try (WigFileWriter writer = new WigFileWriter(wigFileTmp, header)) {
			for (String chr : mapChrId2Len.keySet()) {
				processChromosome(lsSamFiles, writer, chr, mapChrId2Len);
			}
			writer.close();
		}
		FileOperate.moveFile(true, wigFileTmp, wigFile);
	}
	
	private void processChromosome(List<SamFile> lsSamfile, WigFileWriter writer, String chr, Map<String, Long> mapChrID2Len) throws IOException {
		int chunkStart = 1;
		while (chunkStart < mapChrID2Len.get(chr)) {
			int chunkStop = getChunkStop(chr, chunkStart, mapChrID2Len);
			if (chunkStop < 0) {
				break;
			}
			float[] result = compute(lsSamfile, chr, chunkStart, chunkStop);
			// Write the count at each base pair to the output file
			writer.write(new Contig(chr, chunkStart, chunkStop, result, interval));
			// Process the next chunk
			chunkStart = chunkStop + 1;
		}
	}
	
	private int getChunkStop(String chr, int chunkStart, Map<String, Long> mapChrID2Len) {
		int caculateStop = chunkStart+chunkSize-1;
		int chunkStop = (int) Math.min(caculateStop, mapChrID2Len.get(chr));
		if (chunkStop != caculateStop) {
			chunkStop = (chunkStop + 1 - chunkStart) / interval * interval + chunkStart - 1;
		}
		if (chunkStop - chunkStart <= interval) {
			return -1;
		}
		return chunkStop;
	}
	
	/**
	 * Do the computation on a chunk and return the results
	 * Must return chunk.length() values (one for every base pair in chunk)
	 * 
	 * @param chunk the interval to process
	 * @return the results of the computation for this chunk
	 * @throws IOException
	 */
	public float[] compute(List<SamFile> lsSamfile, String chrId, int start, int end) throws IOException {
		float[] sum = new float[end - start + 1];
		for (SamFile samFile : lsSamfile) {
			for (SamRecord samRecord : samFile.readLinesOverlap(chrId, start, end)) {
				if (samRecord.getMapQuality() < 30) {
					continue;
				}
				for (Align align : samRecord.getAlignmentBlocks()) {
					int entryStart = Math.max(start, align.getStartAbs());
					int entryStop = Math.min(end, align.getEndAbs());
					for (int i = entryStart; i <= entryStop; i++) {
						sum[i-start] += 1/(float)samRecord.getMappedReadsWeight();
					}
				}
			}
		}
		
		if (interval > 1) {
			for (int i = 0; i < sum.length - 1; i+=interval) {
				float sumAll = 0;
				int finalNum = interval;
				if (i+interval >= sum.length) {
					finalNum = sum.length - i;
				}
				for (int j = 0; j < finalNum; j++) {
					sumAll += sum[i+j];
				}
				for (int j = 0; j < finalNum; j++) {
					sum[i+j] = sumAll/finalNum;
				}
			}
		}
		
		if (normalizedByReadsNum && readsBpNum > 0) {
			for (int i = 0; i < sum.length; i++) {
				sum[i] = (float) ((double)sum[i]* normalizeNum/readsBpNum );
			}
		}
		return sum;
	}
	
	@Override
	public List<String> getCmdExeStr() {
		return lsCmd;
	}
	
}
