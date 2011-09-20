package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class SeqHash implements SeqHashInt{
private static Logger logger = Logger.getLogger(SeqHash.class);
	/**
	 * @param chrFile �����ļ��������ļ���
	 * @param regx ����������ļ������ø�������ʽ��ȡÿ�����е����֣�����������ļ��У����ø�������ʽ��ȡ���и��ļ������ļ�
	 * ���ļ�Ĭ��Ϊ"";�ļ���Ĭ��Ϊ"\\bchr\\w*"��
	 * @param TOLOWCASE �Ƿ������������ת��ΪСд True��Сд��False����д��null���� Ĭ��Ϊnull
	 * Ĭ��Ϊfalse
	 */
	public SeqHash(String chrFile)
	{
		if (FileOperate.isFile(chrFile)) {
			seqHashAbs =new SeqFastaHash(chrFile, "", true, false);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, "\\bchr\\w*", true);
		}
	}
	
	/**
	 * @param chrFile �����ļ��������ļ���
	 * @param regx ����������ļ������ø�������ʽ��ȡÿ�����е����֣�����������ļ��У����ø�������ʽ��ȡ���и��ļ������ļ�
	 * ���ļ�Ĭ��Ϊ"";�ļ���Ĭ��Ϊ"\\bchr\\w*"��
	 * @param TOLOWCASE �Ƿ������������ת��ΪСд True��Сд��False����д��null���� Ĭ��Ϊnull
	 * Ĭ��Ϊfalse
	 */
	public SeqHash(String chrFile, String regx)
	{
		if (FileOperate.isFile(chrFile)) {
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
		if (FileOperate.isFile(chrFile)) {
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
		if (FileOperate.isFile(chrFile)) {
			seqHashAbs =new SeqFastaHash(chrFile, regx, CaseChange, append);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, regx, CaseChange);
		}
		this.TOLOWCASE = TOLOWCASE;
	}
	
	SeqHashAbs seqHashAbs = null;
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
		seqHashAbs.getChrLengthInfo();
		return null;
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
	public String getSeq(String chrID, long startlocation, long endlocation)
			throws IOException {
		return getSeqCase(seqHashAbs.getSeq(chrID, startlocation, endlocation),TOLOWCASE);
	}

	@Override
	public String getSeq(boolean cisseq, String chrID, long startlocation,
			long endlocation) {
		return getSeqCase(seqHashAbs.getSeq(cisseq, chrID, startlocation, endlocation),TOLOWCASE);
	}

	@Override
	public String getSeq(String chr, int peaklocation, int region,
			boolean cisseq) {
		return getSeqCase(seqHashAbs.getSeq(chr, peaklocation, region, cisseq),TOLOWCASE);
	}


	@Override
	public String getSeq(boolean cisseq, String chrID, ArrayList<int[]> lsInfo,
			boolean getIntron) {
		return getSeqCase(seqHashAbs.getSeq(cisseq, chrID, lsInfo, getIntron),TOLOWCASE);
	}

	//////////////////////  static method  ////////////////////////////////////////////////////////////////////////////////
	/**
	 * ����TOLOWCASE��ѡ�������Ӧ��seq����
	 * @param seq
	 * @param TOLOWCASE null�����䣬false����д��true��Сд
	 * @return
	 */
	private static String getSeqCase(String seq, Boolean TOLOWCASE)
	{
		if (TOLOWCASE == null) {
			return seq;
		}
		else {
			return TOLOWCASE.equals(true) ?  seq.toLowerCase() :  seq.toUpperCase();
		}
	}

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


	
}
