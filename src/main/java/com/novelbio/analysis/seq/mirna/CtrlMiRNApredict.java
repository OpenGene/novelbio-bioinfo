package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileStatistics;
import com.novelbio.analysis.seq.sam.SamMapRate;
import com.novelbio.base.SepSign;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.TitleFormatNBC;

public class CtrlMiRNApredict {
	GffChrAbs gffChrAbs;
	Species species;
	String outPath;
	
	Map<String, ? extends AlignSeq> mapPrefix2SamFile;
	Map<String, String> mapPrefix2UnmapFq = new HashMap<>();
	
	NovelMiRNADeep novelMiRNADeep = new NovelMiRNADeep();
	SoftWareInfo softWareInfo = new SoftWareInfo();
	MiRNACount miRNACount = new MiRNACount();
	
	GeneExpTable expMirPre = new GeneExpTable(TitleFormatNBC.miRNApreName);
	GeneExpTable expMirMature = new GeneExpTable(TitleFormatNBC.miRNAName);
	
	/** 新miRNA的注释，比对到哪些物种上去 */
	List<Species> lsBlastTo = new ArrayList<>();
	
	boolean overlap = false;
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		if (this.gffChrAbs != null && gffChrAbs != null && this.gffChrAbs.getSpecies().equals(gffChrAbs.getSpecies())) {
			return;
		}
		this.gffChrAbs = gffChrAbs;
	}
	public void setOverlap(boolean overlap) {
		this.overlap = overlap;
	}
	public void setSpecies(Species species) {
		this.species = species;
	}
	public void setExpMir(GeneExpTable expMirPre, GeneExpTable expMirMature) {
		this.expMirPre = expMirPre;
		this.expMirMature = expMirMature;
	}
	/** 新miRNA的注释，比对到哪些物种上去 */
	public void setLsSpeciesBlastTo(List<Species> lsBlastTo) {
		this.lsBlastTo = lsBlastTo;
	}
	/** 输出文件夹 */
	public void setOutPath(String outPath) {
		this.outPath = FileOperate.addSep(outPath);
	}
	public void setMapPrefix2GenomeSamFile(Map<String, ? extends AlignSeq> mapPrefix2SamFile) {
		this.mapPrefix2SamFile = mapPrefix2SamFile;
	}

	public void runMiRNApredict(SamMapRate samMapMiRNARate) {
		if (gffChrAbs == null) {
			gffChrAbs = new GffChrAbs(species);
		}
		if (mapPrefix2SamFile.size() <= 0) {
			return;
		}
		String novelMiRNAPathDeep = outPath + "miRNApredictDeep/";
		if (!FileOperate.createFolders(novelMiRNAPathDeep)) {
			JOptionPane.showMessageDialog(null, "cannot create fold: " + novelMiRNAPathDeep, "fold create error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		novelMiRNADeep.setSeqInput(mapPrefix2SamFile.values());
		softWareInfo.setName(SoftWare.mirDeep);
		novelMiRNADeep.setExePath(softWareInfo.getExePath(), species.getIndexChr(SoftWare.bowtie));
		novelMiRNADeep.setGffChrAbs(gffChrAbs);
		novelMiRNADeep.setMiRNASeq(species.getMiRNAmatureFile(), null, species.getMiRNAhairpinFile());
		novelMiRNADeep.setSpecies(species.getCommonName());
		novelMiRNADeep.setOutPath(novelMiRNAPathDeep);
		novelMiRNADeep.predict();
		setMiRNACount_And_Anno();
		calculateExp(samMapMiRNARate);
	}
	
	/**
	 * @param samMapMiRNARate null表示不统计
	 */
	protected void calculateExp(SamMapRate samMapMiRNARate) {
		samMapMiRNARate.setNovelMiRNAInfo();
		miRNACount.setExpTable(expMirPre, expMirMature);
		 for (String prefix : mapPrefix2SamFile.keySet()) {
			 getMirPredictCount(prefix, mapPrefix2SamFile.get(prefix), samMapMiRNARate);
		}
	}
	
	private void getMirPredictCount(String prefix, AlignSeq alignSeq, SamMapRate samMapMiRNARate) {
		FastQ fastQ = alignSeq.getFastQ();
		alignSeq.close();
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.bowtie2);
		String outPathMap = outPath + "tmpMapping/";
		FileOperate.createFolders(outPathMap);
		String novelMiRNAsam = outPathMap + prefix + "novelMiRNAmapping.sam";
		String unmappedFq = outPathMap + prefix + "novelMiRNAunmapped.fq.gz";
		novelMiRNAsam = MiRNAmapPipline.mappingBowtie2(overlap, new SamFileStatistics(prefix), softWareInfo.getExePath(), 3, fastQ.getReadFileName(), 
				novelMiRNADeep.getNovelMiRNAhairpin(), novelMiRNAsam, unmappedFq);
		miRNACount.setAlignFile(new SamFile(novelMiRNAsam));
		mapPrefix2UnmapFq.put(prefix, unmappedFq);
		samMapMiRNARate.setCurrentCondition(prefix);
		miRNACount.setSamMapRate(samMapMiRNARate);
		miRNACount.run();
		expMirMature.setCurrentCondition(prefix);
		expMirMature.addAllReads(miRNACount.getCountMatureAll());
		expMirMature.addGeneExp(miRNACount.getMapMirMature2Value());
		
		expMirPre.setCurrentCondition(prefix);
		expMirPre.addAllReads(miRNACount.getCountPreAll());
		expMirPre.addGeneExp(miRNACount.getMapMiRNApre2Value());
		
		CtrlMiRNAfastq.writeFile(false, outPath + prefix + FileOperate.getSepPath() + prefix + "_NovelMirPre_Counts.txt", expMirPre, EnumExpression.Counts);
		CtrlMiRNAfastq.writeFile(false, outPath + prefix + FileOperate.getSepPath() + prefix + "_NovelMirMature_Counts.txt", expMirMature, EnumExpression.Counts);
	}
	
	public Map<String, String> getMapPrefix2UnmapFq() {
		return mapPrefix2UnmapFq;
	}
	
	public GeneExpTable getExpMirPre() {
		return expMirPre;
	}
	public GeneExpTable getExpMirMature() {
		return expMirMature;
	}
	
	public void writeToFile() {
		String outPathNovel = outPath + "novelMiRNA/";
		CtrlMiRNAfastq.writeFile(true, outPathNovel + "NovelMirPreAll_Counts.txt", expMirPre, EnumExpression.Counts);
		CtrlMiRNAfastq.writeFile(true, outPathNovel + "NovelMirMatureAll_Counts.txt", expMirMature, EnumExpression.Counts);
		CtrlMiRNAfastq.writeFile(true, outPathNovel + "NovelMirPreAll_UQTPM.txt", expMirPre, EnumExpression.UQPM);
		CtrlMiRNAfastq.writeFile(true, outPathNovel + "NovelMirMatureAll_UQTPM.txt", expMirMature, EnumExpression.UQPM);
	}
	
	private void setMiRNACount_And_Anno() {
		Map<String, String> mapID2Blast = null;
		MiRNAnovelAnnotaion miRNAnovelAnnotaion = null;
		if (lsBlastTo != null && lsBlastTo.size() > 0) {
			miRNAnovelAnnotaion = new MiRNAnovelAnnotaion();
			miRNAnovelAnnotaion.setMiRNAthis(novelMiRNADeep.getNovelMiRNAmature());
			miRNAnovelAnnotaion.setLsMiRNAblastTo(lsBlastTo);
			miRNAnovelAnnotaion.annotation();
			mapID2Blast = miRNAnovelAnnotaion.getMapID2Blast();
		}

		ListMiRNAdeep listMiRNAdeep = new ListMiRNAdeep();
		listMiRNAdeep.setBlastMap(mapID2Blast);
//		listMiRNAdeep.setSetMiRNApredict(setMiRNAName);
		listMiRNAdeep.ReadGffarray(novelMiRNADeep.getNovelMiRNAdeepMrdFile());
		
		miRNACount.setListMiRNALocation(listMiRNAdeep);
		if (lsBlastTo != null && lsBlastTo.size() > 0) {
			miRNACount.setMiRNAfile(novelMiRNADeep.getNovelMiRNAhairpin(), miRNAnovelAnnotaion.getMiRNAmatureCope());		
		} else {
			miRNACount.setMiRNAfile(novelMiRNADeep.getNovelMiRNAhairpin(), novelMiRNADeep.getNovelMiRNAmature());		
		}
	}
	
}
