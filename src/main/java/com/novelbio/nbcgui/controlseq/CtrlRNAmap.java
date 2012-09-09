package com.novelbio.nbcgui.controlseq;

import java.util.List;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.mapping.MapLibrary;
import com.novelbio.analysis.seq.mapping.MapRNA;
import com.novelbio.analysis.seq.mapping.MapRsem;
import com.novelbio.analysis.seq.mapping.MapTophat;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.database.model.species.Species;

public class CtrlRNAmap {
	public static final int TOP_HAT = 2;
	public static final int RSEM = 4;
	int mapType;
	MapRNA mapRNA;	
	Species species;
	/** 本项目务必第一个设定 */
	public void setMapType(int mapType) {
		if (mapType == TOP_HAT) {
			mapRNA = new MapTophat();
			this.mapType = TOP_HAT;
		}
		else if (mapType == RSEM) {
			mapRNA = new MapRsem();
			this.mapType = RSEM;
		}
		mapRNA.setExePath("", "");
	}
	public void setSpecies(Species species) {
		this.species = species;
	}
	private void setRefFile() {
		if (mapType == TOP_HAT) {
			mapRNA.setFileRef(species.getIndexChr(mapRNA.getBowtieVersion()));
		}
		else {
			mapRNA.setFileRef(species.getRefseqFile());
		}
	}
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		mapRNA.setGffChrAbs(gffChrAbs);
	}
	
	public void setLeftFq(List<FastQ> lsLeftFqFile) {
		mapRNA.setLeftFq(lsLeftFqFile);
	}
	public void setRightFq(List<FastQ> lsRightFqFile) {
		mapRNA.setRightFq(lsRightFqFile);
	}
	public void setOutPathPrefix(String outPathPrefix) {
		mapRNA.setOutPathPrefix(outPathPrefix);
	}
	public void setMapLibrary(MapLibrary mapLibrary) {
		if (mapLibrary == MapLibrary.SingleEnd) {
			return;
		}
		else if (mapLibrary == MapLibrary.PairEnd) {
			mapRNA.setInsert(450);
		}
		else if (mapLibrary == MapLibrary.MatePair) {
			mapRNA.setInsert(4500);
		}
	}
	/** MapTop里面的参数 */
	public void setStrandSpecifictype(StrandSpecific strandSpecifictype) {
		mapRNA.setStrandSpecifictype(strandSpecifictype);
	}
	public void setThreadNum(int threadNum) {
		mapRNA.setThreadNum(threadNum);
	}
	public void mapping() {
		setRefFile();
		mapRNA.mapReads();
	}
}
