package uk.ac.babraham.FastQC.Modules;

import com.novelbio.bioinfo.fastq.FQrecordCopeInt;
import com.novelbio.bioinfo.fastq.FastQRecord;

import uk.ac.babraham.FastQC.Sequence.Sequence;

public abstract class FastQCmodules implements FQrecordCopeInt, QCModule {

	public boolean copeReads(FastQRecord fastQRecord) {
		try {
			Sequence sequence = fastQRecord.toFastQCsequence();
			processSequence(sequence);
		} catch (Exception e) {
			return false;
		}

		return true;
	}
	
	public abstract void processSequence(Sequence sequence);
	
	public abstract String getSavePath(String outPrefix);
}
