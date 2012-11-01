package com.novelbio.analysis.seq.fasta;

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

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public abstract class SeqHashAbs implements SeqHashInt{
	private static Logger logger = Logger.getLogger(SeqHashAbs.class);
	/** ����chrID��chrLength�Ķ�Ӧ��ϵ
	 * keyСд
	 *  */
	LinkedHashMap<String, Long> hashChrLength = new LinkedHashMap<String, Long>();
	/** ��С��������chrLength��list */
	ArrayList<String[]> lsChrLen = null;
	/** �Ƿ�Ҫ�趨ΪDNA��Ҳ���ǽ������е�Uȫ��ת��ΪT */
	boolean isDNAseq = false;
	/** ץȡchrID��������ʽ */
	String regx = null;	
	String chrFile = "";
	/** ���������ư�˳�����list */
	public ArrayList<String> lsSeqName;
	/** ������֮����ʲô���ָĬ��Ϊ"" */
	String sep = "";
	
	/**
	 * @param chrFile
	 * @param regx ��������������ʽ��null��"   "�����趨
	 * @param TOLOWCASE �Ƿ����н����ΪСд True��Сд��False����д��null����
	 */
	public SeqHashAbs(String chrFile, String regx) {
		this.chrFile = chrFile;
		if (regx != null && !regx.trim().equals("")) {
			this.regx = regx;
		}
	}
	public String getChrFile() {
		return chrFile;
	}
	/**
	 * �Ƿ�Ҫ�趨ΪDNA��Ҳ���ǽ������е�Uȫ��ת��ΪT
	 * ֻ�е�����ΪRNAʱ�Ż��õ�
	 */
	@Override
	public void setDNAseq(boolean isDNAseq){
		this.isDNAseq = isDNAseq;
	}
	/**
	 * ������֮����ʲô���ָĬ��Ϊ""
	 * @param sep
	 */
	@Override
	public void setSep(String sep) {
		this.sep = sep;
	}
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
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��ÿ��chr�ĳ���
	 * @param chrID �ڲ��Զ�ת��ΪСд
	 * @return
	 */
	public long getChrLength(String chrID) {
		return getHashChrLength().get(chrID.toLowerCase());
	}
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��ÿ��chr�ĳ���
	 * @param refID �ڲ��Զ�ת��ΪСд
	 * @return
	 */
	public long getChrLenMin() {
		return Integer.parseInt(getChrLengthInfo().get(0)[1]);
	}
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��ÿ��chr�ĳ���
	 * @param refID �ڲ��Զ�ת��ΪСд
	 * @return
	 */
	public long getChrLenMax() {
		return Integer.parseInt(getChrLengthInfo().get(getChrLengthInfo().size()-1)[1]);
	}
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻������chr�ĳ�����Ϣ
	 * 
	 * @param refID
	 * @return ArrayList<String[]> 0: chrID 1: chr���� ���Ұ���chr���ȴ�С��������
	 */
	public ArrayList<String[]> getChrLengthInfo() {
		if (lsChrLen != null) {
			return lsChrLen;
		}
		lsChrLen = new ArrayList<String[]>();
		for (Entry<String, Long> entry : hashChrLength.entrySet()) {
			String[] tmpResult = new String[2];
			tmpResult[0] = entry.getKey();
			tmpResult[1] = entry.getValue() + "";
			lsChrLen.add(tmpResult);
		}
		//��lsChrLength����chrLen��С�����������
		Collections.sort(lsChrLen, new Comparator<String[]>() {
			public int compare(String[] arg0, String[] arg1) {
				Integer a1 = Integer.parseInt(arg0[1]);
				Integer a2 = Integer.parseInt(arg1[1]);
				return a1.compareTo(a2);
			}
		});
		return lsChrLen;
	}
	/**
	 * ָ���Ⱦɫ���ֵ�����ذ�����ÿ��Ⱦɫ����Ӧֵ��Ⱦɫ�����������,resolution��int[resolution]�������ڻ�ͼ
	 * ��ôresolution���Ƿ��ص�int[]�ĳ���
	 * @param chrID
	 * @param maxresolution
	 */
	public int[] getChrRes(String chrID, int maxresolution) throws Exception {
		chrID = chrID.toLowerCase();
		ArrayList<String[]> chrLengthArrayList = getChrLengthInfo();
		int binLen = Integer.parseInt(chrLengthArrayList.get(chrLengthArrayList.size() - 1)[1]) / maxresolution;
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
	protected void setFile() {
		try {
			setChrFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * ��ȡ����
	 * @throws Exception
	 */
	protected abstract void setChrFile() throws Exception;
	/**
	 * ���趨Chr�ļ��󣬿��Խ����г���������ļ� ����ļ�Ϊ chrID(Сд)+��\t��+chrLength+���� ����˳�����
	 * @param outFile ��������ļ���������ȫ��·��
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
			txtChrLength.ExcelWrite(lsResult);
		} catch (Exception e) {
			logger.error("����ļ�����"+outFile);
			e.printStackTrace();
		}
	}
	/**
	 * @param chrID Ⱦɫ���Ż�������
	 * @param startlocation ���
	 * @param endlocation �յ�
	 * @return �������У�����ͷ���null
	 */
	public SeqFasta getSeq(String chrID, long startlocation, long endlocation) {
		chrID = chrID.toLowerCase();
		SeqFasta seqFasta = getSeqInfo(chrID, startlocation, endlocation);
		if (seqFasta == null) {
			logger.error("��ȡ����"+chrID + " " + startlocation + "_" + endlocation);
			return null;
		}
		seqFasta.setDNA(isDNAseq);
		return seqFasta;
	}
	/** ��ȡ���� */
	protected abstract SeqFasta getSeqInfo(String chrID, long startlocation, long endlocation);
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
	public SeqFasta getSeq(Boolean cisseq, String chrID, long startlocation, long endlocation) {
		SeqFasta seqFasta = getSeq(chrID, startlocation, endlocation);
		if (seqFasta == null) return null;
		if (cisseq == null) cisseq = true;
		
		if (cisseq )
			return seqFasta;
		else
			return seqFasta.reservecom();
	}
	/**
	 * ����peakλ�㣬����ָ����Χ��sequence������CaseChange�ı��Сд
	 * <br>
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * @param chr
	 * @param peaklocation peak summit������
	 * @param region peak���ҵķ�Χ
	 * @param cisseq true:������ false�����򻥲���
	 */
	public SeqFasta getSeq(String chr, int peaklocation, int region, boolean cisseq) {
		int startnum = peaklocation - region;
		int endnum = peaklocation + region;
		return getSeq(cisseq, chr, startnum, endnum);
	}
	/**
	 * seqname = chrID_��һ�������ӵ����_��һ�������ӵ��յ�
	 * ��ȫ����gffgeneinfo��õ�����
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * ����ת¼���ķ������Ǵӻ������5����3����ȡ��
	 * ������Ҫ�˹��趨cisseq
	 * @param cisseq �������Ƿ���Ҫ���򻥲���������Զ��5to3
	 * @param chrID ����ν��Сд
	 * @param lsInfo ArrayList-int[] ������ת¼����ÿһ����һ��������
	 * @param getIntron �Ƿ���ȡ�ں�������True���ں���Сд�������Ӵ�д��False��ֻ��ȡ������
	 */
	private SeqFasta getSeq(boolean cisseq, String chrID, List<ExonInfo> lsInfo, String sep, boolean getIntron) {
		SeqFasta seqFasta = new SeqFasta();
		seqFasta.setName(chrID + "_" + lsInfo.get(0).getName() + "_");
		String myChrID = chrID.toLowerCase();
		
		if (!hashChrLength.containsKey(myChrID)) {
			logger.error("û�и�Ⱦɫ�壺 "+chrID);
			return null;
		}
		
		String result = "";
		ExonInfo exon1 = lsInfo.get(0);
		if (exon1.isCis5to3()) {
			for (int i = 0; i < lsInfo.size(); i++) {
				ExonInfo exon = lsInfo.get(i);
				result = result + sep + getSeq(myChrID, exon.getStartAbs(), exon.getEndAbs()).toString().toUpperCase(); 
				if (getIntron && i < lsInfo.size()-1) {
					result = result + sep + getSeq(myChrID,exon.getEndCis()+1, lsInfo.get(i+1).getStartCis()-1).toString().toLowerCase();
				}
			}
		}
		else {
			for (int i = lsInfo.size() - 1; i >= 0; i--) {
				ExonInfo exon = lsInfo.get(i);
				try {	
					result = result + sep + getSeq(myChrID, exon.getStartAbs(), exon.getEndAbs()).toString().toUpperCase();
					if (getIntron && i > 0) {
						result = result + sep + getSeq(myChrID,exon.getStartCis() + 1, lsInfo.get(i-1).getEndCis() - 1).toString().toLowerCase();;
					}
				} catch (Exception e) {e.printStackTrace();}
			}
		}
		result = result.substring(sep.length());
		seqFasta.setSeq(result);
		if (cisseq) {
			return seqFasta;
		}
		else {
			return seqFasta.reservecom();
		}
	}
	/**
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����<br>
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param lsInfo ArrayList-int[] ������ת¼����ÿһ����һ��������
	 * @param getIntron �Ƿ���ȡ�ں�������True���ں���Сд�������Ӵ�д��False��ֻ��ȡ������
	 */
	public SeqFasta getSeq(GffGeneIsoInfo gffGeneIsoInfo, boolean getIntron) {
		 return getSeq(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, getIntron);
	}
	/**
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����<br>
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param chrID Ⱦɫ��
	 * @param lsInfo ArrayList-int[] ������ת¼����ÿһ����һ��������
	 * @param getIntron �Ƿ���ȡ�ں�������True���ں���Сд�������Ӵ�д��False��ֻ��ȡ������
	 */
	@Override
	public SeqFasta getSeq(String chrID,List<ExonInfo> lsInfo, boolean getIntron) {
		 ExonInfo exon1 = lsInfo.get(0);
		 try {
			 return this.getSeq(exon1.isCis5to3(), chrID, lsInfo, sep,getIntron);
		} catch (Exception e) {
			return null;
		}
	}
	/**
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
	public SeqFasta getSeq(String chrID, int start, int end, List<ExonInfo> lsInfo, boolean getIntron) {
		start--;
		if (start < 0) start = 0;
		
		if (end <= 0 || end > lsInfo.size()) 
			end = lsInfo.size();
		
		ExonInfo exon1 = lsInfo.get(0);
		List<ExonInfo> lsExon = lsInfo.subList(start, end);
		SeqFasta seq = getSeq(exon1.isCis5to3(), chrID, lsExon,sep, getIntron);
		return seq;
	}
	/**
	 * ��˳����ȡ���������У�ÿһ�����α���Ϊһ��SeqFasta����
	 * SeqFasta������ΪchrID:�������-�յ����� ���Ǳ�����
	 * @param refID ����ID
	 * @param lsInfo ���������
	 * @return
	 */
	public ArrayList<SeqFasta> getRegionSeqFasta(List<LocInfo> lsLocInfos) {
		ArrayList<SeqFasta> lsSeqfasta = new ArrayList<SeqFasta>();
		for (LocInfo locInfo : lsLocInfos) {
			String myChrID = locInfo.getChrID();
			myChrID = myChrID.toLowerCase();
			
			if (!hashChrLength.containsKey(myChrID)) {
				logger.error("û�и�Ⱦɫ�壺 "+ locInfo.getChrID());
				return null;
			}
			SeqFasta seqFasta = getSeq(myChrID, locInfo.getStartLoc(), locInfo.getEndLoc(), locInfo.isCis5to3());
			lsSeqfasta.add(seqFasta);
		}
		return lsSeqfasta;
	}
	@Override
	public void getSeq(SiteInfo siteInfo) {
		SeqFasta seqFasta = getSeq(siteInfo.getRefID(), siteInfo.getStartAbs(), siteInfo.getEndAbs());
		siteInfo.setSeq(seqFasta, true);
	}
}
