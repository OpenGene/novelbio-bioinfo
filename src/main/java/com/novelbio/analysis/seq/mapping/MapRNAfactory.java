package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.ExceptionNBCsoft;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class MapRNAfactory {
	public static MapRNA generateMapRNA(SoftWare softWare, GffChrAbs gffChrAbs) {
		MapRNA mapRNA = null;
		if (softWare == SoftWare.tophat) {
			mapRNA = new MapTophat(gffChrAbs);
		} else if (softWare == SoftWare.rsem) {
			mapRNA = new MapRsem(gffChrAbs);
		} else if (softWare == SoftWare.mapsplice) {
			mapRNA = new MapSplice(gffChrAbs);
		} else if (softWare == SoftWare.hisat2) {
			mapRNA = new MapHisat(gffChrAbs);
		} else {
			if (softWare == null) {
				throw new ExceptionNBCsoft("no software is setted");
			}
			throw new ExceptionNBCsoft("This software cannot use as RNAseq: " + softWare);
		}
		return mapRNA;
	}
}
