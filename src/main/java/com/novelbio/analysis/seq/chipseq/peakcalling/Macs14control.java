package com.novelbio.analysis.seq.chipseq.peakcalling;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.bed.BedFile;
import com.novelbio.analysis.seq.bed.BedRecord;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

public class Macs14control  implements IntCmdSoft {
	Logger logger = Logger.getLogger(Macs14control.class);
	PeakCallingMacs macs14 = new PeakCallingMacs();
	FormatSeq formatSeq;
	String resultFile;
	private Species species;
	private long genomeLength;
	private double effectiveGemoneSize = 0.85;
	
	public void setSpecies(Species species) {
		this.species = species;
	} 
	public void setChromosome(String chromosome) {
		genomeLength = 0;
		SeqHash seqHash = new SeqHash(chromosome);
		for (String[] chrId2Len : seqHash.getChrLengthInfo()) {
			genomeLength = genomeLength + Long.parseLong(chrId2Len[1]);
		}
		seqHash.close();
	}
	public void setPathinput(String pathInput) {
		BedFile bedSeq = new BedFile(pathInput);
		int i = 1;
		List<Integer> lsReadsLen = new ArrayList<Integer>();
		for (BedRecord bedRecord : bedSeq.readLines()) {
			if (i > 1000) {
				break;
			}
			lsReadsLen.add(Math.abs(bedRecord.getEndAbs() - bedRecord.getStartAbs()));
			i++;
		}
		int length = (int) MathComput.median(lsReadsLen, 80);
		setEffectiveGenomeSize(length);
		setTsize(length);
		formatSeq = FormatSeq.getFileType(pathInput);
		macs14.setPathinput(pathInput);
	}
	public void setPathoutput(String pathoutput) {
		if (FileOperate.isFileDirectory(pathoutput) && (pathoutput.endsWith("\\") || pathoutput.endsWith("/"))) {
			pathoutput = pathoutput + "result";
		}
		this.resultFile = FileOperate.changeFileSuffix(pathoutput, ".peak.loc", "xls");
		macs14.setPathoutput(pathoutput);
	}
	public void setpathinputColl(String pathinputCol) {
		if (FileOperate.isFileExistAndNotDir(pathinputCol)) {
			macs14.setPathinputCol(pathinputCol);
		}
	}
	public void setMfoldMin(int mfoldMin) {
		macs14.setMfoldMin(mfoldMin);
	}
	public void setmfoldMax(int mfoldMax) {
		macs14.setMfoldMax(mfoldMax);
	}
	public void setPvalue(double pvalue) {
		macs14.setPvalue(pvalue);
	}
	
	/** 默认为false */
	public void setIsNolambda(boolean isNolambda) {
		macs14.setIsNolambda(isNolambda);
	}
	private void setEffectiveGenomeSize(int readsLength) {
		if (readsLength < 25) {
			this.effectiveGemoneSize = 0.65;
		} else if (readsLength >= 25 && readsLength < 30) {
			this.effectiveGemoneSize = 0.75;
		} else if (readsLength >= 35 && readsLength < 50) {
			this.effectiveGemoneSize = 0.80;
		} else if (readsLength >= 50 && readsLength < 60) {
			this.effectiveGemoneSize = 0.85;
		} else if (readsLength >= 60 && readsLength < 70) {
			this.effectiveGemoneSize = 0.88;
		} else if (readsLength >= 70) {
			this.effectiveGemoneSize = 0.90;
		}
	}
	
	private void setTsize(int readsLength) {
		macs14.setTsize(readsLength);
	}
	public void peakCalling() {
		if ( formatSeq == FormatSeq.UNKNOWN) {
			logger.error("unknown file format");
			return;
		}
		if (genomeLength > 0) {
			macs14.setGenomeLength((long) (genomeLength * effectiveGemoneSize));
		} else if (species != null && species.getTaxID() > 0) {
			macs14.setGenomeLength((long) (species.getChromLenAll() * effectiveGemoneSize));
		}
		macs14.setFileType(formatSeq);
		macs14.runPeakCalling();
		writePeaks();
	}
	/**
	 * 修正结果文件	 */
	private void writePeaks() {
		ArrayList<String[]> lsResult = modifyMacs14Result();
		TxtReadandWrite txtWrite = new TxtReadandWrite(resultFile, true);
		for (String[] string : lsResult ) {
			txtWrite.writefileln(string);
		}
		txtWrite.close();
	}
	/**
	 * 修正结果文件，添加summit位点为老的summit位点+length
	 */
	private ArrayList<String[]> modifyMacs14Result() {
		ArrayList<String[]> lsPeaksRaw = ExcelTxtRead.readLsExcelTxt(macs14.getResultPeakFile(), 20);

		ArrayList<String[]> lsPeakResult = new ArrayList<String[]>();
		String[] title = lsPeaksRaw.get(0);
		// 给-log10pvalue加个括弧
		title[6] = "(" + title[6] + ")";
		lsPeakResult.add(title);

		for (int k = 1; k < lsPeaksRaw.size(); k++) {
			String[] strings = lsPeaksRaw.get(k);

			String start = strings[1];
			String tmpSubmmit = strings[4];
			try {
				strings[4] = String.valueOf(Integer.parseInt(tmpSubmmit) + Integer.parseInt(start));
			} catch (Exception e) {
				logger.error("计算summit位点出错：" + ArrayOperate.cmbString(strings,"\t"));
			}			
			lsPeakResult.add(strings);
		}
		return lsPeakResult;
	}
	
	public void clear() {
		species = null;
		resultFile = null;
		effectiveGemoneSize = 0;
		macs14.clear();
	}
	@Override
	public List<String> getCmdExeStr() {
		return macs14.getCmdExeStr();
	}
	
}
