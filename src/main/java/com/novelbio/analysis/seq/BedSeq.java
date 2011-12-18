package com.novelbio.analysis.seq;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

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
 *
 */
public class BedSeq extends SeqComb{	
	private static Logger logger = Logger.getLogger(BedSeq.class);  
	
	public BedSeq(String bedFile) {
		super(bedFile, 1);
	}
	
	public static void main(String[] args) {
		BedSeq bedSeq = null;
		String parentFile = "/media/winE/NBC/Project/Project_FY_Lab/Result/cufflink_evaluate/";
		
		bedSeq = new BedSeq(parentFile+"K0.bed");
		System.out.print("\tK0MappedNum\t"+bedSeq.getSeqNum());
		System.out.println();
		
		bedSeq = new BedSeq(parentFile+"K5.bed");
		System.out.print("\tK5readsFilterNum\t"+bedSeq.getSeqNum());
		System.out.println();
		
		bedSeq = new BedSeq(parentFile+"WT0.bed");
		System.out.print("\tWT0readsFilterNum\t"+bedSeq.getSeqNum());
		System.out.println();
		
		bedSeq = new BedSeq(parentFile+"WT5.bed");
		System.out.print("\tWT5readsFilterNum\t"+bedSeq.getSeqNum());
		System.out.println();
	}
	
	
	/**
	 * 指定bed文件，以及需要排序的列数，产生排序结果
	 * @param chrID ChrID所在的列，从1开始记数，按照字母数字排序
	 * @param sortBedFile 排序后的文件全名
	 * @param arg 除ChrID外，其他需要排序的列，按照数字排序，实际列
	 * @throws Exception
	 */
	public BedSeq sortBedFile(int chrID, String sortBedFile,int...arg)  {
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #第一列起第一列终止排序，第二列起第二列终止按数字排序,第三列起第三列终止按数字排序
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
	 * 指定bed文件，按照chrID和坐标进行排序
	 * @param sortBedFile 排序后的文件全名
	 */
	public BedSeq sortBedFile(String sortBedFile)  {
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #第一列起第一列终止排序，第二列起第二列终止按数字排序,第三列起第三列终止按数字排序
		return sortBedFile(1, sortBedFile, 2,3);
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
	public BedSeq extend(int extendTo, String outFileName)
	{
		try {
			txtSeqFile.setParameter(compressInType, seqFile, false, true);
			BufferedReader reader = txtSeqFile.readfile();
			
			TxtReadandWrite txtOut = new TxtReadandWrite(compressOutType, outFileName, true);
			
			String content = "";
			while ((content = reader.readLine()) != null) {
				String[] ss = content.split("\t");
				int end = Integer.parseInt(ss[2]); int start = Integer.parseInt(ss[1]);
				if ((end - start )< 0) {logger.error("Bed 文件出错，有一列的终点小于起点"+content); }
				
				if ((end - start) < extendTo ) {
					if (ss[5].equals("+")) {
						ss[2] = start + extendTo + "";
					}
					else {
						ss[1] = end - extendTo + ""; 
					}
				}
				String contString = "";
				for (int i = 0; i < ss.length-1; i++) {
					contString = contString+ss[i] + "\t";
				}
				contString = contString + ss[ss.length -1];
				txtOut.writefile(contString+"\n");
			}
			txtSeqFile.close();
			txtOut.close();
			BedSeq bedSeq = new BedSeq(outFileName);
			bedSeq.setCompressType(compressOutType, compressOutType);
			return bedSeq;
		} catch (Exception e) {
			logger.error("extend error! targetFile: " + getSeqFile() + "   resultFIle: "+ outFileName);
		}
		return null;
		
	}
	
	/**
	 * 从含有序列的bed文件获得fastQ文件
	 * @param colSeqNum 序列文件在第几列，实际列
	 * @param outFileName fastQ文件全名（包括路径）
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
			//质量列,一百个
			String qstring = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
			qstring = qstring + qstring + qstring + qstring + qstring + qstring + qstring + qstring + qstring;
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
	 * calDestribution方法和calCoverage方法里面用到
	 */
	private String ALLMAPPEDREADS = "All Mapped Reads";
	/**
	 * 
	 * 排序后再进行计算，否则速度会慢<br>
	 * 计算reads在染色体上的总的mapping数量和单个染色体的mapping数量
	 * @param calFragLen
	 * @param FragmentFile
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
	public LinkedHashMap<String, double[]> calCoverage(String chrLenFile) throws Exception
	{
		TxtReadandWrite txtChrID = new TxtReadandWrite(chrLenFile, false);
		//ChrID和长度的对照表
		LinkedHashMap<String, String> lkHashChrLen = txtChrID.getKey2Value("\t", false);
		LinkedHashMap<String, double[]> hashChrReadsNum = new LinkedHashMap<String, double[]>();
		TxtReadandWrite txtbed = new TxtReadandWrite(compressInType, seqFile, false);
		
		double[] chrMappedReads = new double[2];//仅仅为了值传递，数据保存在[0]中
		int tmpLocStart = 0; int tmpLocEnd = 0;//用来计算coverage
		//全长基因
		double[] allMappedReads = new double[2];
		hashChrReadsNum.put(ALLMAPPEDREADS, allMappedReads);
		
		String content = ""; String chrID = "";
		BufferedReader readBed = txtbed.readfile();
		while ((content = readBed.readLine()) != null) {
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
			chrLenAll = chrLenAll + Long.parseLong(lkHashChrLen.get(key));
		}
		
		hashChrReadsNum.get(ALLMAPPEDREADS)[1] = hashChrReadsNum.get(ALLMAPPEDREADS)[0]/chrLenAll;
		for (Entry<String, double[]> entry : hashChrReadsNum.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			if (key.equals(ALLMAPPEDREADS)) {
				continue;
			}
			value[2] = value[1]/Long.parseLong(lkHashChrLen.get(key));
		}
		return hashChrReadsNum;
	}
	/**
	 * 用dge的方法来获得基因表达量
	 * @param sort 是否需要排序
	 * 出错返回null
	 */
	public HashMap<String, Integer> getDGEnum(boolean sort) {
		BedSeq bedseq = null;
		if (sort) {
			bedseq = sortBedFile(FileOperate.changeFileSuffix(getSeqFile(), "_DGESort", null));
		}
		else {
			bedseq = this;
		}
		try {
			return bedseq.getGeneExpress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * @return
	 * 返回每个基因所对应的表达量， 用 int[1]只是为了地址引用。
	 * bed文件必须排序
	 * @throws Exception
	 */
	private HashMap<String, Integer> getGeneExpress() throws Exception
	{
		txtSeqFile = new TxtReadandWrite(compressInType, seqFile, false);
		BufferedReader reader = txtSeqFile.readfile();
		String content = "";
		HashMap<String, Integer> hashResult = new HashMap<String, Integer>();
		String oldLoc = ""; ArrayList<int[]> lsTmpExpValue = new ArrayList<int[]>();
		int[] tmpCount = new int[]{0}; int tmpLocEnd = -1;
		while ((content = reader.readLine()) != null) {
			if (content.contains("NM_018000")) {
				System.out.println("stop");
			}
			String[] ss = content.split("\t");
			//mapping到互补链上的，是假的信号
			if (ss[5].equals("-")) {
				continue;
			}
			if (!oldLoc.equals(ss[0]) && !oldLoc.equals("")) {
				hashResult.put(oldLoc, max(lsTmpExpValue));
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
	 * 无法设定compressType
	 * 将bed文件转化成DGE所需的信息，直接可以用DEseq分析的
	 */
	public static void dgeCal(String result, boolean sort, String... bedFile)
	{
		ArrayList<HashMap<String, Integer>> lsDGEvalue = new ArrayList<HashMap<String,Integer>>();
		for (String string : bedFile) {
			BedSeq bedSeq = new BedSeq(string);
			lsDGEvalue.add(bedSeq.getDGEnum(sort));
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
	
	
	
	
}
