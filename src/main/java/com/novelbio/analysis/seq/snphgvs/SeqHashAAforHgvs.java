package com.novelbio.analysis.seq.snphgvs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHashInt;
import com.novelbio.analysis.seq.fasta.StrandType;
import com.novelbio.analysis.seq.genome.gffoperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffoperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingoperate.SiteSeqInfo;
import com.novelbio.base.dataStructure.Alignment;

/** 用于氨基酸重复的偏移工作
 * 譬如 GMCDARM-[M]-MCK
 * 需要偏移为 GMCDARMM-[M]-CK
 * @author zong0jie
 * @data 2018年3月3日
 */
public class SeqHashAAforHgvs implements SeqHashInt {
	String seq;
	public SeqHashAAforHgvs(String seq) {
		this.seq = seq;
	}
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HashMap<String, Long> getMapChrLength() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String[]> getChrLengthInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getChrLength(String chrID) {
		// TODO Auto-generated method stub
		return null;
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
	public void saveChrLengthToFile(String outFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<String> getLsSeqName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SeqFasta getSeq(String chrID, long startlocation, long endlocation) {
		if (endlocation > seq.length()) {
			endlocation = seq.length();
		}
		if (startlocation < 1) {
			startlocation = 1;
		}
		return new SeqFasta(seq.substring((int)startlocation-1, (int)endlocation));
	}

	@Override
	public SeqFasta getSeq(Boolean cis5to3, String chrID, long startlocation, long endlocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SeqFasta getSeq(String chr, int peaklocation, int region, boolean cis5to3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SeqFasta getSeq(StrandType strandType, String chrID, List<ExonInfo> lsInfo, boolean getIntron) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SeqFasta getSeq(GffGeneIsoInfo gffGeneIsoInfo, boolean getIntron) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSep(String sep) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDNAseq(boolean isDNAseq) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getSeq(SiteSeqInfo mapInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterable<Character> readBase(String refID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMaxExtractSeqLength(int maxSeqLen) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SeqFasta getSeq(String seqName) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SeqFasta getSeqCis(Alignment alignment) {
		return getSeq(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs());
	}

}
