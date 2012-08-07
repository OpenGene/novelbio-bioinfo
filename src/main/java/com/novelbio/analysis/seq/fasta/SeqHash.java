package com.novelbio.analysis.seq.fasta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.SiteInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class SeqHash implements SeqHashInt{
private static Logger logger = Logger.getLogger(SeqHash.class);
//	String chrFile = "";
	SeqHashAbs seqHashAbs = null;
	/**
	 * �����ļ��ͷ��أ���Ϊһ���Ķ���
	 * @param chrFile �����ļ��������ļ���
	 * @param regx ����������ļ������ø�������ʽ��ȡÿ�����е����֣�����������ļ��У����ø�������ʽ��ȡ���и��ļ������ļ�
	 * ���ļ�Ĭ��Ϊ"";�ļ���Ĭ��Ϊ"\\bchr\\w*"��
	 * @param TOLOWCASE �Ƿ������������ת��ΪСд True��Сд��False����д��null���� Ĭ��Ϊnull
	 * Ĭ��Ϊfalse
	 */
	public SeqHash(String chrFile) {
		if (FileOperate.isFileExistAndBigThanSize(chrFile,1)) {
			seqHashAbs =new SeqFastaHash(chrFile, "", true, false);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, "\\bchr\\w*", true);
		}
	}
	/**
	 * �����ļ��� 
	 * @return
	 */
	public String getChrFile() {
		return seqHashAbs.getChrFile();
	}
	/**
	 * @param chrFile �����ļ��������ļ���
	 * @param regx ����������ļ������ø�������ʽ��ȡÿ�����е����֣�����������ļ��У����ø�������ʽ��ȡ���и��ļ������ļ�
	 * ���ļ�Ĭ��Ϊ"";�ļ���Ĭ��Ϊ"\\bchr\\w*"��
	 * @param TOLOWCASE �Ƿ������������ת��ΪСд True��Сд��False����д��null���� Ĭ��Ϊnull
	 * Ĭ��Ϊfalse
	 */
	public SeqHash(String chrFile, String regx) {
		if (FileOperate.isFileExistAndBigThanSize(chrFile,1)) {
			seqHashAbs =new SeqFastaHash(chrFile, regx, true, false);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, regx, true);
		}
	}
	
	/**
	 * @param chrFile �����ļ��������ļ���
	 * @param regx ����������ļ������ø�������ʽ��ȡÿ�����е����֣�����������ļ��У����ø�������ʽ��ȡ���и��ļ������ļ�
	 * ���ļ�Ĭ��Ϊ"";�ļ���Ĭ��Ϊ"\\bchr\\w*"��
	 * @param TOLOWCASE �Ƿ������������ת��ΪСд True��Сд��False����д��null���� Ĭ��Ϊnull
	 * Ĭ��Ϊfalse
	 */
	public SeqHash(String chrFile, String regx,Boolean TOLOWCASE)
	{
		if (FileOperate.isFileExistAndBigThanSize(chrFile,1)) {
			seqHashAbs =new SeqFastaHash(chrFile, regx, true, false);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, regx, true);
		}
		this.TOLOWCASE = TOLOWCASE;
	}
	
	/**
	 * @param chrFile �����ļ��������ļ���
	 * @param regx ����������ļ������ø�������ʽ��ȡÿ�����е����֣�����������ļ��У����ø�������ʽ��ȡ���и��ļ������ļ�
	 * ���ļ�Ĭ��Ϊ"";�ļ���Ĭ��Ϊ"\\bchr\\w*"��
	 * @param CaseChange �Ƿ�������Сд Ĭ��Сд
	 * @param TOLOWCASE �Ƿ������������ת��ΪСд True��Сд��False����д��null���� Ĭ��Ϊnull
	 * @param append <b>����Ե����ı�������</b>������ͬ�������еĴ���
	 * true����������������У����ڵڶ������ֺ����"<"��Ϊ��� false����������������У����ó�������ȥ�滻�̵�����
	 * Ĭ��Ϊfalse
	 */
	public SeqHash(String chrFile, String regx, boolean CaseChange,Boolean TOLOWCASE, boolean append)
	{
		if (FileOperate.isFileExistAndBigThanSize(chrFile,1)) {
			seqHashAbs =new SeqFastaHash(chrFile, regx, CaseChange, append);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, regx, CaseChange);
		}
		this.TOLOWCASE = TOLOWCASE;
	}
	

	/**
	 * ������ļ��Ƿ�ת��Ϊ��Сд
	 * True��Сд
	 * False����д
	 * null������
	 */
	Boolean TOLOWCASE = null;
	@Override
	public LinkedHashMap<String, Long> getHashChrLength() {
		return seqHashAbs.getHashChrLength();
	}
	
	@Override
	public ArrayList<String[]> getChrLengthInfo() {
		return seqHashAbs.getChrLengthInfo();
	}

	@Override
	public long getChrLength(String chrID) {
		return seqHashAbs.getChrLength(chrID);
	}

	@Override
	public long getChrLenMin() {
		return seqHashAbs.getChrLenMin();
	}

	@Override
	public long getChrLenMax() {
		return seqHashAbs.getChrLenMax();
	}

	@Override
	public int[] getChrRes(String chrID, int maxresolution) throws Exception {
		return seqHashAbs.getChrRes(chrID, maxresolution);
	}

	@Override
	public void saveChrLengthToFile(String outFile) {
		seqHashAbs.saveChrLengthToFile(outFile);
		
	}
	@Override
	public ArrayList<String> getLsSeqName() {
		return seqHashAbs.getLsSeqName();
	}
	/////////////////////  �� ȡ �� ��  /////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public SeqFasta getSeq(String chrID, long startlocation, long endlocation)
	{
		SeqFasta seqFasta = seqHashAbs.getSeq(chrID, startlocation, endlocation);
		if (seqFasta != null) {
			seqFasta.setTOLOWCASE(TOLOWCASE);
		}
		return seqFasta;
	}

	@Override
	public SeqFasta getSeq(Boolean cisseq, String chrID, long startlocation,
			long endlocation) {
		SeqFasta seqFasta = seqHashAbs.getSeq(cisseq, chrID, startlocation, endlocation);
		if (seqFasta != null) {
			seqFasta.setTOLOWCASE(TOLOWCASE);
		}
		return seqFasta;
	}

	@Override
	public SeqFasta getSeq(String chr, int peaklocation, int region,
			boolean cisseq) {
		SeqFasta seqFasta = seqHashAbs.getSeq(chr, peaklocation, region, cisseq);
		if (seqFasta != null) {
			seqFasta.setTOLOWCASE(TOLOWCASE);
		}
		return seqFasta;
	}
	/**
	 * ���ݸ�����mapInfo��������У�ע�����в�û�и���cis5to3���з���
	 * �Զ���seqfasta�������
	 * @param mapInfo
	 */
	@Override
	public void getSeq(SiteInfo mapInfo) {
		seqHashAbs.getSeq(mapInfo);
	}
	/**
	 * ���ݸ�����mapInfo��������У�ע�����в�û�и���cis5to3���з���ֻ�Ǳ����cis5to3
	 * @param mapinfoRefSeqIntactAA
	 */
	public void getSeq(ArrayList<? extends SiteInfo> lsMapInfos) {
		for (SiteInfo mapInfo : lsMapInfos) {
			getSeq(mapInfo);
		}
	}

	public SeqFasta getSeq(GffGeneIsoInfo gffGeneIsoInfo, boolean getIntron) {
		SeqFasta seqFasta = seqHashAbs.getSeq(gffGeneIsoInfo, getIntron);
		if (seqFasta != null) {
			seqFasta.setTOLOWCASE(TOLOWCASE);
		}
		return seqFasta;
	}
	@Override
	public SeqFasta getSeq(String chrID, List<ExonInfo> lsInfo, boolean getIntron) {
		SeqFasta seqFasta = seqHashAbs.getSeq(chrID, lsInfo, getIntron);
		if (seqFasta != null) {
			seqFasta.setTOLOWCASE(TOLOWCASE);
		}
		return seqFasta;
	}
	@Override
	public SeqFasta getSeq(String chrID, int start, int end, List<ExonInfo> lsInfo, boolean getIntron) {
		SeqFasta seqFasta = seqHashAbs.getSeq(chrID, start, end, lsInfo, getIntron);
		if (seqFasta != null) {
			seqFasta.setTOLOWCASE(TOLOWCASE);
		}
		return seqFasta;
	}
	@Override
	public void setSep(String sep) {
		seqHashAbs.setSep(sep);
		
	}
	@Override
	public void setDNAseq(boolean isDNAseq) {
		seqHashAbs.setDNAseq(isDNAseq);
	}
	//////////////////////  static method  ////////////////////////////////////////////////////////////////////////////////}

	/**
	 * �ж�����ĳ����Ƿ���Ŀ�������ڣ�������
	 * @param seqlen �������еĳ���
	 * @param len
	 * 	int[2] :0�����ޣ�С��0��ʾû������
	 * 1�����ޣ�С��0��ʾû������
	 * ���ޱ�����ڵ������ޣ��������С�����ޣ��򱨴�
	 * @return
	 */
	public static boolean testSeqLen(int seqlen, int[] len) {
		if (len[1] > 0 && len[1] < len[0]) {
			logger.error("Ҫ��������еĳ������޲���С������");
		}
		
		if (len[0] <= 0) { //������
			if (len[1] <= 0)  //������
				return true;
			else { //������
				if (seqlen <= len[1])  //����С�ڵ�������
					return true;
				else
					// ���ȴ�������
					return false;
			}
		}
		else // ������
		{
			if (seqlen < len[0]) //����С������
				return false;
			else {  //���ȴ�������
				if (len[1] > 0) { //������
					if (seqlen <= len[1]) //����С������
						return true;
					else
						return false;
				}
				else {
					return true;
				}
			}
		}
	}
	/**
	 * ����fasta�ļ��������fasta�ļ�����ֻ��һ�����У����׻���ᣬ�жϸ�fasta�ļ��ǵ��ף�����DNA������RNA
	 * @return
	 * SeqFasta.SEQ_DNA��
	 */
	public static int getSeqType(String fastaFile)
	{
		int readBp = 1000;//��ȡǰ1000��������ж�
		TxtReadandWrite txtRead = new TxtReadandWrite(fastaFile, false);
		StringBuilder stringBuilder = new StringBuilder();
		for (String string : txtRead.readlines()) {
			if (string.trim().startsWith(">")) {
				continue;
			}
			stringBuilder.append(string);
			if (stringBuilder.length() > readBp) {
				break;
			}
		}
		SeqFasta seqFasta = new SeqFasta("test", stringBuilder.toString());
		return seqFasta.getSeqType();		
	}
	

	
}
