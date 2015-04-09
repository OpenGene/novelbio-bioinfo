package com.novelbio.database.updatedb.omim;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.omim.GeneMIM;
import com.novelbio.database.domain.omim.MIMInfo;
import com.novelbio.database.domain.omim.MorbidMap;
import com.novelbio.database.model.modomim.MgmtGeneMIMInfo;
import com.novelbio.database.model.modomim.MgmtMorbidMap;
import com.novelbio.database.model.modomim.MgmtOMIMUnit;

public class UpdataOMIM {
	
	static String omimFilePath = "/home/novelbio/omim.txt";	
	static String inFile = "/home/novelbio/morbidmap.txt";
	static String inGeneIdFile = "/home/novelbio/mim2gene.txt";

	public static void main(String[] args) {
		
		//导入omim.txt的表格
		UpdataOMIM updataOMIM = new UpdataOMIM();
		updataOMIM.creatMIMTable(omimFilePath);
		
		//导入morbidmap.txt表格
		updataOMIM.creatMorbidMapTable(inFile, inGeneIdFile);
		
		//导入genemap.txt表格
		updataOMIM.creatGeneMIM(inFile, inGeneIdFile);
		
	}
	public boolean creatMIMTable(String inFile) {
		TxtReadandWrite txtMIMRead = new TxtReadandWrite(inFile);
		List<String> lsOmimUnit = new ArrayList<String>();
		MgmtOMIMUnit mgmtOMIMUnit =MgmtOMIMUnit.getInstance();
		for (String content : txtMIMRead.readlines()) {
			if(content.startsWith("*RECORD*")) {
				MIMInfo mIMInfo = MIMInfo.getInstanceFromOmimUnit(lsOmimUnit);
				if ((mIMInfo != null) && (mIMInfo.getMimId() != 0) && (mIMInfo.getType() != '^')) {
					mgmtOMIMUnit.save(mIMInfo);
				}
				lsOmimUnit.clear();
			}
			lsOmimUnit.add(content);
		}
		MIMInfo mIMInfo = MIMInfo.getInstanceFromOmimUnit(lsOmimUnit);
		if ((mIMInfo != null) && (mIMInfo.getMimId() != 0)) {
			mgmtOMIMUnit.save(mIMInfo);
		}
		return true;
	}
	
	public static void creatMorbidMapTable(String inFile, String inGeneIdFile) {
		MgmtMorbidMap mgmtMorbidMap = MgmtMorbidMap.getInstance();
		TxtReadandWrite txtMorbidMapRead = new TxtReadandWrite(inFile);
		for (String content : txtMorbidMapRead.readlines()) {
			MorbidMap morbidMap = new MorbidMap();
			morbidMap.getInstanceFromOmimRecord(content);
			mgmtMorbidMap.save(morbidMap);;
		}
		txtMorbidMapRead.close();
	}
	
	public static void creatGeneMIM(String inFile, String inGeneIdFile) {
		MgmtGeneMIMInfo mgmtGeneMIMInfo = MgmtGeneMIMInfo.getInstance();
		TxtReadandWrite txtMorbidMapRead = new TxtReadandWrite(inFile);
		for (String content : txtMorbidMapRead.readlines()) {
			GeneMIM geneMIM = GeneMIM.getInstanceFromGeneOmim(content);
			mgmtGeneMIMInfo.save(geneMIM);;
		}
		txtMorbidMapRead.close();
	}
}