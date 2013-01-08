package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import com.novelbio.analysis.seq.BedSeq;
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
	
	ArrayList<String[]> lsBedFile2Prefix = new ArrayList<String[]>();
	
	NovelMiRNADeep novelMiRNADeep = new NovelMiRNADeep();
	SoftWareInfo softWareInfo = new SoftWareInfo();
	MiRNACount miRNACount = new MiRNACount();
	HashMap<String, HashMap<String, Double>> mapPrefix2MapMature = new HashMap<String, HashMap<String,Double>>();
	HashMap<String, HashMap<String, Double>> mapPrefix2MapPre= new HashMap<String, HashMap<String,Double>>();

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
	public void setLsBedFile2Prefix(ArrayList<String[]> lsBedFile2Prefix) {
		this.lsBedFile2Prefix = lsBedFile2Prefix;
	}

	public void runMiRNApredict() {
		mapPrefix2MapMature.clear();
		mapPrefix2MapPre.clear();
		
		if (gffChrAbs == null) {
			gffChrAbs = new GffChrAbs(species);
		}
		if (lsBedFile2Prefix.size() <= 0) {
			return;
		}
		String novelMiRNAPathDeep = outPath + "miRNApredictDeep/";
		if (!FileOperate.createFolders(novelMiRNAPathDeep)) {
			JOptionPane.showMessageDialog(null, "cannot create fold: " + novelMiRNAPathDeep, "fold create error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ArrayList<String> lsBedFile = new ArrayList<String>();
		for (String[] prefix2bed : lsBedFile2Prefix) {
			lsBedFile.add(prefix2bed[0]);
		}
		
		novelMiRNADeep.setBedSeqInput(outPath + "SampleAll_To_Predict.bed", lsBedFile);
		softWareInfo.setName(SoftWare.mirDeep);
		novelMiRNADeep.setExePath(softWareInfo.getExePath(), species.getIndexChr(SoftWare.bowtie));
		novelMiRNADeep.setGffChrAbs(gffChrAbs);
		novelMiRNADeep.setMiRNASeq(species.getMiRNAmatureFile(), null, species.getMiRNAhairpinFile());
		novelMiRNADeep.setSpecies(species.getCommonName());
		novelMiRNADeep.setOutPath(novelMiRNAPathDeep);
		novelMiRNADeep.predict();
		
		for (String[] bed2Prefix : lsBedFile2Prefix) {
			getMirPredictCount(bed2Prefix[0], bed2Prefix[1]);
		}
	}
	
	private void getMirPredictCount(String bedFile, String prefix) {
		BedSeq bedSeqInput = new BedSeq(bedFile);
		FastQ fastQ = bedSeqInput.getFastQ();
		
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.bwa);
		MiRNAmapPipline miRNAmapPipline = new MiRNAmapPipline();
		
		miRNAmapPipline.setExePath(softWareInfo.getExePath());
		miRNAmapPipline.setMiRNApreSeq(novelMiRNADeep.getNovelMiRNAhairpin());
		miRNAmapPipline.setOutPathTmp(outPath +"novelMiRNAmapping", outPath + "novelMiRNATmpBed");
		
		miRNAmapPipline.setSample(prefix, fastQ.getReadFileName());
		miRNAmapPipline.mappingMiRNA();
		String bedSeqMiRNAnovel = miRNAmapPipline.getOutMiRNAbed();
		
		miRNACount.setBedSeqMiRNA(bedSeqMiRNAnovel);
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
