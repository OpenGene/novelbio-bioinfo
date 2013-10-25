package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JOptionPane;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

public class CtrlMiRNApredict {
	GffChrAbs gffChrAbs;
	Species species;
	String outPath;
	
	Map<? extends AlignSeq, String> lsSamFile2Prefix;
	
	NovelMiRNADeep novelMiRNADeep = new NovelMiRNADeep();
	SoftWareInfo softWareInfo = new SoftWareInfo();
	MiRNACount miRNACount = new MiRNACount();
	Map<String, Map<String, Double>> mapPrefix2MapMature = new LinkedHashMap<>();
	Map<String, double[]> mapPrefix2CountsMature = new LinkedHashMap<>();
	Map<String, Map<String, Double>> mapPrefix2MapPre= new LinkedHashMap<>();
	Map<String, double[]> mapPrefix2CountsPre = new LinkedHashMap<>();
	
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
		mapPrefix2MapMature.clear();
		mapPrefix2MapPre.clear();
		mapPrefix2CountsMature.clear();
		mapPrefix2CountsPre.clear();
		
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
		
		calculateExp();
	}
	
	protected void calculateExp() {
		for (Entry<? extends AlignSeq, String> seq2Prefix : lsSamFile2Prefix.entrySet()) {
			getMirPredictCount(seq2Prefix.getKey(), seq2Prefix.getValue());
		}
	}
	
	private void getMirPredictCount(AlignSeq alignSeq, String prefix) {
		FastQ fastQ = alignSeq.getFastQ();
		alignSeq.close();
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.bwa);
		MiRNAmapPipline miRNAmapPipline = new MiRNAmapPipline();
		
		miRNAmapPipline.setExePath(softWareInfo.getExePath());
		miRNAmapPipline.setMiRNApreSeq(novelMiRNADeep.getNovelMiRNAhairpin());
		miRNAmapPipline.setOutPathTmp(outPath +"novelMiRNAmapping");
		
		miRNAmapPipline.setSample(prefix, fastQ.getReadFileName());
		miRNAmapPipline.mappingMiRNA();
		
		setMiRNACount_And_Anno();
		miRNACount.setAlignFile(miRNAmapPipline.getOutMiRNAAlignSeq());

		String outPathNovel = outPath + prefix + FileOperate.getSepPath();
		FileOperate.createFolders(outPathNovel);
		miRNACount.run();
		miRNACount.writeResultToOut(outPathNovel + "NovelmiRNA");
		
		mapPrefix2MapMature.put(prefix, miRNACount.getMapMirMature2Value());
		mapPrefix2MapPre.put(prefix, miRNACount.getMapMiRNApre2Value());
		mapPrefix2CountsPre.put(prefix, miRNACount.getCountPre());
		mapPrefix2CountsMature.put(prefix, miRNACount.getCountMature());
		
	}
	
	public void writeToFile() {
		ArrayList<String[]> lsMirPreCounts = miRNACount.combMapMir2ValueCounts(mapPrefix2MapPre, mapPrefix2CountsPre);
		ArrayList<String[]> lsMirMatureCounts = miRNACount.combMapMir2MatureValueCounts(mapPrefix2MapMature, mapPrefix2CountsMature);
		
		ArrayList<String[]> lsMirPreUQTM = miRNACount.combMapMir2ValueUQPM(mapPrefix2MapPre, mapPrefix2CountsPre);
		ArrayList<String[]> lsMirMatureUQTM = miRNACount.combMapMir2MatureValueUQPM(mapPrefix2MapMature, mapPrefix2CountsMature);
//		annoMiRNA(novelMiRNADeep, lsMirMature);
		writeFile(outPath + "NovelMirPreAll_Counts.txt", lsMirPreCounts);
		writeFile(outPath + "NovelMirMatureAll_Counts.txt", lsMirMatureCounts);
		writeFile(outPath + "NovelMirPreAll_UQTPM.txt", lsMirPreUQTM);
		writeFile(outPath + "NovelMirMatureAll_UQTPM.txt", lsMirMatureUQTM);
	}
	
	private void setMiRNACount_And_Anno() {
		SeqHash seqHash = new SeqHash(novelMiRNADeep.getNovelMiRNAmature());
		Set<String> setMiRNAName = new HashSet<>(seqHash.getLsSeqName());
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
	
	/** 新miRNA添上blast到的miRNA名字 */
	private void annoMiRNA(NovelMiRNADeep novelMiRNADeep, List<String[]> lsMirMature) {
		if (lsBlastTo == null || lsBlastTo.size() == 0) return;
		
		MiRNAnovelAnnotaion miRNAnovelAnnotaion = new MiRNAnovelAnnotaion();
		miRNAnovelAnnotaion.setMiRNAthis(novelMiRNADeep.getNovelMiRNAmature());
		miRNAnovelAnnotaion.setLsMiRNAblastTo(lsBlastTo);
		miRNAnovelAnnotaion.annotation();
		Map<String, String> mapID2Blast = miRNAnovelAnnotaion.getMapID2Blast();
		for (String[] strings : lsMirMature) {
			String miRNAblast = mapID2Blast.get(strings[0]);
			if (miRNAblast != null) {
				strings[0] += SepSign.SEP_INFO + miRNAblast;
			}
		}
	}
	
	private void writeFile(String fileName, ArrayList<String[]> lsInfo) {
		if (lsInfo == null || lsInfo.size() == 0) {
			return;
		}
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileName, true);
		txtWrite.ExcelWrite(lsInfo);
		txtWrite.close();
	}
}
