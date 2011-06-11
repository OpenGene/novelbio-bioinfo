package com.novelbio.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.bcel.classfile.Code;
import org.openxmlformats.schemas.presentationml.x2006.main.SldDocument;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.base.genome.gffOperate.GffHashUCSCgene;
import com.novelbio.database.entity.friceDB.NCBIID;


public class mytest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
       CopedID copedID = new CopedID();
       copedID.setInfo("", "", 12334, "");
       CopedID copedID2 = new CopedID();
       copedID2.setInfo("", "", 123, "");
       System.out.println(copedID.equals(copedID2));
       HashSet<CopedID> hashSet = new HashSet<CopedID>();
       hashSet.add(copedID);
       hashSet.add(copedID2);
       System.out.println(copedID.hashCode());
       System.out.println(copedID2.hashCode());
       
       System.out.println("ok  "+hashSet.size());
	}

}
