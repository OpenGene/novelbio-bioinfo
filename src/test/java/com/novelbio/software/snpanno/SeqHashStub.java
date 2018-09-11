package com.novelbio.software.snpanno;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fasta.SeqFasta.SeqCharacter;
import com.novelbio.bioinfo.fasta.SeqHashInt;
import com.novelbio.bioinfo.fasta.StrandType;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.mappedreads.SiteSeqInfo;

public class SeqHashStub implements SeqHashInt {
	String seq;
	public void setSeq(String seq) {
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
	public SeqFasta getSeqCis(Alignment alignment) {
		return new SeqFasta(seq.substring(alignment.getStartAbs()-1, alignment.getEndAbs()));
	}
	@Override
	public SeqFasta getSeq(Boolean cis5to3, String chrID, long startlocation, long endlocation) {
		SeqFasta seqFasta = getSeq(chrID, startlocation, endlocation);
		if (cis5to3 != null && !cis5to3) {
			seqFasta = seqFasta.reservecom();
		}
		return seqFasta;
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
	public SeqFasta getSeq(GffIso gffGeneIsoInfo, boolean getIntron) {
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
	public void setMaxExtractSeqLength(int maxSeqLen) {
		
	}

	@Override
	public SeqFasta getSeq(String seqName) {
		// TODO Auto-generated method stub
		return null;
	}

}
