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
public class BedSeq extends SeqComb{
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
	 * ��ȡǰ���У���Ӱ��{@link #readlines()}
	 * @param num
	 * @return
	 */
	public ArrayList<BedRecord> readHeadLines(int num) {
		ArrayList<BedRecord> lsResult = new ArrayList<BedRecord>();
		int i = 0;
		for (BedRecord bedRecord : readlines()) {
			if (i >= num) {
				break;
			}
			lsResult.add(bedRecord);
		}
		return lsResult;
	}
	/**
	 * ��ȡǰ���У���Ӱ��{@link #readlines()}
	 * @param num
	 * @return
	 */
	public BedRecord readFirstLine() {
		int i = 0;
		for (BedRecord bedRecord : readlines()) {
			return bedRecord;
		}
		return null;
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
	public Iterable<BedRecord> readlines() {
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
	public Iterable<BedRecord> readlines(int lines) {
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
		
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground("sortBed");
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
	public BedSeq sortBedFile(String sortBedFile)  {
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #��һ�����һ����ֹ���򣬵ڶ�����ڶ�����ֹ����������,���������������ֹ����������
		return sortBedFile(1, sortBedFile, 2,3);
	}
	/**
	 * ָ��bed�ļ�������chrID�������������<br>
	 * @param sortBedFile �������ļ�ȫ��<br>
	 * ��������ΪFileOperate.changeFileSuffix(getFileName(), "_sorted", null);
	 */
	public BedSeq sortBedFile()  {
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
		return sortBedFile(outFile);
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
	 * @param colSeqNum �����ļ��ڵڼ��У�ʵ����
	 * @param outFileName fastQ�ļ�ȫ��������·����
	 * @throws Exception
	 */
	public void getFastQ(int colSeqNum, String outFileName)
	{
		colSeqNum--;
		txtSeqFile.setParameter(compressInType, seqFile, false, true);
		BufferedReader reader;
		try {
			reader = txtSeqFile.readfile();
			TxtReadandWrite txtOut = new TxtReadandWrite();
			txtOut.setParameter(compressOutType, outFileName, true, false);
			String content = "";
			//������,һ�ٸ�
			String qstring = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
			qstring = qstring + qstring + qstring + qstring + qstring;
			while ((content = reader.readLine()) != null) {
				String[] ss = content.split("\t");
				txtOut.writefileln("@A80TF3ABXX:6:1:1223:2180#/1");
				txtOut.writefileln(ss[colSeqNum]);
				txtOut.writefileln("+");
				txtOut.writefileln(qstring.substring(0,ss[colSeqNum].length()));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			bedseq = sortBedFile(FileOperate.changeFileSuffix(getFileName(), "_DGESort", null));
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
	private HashMap<String, Integer> getGeneExpress(boolean Alltags) throws Exception
	{
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
	 * �ӵ�һ�п�ʼ�ϲ�
	 * @return
	 */
	public BedSeq combBedFile() {
		return combBedFile(0);
	}
	public BedSeq combBedFile(String outFile) {
		return combBedFile(0, outFile);
	}
	@Deprecated
	public BedSeq combBedFile(int readLines) {
		String out = FileOperate.changeFileSuffix(getFileName(), "_comb", null);
		return combBedFile(0, out);
	}
	
	/**
	 * ���뾭�������peakfile,����˵bedfile�����ص���peak���кϲ�
	 * ע�⣬����н�����peak��û�б������������Ϣ
	 * @param peakFile
	 * @param readLines �ӵڼ��п�ʼ��
	 */
	public BedSeq combBedFile(int readLines, String outFile)
	{
		BedSeq bedSeqResult = new BedSeq(outFile, true);
		if (readLines < 1) {
			readLines = 1;
		}
		BedRecord bedRecordLast = null;
		for (BedRecord bedRecord : readlines(readLines)) {
			if (bedRecordLast == null)
			{
				bedRecordLast = new BedRecord();
				bedRecordLast.setRefID(bedRecord.getRefID());
				bedRecordLast.setStartEndLoc(bedRecord.getStart(), bedRecord.getEnd());
			}
				
			if	(!bedRecord.getRefID().equals(bedRecordLast.getRefID()) || bedRecord.getStart() >= bedRecordLast.getEnd()) {
				bedSeqResult.writeBedRecord(bedRecordLast);
				bedRecordLast = new BedRecord();
				bedRecordLast.setRefID(bedRecord.getRefID());
				bedRecordLast.setStartEndLoc(bedRecord.getStart(), bedRecord.getEnd());
				//��ΪbedRecord�ڲ�Ĭ��ReadsNumΪnull�������Ϊnull����ȡʱ��ʾΪ1����������Ϊ1�Ķ�Ҫ�ֹ��趨һ��
				if (bedRecordLast.getReadsNum() == 1) {
					bedRecordLast.setReadsNum(1);
				}
				continue;
			}
			else if (bedRecord.getStart() < bedRecordLast.getEnd()) {
				//����һ��overlap�ͼ���1����ʾ�������ж���reads
				bedRecordLast.setReadsNum(bedRecordLast.getReadsNum() + 1);
				if (bedRecordLast.getEnd() < bedRecord.getEnd()) {
					bedRecordLast.setStartEndLoc(bedRecordLast.getStart(), bedRecord.getEnd());
				}
			}
		}
		bedSeqResult.writeBedRecord(bedRecordLast);
		bedSeqResult.closeWrite();
		return bedSeqResult;
	}
	
}
