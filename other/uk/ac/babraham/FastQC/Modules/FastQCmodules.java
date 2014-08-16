package uk.ac.babraham.FastQC.Modules;

import uk.ac.babraham.FastQC.Sequence.Sequence;

import com.novelbio.analysis.seq.fastq.FQrecordCopeInt;
import com.novelbio.analysis.seq.fastq.FastQRecord;

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
}
