package com.novelbio.analysis.seq.mirna;

import java.util.List;
import java.util.Map;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.database.model.species.Species;

/** 指定一系列的物种，将miRNA依次比对到这些物种上去 */
public class CtrlMiRNAToSpecies {
	List<Species> lsSpecies;
	Map<String, FastQ> mapPrefix2Fastq;
	GeneExpTable expMirMature;
	GeneExpTable expMirPre;
	/** 指定一系列的物种，依次比对上去 */
	public void setLsSpecies(List<Species> lsSpecies) {
		this.lsSpecies = lsSpecies;
	}
	/** 前缀--fastq文件，用于比对到的物种 */
	public void setMapPrefix2Fastq(Map<String, FastQ> mapPrefix2Fastq) {
		this.mapPrefix2Fastq = mapPrefix2Fastq;
	}
	public void setExpMirPre(GeneExpTable expMirPre) {
		this.expMirPre = expMirPre;
	}
	public void setExpMirMature(GeneExpTable expMirMature) {
		this.expMirMature = expMirMature;
	}
	
	
	
}
