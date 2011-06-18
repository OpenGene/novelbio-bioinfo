package com.novelbio.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.openxmlformats.schemas.presentationml.x2006.main.SldDocument;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.seq.mapping.FastQ;
import com.novelbio.base.genome.gffOperate.GffHashUCSCgene;
import com.novelbio.database.entity.friceDB.NCBIID;

public class mytest {
	private static Logger logger = Logger.getLogger(mytest.class);  
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int rowAllNum = 402;
		HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
		Integer i = 1;
		int  j = 1;
		System.out.println(i == j);
	}

}
