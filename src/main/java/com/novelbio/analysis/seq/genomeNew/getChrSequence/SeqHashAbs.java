package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public abstract class SeqHashAbs implements SeqHashInt{
	private static Logger logger = Logger.getLogger(SeqHashAbs.class);
	/**
	 * ����chrID��chrLength�Ķ�Ӧ��ϵ
	 */
	LinkedHashMap<String, Long> hashChrLength = new LinkedHashMap<String, Long>();
	/**
	 * ��С��������chrLength��list
	 */
	ArrayList<String[]> lsChrLen = null;

	/**
	 * ��������չ�ϣ��ֵ Ŀǰ��A-T�� G-C��N-N �Ķ�Ӧ��ϵ�������˴�Сд�Ķ�Ӧ�� ��������Ҫ����µ�
	 */
	protected static HashMap<Character, Character> getCompmap() {
		return SeqFasta.getCompMap();
	}
	
	String regx = null;
	boolean append;
	
	String chrFile = "";
	/**
	 * �Ƿ���������ΪСд
	 */
	boolean CaseChange = true;
//	/**
//	 * @param chrFilePath
//	 */
//	public SeqHashAbs(String chrFile) 
//	{
//		this.chrFile = chrFile;
//	}
//	
//	/**
//	 * @param chrFile
//	 * @param regx ��������������ʽ��null���趨
//	 * @param TOLOWCASE �Ƿ����н����ΪСд True��Сд��False����д��null����
//	 */
//	public SeqHashAbs(String chrFile, String regx, Boolean TOLOWCASE) 
//	{
//		this.chrFile = chrFile;
//		this.regx = regx;
//		this.TOLOWCASE = TOLOWCASE;
//	}
	/**
	 * 
	 * @param chrFile
	 * @param regx ��������������ʽ��null��"   "�����趨
	 * @param CaseChange �Ƿ���������ΪСд
	 * @param TOLOWCASE �Ƿ����н����ΪСд True��Сд��False����д��null����
	 */
	public SeqHashAbs(String chrFile, String regx,boolean CaseChange) 
	{
		this.chrFile = chrFile;
		if (regx != null && !regx.trim().equals("")) {
			this.regx = regx;
		}
		this.CaseChange = CaseChange;
	}
	
	/**
	 * ���������ư�˳�����list
	 */
	public ArrayList<String> lsSeqName;
	/**
	 * ����������е�����
	 * @return
	 */
	public ArrayList<String> getLsSeqName() {
		return lsSeqName;
	}
	/**
	 * ����chrID��chrLength�Ķ�Ӧ��ϵ
	 * chrIDͨͨСд
	 * @return
	 */
	public LinkedHashMap<String, Long> getHashChrLength() {
		return hashChrLength;
	}
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻������chr�ĳ�����Ϣ
	 * 
	 * @param chrID
	 * @return ArrayList<String[]> 0: chrID 1: chr���� ���Ұ���chr���ȴ�С��������
	 */
	public ArrayList<String[]> getChrLengthInfo() {
		if (lsChrLen != null) {
			return lsChrLen;
		}
		lsChrLen = new ArrayList<String[]>();
		for (Entry<String, Long> entry : hashChrLength.entrySet()) {
			String[] tmpResult = new String[2];
			String chrID = entry.getKey();
			long lengthChrSeq = entry.getValue();
			tmpResult[0] = chrID;
			tmpResult[1] = lengthChrSeq + "";
			lsChrLen.add(tmpResult);
		}
		// //////////////////////////��lsChrLength����chrLen��С�����������/////////////////////////////////////////////////////////////////////////////
		Collections.sort(lsChrLen, new Comparator<String[]>() {
			public int compare(String[] arg0, String[] arg1) {
				if (Integer.parseInt(arg0[1]) < Integer.parseInt(arg1[1]))
					return -1;
				else if (Integer.parseInt(arg0[1]) == Integer.parseInt(arg1[1]))
					return 0;
				else
					return 1;
			}
		});
		// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		return lsChrLen;
	}
	
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��ÿ��chr�ĳ���
	 * @param chrID �ڲ��Զ�ת��ΪСд
	 * @return
	 */
	public long getChrLength(String chrID) 
	{
		return getHashChrLength().get(chrID.toLowerCase());
	}
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��ÿ��chr�ĳ���
	 * @param chrID �ڲ��Զ�ת��ΪСд
	 * @return
	 */
	public long getChrLenMin() 
	{
		return Integer.parseInt(getChrLengthInfo().get(0)[1]);
	}
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��ÿ��chr�ĳ���
	 * @param chrID �ڲ��Զ�ת��ΪСд
	 * @return
	 */
	public long getChrLenMax() 
	{
		return Integer.parseInt(getChrLengthInfo().get(getChrLengthInfo().size()-1)[1]);
	}
	/**
	 * ָ���Ⱦɫ���ֵ�����ذ�����ÿ��Ⱦɫ����Ӧֵ��Ⱦɫ�����������,resolution��int[resolution]�������ڻ�ͼ
	 * ��ôresolution���Ƿ��ص�int[]�ĳ���
	 * 
	 * @param chrID
	 * @param maxresolution
	 */
	public int[] getChrRes(String chrID, int maxresolution) throws Exception {
		ArrayList<String[]> chrLengthArrayList = getChrLengthInfo();
		int binLen = Integer.parseInt(chrLengthArrayList.get(chrLengthArrayList
				.size() - 1)[1]) / maxresolution;
		int resolution = (int) (hashChrLength.get(chrID) / binLen);

		Long chrLength = hashChrLength.get(chrID.toLowerCase());
		double binLength = (double) chrLength / resolution;
		int[] chrLengtharray = new int[resolution];
		for (int i = 0; i < resolution; i++) {
			chrLengtharray[i] = (int) ((i + 1) * binLength);
		}
		return chrLengtharray;
	}
	/**
	 * �����ȡ�ļ�
	 */
	protected void setFile()
	{
		try {
			setChrFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	protected abstract void setChrFile() throws Exception;
	
	/**
	 * ���趨Chr�ļ��󣬿��Խ����г���������ļ� ����ļ�Ϊ chrID(Сд)+��\t��+chrLength+���� ����˳�����
	 * 
	 * @param outFile
	 *            ��������ļ���������ȫ��·��
	 * @throws IOException
	 */
	public void saveChrLengthToFile(String outFile) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();// ��������
		for (Entry<String, Long> entry : hashChrLength.entrySet()) {
			String[] tmpResult = new String[2];
			tmpResult[0] = entry.getKey();
			tmpResult[1] = entry.getValue() + "";
			lsResult.add(tmpResult);
		}
		TxtReadandWrite txtChrLength = new TxtReadandWrite(outFile, true);
		try {
			txtChrLength.ExcelWrite(lsResult, "\t", 1, 1);
		} catch (Exception e) {
			logger.error("����ļ�����"+outFile);
			e.printStackTrace();
		}
	}
	protected abstract String getSeqInfo(String chrID, long startlocation, long endlocation) throws IOException;
	
	/**
	 * @param chrID Ⱦɫ���Ż�������
	 * @param startlocation ���
	 * @param endlocation �յ�
	 * @return �������У�����ͷ���null
	 */
	public String getSeq(String chrID, long startlocation, long endlocation)
	{
		try {
			return getSeqInfo(chrID, startlocation, endlocation);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * * ����Ⱦɫ��list��Ϣ �������������Լ��Ƿ�Ϊ���򻥲�,����ChrIDΪ chr1��chr2��chr10���� ��������
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * @param cisseq
	 *            ������
	 * @param chrID
	 *            Ŀ��Ⱦɫ�����ƣ������ڹ�ϣ���в��Ҿ���ĳ��Ⱦɫ��
	 * @param startlocation
	 *            �������
	 * @param endlocation
	 *            �����յ�
	 * @return
	 */
	public String getSeq(boolean cisseq, String chrID, long startlocation,
			long endlocation) {
		String sequence = getSeq(chrID, startlocation, endlocation);

		if (sequence == null) {
			return null;
		}
		if (cisseq ) {
			return sequence;
		} else {
			return SeqFasta.reservecom(sequence);
		}
	}

	/**
	 * ����peakλ�㣬����ָ����Χ��sequence������CaseChange�ı��Сд
	 * <br>
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * @param chr
	 *            ,
	 * @param peaklocation
	 *            peak summit������
	 * @param region
	 *            peak���ҵķ�Χ
	 * @param cisseq
	 *            true:������ false�����򻥲���
	 */
	public String getSeq(String chr, int peaklocation, int region,
			boolean cisseq) {
		if (CaseChange) {
			chr = chr.toLowerCase();
		}
		int startnum = peaklocation - region;
		int endnum = peaklocation + region;
		return getSeq(cisseq, chr, startnum, endnum);
	}

	/**
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * ����ת¼���ķ������Ǵӻ������5����3����ȡ��
	 * ������Ҫ�˹��趨cisseq
	 * @param cisseq �������Ƿ���Ҫ���򻥲���������Զ��5to3
	 * @param lsInfo ArrayList-int[] ������ת¼����ÿһ����һ��������
	 * @param getIntron �Ƿ���ȡ�ں�������True���ں���Сд�������Ӵ�д��False��ֻ��ȡ������
	 */
	private String getSeq(boolean cisseq, String chrID,List<int[]> lsInfo, String sep, boolean getIntron) {
		String myChrID = chrID;
		if (CaseChange) {
			myChrID = chrID.toLowerCase();
		}
		if (!hashChrLength.containsKey(myChrID)) {
			logger.error("û�и�Ⱦɫ�壺 "+chrID);
			return null;
		}
		
		String result = ""; boolean cis5to3 = true;
		int[] exon1 = lsInfo.get(0);
		if (exon1[0] > exon1[1] || (lsInfo.size() > 1 && lsInfo.get(0)[0] > lsInfo.get(1)[0]) ) {
			cis5to3 = false;
		}
		if (cis5to3) {
			for (int i = 0; i < lsInfo.size(); i++) {
				int[] exon = lsInfo.get(i);
				try {	
					result = result + sep + getSeq(chrID, exon[0], exon[1]).toUpperCase(); 
					if (getIntron && i < lsInfo.size()-1) {
						result = result + sep + getSeq(chrID,exon[1]+1, lsInfo.get(i+1)[0]-1).toLowerCase();
					}
				} catch (Exception e) {e.printStackTrace();}
			}
		}
		else {
			for (int i = lsInfo.size() - 1; i >= 0; i--) {
				int[] exon = lsInfo.get(i);
				try {	
					result = result + sep + getSeq(chrID, exon[1], exon[0]).toUpperCase();
					if (getIntron && i > 0) {
						result = result + sep + getSeq(chrID,exon[0] + 1, lsInfo.get(i-1)[1] - 1).toLowerCase();;
					}
				} catch (Exception e) {e.printStackTrace();}
			}
		}
		result = result.substring(sep.length());
		if (!cisseq) {
			result = SeqFasta.reservecom(result);
		}
		return result;
	}
	String sep = "";
	/**
	 * ������֮����ʲô���ָĬ��Ϊ""
	 * @param sep
	 */
	@Override
	public void setSep(String sep) {
		this.sep = sep;
	}
	/**
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * ����ת¼���ķ������Ǵӻ������5����3����ȡ��
	 * ������Ҫ�˹��趨cisseq
	 * @param cisseq �������Ƿ���Ҫ���򻥲���������Զ��5to3��
	 * @param lsInfo ArrayList-int[] ������ת¼����ÿһ����һ��������
	 * @param getIntron �Ƿ���ȡ�ں�������True���ں���Сд�������Ӵ�д��False��ֻ��ȡ������
	 */
	public String getSeq(boolean cisseq, String chrID,List<int[]> lsInfo, boolean getIntron) {
		return getSeq(cisseq, chrID, lsInfo, sep, getIntron);
	}
	
	/**
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����<br>
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param lsInfo ArrayList-int[] ������ת¼����ÿһ����һ��������
	 * @param getIntron �Ƿ���ȡ�ں�������True���ں���Сд�������Ӵ�д��False��ֻ��ȡ������
	 */
	@Override
	public String getSeq(String chrID,List<int[]> lsInfo, boolean getIntron) {
		 boolean cis5to3 = true;
		 int[] exon1 = lsInfo.get(0);
		 if (exon1[0] > exon1[1] || (lsInfo.size() > 1 && lsInfo.get(0)[0] > lsInfo.get(1)[0]) ) {
			 cis5to3 = false;
		 }
		 return this.getSeq(cis5to3, chrID, lsInfo, getIntron);
	}
	
	/**
	 * ����git
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����<br>
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * �������ʼexon����ֹexon�����������յ㣩��exon list
	 * @param chrID
	 * @param cisseq �����������������ת¼���Ļ����ϣ��Ƿ���Ҫ���򻥲���
	 * @param lsInfo ArrayList-int[] ������ת¼����ÿһ����һ��������
	 * @param getIntron �Ƿ���ȡ�ں�������True���ں���Сд�������Ӵ�д��False��ֻ��ȡ������
	 * @param cisseq ������
	 * @param start ʵ�ʵڼ���exon ������С�ڵ����յ�
	 * @param end ʵ�ʵڼ���axon
	 * @param lsInfo
	 * @param getIntron �Ƿ��ȡ�ں��ӣ��ں����Զ�Сд
	 * @return
	 */
	@Override
	public String getSeq(String chrID,boolean cisseq, int start, int end, List<int[]> lsInfo, boolean getIntron) {
		start--;
		if (start < 0) {
			start = 0;
		}
		if (end <= 0 || end > lsInfo.size()) {
			end = lsInfo.size();
		}
		boolean cis5to3 = true;
		int[] exon1 = lsInfo.get(0);
		if (exon1[0] > exon1[1]) {
			cis5to3 = false;
		}
		List<int[]> lsExon = lsInfo.subList(start, end);
		String seq = getSeq(cis5to3, chrID, lsExon, getIntron);
		if (cisseq) {
			return seq;
		}
		else {
			return SeqFasta.reservecom(seq);
		}
	}
	
	
	/**
	 * ��˳����ȡ���������У�ÿһ�����α���Ϊһ��SeqFasta����
	 * SeqFasta������ΪchrID:�������-�յ����� ���Ǳ�����
	 * @param chrID ����ID
	 * @param lsInfo ���������
	 * @return
	 */
	public ArrayList<SeqFasta> getRegionSeqFasta(List<LocInfo> lsLocInfos) {
		ArrayList<SeqFasta> lsSeqfasta = new ArrayList<SeqFasta>();
		for (LocInfo locInfo : lsLocInfos) {
			String myChrID = locInfo.getChrID();
			if (CaseChange) {
				myChrID = myChrID.toLowerCase();
			}
			if (!hashChrLength.containsKey(myChrID)) {
				logger.error("û�и�Ⱦɫ�壺 "+ locInfo.getChrID());
				return null;
			}
			SeqFasta seqFasta = new SeqFasta(locInfo.getChrID()+":"+locInfo.getStartLoc()+"-"+ locInfo.getEndLoc(),
					getSeq(myChrID, locInfo.getStartLoc(),
							locInfo.getEndLoc()), locInfo.isCis5to3());
			lsSeqfasta.add(seqFasta);
		}
		return lsSeqfasta;
	}
	
	
}
