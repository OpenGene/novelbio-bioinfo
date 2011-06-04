package com.novelbio.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.openxmlformats.schemas.presentationml.x2006.main.SldDocument;

import com.novelbio.base.genome.gffOperate.GffHashUCSCgene;
import com.novelbio.database.entity.friceDB.NCBIID;


public class mytest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
       TreeSet<NCBIID> hashTest1 = new TreeSet<NCBIID>();
       TreeSet<NCBIID> hashTest2 = new TreeSet<NCBIID>();
       NCBIID ncbiid1 = new NCBIID();
       ncbiid1.setAccID("sfeesf");ncbiid1.setGeneId(12);
       NCBIID ncbiid2 = new NCBIID();
       ncbiid2.setAccID("aaaesf");ncbiid2.setGeneId(134);
       NCBIID ncbiid3 = new NCBIID();
       ncbiid3.setAccID("afewtgesf");ncbiid3.setGeneId(12);
       NCBIID ncbiid4 = new NCBIID();
       ncbiid4.setAccID("awescvbesf");ncbiid4.setGeneId(134);
       
       hashTest1.add(ncbiid2);hashTest1.add(ncbiid1);
       hashTest2.add(ncbiid3);hashTest2.add(ncbiid4);
       System.out.println(hashTest1.equals(hashTest2));
	}

}
