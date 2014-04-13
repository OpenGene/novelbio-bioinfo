package com.novelbio.analysis.seq.rnaseq;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.mapping.MapBwaAln;
import com.novelbio.analysis.seq.mapping.MapDNAint;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamToFastq;
import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

/** 去除rrna的模块 */
public class RemoveRrna {
	String rrnaFile;
	String rrnaSpecies;
	MapDNAint mapDNAint;
	
	FastQ fastQleft;
	FastQ fastQRight;
	
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
		this.fastQleft = fastQleft;
		this.fastQRight = fastQRight;
	}
	
	private void run() {
		mapDNAint = new MapBwaAln();
		mapDNAint.setChrIndex(getRrnaFile());
		mapDNAint.setFqFile(fastQleft, fastQRight);
		mapDNAint.setThreadNum(threadNum);
		SamToFastq samToFastq = new SamToFastq();
		samToFastq.setGenerateTmpFile(true);
		//TODO 文件名怎么取是个问题
		samToFastq.setOutFileInfo(fastQleft.getReadFileName(), true);
		
		mapDNAint.addAlignmentRecorder(samToFastq);
		SamFile samFile = mapDNAint.mapReads();
		FileOperate.DeleteFileFolder(samFile.getFileName());
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
}
