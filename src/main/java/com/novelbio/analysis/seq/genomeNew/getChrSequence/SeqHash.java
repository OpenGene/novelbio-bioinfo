package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.fileOperate.FileOperate;

public class SeqHash implements SeqHashInt{

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
	public HashMap<String, Long> getHashChrLength() {
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
	/**
	 * ����TOLOWCASE��ѡ�������Ӧ��seq����
	 * @param seq
	 * @param TOLOWCASE null�����䣬false����д��true��Сд
	 * @return
	 */
	static private String getSeqCase(String seq, Boolean TOLOWCASE)
	{
		if (TOLOWCASE == null) {
			return seq;
		}
		else {
			return TOLOWCASE.equals(true) ?  seq.toLowerCase() :  seq.toUpperCase();
		}
	}


}
