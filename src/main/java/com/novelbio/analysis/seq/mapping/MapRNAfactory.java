package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.ExceptionNBCsoft;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

public class MapRNAfactory {
	public static MapRNA generateMapRNA(SoftWare softWare) {
		MapRNA mapRNA = null;
		if (softWare == SoftWare.tophat) {
			mapRNA = new MapTophat();
		} else if (softWare == SoftWare.rsem) {
			mapRNA = new MapRsem();
		} else if (softWare == SoftWare.mapsplice) {
			mapRNA = new MapSplice();
		} else if (softWare == SoftWare.hisat2) {
			mapRNA = new MapHisat();
		} else {
			if (softWare == null) {
				throw new ExceptionNBCsoft("no software is setted");
			}
			throw new ExceptionNBCsoft("This software cannot use as RNAseq: " + softWare);
		}
		return mapRNA;
	}
}
