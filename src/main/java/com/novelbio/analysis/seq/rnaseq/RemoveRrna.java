package com.novelbio.analysis.seq.rnaseq;

import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.mapping.MapBwaAln;
import com.novelbio.analysis.seq.mapping.MapDNAint;
import com.novelbio.analysis.seq.sam.SamFileStatistics;
import com.novelbio.analysis.seq.sam.SamToFastq;
import com.novelbio.analysis.seq.sam.SamToFastq.EnumSamToFastqType;
import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.species.Species;

/** 去除rrna的模块 */
public class RemoveRrna implements IntCmdSoft {
	String rrnaFile;
	String rrnaSpecies;
	MapDNAint mapDNAint;
	String prefix;
	String outPath;
	FastQ fastQleft;
	String outLeft;
	FastQ fastQRight;
	String outRight;
	
	int threadNum = 7;
	
	/**先添加species，再添加rrnaFile */
	public void setRrnaFile(String rrnaFile) {
		this.rrnaFile = rrnaFile;
	}
	/**先添加species，再添加rrnaFile */
	public void setSpecies(Species species) {
		this.rrnaSpecies = species.getRrnaFile();
	}
	
	public void setFastQs(FastQ fastQleft, FastQ fastQRight) {
		if (fastQleft == null) {
			throw new ExceptionNullParam("Left Fastq Must Exist");
		}
		this.fastQleft = fastQleft;
		outLeft = FileOperate.changeFileSuffix(fastQleft.getReadFileName(), "_filterReads", null);
		this.fastQRight = fastQRight;
		if (fastQRight != null) {
			outRight = FileOperate.changeFileSuffix(fastQleft.getReadFileName(), "_filterReads", null);
		}
	}
	
	public void setOutPath(String prefix, String outPath) {
		this.outPath = outPath;
	}
	
	private void setOutFile() {
		outLeft = FileOperate.changeFileSuffix(outPath + FileOperate.getFileName(fastQleft.getReadFileName()), "_filterReads", null);
		if (fastQRight != null) {
			outRight = FileOperate.changeFileSuffix(outPath + FileOperate.getFileName(fastQRight.getReadFileName()), "_filterReads", null);
		}
	}
	
	private boolean isPairend() {
		if (fastQleft != null && fastQRight != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public void filter() {
		setOutFile();
		
		mapDNAint = new MapBwaAln();
		mapDNAint.setChrIndex(getRrnaFile());
		mapDNAint.setFqFile(fastQleft, fastQRight);
		mapDNAint.setThreadNum(threadNum);
		mapDNAint.setWriteToBam(false);
		SamToFastq samToFastq = new SamToFastq(false);
		String outLeftTmp = FileOperate.changeFileSuffix(outLeft, "_tmp", null);
		String outRightTmp = null;
		if (isPairend()) {
			outRightTmp = FileOperate.changeFileSuffix(outRight, "_tmp", null);
		}
		samToFastq.setOutFileInfo(isPairend(), outLeftTmp, outRightTmp, EnumSamToFastqType.UnmappedReads);
		SamFileStatistics samFileStatistics = new SamFileStatistics(prefix);
		mapDNAint.addAlignmentRecorder(samFileStatistics);
		mapDNAint.addAlignmentRecorder(samToFastq);
		try {
			mapDNAint.mapReads();
		} catch (Exception e) {
			FileOperate.deleteFileFolder(outLeftTmp);
			FileOperate.deleteFileFolder(outRightTmp);
			throw e;
		}
		
		FileOperate.moveFile(true, outLeftTmp, outLeft);
		if (isPairend()) {
			FileOperate.moveFile(true, outRightTmp, outRight);
		}
		
		SamFileStatistics.saveExcel(outPath + prefix, samFileStatistics);
		SamFileStatistics.savePic(outPath + prefix, samFileStatistics);
	}
	
	private String getRrnaFile() {
		if (FileOperate.isFileExistAndBigThanSize(rrnaFile, 0)) {
			return rrnaFile;
		} else if (FileOperate.isFileExistAndBigThanSize(rrnaSpecies, 0)) {
			return rrnaSpecies;
		} else {
			throw new ExceptionNullParam("No Rrna File Exist");
		}
	}
	
	public FastQ[] getBamFiltered() {
		FastQ[] fastQs = new FastQ[2];
		fastQs[0] = new FastQ(outLeft);
		if (isPairend()) {
			fastQs[1] = new FastQ(outRight);
		}
		return fastQs;
	}
	@Override
	public List<String> getCmdExeStr() {
		return mapDNAint.getCmdExeStr();
	}
}
