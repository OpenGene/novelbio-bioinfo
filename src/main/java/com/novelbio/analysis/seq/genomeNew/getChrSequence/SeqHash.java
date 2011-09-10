package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.fileOperate.FileOperate;

public class SeqHash implements SeqHashInt{
	SeqHashAbs seqHashAbs = null;
	/**
	 * 根据给定的是文件夹或文件，生成不同的类
	 * @param chrFile
	 */
	public SeqHash(String chrFile)
	{
		if (FileOperate.isFile(chrFile)) {
			seqHashAbs =new SeqFastaHash(chrFile);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile);
		}
	}
	
	
	
	@Override
	public void setInfo(boolean CaseChange, String regx, boolean append,
			String chrPattern) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HashMap<String, Long> getHashChrLength() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String[]> getChrLengthInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getChrLength(String chrID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getChrLenMin() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getChrLenMax() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getChrRes(String chrID, int maxresolution) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFile() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveChrLengthToFile(String outFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSeq(String chrID, long startlocation, long endlocation)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSeq(boolean cisseq, String chrID, long startlocation,
			long endlocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSeq(String chrlocation, boolean cisseq) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSeq(String chr, int peaklocation, int region,
			boolean cisseq) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String resCompSeq(String sequence,
			HashMap<Character, Character> complementmap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSeq(boolean cisseq, String chrID, ArrayList<int[]> lsInfo,
			boolean getIntron) {
		// TODO Auto-generated method stub
		return null;
	}

}
