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
 * bed��ʽ���ļ���ͳͳ��������bed�ļ�
 * novelbio��bedfile��ʽ��
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
	 * һ���ڵ�����
	 */
	public static final int MAPPING_NUM_COLUMN = 6;
	/**
	 * mappingNum. 1 means unique mapping
	 * һ���ڵ�����
	 */
	public static final int MAPPING_NUM_STRAND = 5;
	
	public BedSeq(String bedFile) {
		super(bedFile, 1);
	}
	/**
	 * �½�һ��bed������������д����������
	 * ע��д�����ص���{@link #closeWrite()}�����ر�д��������bedд��ת��Ϊbed��ȡ
	 * @param bedFile
	 * @param creatBed
	 */
	public BedSeq(String bedFile, boolean creatBed) {
		super(bedFile, 1, creatBed);
	}
	public static void main(String[] args) {
	}
	/**
	 * <b>д�������� {@link #closeWrite} �����ر�</b>
	 * ������ʱ��Ҫ�趨Ϊcreatģʽ
	 * @param bedRecord
	 */
	public void writeBedRecord(BedRecord bedRecord) {
		if (bedRecord == null) {
			return;
		}
		txtSeqFile.writefileln(bedRecord.toString());
	}
	/**
	 * �ڲ��ر�
	 * @param lsBedRecord
	 */
	public void wirteBedRecord(List<BedRecord> lsBedRecord) {
		for (BedRecord bedRecord : lsBedRecord) {
			txtSeqFile.writefileln(bedRecord.toString());
		}
		closeWrite();
	}
	/**
	 * ��ȡǰ���У���Ӱ��{@link #readLines()}
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
	 * ��ȡǰ���У���Ӱ��{@link #readLines()}
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
	 * д�������ô˷����ر�
	 * �ر�������������bedseqд��ת��Ϊbedseq��ȡ
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
	 * �ӵڼ��п�ʼ������ʵ����
	 * @param lines ���linesС��1�����ͷ��ʼ��ȡ
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
	 * ������ȡ�ļ�
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
	 * ָ��bed�ļ����Լ���Ҫ���������������������
	 * @param chrID ChrID���ڵ��У���1��ʼ������������ĸ��������
	 * @param sortBedFile �������ļ�ȫ��
	 * @param arg ��ChrID�⣬������Ҫ������У�������������ʵ����
	 * @throws Exception
	 */
	public BedSeq sortBedFile(int chrID, String sortBedFile,int...arg)  {
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #��һ�����һ����ֹ����
		//�ڶ�����ڶ�����ֹ����������,���������������ֹ����������
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
	 * ָ��bed�ļ�������chrID�������������
	 * @param sortBedFile �������ļ�ȫ��
	 */
	public BedSeq sort(String sortBedFile)  {
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #��һ�����һ����ֹ���򣬵ڶ�����ڶ�����ֹ����������,���������������ֹ����������
		return sortBedFile(1, sortBedFile, 2,3);
	}
	/**
	 * ָ��bed�ļ�������chrID�������������<br>
	 * @param sortBedFile �������ļ�ȫ��<br>
	 * ��������ΪFileOperate.changeFileSuffix(getFileName(), "_sorted", null);
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
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #��һ�����һ����ֹ���򣬵ڶ�����ڶ�����ֹ����������,���������������ֹ����������
		return sort(outFile);
	}
	/**
	 * ר�Ÿ������µ�GSM307618���˵��ļ���
	 * chr11   79993182        79993208        -       2119.5.3904     0       CTTGGGGCAGAAGAGCCCTTGCAGCC
	 ������ ���� <= 2
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
	 * ר�Ÿ��������GSM531964_PHF8.bed���˵��ļ���
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
	 * ר�Ÿ��������GSM531964_PHF8.bed���˵��ļ���
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
	 * ���bed�ļ�������̫�̣������������ӳ�������ָ��λ��<br>
	 * ��׼bed�ļ���ʽΪ��chr1  \t  7345  \t  7370  \t  25  \t  52  \t  - <br>
	 * �����е�����
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
	 * ���bed�ļ�������̫�̣������������ӳ�������ָ��λ��<br>
	 * ��׼bed�ļ���ʽΪ��chr1  \t  7345  \t  7370  \t  25  \t  52  \t  - <br>
	 * �����е�����
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
	 * �Ӻ������е�bed�ļ����fastQ�ļ�
	 * @param outFileName fastQ�ļ�ȫ��������·����
	 * @throws Exception
	 */
	public FastQ getFastQ() {
		String outFileName = FileOperate.changeFileSuffix(getFileName(), "", "fastq");
		return getFastQ(outFileName);
	}
	/**
	 * �Ӻ������е�bed�ļ����fastQ�ļ�
	 * @param outFileName fastQ�ļ�ȫ��������·����
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
	/** ����reads */
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
	 * calDestribution������calCoverage���������õ�
	 */
	public static final String ALLMAPPEDREADS = "All Mapped Reads";
	/**
	 * 
	 * ������ٽ��м��㣬�����ٶȻ���<br>
	 * ����reads��Ⱦɫ���ϵ��ܵ�mapping�����͵���Ⱦɫ���mapping����
	 * @param calFragLen
	 * @param FragmentFile д����ļ���д��Ȼ�����R
	 * @return linkedHashMap
	 * ��һ��key �� value�� allMappingReads    allMappingReads/allReads<br>
	 * ����: <br>
	 * key: ChrID��Сд<br> 
	 * value double[2] 0: chrReads 1: chrReads/allMappingReads
	 */
	public LinkedHashMap<String, double[]> calDestribution(boolean calFragLen, String FragmentFile)
	{
		TxtReadandWrite txtbed = new TxtReadandWrite(compressInType, seqFile, false);
		TxtReadandWrite txtFragmentFile = null;
		if (calFragLen) {
			txtFragmentFile =new TxtReadandWrite(compressOutType, FragmentFile, false);
		}
		//chrID���chrID��mapping����reads����
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
					if (!chrID.equals("")) {//�����ʼ��chrID
						hashChrReadsNum.put(chrID.toLowerCase(), chrMappedReads);
					}
				}
				allMappedReads[0] ++; chrMappedReads[0]++;
				////////////////// fragment �ĳ��ȷֲ��ļ�   ////////////////////////////////////////////////////////////////
				if (calFragLen) //���Ҫ����fragment�ķֲ�����ô�ͽ�fragment�ĳ��ȼ�¼��txt�ı��У�������R�����㳤�ȷֲ�
				{
					txtFragmentFile.writefile(Locend - Locstart+""+"\n");
				}
			}
		} catch (Exception e) {
			logger.error("calculate mapping rates error: "+ seqFile);
		}
		//�����ֵ������arraylist
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
	 * ����һ��
	 * @param �����Ⱦɫ�峤���ļ����϶���txt
	 * �����������coverage������Լ�reads��ÿ��Ⱦɫ���ϵ�coverage���������������Ź����.
	 * @return linkedHashMap
	 * ��һ��key �� value�� allMappingReadsCoverage    allMappingReadsCoverage/allChrLen<br>
	 * ����: <br>
	 * key: ChrID��Сд<br> 
	 * value double[2] 0: Coverage 1: Coverage/ChrLen
	 */
	public LinkedHashMap<String, double[]> getCoverage(String chrLenFile) throws Exception
	{
		TxtReadandWrite txtChrID = new TxtReadandWrite(chrLenFile, false);
		//ChrID�ͳ��ȵĶ��ձ�
		LinkedHashMap<String, Double> lkHashChrLen = txtChrID.getKey2ValueDouble("\t", false);
		return getCoverage(lkHashChrLen);
	}
	
	/**
	 * ����һ��
	 * @param �����Ⱦɫ�峤���ļ����϶���txt
	 * �����������coverage������Լ�reads��ÿ��Ⱦɫ���ϵ�coverage���������������Ź����.
	 * @return linkedHashMap
	 * ��һ��key �� value�� allMappingReadsCoverage    allMappingReadsCoverage/allChrLen<br>
	 * ����: <br>
	 * key: ChrID��Сд<br> 
	 * value double[2] 0: Coverage 1: Coverage/ChrLen
	 */
	public LinkedHashMap<String, double[]> getCoverage(HashMap<String, Double> hashChr2Len)
	{
		//ChrID�ͳ��ȵĶ��ձ�
		LinkedHashMap<String, double[]> hashChrReadsNum = new LinkedHashMap<String, double[]>();
		TxtReadandWrite txtbed = new TxtReadandWrite(compressInType, seqFile, false);
		
		double[] chrMappedReads = new double[2];//����Ϊ��ֵ���ݣ����ݱ�����[0]��
		int tmpLocStart = 0; int tmpLocEnd = 0;//��������coverage
		//ȫ������
		double[] allMappedReads = new double[2];
		hashChrReadsNum.put(ALLMAPPEDREADS, allMappedReads);
		String chrID = "";
		for (String content : txtbed.readlines()) {
			String[] ss = content.split("\t");
			int Locstart = Integer.parseInt(ss[1]); int Locend = Integer.parseInt(ss[2]); 
			if (!ss[0].trim().equals(chrID)) {
				chrID = ss[0].trim();
				chrMappedReads = new double[2];
				if (!chrID.equals("")) {//�����ʼ��chrID
					hashChrReadsNum.put(chrID.toLowerCase(), chrMappedReads);
				}
				tmpLocStart = 0; tmpLocEnd = 0;//��������coverage
			}
			////////////////// fragment �ĳ��ȷֲ��ļ�   ////////////////////////////////////////////////////////////////			
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
		//�����ֵ������arraylist
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
	 * ��dge�ķ�������û�������
	 * @param sort �Ƿ���Ҫ����
	 * ������null
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
	 * @param Alltags true: ѡ��ȫ��tag��false��ֻѡ������tag
	 * @return
	 * ����ÿ����������Ӧ�ı�������������tag֮��--���˷���tag�� �� int[1]ֻ��Ϊ�˵�ַ���á�
	 * bed�ļ���������
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
			//mapping���������ϵģ��Ǽٵ��ź�
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
	 * ����int[0] ֻ��0λ����Ϣ
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
	 * ����int[0] ֻ��0λ����Ϣ
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
	 * �޷��趨compressType
	 * ��bed�ļ�ת����DGE�������Ϣ��ֱ�ӿ�����DEseq������
	 * @param result
	 * @param sort 
	 * @param allTags �Ƿ���ȫ��������tag��false�Ļ���ֻѡ����������tag������
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
	 * ����һ��hash��key��locID   value��expressValue
	 * �����Ǻϲ���һ��hash��
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
	 * ���뾭�������peakfile,����˵bedfile�����ص���peak���кϲ� ע�⣬����н�����peak��û�б������������Ϣ
	 * �ӵ�һ�п�ʼ�ϲ�
	 * @return
	 */
	public BedSeq combBedOverlap() {
		return combBedOverlap(false);
	}
	/**
	 * ���뾭�������peakfile,����˵bedfile�����ص���peak���кϲ� ע�⣬����г�����peak������ѡ���Ƿ���ݷ�����кϲ���ͬʱ����������Ϣ
	 * �ӵ�һ�п�ʼ�ϲ�
	 * @return
	 */
	public BedSeq combBedOverlap(boolean cis5to3) {
		String outFile = FileOperate.changeFileSuffix(getFileName(), "_comb", null);
		return combBedOverlap(cis5to3, 0, outFile);
	}
	/**
	 * ���뾭�������peakfile,����˵bedfile�����ص���peak���кϲ� ע�⣬����н�����peak��û�б������������Ϣ
	*/
	public BedSeq combBedOverlap(String outFile, boolean cis5to3) {
		return combBedOverlap(cis5to3, 0, outFile);
	}
	/**
	 * ���뾭�������peakfile,����˵bedfile�����ص���peak���кϲ� ע�⣬����н�����peak��û�б������������Ϣ
	*/
	@Deprecated
	public BedSeq combBedOverlap(int readLines) {
		String out = FileOperate.changeFileSuffix(getFileName(), "_comb", null);
		return combBedOverlap(false, 0, out);
	}
	/**
	 * ���뾭�������peakfile,����˵bedfile�����ص���peak���кϲ�
	 * ע�⣬����н�����peak��û�б������������Ϣ
	 * @param cis5to3 �Ƿ���ݷ�����кϲ�
	 * @param peakFile
	 * @param readLines �ӵڼ��п�ʼ��
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
	 * ���뾭�������peakfile,����˵bedfile�����ص���peak���кϲ�
	 * ע�⣬����н�����peak��û�б������������Ϣ
	 * @param peakFile
	 * @param readLines �ӵڼ��п�ʼ��
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
				//��ΪbedRecord�ڲ�Ĭ��ReadsNumΪnull�������Ϊnull����ȡʱ��ʾΪ1����������Ϊ1�Ķ�Ҫ�ֹ��趨һ��
				if (bedRecordLast.getReadsNum() == 1) {
					bedRecordLast.setReadsNum(1);
				}
				continue;
			}
			else if (bedRecord.getStartAbs() < bedRecordLast.getEndAbs()) {
				//����һ��overlap�ͼ���1����ʾ�������ж���reads
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
	 * ���뾭�������peakfile,����˵bedfile�����ص���peak���кϲ�
	 * ע�⣬����н�����peak��û�б������������Ϣ
	 * @param peakFile
	 * @param readLines �ӵڼ��п�ʼ��
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
					//��ΪbedRecord�ڲ�Ĭ��ReadsNumΪnull�������Ϊnull����ȡʱ��ʾΪ1����������Ϊ1�Ķ�Ҫ�ֹ��趨һ��
					if (bedRecordLast.getReadsNum() == 1) {
						bedRecordLast.setReadsNum(1);
					}
					continue;
				}
				else if (bedRecord.getStartAbs() < bedRecordLast.getEndAbs()) {
					//����һ��overlap�ͼ���1����ʾ�������ж���reads
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
	 * ���뾭�������bedfile�����ظ���bedrecord���кϲ����ϲ�����Ϣȡ��һ��
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
				// ��ΪbedRecord�ڲ�Ĭ��ReadsNumΪnull�������Ϊnull����ȡʱ��ʾΪ1����������Ϊ1�Ķ�Ҫ�ֹ��趨һ��
				if (bedRecordLast.getReadsNum() == 1) {
					bedRecordLast.setReadsNum(1);
				}
				continue;
			} else {
				// ����һ��overlap�ͼ���1����ʾ�������ж���reads
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
	 * ������mapping��genomic�ϵ�bed�ļ��ϲ��������Ԥ��novel miRNA��
	 * @param outFile ����ļ�
	 * @param bedSeqFile �����ļ�
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
	 * ������mapping��genomic�ϵ�bed�ļ��ϲ��������Ԥ��novel miRNA��
	 * @param outFile ����ļ�
	 * @param bedSeqFile �����ļ�
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
	
	/** �����ļ����ڣ��ж����Ƿ�Ϊnovelbio�������bed�ļ� */
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
