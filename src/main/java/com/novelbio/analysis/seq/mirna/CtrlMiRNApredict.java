package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

public class CtrlMiRNApredict {
	GffChrAbs gffChrAbs;
	Species species;
	String outPath;
	
	Map<AlignSeq, String> lsSamFile2Prefix;
	
	NovelMiRNADeep novelMiRNADeep = new NovelMiRNADeep();
	SoftWareInfo softWareInfo = new SoftWareInfo();
	MiRNACount miRNACount = new MiRNACount();
	Map<String, Map<String, Double>> mapPrefix2MapMature = new LinkedHashMap<>();
	Map<String, Map<String, Double>> mapPrefix2MapPre= new LinkedHashMap<>();

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		if (this.gffChrAbs != null && gffChrAbs != null && this.gffChrAbs.getSpecies().equals(gffChrAbs.getSpecies())) {
			return;
		}
		this.gffChrAbs = gffChrAbs;
	}
	
	public void setSpecies(Species species) {
		this.species = species;
	}
	/** 输出文件夹 */
	public void setOutPath(String outPath) {
		this.outPath = FileOperate.addSep(outPath);
	}
	public void setLsSamFile2Prefix(Map<AlignSeq, String> lsSamFile2Prefix) {
		this.lsSamFile2Prefix = lsSamFile2Prefix;
	}

	public void runMiRNApredict() {
		mapPrefix2MapMature.clear();
		mapPrefix2MapPre.clear();
		
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
		
		for (Entry<AlignSeq, String> seq2Prefix : lsSamFile2Prefix.entrySet()) {
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
		
		miRNACount.setAlignFile(miRNAmapPipline.getOutMiRNAAlignSeq());
		miRNACount.setMiRNAfile(novelMiRNADeep.getNovelMiRNAhairpin(), novelMiRNADeep.getNovelMiRNAmature());
		miRNACount.setMiRNAinfo(ListMiRNALocation.TYPE_MIRDEEP, new Species(), novelMiRNADeep.getNovelMiRNAdeepMrdFile());
		String outPathNovel = outPath + prefix + FileOperate.getSepPath();
		FileOperate.createFolders(outPathNovel);
		miRNACount.writeResultToOut(outPathNovel + "NovelmiRNA");
		
		mapPrefix2MapMature.put(prefix, miRNACount.getMapMirMature2Value());
		mapPrefix2MapPre.put(prefix, miRNACount.getMapMiRNApre2Value());
	}
	
	public void writeToFile() {
		String outFilePre = outPath + "NovelMirPreAll.txt";
		String outFileMature = outPath + "NovelMirMatureAll.txt";
		ArrayList<String[]> lsMirPre = miRNACount.combMapMir2Value(mapPrefix2MapPre);
		ArrayList<String[]> lsMirMature = miRNACount.combMapMir2MatureValue(mapPrefix2MapMature);
		writeFile(outFilePre, lsMirPre);
		writeFile(outFileMature, lsMirMature);
	}

	private void writeFile(String fileName, ArrayList<String[]> lsInfo) {
		if (lsInfo == null || lsInfo.size() == 0) {
			return;
		}
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileName, true);
		txtWrite.ExcelWrite(lsInfo);
	}
}
