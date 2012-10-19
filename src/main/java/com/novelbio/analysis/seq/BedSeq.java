package com.novelbio.analysis.seq;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.junit.runners.Parameterized.Parameters;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

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
public class BedSeq extends SeqComb implements AlignSeq{
	private static Logger logger = Logger.getLogger(BedSeq.class);  
	/**
	 * mappingNum. 1 means unique mapping
	 * 一般在第六列
	 */
	public static final int MAPPING_NUM_COLUMN = 6;
	/**
	 * mappingNum. 1 means unique mapping
	 * 一般在第六列
	 */
	public static final int MAPPING_NUM_STRAND = 5;
	
	public BedSeq(String bedFile) {
		super(bedFile, 1);
	}
	/**
	 * 新建一个bed，可以往里面写东西的那种
	 * 注意写完后务必调用{@link #closeWrite()}方法关闭写入流并将bed写入转化为bed读取
	 * @param bedFile
	 * @param creatBed
	 */
	public BedSeq(String bedFile, boolean creatBed) {
		super(bedFile, 1, creatBed);
	}
	public static void main(String[] args) {
	}
	/**
	 * <b>写完后务必用 {@link #closeWrite} 方法关闭</b>
	 * 创建的时候要设定为creat模式
	 * @param bedRecord
	 */
	public void writeBedRecord(BedRecord bedRecord) {
		if (bedRecord == null) {
			return;
		}
		txtSeqFile.writefileln(bedRecord.toString());
	}
	/**
	 * 内部关闭
	 * @param lsBedRecord
	 */
	public void wirteBedRecord(List<BedRecord> lsBedRecord) {
		for (BedRecord bedRecord : lsBedRecord) {
			txtSeqFile.writefileln(bedRecord.toString());
		}
		closeWrite();
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
		return readLines().iterator().next();
	}
	public void close() {
		txtSeqFile.close();
	}
	/**
	 * 写完后务必用此方法关闭
	 * 关闭输入流，并将bedseq写入转化为bedseq读取
	 */
	public void closeWrite() {
		txtSeqFile.close();
		super.compressInType = super.compressOutType;
		txtSeqFile = new TxtReadandWrite(compressInType, seqFile, false);
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
		txtSeqFile.setFiletype(compressInType);
		 final BufferedReader bufread =  txtSeqFile.readfile(); 
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
						BedRecord bedRecord = null;
						try {
							String linestr = bufread.readLine();
							if (linestr == null) {
								return null;
							}
							bedRecord = new BedRecord(linestr);
						} catch (IOException ioEx) {
							bedRecord = null;
						}
						return bedRecord;
					}
					BedRecord bedRecord = getLine();
				};
			}
		};
	}

	/**
	 * 指定bed文件，以及需要排序的列数，产生排序结果
	 * @param chrID ChrID所在的列，从1开始记数，按照字母数字排序
	 * @param sortBedFile 排序后的文件全名
	 * @param arg 除ChrID外，其他需要排序的列，按照数字排序，实际列
	 * @throws Exception
	 */
	public BedSeq sortBedFile(int chrID, String sortBedFile,int...arg)  {
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #第一列起第一列终止排序，
		//第二列起第二列终止按数字排序,第三列起第三列终止按数字排序
		String tmpTxt = "";
		if (!compressInType.equals(TxtReadandWrite.TXT))
		{
			tmpTxt = FileOperate.changeFileSuffix(seqFile, "_unzip", "txt");
			txtSeqFile = new TxtReadandWrite(compressInType, seqFile, false);
			txtSeqFile.unZipFile(tmpTxt);
		} 
		else {
			tmpTxt = seqFile;
		}
		String cmd = "sort";
		
		if (chrID != 0) {
			cmd = cmd + " -k"+chrID+","+chrID+" ";
		}
		for (int i : arg) {
			cmd = cmd + " -k"+i+","+i+"n ";
		}
		
		if (compressOutType.equals(TxtReadandWrite.GZIP)) {
			cmd = cmd + tmpTxt  +" | gzip -c > " + sortBedFile;
		}
		else if (compressOutType.equals(TxtReadandWrite.BZIP2)) {
			cmd = cmd + tmpTxt  +" | bzip2 -c > " + sortBedFile;
		}
		else if (compressOutType.equals(TxtReadandWrite.TXT)) {
			cmd = cmd + tmpTxt + " > " + sortBedFile;
		}
		
		CmdOperate cmdOperate = new CmdOperate(cmd,"sortBed");
		cmdOperate.run();
		BedSeq bedSeq = new BedSeq(sortBedFile);
		bedSeq.setCompressType(compressOutType, compressOutType);
		if (!compressInType.equals(TxtReadandWrite.TXT))
		{
			FileOperate.delFile(tmpTxt);
		}
		return bedSeq;
	}
	
	/**
	 * 指定bed文件，按照chrID和坐标进行排序
	 * @param sortBedFile 排序后的文件全名
	 */
	public BedSeq sort(String sortBedFile)  {
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #第一列起第一列终止排序，第二列起第二列终止按数字排序,第三列起第三列终止按数字排序
		return sortBedFile(1, sortBedFile, 2,3);
	}
	/**
	 * 指定bed文件，按照chrID和坐标进行排序<br>
	 * @param sortBedFile 排序后的文件全名<br>
	 * 返回名字为FileOperate.changeFileSuffix(getFileName(), "_sorted", null);
	 */
	public BedSeq sort()  {
		String outFile = null;
		if (!compressInType.equals(TxtReadandWrite.TXT)) {
			if (getFileName().endsWith("gz") || getFileName().endsWith("bz") || getFileName().endsWith("zip")) {
				outFile = getFileName().substring(0, getFileName().lastIndexOf("."));
			}
			outFile = FileOperate.changeFileSuffix(outFile, "_sorted", null);
		}
		else {
			outFile = FileOperate.changeFileSuffix(getFileName(), "_sorted", null);
		}
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #第一列起第一列终止排序，第二列起第二列终止按数字排序,第三列起第三列终止按数字排序
		return sort(outFile);
	}
	/**
	 * 专门给徐龙勇的GSM307618过滤的文件，
	 * chr11   79993182        79993208        -       2119.5.3904     0       CTTGGGGCAGAAGAGCCCTTGCAGCC
	 第六列 必须 <= 2
	 * @throws Exception 
	 */
	public BedSeq filterXLY(String filterOut) throws Exception {
		txtSeqFile.setParameter(compressInType, seqFile, false, true);
		BufferedReader reader   = txtSeqFile.readfile();
		
		TxtReadandWrite txtOut = new TxtReadandWrite();
		txtOut.setParameter(compressOutType, filterOut, true, false);
		
		String content = "";
		while ((content = reader.readLine())!=null) {
			String[] ss = content.split("\t");
			if (Integer.parseInt(ss[5]) <= 2) {
				txtOut.writefile(content + "\n");
			}
		}
		BedSeq bedSeq = new BedSeq(filterOut);
		bedSeq.setCompressType(compressOutType, compressOutType);
		return bedSeq;
	}
	
	
	
	/**
	 * 专门给王彦儒的GSM531964_PHF8.bed过滤的文件，
	 * @throws Exception 
	 */
	public BedSeq filterWYR(String filterOut) throws Exception {
		txtSeqFile.setParameter(compressInType, seqFile, false, true);
		BufferedReader reader   = txtSeqFile.readfile();
		
		TxtReadandWrite txtOut = new TxtReadandWrite();
		txtOut.setParameter(compressOutType,filterOut, true, false);
		
		String content = "";
		while ((content = reader.readLine())!=null) {
			String[] ss = content.split("\t");
			int a = 0;
			try {
				a = Integer.parseInt(ss[3]);
				if (a >= 23) {
					txtOut.writefile(content+"\n");
				}
			} catch (Exception e) {
				if (filterBigNum(ss[3], 23)) {
					txtOut.writefile(content+"\n");
				}
			}
		}
		BedSeq bedSeq = new BedSeq(filterOut);
		bedSeq.setCompressType(compressOutType, compressOutType);
		return bedSeq;
	}
	/**
	 * 专门给王彦儒的GSM531964_PHF8.bed过滤的文件，
	 * @throws Exception 
	 */
	private boolean filterBigNum(String str,int filter)
	{
		String[] ss = str.split("\\D");
		for (String string : ss) {
			int a =0;
			try {
				a = Integer.parseInt(string);
			} catch (Exception e) {
				continue;
			}
			if (a>=filter) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 如果bed文件的坐标太短，根据正负链延长坐标至指定位置<br>
	 * 标准bed文件格式为：chr1  \t  7345  \t  7370  \t  25  \t  52  \t  - <br>
	 * 必须有第六列
	 * @throws Exception 
	 * @throws Exception 
	 */
	public BedSeq extend(int extendTo) {
		String outFile = null;
		if (!compressInType.equals(TxtReadandWrite.TXT)) {
			if (getFileName().endsWith("gz") || getFileName().endsWith("bz") || getFileName().endsWith("zip")) {
				outFile = getFileName().substring(0, getFileName().lastIndexOf("."));
			}
			outFile = FileOperate.changeFileSuffix(outFile, "_extend", null);
		}
		else {
			outFile = FileOperate.changeFileSuffix(getFileName(), "_extend", null);
		}
		return extend(extendTo, outFile);
	}
	/**
	 * 如果bed文件的坐标太短，根据正负链延长坐标至指定位置<br>
	 * 标准bed文件格式为：chr1  \t  7345  \t  7370  \t  25  \t  52  \t  - <br>
	 * 必须有第六列
	 * @throws Exception 
	 * @throws Exception 
	 */
	public BedSeq extend(int extendTo, String outFileName)
	{
		BedSeq bedSeq = new BedSeq(outFileName, true);
		bedSeq.setCompressType(compressInType, compressOutType);
		try {
			for (BedRecord bedRecord : readPerlines()) {
			bedRecord.extend(extendTo);
			bedSeq.writeBedRecord(bedRecord);
		}
			} catch (Exception e) {
			logger.error("extend error! targetFile: " + getFileName() + "   resultFIle: "+ outFileName);
		}
		bedSeq.closeWrite();
		return bedSeq;
	}
	
	/**
	 * 从含有序列的bed文件获得fastQ文件
	 * @param outFileName fastQ文件全名（包括路径）
	 * @throws Exception
	 */
	public FastQ getFastQ() {
		String outFileName = FileOperate.changeFileSuffix(getFileName(), "", "fastq");
		return getFastQ(outFileName);
	}
	/**
	 * 从含有序列的bed文件获得fastQ文件
	 * @param outFileName fastQ文件全名（包括路径）
	 * @throws Exception
	 */
	public FastQ getFastQ(String outFileName) {
		FastQ fastQ = new FastQ(outFileName, true);
		fastQ.setCompressType(compressOutType, compressOutType);
		for (BedRecord bedRecord : readLines()) {
			FastQRecord fastQRecord = new FastQRecord();
			fastQRecord.setName(bedRecord.getName());
			fastQRecord.setFastaQuality(getQuality(bedRecord.getSeqFasta().Length()));
			fastQRecord.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
			fastQRecord.setSeq(bedRecord.getSeqFasta().toString());
			fastQ.writeFastQRecord(fastQRecord);
		}
		fastQ.close();
		return fastQ;
	}
	
	private String getQuality(int length) {
		char[] qualityChar = new char[length];
		for (int i = 0; i < qualityChar.length; i++) {
			qualityChar[i] = 'f';
		}
		return String.copyValueOf(qualityChar);
	}
	/** 过滤reads */
	public BedSeq filterSeq(int mappingNumSmall, int mappingNumBig, Boolean strand) {
		if (mappingNumSmall < 1) {
			mappingNumSmall = 1;
		}
		if (mappingNumBig < mappingNumSmall) {
			mappingNumBig = mappingNumSmall;
		}
		
		String bedFileFiltered = FileOperate.changeFileSuffix(getFileName(), "_filtered", null);		
		BedSeq bedSeqFiltered = new BedSeq(bedFileFiltered, true);
		for (BedRecord bedRecord : readLines()) {
			if (strand != null && bedRecord.isCis5to3() != strand) {
				continue;
			}
			if (bedRecord.getMappingNum() >= mappingNumSmall && bedRecord.getMappingNum() <= mappingNumBig) {
				bedSeqFiltered.writeBedRecord(bedRecord);
			}
		}
		close();
		bedSeqFiltered.closeWrite();
		return bedSeqFiltered;
	}
	/**
	 * calDestribution方法和calCoverage方法里面用到
	 */
	public static final String ALLMAPPEDREADS = "All Mapped Reads";
	/**
	 * 
	 * 排序后再进行计算，否则速度会慢<br>
	 * 计算reads在染色体上的总的mapping数量和单个染色体的mapping数量
	 * @param calFragLen
	 * @param FragmentFile 写入的文件，写入然后调用R
	 * @return linkedHashMap
	 * 第一个key 和 value： allMappingReads    allMappingReads/allReads<br>
	 * 其他: <br>
	 * key: ChrID，小写<br> 
	 * value double[2] 0: chrReads 1: chrReads/allMappingReads
	 */
	public LinkedHashMap<String, double[]> calDestribution(boolean calFragLen, String FragmentFile)
	{
		TxtReadandWrite txtbed = new TxtReadandWrite(compressInType, seqFile, false);
		TxtReadandWrite txtFragmentFile = null;
		if (calFragLen) {
			txtFragmentFile =new TxtReadandWrite(compressOutType, FragmentFile, false);
		}
		//chrID与该chrID所mapping到的reads数量
		LinkedHashMap<String, double[]> hashChrReadsNum = new LinkedHashMap<String, double[]>();
		double[] chrMappedReads = null;
		double[] allMappedReads = new double[2];
		hashChrReadsNum.put(ALLMAPPEDREADS, allMappedReads);
		String content = ""; String chrID = "";
		try {
			BufferedReader readBed = txtbed.readfile();
			long depth = 0;
			while ((content = readBed.readLine()) != null) {
				String[] ss = content.split("\t");
				int Locstart = Integer.parseInt(ss[1]); int Locend = Integer.parseInt(ss[2]); 
				depth = depth + Locend - Locstart;
				if (!ss[0].trim().equals(chrID)) {
					chrID = ss[0].trim();
					chrMappedReads = new double[2];
					if (!chrID.equals("")) {//跳过最开始的chrID
						hashChrReadsNum.put(chrID.toLowerCase(), chrMappedReads);
					}
				}
				allMappedReads[0] ++; chrMappedReads[0]++;
				////////////////// fragment 的长度分布文件   ////////////////////////////////////////////////////////////////
				if (calFragLen) //如果要计算fragment的分布，那么就将fragment的长度记录在txt文本中，最后调用R来计算长度分布
				{
					txtFragmentFile.writefile(Locend - Locstart+""+"\n");
				}
			}
		} catch (Exception e) {
			logger.error("calculate mapping rates error: "+ seqFile);
		}
		//计算比值并加入arraylist
		double readsMappedAll = hashChrReadsNum.get(ALLMAPPEDREADS)[0];
		hashChrReadsNum.get(ALLMAPPEDREADS)[1] = readsMappedAll/getSeqNum();
		for (Entry<String, double[]> entry : hashChrReadsNum.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			if (key.equals(ALLMAPPEDREADS)) {
				continue;
			}
			value[2] = value[1]/readsMappedAll;
		}
		txtbed.close();
		txtFragmentFile.close();
		return hashChrReadsNum;
	}
	
	/**
	 * 测试一下
	 * @param 输入的染色体长度文件，肯定是txt
	 * 计算测序结果的coverage情况，以及reads在每条染色体上的coverage情况，结果必须是排过序的.
	 * @return linkedHashMap
	 * 第一个key 和 value： allMappingReadsCoverage    allMappingReadsCoverage/allChrLen<br>
	 * 其他: <br>
	 * key: ChrID，小写<br> 
	 * value double[2] 0: Coverage 1: Coverage/ChrLen
	 */
	public LinkedHashMap<String, double[]> getCoverage(String chrLenFile) throws Exception
	{
		TxtReadandWrite txtChrID = new TxtReadandWrite(chrLenFile, false);
		//ChrID和长度的对照表
		LinkedHashMap<String, Double> lkHashChrLen = txtChrID.getKey2ValueDouble("\t", false);
		return getCoverage(lkHashChrLen);
	}
	
	/**
	 * 测试一下
	 * @param 输入的染色体长度文件，肯定是txt
	 * 计算测序结果的coverage情况，以及reads在每条染色体上的coverage情况，结果必须是排过序的.
	 * @return linkedHashMap
	 * 第一个key 和 value： allMappingReadsCoverage    allMappingReadsCoverage/allChrLen<br>
	 * 其他: <br>
	 * key: ChrID，小写<br> 
	 * value double[2] 0: Coverage 1: Coverage/ChrLen
	 */
	public LinkedHashMap<String, double[]> getCoverage(HashMap<String, Double> hashChr2Len)
	{
		//ChrID和长度的对照表
		LinkedHashMap<String, double[]> hashChrReadsNum = new LinkedHashMap<String, double[]>();
		TxtReadandWrite txtbed = new TxtReadandWrite(compressInType, seqFile, false);
		
		double[] chrMappedReads = new double[2];//仅仅为了值传递，数据保存在[0]中
		int tmpLocStart = 0; int tmpLocEnd = 0;//用来计算coverage
		//全长基因
		double[] allMappedReads = new double[2];
		hashChrReadsNum.put(ALLMAPPEDREADS, allMappedReads);
		String chrID = "";
		for (String content : txtbed.readlines()) {
			String[] ss = content.split("\t");
			int Locstart = Integer.parseInt(ss[1]); int Locend = Integer.parseInt(ss[2]); 
			if (!ss[0].trim().equals(chrID)) {
				chrID = ss[0].trim();
				chrMappedReads = new double[2];
				if (!chrID.equals("")) {//跳过最开始的chrID
					hashChrReadsNum.put(chrID.toLowerCase(), chrMappedReads);
				}
				tmpLocStart = 0; tmpLocEnd = 0;//用来计算coverage
			}
			////////////////// fragment 的长度分布文件   ////////////////////////////////////////////////////////////////			
			if (Locend <= tmpLocEnd) {
				continue;
			}
			else if (Locstart < tmpLocEnd && Locend> tmpLocEnd ) {
				allMappedReads[0] = allMappedReads[0] + Locend - tmpLocEnd;
				chrMappedReads[0] = chrMappedReads[0] + Locend - tmpLocEnd;
				tmpLocEnd = Locend;
			}
			else if (Locstart > tmpLocEnd ) {
				allMappedReads[0] = allMappedReads[0] + Locend - Locstart;
				chrMappedReads[0] = chrMappedReads[0] + Locend - Locstart;
				tmpLocEnd = Locend;
			}
		}
		//计算比值并加入arraylist
		long chrLenAll = 0L;
		for (String key: hashChrReadsNum.keySet()) {
			chrLenAll = chrLenAll + hashChr2Len.get(key).longValue();
		}
		
		hashChrReadsNum.get(ALLMAPPEDREADS)[1] = hashChrReadsNum.get(ALLMAPPEDREADS)[0]/chrLenAll;
		for (Entry<String, double[]> entry : hashChrReadsNum.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			if (key.equals(ALLMAPPEDREADS)) {
				continue;
			}
			value[2] = value[1]/hashChr2Len.get(key);
		}
		return hashChrReadsNum;
	}
	
	/**
	 * 用dge的方法来获得基因表达量
	 * @param sort 是否需要排序
	 * 出错返回null
	 */
	public HashMap<String, Integer> getDGEnum(boolean sort, boolean allTags) {
		BedSeq bedseq = null;
		if (sort) {
			bedseq = sort(FileOperate.changeFileSuffix(getFileName(), "_DGESort", null));
		}
		else {
			bedseq = this;
		}
		try {
			return bedseq.getGeneExpress(allTags);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param Alltags true: 选择全部tag，false，只选择最多的tag
	 * @return
	 * 返回每个基因所对应的表达量，包括多个tag之和--除了反向tag， 用 int[1]只是为了地址引用。
	 * bed文件必须排序
	 * @throws Exception
	 */
	private HashMap<String, Integer> getGeneExpress(boolean Alltags) throws Exception {
		txtSeqFile = new TxtReadandWrite(compressInType, seqFile, false);
		BufferedReader reader = txtSeqFile.readfile();
		String content = "";
		HashMap<String, Integer> hashResult = new HashMap<String, Integer>();
		String oldLoc = ""; ArrayList<int[]> lsTmpExpValue = new ArrayList<int[]>();
		int[] tmpCount = new int[]{0}; int tmpLocEnd = -1;
		while ((content = reader.readLine()) != null) {
			String[] ss = content.split("\t");
			//mapping到互补链上的，是假的信号
			if (ss[5].equals("-")) {
				continue;
			}
			if (!oldLoc.equals(ss[0]) && !oldLoc.equals("")) {
				if (Alltags) {
					hashResult.put(oldLoc, sum(lsTmpExpValue));
				}
				else {
					hashResult.put(oldLoc, max(lsTmpExpValue));
				}
				lsTmpExpValue.clear();
				tmpCount = new int[]{0};
				tmpLocEnd = -1;
			}
			if (Integer.parseInt(ss[1]) > tmpLocEnd) {
				tmpCount = new int[]{0};
				lsTmpExpValue.add(tmpCount);
			}
			tmpCount[0] ++;
			tmpLocEnd = Integer.parseInt(ss[2]);
			oldLoc = ss[0];
		}
		return hashResult;
	}
	/**
	 * 输入int[0] 只有0位有信息
	 * @param lsReads
	 * @return
	 */
	private int max(ArrayList<int[]> lsReads)
	{
		int max = lsReads.get(0)[0];
		for (int[] is : lsReads) {
			if (is[0] > max) {
				max = is[0];
			}
		}
		return max;
	}
	/**
	 * 输入int[0] 只有0位有信息
	 * @param lsReads
	 * @return
	 */
	private int sum(ArrayList<int[]> lsReads)
	{
		int sum = 0;
		for (int[] is : lsReads) {
			sum = sum + is[0];
		}
		return sum;
	}
	/**
	 * 
	 * 无法设定compressType
	 * 将bed文件转化成DGE所需的信息，直接可以用DEseq分析的
	 * @param result
	 * @param sort 
	 * @param allTags 是否获得全部的正向tag，false的话，只选择最多的正向tag的数量
	 * @param bedFile
	 */
	public static void dgeCal(String result, boolean sort, boolean allTags, String... bedFile)
	{
		ArrayList<HashMap<String, Integer>> lsDGEvalue = new ArrayList<HashMap<String,Integer>>();
		for (String string : bedFile) {
			BedSeq bedSeq = new BedSeq(string);
			lsDGEvalue.add(bedSeq.getDGEnum(sort,allTags));
		}
		HashMap<String, int[]> hashResult = combineHashDGEvalue(lsDGEvalue);
		TxtReadandWrite txtOut = new TxtReadandWrite(result, true);
		String title = "GeneID";
		for (String string : bedFile) {
			title = title + "\t"+ FileOperate.getFileNameSep(string)[0];
		}
		txtOut.writefileln(title);
		for (Entry<String, int[]> entry : hashResult.entrySet()) {
			String loc = entry.getKey(); int[] value = entry.getValue();
			for (int i : value) {
				loc = loc + "\t" + i;
			}
			txtOut.writefileln(loc);
		}
	}
	/**
	 * 给定一组hash表，key：locID   value：expressValue
	 * 将他们合并成一个hash表
	 * @param lsDGEvalue
	 * @return
	 */
	private static HashMap<String, int[]> combineHashDGEvalue(ArrayList<HashMap<String, Integer>> lsDGEvalue)
	{
		HashMap<String, int[]> hashValue = new HashMap<String, int[]>();
		for (int i = 0; i < lsDGEvalue.size(); i++) {
			HashMap<String, Integer> hashTmp = lsDGEvalue.get(i);
			for (Entry<String, Integer> entry : hashTmp.entrySet()) {
				String loc = entry.getKey(); int value = entry.getValue();
			
				if (hashValue.containsKey(loc)) {
					int[] tmpvalue = hashValue.get(loc);
					tmpvalue[i] = value;
				}
				else
				{
					int[] tmpvalue = new int[lsDGEvalue.size()];
					tmpvalue[i] = value;
					hashValue.put(loc, tmpvalue);
				}
			}
		}
		return hashValue;
	}
	/**
	 * 输入经过排序的peakfile,或者说bedfile，将重叠的peak进行合并 注意，结果中仅保留peak，没有保留其他多的信息
	 * 从第一行开始合并
	 * @return
	 */
	public BedSeq combBedOverlap() {
		return combBedOverlap(false);
	}
	/**
	 * 输入经过排序的peakfile,或者说bedfile，将重叠的peak进行合并 注意，结果中除保留peak，还可选择是否根据方向进行合并，同时保留方向信息
	 * 从第一行开始合并
	 * @return
	 */
	public BedSeq combBedOverlap(boolean cis5to3) {
		String outFile = FileOperate.changeFileSuffix(getFileName(), "_comb", null);
		return combBedOverlap(cis5to3, 0, outFile);
	}
	/**
	 * 输入经过排序的peakfile,或者说bedfile，将重叠的peak进行合并 注意，结果中仅保留peak，没有保留其他多的信息
	*/
	public BedSeq combBedOverlap(String outFile, boolean cis5to3) {
		return combBedOverlap(cis5to3, 0, outFile);
	}
	/**
	 * 输入经过排序的peakfile,或者说bedfile，将重叠的peak进行合并 注意，结果中仅保留peak，没有保留其他多的信息
	*/
	@Deprecated
	public BedSeq combBedOverlap(int readLines) {
		String out = FileOperate.changeFileSuffix(getFileName(), "_comb", null);
		return combBedOverlap(false, 0, out);
	}
	/**
	 * 输入经过排序的peakfile,或者说bedfile，将重叠的peak进行合并
	 * 注意，结果中仅保留peak，没有保留其他多的信息
	 * @param cis5to3 是否根据方向进行合并
	 * @param peakFile
	 * @param readLines 从第几行开始读
	 */
	public BedSeq combBedOverlap(boolean cis5to3, int readLines, String outFile) {
		if (cis5to3) {
			return combBedOverlapCis5to3(readLines, outFile);
		}
		else {
			return combBedOverlap(readLines, outFile);
		}
	}
	
	/**
	 * 输入经过排序的peakfile,或者说bedfile，将重叠的peak进行合并
	 * 注意，结果中仅保留peak，没有保留其他多的信息
	 * @param peakFile
	 * @param readLines 从第几行开始读
	 */
	private BedSeq combBedOverlap(int readLines, String outFile) {
		BedSeq bedSeqResult = new BedSeq(outFile, true);
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
		bedSeqResult.closeWrite();
		return bedSeqResult;
	}
	/**
	 * 输入经过排序的peakfile,或者说bedfile，将重叠的peak进行合并
	 * 注意，结果中仅保留peak，没有保留其他多的信息
	 * @param peakFile
	 * @param readLines 从第几行开始读
	 */
	private BedSeq combBedOverlapCis5to3(int readLines, String outFile) {
			BedSeq bedSeqResult = new BedSeq(outFile, true);
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
				if	(!bedRecord.getRefID().equals(bedRecordLast.getRefID()) || bedRecord.getStartAbs() >= bedRecordLast.getEndAbs() || bedRecord.isCis5to3() != bedRecordLast.isCis5to3()) {
					bedSeqResult.writeBedRecord(bedRecordLast);
					bedRecordLast = new BedRecord();
					bedRecordLast.setRefID(bedRecord.getRefID());
					bedRecordLast.setStartEndLoc(bedRecord.getStartAbs(), bedRecord.getEndAbs());
					bedRecordLast.setCis5to3(bedRecord.isCis5to3());
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
			bedSeqResult.closeWrite();
			return bedSeqResult;
		}
	public BedSeq removeDuplicat() {
		String out = FileOperate.changeFileSuffix(getFileName(), "_removeDup", null);
		return removeDuplicat(out);
	}
	
	/**
	 * 输入经过排序的bedfile，将重复的bedrecord进行合并，合并的信息取第一条
	 * @param peakFile 
	 */
	public BedSeq removeDuplicat(String outFile) {
		BedSeq bedSeqResult = new BedSeq(outFile, true);
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
		bedSeqResult.closeWrite();
		return bedSeqResult;
	}
	
	public ArrayList<String[]> getChrReadsNum() {
		HashMap<String, int[]> hashChrID2ReadsNum = new HashMap<String, int[]>();
		for (BedRecord bedRecord : readLines()) {
			if (hashChrID2ReadsNum.containsKey(bedRecord.getRefID())) {
				int[] ReadsNum = hashChrID2ReadsNum.get(bedRecord.getRefID());
				ReadsNum[0] = ReadsNum[0] + bedRecord.getReadsNum();
			}
			else {
				hashChrID2ReadsNum.put(bedRecord.getRefID(), new int[]{bedRecord.getReadsNum(), 0});
			}
		}
		
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (Entry<String, int[]> entry : hashChrID2ReadsNum.entrySet()) {
			String[] info = new String[]{entry.getKey(), entry.getValue()[0] + ""};
			lsResult.add(info);
		}
		return lsResult;
	}
	
	
	/**
	 * 将所有mapping至genomic上的bed文件合并，这个是预测novel miRNA的
	 * @param outFile 输出文件
	 * @param bedSeqFile 输入文件
	 */
	public static BedSeq combBedFile(String outFile, String... bedSeqFile) {
		BedSeq bedSeq = new BedSeq(outFile, true);
		for (String string : bedSeqFile) {
			BedSeq bedSeq2 = new BedSeq(string);
			for (BedRecord bedRecord : bedSeq2.readLines()) {
				bedSeq.writeBedRecord(bedRecord);
			}
			bedSeq2.closeWrite();
		}
		bedSeq.closeWrite();
		return bedSeq;
	}
	/**
	 * 将所有mapping至genomic上的bed文件合并，这个是预测novel miRNA的
	 * @param outFile 输出文件
	 * @param bedSeqFile 输入文件
	 */
	public static BedSeq combBedFile(String outFile, ArrayList<String> lsbedSeqFile) {
		BedSeq bedSeq = new BedSeq(outFile, true);
		for (String string : lsbedSeqFile) {
			BedSeq bedSeq2 = new BedSeq(string);
			for (BedRecord bedRecord : bedSeq2.readLines()) {
				bedSeq.writeBedRecord(bedRecord);
			}
			bedSeq2.closeWrite();
		}
		bedSeq.closeWrite();
		return bedSeq;
	}
	
	/** 假设文件存在，判断其是否为novelbio所定义的bed文件 */
	public static boolean isBedFile(String bedFile) {
		final int allReadLines = 100;
		final int maxBedLines = 50;
		String fileType = TxtReadandWrite.TXT;
		if (bedFile.endsWith(".gz")) {
			fileType = TxtReadandWrite.GZIP;
		}
		TxtReadandWrite txtSeqFile = new TxtReadandWrite(bedFile, false);
		txtSeqFile.setFiletype(fileType);
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
}
