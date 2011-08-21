package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.getChrSequence.ChrStringHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public abstract class SeqHash {
	private static Logger logger = Logger.getLogger(SeqHash.class);
	
	String Chrpatten = "Chr\\w+";
	/**
	 * ����chrID��chrLength�Ķ�Ӧ��ϵ
	 */
	HashMap<String, Long> hashChrLength = new HashMap<String, Long>();
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
	
	boolean CaseChange;
	String regx = "";
	boolean append;
	
	String chrFile = "";
	
	
	/**
	 * �趨������Ϣ
	 * @param CaseChange �Ƿ�������ת��ΪСд
	 * @param regx ��������������ʽ���ڶ�ȡChromFa�ļ���ʱʹ�ã�����ץȡ�ļ����е����������ļ���null���趨
	 * ��ȡChr�ļ��е�ʱ��Ĭ���趨�� "\\bchr\\w*"
	 * @param append ��ȡChrID��ʱ��û��
	 * @param chrPattern ����������chr1:1123-4567����ʱ��chr1��ȡ������������ʽ
	 */
	public void setInfo(boolean CaseChange, String regx,boolean append, String chrPattern) {
		this.CaseChange = CaseChange;
		if (regx != null) {
			this.regx = regx;
		}
		this.append = append;
		this.Chrpatten = chrPattern;
	}
	
	/**
	 * @param chrFilePath
	 * @throws Exception 
	 * @throws IOException
	 */
	public SeqHash(String chrFile) 
	{
		this.chrFile = chrFile;
	}
	
	/**
	 * @param chrFilePath
	 * @param regx ��������������ʽ��null���趨
	 * @throws Exception 
	 * @throws IOException
	 */
	public SeqHash(String chrFile, String regx) 
	{
		this.chrFile = chrFile;
		this.regx = regx;
	}
	
	/**
	 * ����chrID��chrLength�Ķ�Ӧ��ϵ
	 * chrIDͨͨСд
	 * @return
	 */
	public HashMap<String, Long> getHashChrLength() {
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
//		return getChrLengthInfo().get(0);
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
	
	public void setFile()
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
		TxtReadandWrite txtChrLength = new TxtReadandWrite();
		txtChrLength.setParameter(outFile, true, false);
		try {
			txtChrLength.ExcelWrite(lsResult, "\t", 1, 1);
		} catch (Exception e) {
			logger.error("����ļ�����"+outFile);
			e.printStackTrace();
		}
	}
	public abstract String getSeq(String chrID, long startlocation, long endlocation) throws IOException ;
	
	
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
		String sequence = null;
		try {
			sequence = getSeq(chrID, startlocation, endlocation);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (sequence == null) {
			return null;
		}
		if (cisseq ) {
			return sequence;
		} else {
			return resCompSeq(sequence, getCompmap());
		}
	}

	/**
	 * ����Ⱦɫ����λ�úͷ��򷵻�����<br>
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * @param chrlocationȾɫ���ŷ�����
	 *            ��Chr:1000-2000,�Զ���chrIDСд,chrID����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд
	 * @param cisseq����
	 *            ��true:���� false:���򻥲�
	 */
	public String getSeq(String chrlocation, boolean cisseq) {
		/**
		 * �ж�Chr��ʽ�Ƿ���ȷ���Ƿ�����Ч��Ⱦɫ��
		 */
		Pattern pattern = Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE);
		Matcher matcher;
		matcher = pattern.matcher(chrlocation);
		if (!matcher.find()) {
			logger.error("ReadSiteȾɫ���ʽ����"+ chrlocation);
			return null;
		}
		String chr = matcher.group();

		/**
		 * ��ȡ��ʼλ�����ֹλ��
		 */
		Pattern patternnumber = Pattern.compile("(?<!\\w)\\d+(?!\\w)",
				Pattern.CASE_INSENSITIVE);
		Matcher matchernumber;
		matchernumber = patternnumber.matcher(chrlocation);
		int[] location = new int[2];
		int i = 0;
		while (matchernumber.find()) {
			location[i] = Integer.parseInt(matchernumber.group());
			i++;
		}
		if (i > 2 || location[1] <= location[0]) {
			logger.error(chrlocation + " " + cisseq + " Ⱦɫ��λ�ô���");
			return null;
		}
		return getSeq(cisseq, chr.toLowerCase(), location[0], location[1]);
	}

	/**
	 * ����peakλ�㣬����ָ����Χ��sequence,chr����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд
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
		/**
		 * �ж�Chr��ʽ�Ƿ���ȷ���Ƿ�����Ч��Ⱦɫ��
		 */
		Pattern pattern = Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE);
		Matcher matcher; // matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
		matcher = pattern.matcher(chr);
		if (!matcher.find()) {
			logger.error(chr + " " + peaklocation + " " + region + " Ⱦɫ���ʽ����");
			return "ReadSiteȾɫ���ʽ����";
		} else {
			chr = matcher.group().toLowerCase();
		}
		int startnum = peaklocation - region;
		int endnum = peaklocation + region;
		return getSeq(cisseq, chr, startnum, endnum);
	}

	/**
	 * �������У��������ձ� ��÷��򻥲�����
	 */
	public String resCompSeq(String sequence,
			HashMap<Character, Character> complementmap) {
		StringBuilder recomseq = new StringBuilder();
		int length = sequence.length();
		Character base;
		for (int i = length - 1; i >= 0; i--) {
			base = complementmap.get(sequence.charAt(i));
			if (base != null) {
				recomseq.append(complementmap.get(sequence.charAt(i)));
			} else {
				logger.error(sequence + " ����δ֪���");

				return "����δ֪��� " + sequence.charAt(i);
			}
		}
		return recomseq.toString();
	}
	/**
	 * <br>
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * @param cisseq ������
	 * @param lsInfo ArrayList-int[] ������ת¼����ÿһ����һ��������
	 * @param getIntron �Ƿ���ȡ�ں�������True���ں���Сд�������Ӵ�д��False��ֻ��ȡ������
	 */
	public String getSeq(boolean cisseq, String chrID,ArrayList<int[]> lsInfo, boolean getIntron) {
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
		if (exon1[0] > exon1[1]) {
			cis5to3 = false;
		}
		if (cis5to3) {
			for (int i = 0; i < lsInfo.size(); i++) {
				int[] exon = lsInfo.get(i);
				try {	
					result = result + getSeq(chrID, exon[0], exon[1]).toUpperCase(); 
					if (getIntron && i < lsInfo.size()-1) {
						result = result + getSeq(chrID,exon[1]+1, lsInfo.get(i+1)[0]-1).toLowerCase();
					}
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		else {
			for (int i = lsInfo.size() - 1; i >= 0; i--) {
				int[] exon = lsInfo.get(i);
				try {	
					result = result + getSeq(chrID, exon[1], exon[0]).toUpperCase(); 
					if (getIntron && i > 0) {
						result = result + getSeq(chrID,exon[0] + 1, lsInfo.get(i-1)[1] - 1).toLowerCase();
					}
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		if (!cisseq) {
			result = resCompSeq(result, getCompmap());
		}
		return result;
	}
	
}
