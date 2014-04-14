package com.novelbio.analysis.seq.rnaseq;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.mapping.MapBwaAln;
import com.novelbio.analysis.seq.mapping.MapDNAint;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamToFastq;
import com.novelbio.analysis.seq.sam.SamToFastq.SamToFastqType;
import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

/** 去除rrna的模块 */
public class RemoveRrna {
	String rrnaFile;
	String rrnaSpecies;
	MapDNAint mapDNAint;
	
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
	
	private boolean isPairend() {
		if (fastQleft != null && fastQRight != null) {
			return true;
		} else {
			return false;
		}
	}
	
	private void run() {
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
		samToFastq.setOutFileInfo(isPairend(), outLeftTmp, outRightTmp, SamToFastqType.UnmappedReads);
		mapDNAint.addAlignmentRecorder(samToFastq);
		try {
			mapDNAint.mapReads();
		} catch (Exception e) {
			FileOperate.DeleteFileFolder(outLeftTmp);
			FileOperate.DeleteFileFolder(outRightTmp);
			throw e;
		}
		FileOperate.moveFile(true, outLeftTmp, outLeft);
		if (isPairend()) {
			FileOperate.moveFile(true, outRightTmp, outRight);
		}
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
}
