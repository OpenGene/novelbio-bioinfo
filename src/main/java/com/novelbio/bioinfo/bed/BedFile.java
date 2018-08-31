package com.novelbio.bioinfo.bed;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataOperate.TxtReadandWrite.TXTtype;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.IntCmdSoft;
import com.novelbio.bioinfo.base.AlignSeq;
import com.novelbio.bioinfo.fastq.FastQ;
import com.novelbio.bioinfo.fastq.FastQRecord;
import com.novelbio.bioinfo.sam.SamFile;
import com.novelbio.bioinfo.sam.SamRecord;

/**
 * bed格式的文件，统统用来处理bed文件
 * novelbio的bedfile格式：
 * 0: chrID <br>
 * 1: startLoc <br>
 * 2: endLoc <br>
 * 3: MD:tag <br>
 * 4: CIGAR <br>
 * 5: strand <br>
 * 6: mappingNum. 1 means unique mapping
 * @author zong0jie
 */
public class BedFile implements AlignSeq, IntCmdSoft {
	private static Logger logger = Logger.getLogger(BedFile.class);  	
	CmdOperate cmdOperate;
	boolean read = true;
	TxtReadandWrite txtReadandWrite;
	
	/**
	 * 默认为读取bed文件
	 * @param bedFile
	 */
	public BedFile(String bedFile) {
		this(bedFile, false);
	}
	/**
	 * 新建一个bed，可以往里面写东西的那种
	 * 注意写完后务必调用{@link #closeWrite()}方法关闭写入流并将bed写入转化为bed读取
	 * @param bedFile
	 * @param creatBed
	 */
	public BedFile(String bedFile, boolean creatBed) {
		txtReadandWrite = new TxtReadandWrite(bedFile, creatBed);
	}
	
	@Override
	public String getFileName() {
		return txtReadandWrite.getFileName();
	}
	
	/**
	 * <b>写完后务必用 {@link #close} 方法关闭</b>
	 * 创建的时候要设定为creat模式
	 * @param bedRecord
	 */
	public void writeBedRecord(BedRecord bedRecord) {
		if (bedRecord == null) return;
		String bed = bedRecord.toString();
		if (StringOperate.isRealNull(bed)) {
			return;
		}
		txtReadandWrite.writefileln(bed);
	}
	
	/**
	 * <b>写完后务必用 {@link #close} 方法关闭</b>
	 * 创建的时候要设定为creat模式
	 * @param bedRecord
	 */
	public void writeBedRecordSimple(BedRecord bedRecord) {
		if (bedRecord == null) return;
		String bed = bedRecord.toStringSimple();
		if (StringOperate.isRealNull(bed)) {
			return;
		}
		txtReadandWrite.writefileln(bed);
	}
	
	/**
	 * 内部关闭
	 * @param lsBedRecord
	 */
	public void wirteBedRecord(List<BedRecord> lsBedRecord) {
		for (BedRecord bedRecord : lsBedRecord) {
			txtReadandWrite.writefileln(bedRecord.toString());
		}
		close();
	}
	
	/** 读取的具体长度，出错返回 -1 */
	public long getReadByte() {
		if (txtReadandWrite != null) {
			return txtReadandWrite.getReadByte();
		}
		return -1;
	}
	
	/**
	 * 获得读取的百分比
	 * @return 结果在0-1之间，小于0表示出错
	 */
	public double getReadPercentage() {
		if (txtReadandWrite != null) {
			return txtReadandWrite.getReadPercentage();
		}
		return -1;
	}
	
	/**
	 * 读取前几行，不影响{@link #readLines()}
	 * @param num
	 * @return
	 */
	public ArrayList<BedRecord> readHeadLines(int num) {
		ArrayList<BedRecord> lsResult = new ArrayList<BedRecord>();
		int i = 0;
		for (BedRecord bedRecord : readLines()) {
			if (i >= num) {
				break;
			}
			lsResult.add(bedRecord);
		}
		return lsResult;
	}
	/**
	 * 读取前几行，不影响{@link #readLines()}
	 * @param num
	 * @return
	 */
	public BedRecord readFirstLine() {
		BedRecord bedRecord = readLines().iterator().next();
		close();
		return bedRecord;
	}
	
	public Iterable<BedRecord> readLines() {
		try {
			return readPerlines();
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public Iterable<BedRecord> readLines(int lines) {
		lines = lines - 1;
		try {
			Iterable<BedRecord> itContent = readPerlines();
			if (lines > 0) {
				for (int i = 0; i < lines; i++) {
					itContent.iterator().hasNext();
				}
			}
			return itContent;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 迭代读取文件
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<BedRecord> readPerlines() throws Exception {
		final BufferedReader bufread =  txtReadandWrite.readfile(); 
		return new Iterable<BedRecord>() {
			public Iterator<BedRecord> iterator() {
				return new Iterator<BedRecord>() {
					public boolean hasNext() {
						return bedRecord != null;
					}
					public BedRecord next() {
						BedRecord retval = bedRecord;
						bedRecord = getLine();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					BedRecord getLine() {
						String linestr = null;
						BedRecord bedRecord = null;
						try {
							while ((linestr = bufread.readLine()) != null) {
								try {
									bedRecord = new BedRecord(linestr);
									return bedRecord;
								} catch (Exception e) {
									logger.error("error:" + linestr);
								}
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					}
					BedRecord bedRecord = getLine();
				};
			}
		};
	}
	/**
	 * 指定bed文件，按照chrID和坐标进行排序<br>
	 * @param sortBedFile 排序后的文件全名<br>
	 * 返回名字为FileOperate.changeFileSuffix(getFileName(), "_sorted", null);
	 */
	public BedFile sort()  {
		String fileName = txtReadandWrite.getFileName();		
		fileName = FileOperate.changeFileSuffix(fileName, "_sorted", "bed", null);
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #第一列起第一列终止排序，第二列起第二列终止按数字排序,第三列起第三列终止按数字排序
		return sort(fileName);
	}
	
	/**
	 * 指定bed文件，按照chrID和坐标进行排序
	 * @param sortBedFile 排序后的文件全名
	 */
	public BedFile sort(String sortBedFile)  {
		String fileName = sortBedFile(1, sortBedFile, 2,3);
		return new BedFile(fileName);
	}
	
	/**
	 * 指定bed文件，以及需要排序的列数，产生排序结果
	 * @param chrID ChrID所在的列，从1开始记数，按照字母数字排序
	 * @param sortBedFile 排序后的文件全名
	 * @param arg 除ChrID外，其他需要排序的列，按照数字排序，实际列
	 * @throws Exception
	 */
	public String sortBedFile(int chrID, String sortBedFile, int...arg)  {
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #第一列起第一列终止排序，
		//第二列起第二列终止按数字排序,第三列起第三列终止按数字排序
		String tmpTxt = txtReadandWrite.getFileName();
		
		TXTtype txtTtypeThis = TXTtype.getTxtType(txtReadandWrite.getFileName());
		if (txtTtypeThis != TXTtype.Txt) {
			tmpTxt = FileOperate.changeFileSuffix(txtReadandWrite.getFileName(), "_unzip", "txt");
			txtReadandWrite.unZipFile(tmpTxt);
		}
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("sort");
				
		if (chrID != 0) {
			lsCmd.add("-k" + chrID + "," + chrID);
		}
		for (int i : arg) {
			lsCmd.add("-k" + i + "," + i + "n");
		}
		lsCmd.add(tmpTxt);
		lsCmd.add(">");
		lsCmd.add(sortBedFile);
		
		cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.runWithExp();
		return sortBedFile;
	}
	
	public List<String> getCmdExeStr() {
		return new ArrayList<>();
	}
	
	/**
	 * 如果bed文件的坐标太短，根据正负链延长坐标至指定位置<br>
	 * 标准bed文件格式为：chr1  \t  7345  \t  7370  \t  25  \t  52  \t  - <br>
	 * 必须有第六列
	 * @throws Exception 
	 * @throws Exception 
	 */
	public BedFile extend(int extendTo) {
		String outFile = FileOperate.changeFileSuffix(getFileName(), "_extend", "bed", null);
		return extend(extendTo, outFile);
	}
	/**
	 * 
	 * 如果bed文件的坐标太短，根据正负链延长坐标至指定位置<br>
	 * 标准bed文件格式为：chr1  \t  7345  \t  7370  \t  25  \t  52  \t  - <br>
	 * 必须有第六列
	 * @param extendTo 把reads延长或缩短至指定的长度。如果小于等于0则会返回一个没有延长的文件。
	 * @param outFileName
	 * @return
	 */
	public BedFile extend(int extendTo, String outFileName) {
		BedFile bedSeq = new BedFile(outFileName, true);
		try {
			for (BedRecord bedRecord : readLines()) {
			bedRecord.extendTo(extendTo);
			bedSeq.writeBedRecord(bedRecord);
		}
			} catch (Exception e) {
			logger.error("extend error! targetFile: " + getFileName() + "   resultFIle: "+ outFileName);
		}
		bedSeq.close();
		return bedSeq;
	}
	
	/**
	 * 从含有序列的bed文件获得fastQ文件
	 * @param outFileName fastQ文件全名（包括路径）
	 * @throws Exception
	 */
	public FastQ getFastQ() {
		String outFileName = FileOperate.changeFileSuffix(getFileName(), "", "fastq.gz");
		return getFastQ(outFileName);
	}
	/**
	 * 从含有序列的bed文件获得fastQ文件<b>注意bed文件务必没有排过序</b>
	 * @param outFileName fastQ文件全名（包括路径）
	 * @throws Exception
	 */
	public FastQ getFastQ(String outFileName) {
		FastQ fastQ = new FastQ(outFileName, true);
		int i = 1;
		for (BedRecord bedRecord : readLines()) {
			FastQRecord fastQRecord = null;
			if (bedRecord.getMappedReadsWeight() == 1) {
				fastQRecord = bedRecord.toFastQRecord();
			} else {
				if (i == 1) {
					fastQRecord = bedRecord.toFastQRecord();
					i++;
				} else if (i < bedRecord.getMappedReadsWeight()) {
					i++;
					continue;
				} else if (i == bedRecord.getMappedReadsWeight()) {
					i = 1;
					continue;
				}
			}
			fastQ.writeFastQRecord(fastQRecord);
		}
		fastQ.close();
		return fastQ;
	}

	/**
	 *  过滤reads
	 *  @param outBedFile 输出文件名
	 * @param mappingNumSmall 小于0表示不过滤 smallNum
	 * @param mappingNumBig 小于0表示不过滤 bigNum
	 * @param strand null表示不过滤方向
	 * @return
	 */
	public BedFile filterSeq(String outBedFile, int mappingNumSmall, int mappingNumBig, Boolean strand) {
		boolean isFilterMapNum = false;
		if (mappingNumSmall > 0 || mappingNumBig > 0) {
			isFilterMapNum = true;
		}
		if (mappingNumBig <= 0) mappingNumBig = Integer.MAX_VALUE;
		
		BedFile bedSeqFiltered = new BedFile(outBedFile, true);
		for (BedRecord bedRecord : readLines()) {
			if (strand != null && bedRecord.isCis5to3() != null && bedRecord.isCis5to3() != strand) {
				continue;
			}
			if (isFilterMapNum && (bedRecord.getMappingNum() < mappingNumSmall || bedRecord.getMappingNum() > mappingNumBig)) {
				continue;
			}
			bedSeqFiltered.writeBedRecord(bedRecord);
		}
		close();
		bedSeqFiltered.close();
		return bedSeqFiltered;
	}
	
	/**
	 *  过滤reads
	 * @param mappingNumSmall 小于0表示不过滤 smallNum
	 * @param mappingNumBig 小于0表示不过滤 bigNum
	 * @param strand null表示不过滤方向
	 * @return
	 */
	public BedFile filterSeq(int mappingNumSmall, int mappingNumBig, Boolean strand) {
		String bedFileFiltered = FileOperate.changeFileSuffix(getFileName(), "_filtered", "bed", null);		
		return filterSeq(bedFileFiltered, mappingNumSmall, mappingNumBig, strand);
	}

	/**
	 * 输入经过排序的peakfile,或者说bedfile，将重叠的peak进行合并 注意，结果中仅保留peak，没有保留其他多的信息
	 * 从第一行开始合并
	 * @return
	 */
	public BedFile combBedOverlap() {
		return combBedOverlap(false);
	}
	/**
	 * 输入经过排序的peakfile,或者说bedfile，将重叠的peak进行合并 注意，结果中除保留peak，还可选择是否根据方向进行合并，同时保留方向信息
	 * 从第一行开始合并
	 * @return
	 */
	public BedFile combBedOverlap(boolean cis5to3) {
		String outFile = FileOperate.changeFileSuffix(getFileName(), "_comb", null);
		return combBedOverlap(cis5to3, 0, outFile);
	}
	/**
	 * 输入经过排序的peakfile,或者说bedfile，将重叠的peak进行合并 注意，结果中仅保留peak，没有保留其他多的信息
	*/
	public BedFile combBedOverlap(String outFile, boolean cis5to3) {
		return combBedOverlap(cis5to3, 0, outFile);
	}

	/**
	 * 输入经过排序的peakfile,或者说bedfile，将重叠的peak进行合并
	 * 注意，结果中仅保留peak，没有保留其他多的信息
	 * @param cis5to3 是否根据方向进行合并
	 * @param peakFile
	 * @param readLines 从第几行开始读
	 */
	public BedFile combBedOverlap(boolean cis5to3, int readLines, String outFile) {
		if (cis5to3) {
			return combBedOverlapCis5to3(readLines, outFile);
		} else {
			return combBedOverlap(readLines, outFile);
		}
	}
	
	/**
	 * 输入经过排序的peakfile,或者说bedfile，将重叠的peak进行合并
	 * 注意，结果中仅保留peak，没有保留其他多的信息
	 * @param peakFile
	 * @param readLines 从第几行开始读
	 */
	private BedFile combBedOverlap(int readLines, String outFile) {
		BedFile bedSeqResult = new BedFile(outFile, true);
		if (readLines < 1) {
			readLines = 1;
		}
		BedRecord bedRecordLast = null;
		for (BedRecord bedRecord : readLines(readLines)) {
			if (bedRecordLast == null) {
				bedRecordLast = new BedRecord();
				bedRecordLast.setRefID(bedRecord.getRefID());
				bedRecordLast.setStartEndLoc(bedRecord.getStartAbs(), bedRecord.getEndAbs());
			}
			if	(!bedRecord.getRefID().equals(bedRecordLast.getRefID()) || bedRecord.getStartAbs() >= bedRecordLast.getEndAbs()) {
				bedSeqResult.writeBedRecord(bedRecordLast);
				bedRecordLast = new BedRecord();
				bedRecordLast.setRefID(bedRecord.getRefID());
				bedRecordLast.setStartEndLoc(bedRecord.getStartAbs(), bedRecord.getEndAbs());
				//因为bedRecord内部默认ReadsNum为null，而如果为null，提取时显示为1，所以所有为1的都要手工设定一下
				if (bedRecordLast.getReadsNum() == 1) {
					bedRecordLast.setReadsNum(1);
				}
				continue;
			}
			else if (bedRecord.getStartAbs() < bedRecordLast.getEndAbs()) {
				//发现一个overlap就加上1，表示该区域有多条reads
				bedRecordLast.setReadsNum(bedRecordLast.getReadsNum() + 1);
				if (bedRecordLast.getEndAbs() < bedRecord.getEndAbs()) {
					bedRecordLast.setStartEndLoc(bedRecordLast.getStartAbs(), bedRecord.getEndAbs());
				}
			}
		}
		bedSeqResult.writeBedRecord(bedRecordLast);
		bedSeqResult.close();
		return bedSeqResult;
	}
	/**
	 * 输入经过排序的peakfile,或者说bedfile，将重叠的peak进行合并
	 * 注意，结果中仅保留peak，没有保留其他多的信息
	 * @param peakFile
	 * @param readLines 从第几行开始读
	 */
	private BedFile combBedOverlapCis5to3(int readLines, String outFile) {
		BedFile bedSeqResult = new BedFile(outFile, true);
		if (readLines < 1) {
			readLines = 1;
		}
		BedRecord bedRecordLast = null;
		for (BedRecord bedRecord : readLines(readLines)) {
			if (bedRecordLast == null) {
				bedRecordLast = new BedRecord();
				bedRecordLast.setRefID(bedRecord.getRefID());
				bedRecordLast.setStartEndLoc(bedRecord.getStartAbs(), bedRecord.getEndAbs());
				bedRecordLast.setCis5to3(bedRecord.isCis5to3());
			}
			if (!bedRecord.getRefID().equals(bedRecordLast.getRefID())
					|| bedRecord.getStartAbs() >= bedRecordLast.getEndAbs()
					|| bedRecord.isCis5to3() != bedRecordLast.isCis5to3()) {
				bedSeqResult.writeBedRecord(bedRecordLast);
				bedRecordLast = new BedRecord();
				bedRecordLast.setRefID(bedRecord.getRefID());
				bedRecordLast.setStartEndLoc(bedRecord.getStartAbs(), bedRecord.getEndAbs());
				bedRecordLast.setCis5to3(bedRecord.isCis5to3());
				// 因为bedRecord内部默认ReadsNum为null，而如果为null，提取时显示为1，所以所有为1的都要手工设定一下
				if (bedRecordLast.getReadsNum() == 1) {
					bedRecordLast.setReadsNum(1);
				}
				continue;
			} else if (bedRecord.getStartAbs() < bedRecordLast.getEndAbs()) {
				// 发现一个overlap就加上1，表示该区域有多条reads
				bedRecordLast.setReadsNum(bedRecordLast.getReadsNum() + 1);
				if (bedRecordLast.getEndAbs() < bedRecord.getEndAbs()) {
					bedRecordLast.setStartEndLoc(bedRecordLast.getStartAbs(), bedRecord.getEndAbs());
				}
			}
		}
		bedSeqResult.writeBedRecord(bedRecordLast);
		bedSeqResult.close();
		return bedSeqResult;
	}
	/**
	 * 输入经过排序的bedfile，将重复的bedrecord进行合并，合并的信息取第一条
	 * @param peakFile 
	 */
	public BedFile removeDuplicat() {
		String out = FileOperate.changeFileSuffix(getFileName(), "_removeDup", null);
		return removeDuplicat(out);
	}
	
	/**
	 * 输入经过排序的bedfile，将重复的bedrecord进行合并，合并的信息取第一条
	 * @param peakFile 
	 */
	public BedFile removeDuplicat(String outFile) {
		BedFile bedSeqResult = new BedFile(outFile, true);
		BedRecord bedRecordLast = null;
		for (BedRecord bedRecord : readLines()) {
			if (bedRecordLast == null) {
				bedRecordLast = bedRecord.clone();
				continue;
			}
			if (!bedRecord.getRefID().equals(bedRecordLast.getRefID())
					|| bedRecord.getStartAbs() != bedRecordLast.getStartAbs()
					|| bedRecord.getEndAbs() == bedRecordLast.getEndAbs()
					|| bedRecord.isCis5to3() == bedRecordLast.isCis5to3()) {
				bedSeqResult.writeBedRecord(bedRecordLast);
				bedRecordLast = new BedRecord();
				bedRecordLast = bedRecord;
				// 因为bedRecord内部默认ReadsNum为null，而如果为null，提取时显示为1，所以所有为1的都要手工设定一下
				if (bedRecordLast.getReadsNum() == 1) {
					bedRecordLast.setReadsNum(1);
				}
				continue;
			} else {
				// 发现一个overlap就加上1，表示该区域有多条reads
				bedRecordLast.setReadsNum(bedRecordLast.getReadsNum() + 1);
			}
		}
		bedSeqResult.writeBedRecord(bedRecordLast);
		bedSeqResult.close();
		return bedSeqResult;
	}
	
	public void close() {
		txtReadandWrite.close();
	}
	
	/**
	 * 将所有mapping至genomic上的bed文件合并，这个是预测novel miRNA的
	 * @param outFile 输出文件
	 * @param bedSeqFile 输入文件
	 */
	public static BedFile combBedFile(String outFile, String... bedSeqFile) {
		BedFile bedSeq = new BedFile(outFile, true);
		for (String string : bedSeqFile) {
			BedFile bedSeq2 = new BedFile(string);
			for (BedRecord bedRecord : bedSeq2.readLines()) {
				bedSeq.writeBedRecord(bedRecord);
			}
			bedSeq2.close();
		}
		bedSeq.close();
		return bedSeq;
	}
	/**
	 * 将所有mapping至genomic上的bed文件合并，这个是预测novel miRNA的
	 * @param outFile 输出文件
	 * @param bedSeqFile 输入文件
	 */
	public static BedFile combBedFile(String outFile, ArrayList<String> lsbedSeqFile) {
		BedFile bedSeq = new BedFile(outFile, true);
		for (String string : lsbedSeqFile) {
			BedFile bedSeq2 = new BedFile(string);
			for (BedRecord bedRecord : bedSeq2.readLines()) {
				bedSeq.writeBedRecord(bedRecord);
			}
			bedSeq2.close();
		}
		bedSeq.close();
		return bedSeq;
	}
	
	/** 假设文件存在，判断其是否为novelbio所定义的bed文件 */
	public static boolean isBedFile(String bedFile) {
		final int allReadLines = 100;
		final int maxBedLines = 50;
		TxtReadandWrite txtSeqFile = new TxtReadandWrite(bedFile, false);
		int readLines = 0;
		int bedLines = 0;
		for (String content : txtSeqFile.readlines()) {
			if (readLines > allReadLines) {
				break;
			}
			if (BedRecord.isBedRecord(content)) {
				bedLines ++;
			}
			readLines++;
		}
		txtSeqFile.close();
		if (bedLines > maxBedLines || (double)bedLines/readLines > 0.5) {
			return true;
		}
		return false;
	}
	
	/** 把sam文件转换成bed文件，目前只支持单端测序，主要用于ChIP-Seq */
	public static void convertSamToBed(String sam, String bed) {
		SamFile samFile = new SamFile(sam);
		BedFile bedFile = new BedFile(bed, true);
		int i = 0;
		for (SamRecord	samRecord : samFile.readLines()) {
			if (i++ % 100000 == 0) {
				logger.info("convert " + i + " reads");
			}
			bedFile.writeBedRecord(samRecord.toBedRecordSE());
		}
		samFile.close();
		bedFile.close();
	}
}
