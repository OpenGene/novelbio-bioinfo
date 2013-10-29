package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.TitleFormatNBC;

public class CtrlMiRNApredict {
	GffChrAbs gffChrAbs;
	Species species;
	String outPath;
	
	Map<? extends AlignSeq, String> lsSamFile2Prefix;
	Map<String, String> mapPrefix2UnmapFq = new HashMap<>();
	
	NovelMiRNADeep novelMiRNADeep = new NovelMiRNADeep();
	SoftWareInfo softWareInfo = new SoftWareInfo();
	MiRNACount miRNACount = new MiRNACount();
	
	GeneExpTable expMirPre = new GeneExpTable(TitleFormatNBC.miRNApreName);
	GeneExpTable expMirMature = new GeneExpTable(TitleFormatNBC.miRNAName);
	
	/** 新miRNA的注释，比对到哪些物种上去 */
	List<Species> lsBlastTo = new ArrayList<>();
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		if (this.gffChrAbs != null && gffChrAbs != null && this.gffChrAbs.getSpecies().equals(gffChrAbs.getSpecies())) {
			return;
		}
		this.gffChrAbs = gffChrAbs;
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
	public void setLsSamFile2Prefix(Map<? extends AlignSeq, String> lsSamFile2Prefix) {
		this.lsSamFile2Prefix = lsSamFile2Prefix;
	}

	public void runMiRNApredict() {
		if (gffChrAbs == null) {
			gffChrAbs = new GffChrAbs(species);
		}
		if (lsSamFile2Prefix.size() <= 0) {
			return;
		}
		String novelMiRNAPathDeep = outPath + "miRNApredictDeep/";
		if (!FileOperate.createFolders(novelMiRNAPathDeep)) {
			JOptionPane.showMessageDialog(null, "cannot create fold: " + novelMiRNAPathDeep, "fold create error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		novelMiRNADeep.setSeqInput(lsSamFile2Prefix.keySet());
		softWareInfo.setName(SoftWare.mirDeep);
		novelMiRNADeep.setExePath(softWareInfo.getExePath(), species.getIndexChr(SoftWare.bowtie));
		novelMiRNADeep.setGffChrAbs(gffChrAbs);
		novelMiRNADeep.setMiRNASeq(species.getMiRNAmatureFile(), null, species.getMiRNAhairpinFile());
		novelMiRNADeep.setSpecies(species.getCommonName());
		novelMiRNADeep.setOutPath(novelMiRNAPathDeep);
		novelMiRNADeep.predict();
		setMiRNACount_And_Anno();
		calculateExp();
	}
	/** 仅用于测试
	 * @param predictPath 类似 /media/public/customer/miRNAtest/miRNApredictDeep<br>
	 * 会自动获取 /media/public/customer/miRNAtest/miRNApredictDeep/run/output.mrd 文件
	 */
	protected void predictAndCalculate() {
		novelMiRNADeep.setOutPath(outPath + "miRNApredictDeep/");
		novelMiRNADeep.moveAndCopeFile();
		setMiRNACount_And_Anno();
		calculateExp();
	}
	
	protected void calculateExp() {
		expMirPre.addLsGeneName(miRNACount.getLsMirNamePre());
		expMirPre.addAnnotation(miRNACount.getMapPre2Seq());
		expMirPre.addLsTitle(MiRNACount.getLsTitleAnnoPre());
		
		expMirMature.addLsGeneName(miRNACount.getLsMirNameMature());
		expMirMature.addAnnotation(miRNACount.getMapMature2Pre());
		expMirMature.addAnnotation(miRNACount.getMapMature2Seq());
		expMirMature.addLsTitle(MiRNACount.getLsTitleAnnoMature());
		 
		for (Entry<? extends AlignSeq, String> seq2Prefix : lsSamFile2Prefix.entrySet()) {
			getMirPredictCount(seq2Prefix.getKey(), seq2Prefix.getValue());
		}
	}
	
	private void getMirPredictCount(AlignSeq alignSeq, String prefix) {
		FastQ fastQ = alignSeq.getFastQ();
		alignSeq.close();
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.bowtie2);
		String novelMiRNAsam = outPath + prefix + "novelMiRNAmapping.sam";
		String unmappedFq = outPath + prefix + "novelMiRNAunmapped.fq.gz";
		novelMiRNAsam = MiRNAmapPipline.mappingBowtie2(softWareInfo.getExePath(), 3, fastQ.getReadFileName(), 
				novelMiRNADeep.getNovelMiRNAhairpin(), novelMiRNAsam, unmappedFq);
		miRNACount.setAlignFile(new SamFile(novelMiRNAsam));
		mapPrefix2UnmapFq.put(prefix, unmappedFq);
		String outPathNovel = outPath + prefix + FileOperate.getSepPath();
		FileOperate.createFolders(outPathNovel);
		miRNACount.run();
		expMirMature.setCurrentCondition(prefix);
		expMirMature.addAllReads(miRNACount.getCountMatureAll());
		expMirMature.addGeneExp(miRNACount.getMapMirMature2Value());
		
		expMirPre.addAllReads(miRNACount.getCountPreAll());
		expMirPre.addGeneExp(miRNACount.getMapMiRNApre2Value());
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
		CtrlMiRNAfastq.writeFile(outPath + "NovelMirPreAll_Counts.txt", expMirPre, EnumExpression.Counts);
		CtrlMiRNAfastq.writeFile(outPath + "NovelMirMatureAll_Counts.txt", expMirMature, EnumExpression.Counts);
		CtrlMiRNAfastq.writeFile(outPath + "NovelMirPreAll_UQTPM.txt", expMirPre, EnumExpression.UQPM);
		CtrlMiRNAfastq.writeFile(outPath + "NovelMirMatureAll_UQTPM.txt", expMirMature, EnumExpression.UQPM);
	}
	
	private void setMiRNACount_And_Anno() {
		SeqHash seqHash = new SeqHash(novelMiRNADeep.getNovelMiRNAmature());
		seqHash.close();
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
