package com.novelbio.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.openxmlformats.schemas.presentationml.x2006.main.SldDocument;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.seq.chipseq.BedPeakMacs;
import com.novelbio.analysis.seq.chipseq.preprocess.Comb;
import com.novelbio.analysis.seq.mapping.BedSeq;
import com.novelbio.analysis.seq.mapping.FastQ;
import com.novelbio.base.genome.gffOperate.GffHashUCSCgene;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.updatedb.database.UpDateNBCDBFile;

public class mytest {
	private static Logger logger = Logger.getLogger(mytest.class);  
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BedSeq bedSeq = new BedSeq("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110608Paper/mapping/GSM531964_PHF8Filter.bed");
		try {
			bedSeq.extend(300, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110608Paper/mapping/GSM531964_PHF8Filter_extend.bed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
