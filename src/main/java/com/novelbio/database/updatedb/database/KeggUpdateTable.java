package com.novelbio.database.updatedb.database;

import org.apache.jasper.tagplugins.jstl.core.If;
import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.kegg.KGIDgen2Keg;
import com.novelbio.database.domain.kegg.KGIDkeg2Ko;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathRelation;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.domain.kegg.KGreaction;
import com.novelbio.database.domain.kegg.KGrelation;
import com.novelbio.database.service.servkegg.ServKEntry;
import com.novelbio.database.service.servkegg.ServKIDKeg2Ko;
import com.novelbio.database.service.servkegg.ServKIDgen2Keg;
import com.novelbio.database.service.servkegg.ServKPathRelation;
import com.novelbio.database.service.servkegg.ServKPathway;
import com.novelbio.database.service.servkegg.ServKReaction;
import com.novelbio.database.service.servkegg.ServKRelation;

/** 把mysql中kegg的信息导入到mongodb中去 */
public class KeggUpdateTable {
	private static final Logger logger = Logger.getLogger(KeggUpdateTable.class);
	
	String parentPath = "/home/novelbio/NBCsource/database/kegg/keggtable/";
	public static void main(String[] args) {
//		ServKRelation.getInstance().deleteAll();
//		ServKReaction.getInstance().deleteAll();
//		ServKPathway.getInstance().deleteAll();
//		ServKPathRelation.getInstance().deleteAll();
//		ServKIDgen2Keg.getInstance().deleteAll();
//		ServKEntry.getInstance().deleteAll();
		
		
//		KeggUpdateTable keggUpdateTable = new KeggUpdateTable();
//		keggUpdateTable.updateEntry();
		
//		keggUpdateTable.updatePathRelation();
//		keggUpdateTable.updatePathway();
//		keggUpdateTable.updateReaction();
//		keggUpdateTable.updateIdGene2Keg();
//		keggUpdateTable.updateRelation();
		
		
		
		for (int i = 2; i < 22; i++) {
			UpdateGen2Keg updateGen2Keg = new UpdateGen2Keg();
			updateGen2Keg.setNumStart(i);
			Thread thread = new Thread(updateGen2Keg);
			thread.start();
		}
		
	}
	public void updateRelation() {
		ServKRelation servKRelation = ServKRelation.getInstance();
		TxtReadandWrite txtRead = new TxtReadandWrite(parentPath + "relation.txt");
		for (String content : txtRead.readlines(2)) {
			String[] ss = content.split("\t");
			KGrelation kGrelation = new KGrelation();
			kGrelation.setPathName(ss[0]);
			kGrelation.setEntry1ID(Integer.parseInt(ss[1]));
			kGrelation.setEntry2ID(Integer.parseInt(ss[2]));
			kGrelation.setType(ss[3]);
			kGrelation.setSubtypeName(ss[4]);
			if (ss.length == 6) {
				kGrelation.setSubtypeValue(ss[5]);
			}
			
			if (servKRelation.findByPathNameAndEntry1IdAndEntry2IdAndType(kGrelation.getPathName(), 
					kGrelation.getEntry1ID(), kGrelation.getEntry2ID(), kGrelation.getType()) == null) {
				servKRelation.save(kGrelation);
			}
		}
		txtRead.close();
	}
	
	public void updateReaction() {
		ServKReaction servKReaction = ServKReaction.getInstance();
		TxtReadandWrite txtRead = new TxtReadandWrite(parentPath + "reaction.txt");
		for (String content : txtRead.readlines(2)) {
			String[] ss = content.split("\t");
			KGreaction kGreaction = new KGreaction();
			kGreaction.setPathName(ss[0]);
			kGreaction.setReactionId(Integer.parseInt(ss[1]));
			kGreaction.setName(ss[2]);
			kGreaction.setType(ss[3]);
			if (ss.length == 5) {
				kGreaction.setAlt(ss[4]);
			}
			
			if (servKReaction.findByNameAndPathNameAndId(kGreaction.getName(), 
					kGreaction.getPathName(), kGreaction.getReactionId()) == null) {
				servKReaction.save(kGreaction);
			}
		}
		txtRead.close();
	}
	
	public void updatePathway() {
		ServKPathway servKPathway = ServKPathway.getInstance();
		TxtReadandWrite txtRead = new TxtReadandWrite(parentPath + "pathway.txt");
		for (String content : txtRead.readlines(2)) {
			String[] ss = content.split("\t");
			KGpathway kGpathway = new KGpathway();
			kGpathway.setPathName(ss[0]);
			kGpathway.setSpecies(ss[1]);
			kGpathway.setMapNum(ss[2]);
			kGpathway.setTitle(ss[3]);
			kGpathway.setLinkUrl(ss[4]);
			kGpathway.setTaxID(Integer.parseInt(ss[5]));
			if (servKPathway.findByPathName(kGpathway.getPathName()) == null) {
				servKPathway.save(kGpathway);
			}
		}
		txtRead.close();
	}
	
	public void updatePathRelation() {
		ServKPathRelation servKPathRelation = ServKPathRelation.getInstance();
		TxtReadandWrite txtRead = new TxtReadandWrite(parentPath + "pathrelation.txt");
		for (String content : txtRead.readlines(2)) {
			String[] ss = content.split("\t");
			KGpathRelation kGpathRelation = new KGpathRelation();
			kGpathRelation.setPathName(ss[0]);
			kGpathRelation.setScrPath(ss[1]);
			kGpathRelation.setTrgPath(ss[2]);
			kGpathRelation.setType(ss[3]);
			if (servKPathRelation.findByPathNameSrcTrg(kGpathRelation.getPathName(), 
					kGpathRelation.getSrcPath(), kGpathRelation.getTrgPath()) == null) {
				servKPathRelation.save(kGpathRelation);
			}
		}
		txtRead.close();
	}
	
	public void updateIdGene2Keg() {
		ServKIDgen2Keg servKIDgen2Keg = ServKIDgen2Keg.getInstance();
		TxtReadandWrite txtRead = new TxtReadandWrite(parentPath + "idgen2keg.txt");
		for (String content : txtRead.readlines(2)) {
			String[] ss = content.split("\t");
			KGIDgen2Keg kgiDgen2Keg = new KGIDgen2Keg();
			kgiDgen2Keg.setGeneID(Long.parseLong(ss[0]));
			kgiDgen2Keg.setKeggID(ss[1]);
			kgiDgen2Keg.setTaxID(Integer.parseInt(ss[2]));
			KGIDgen2Keg search = servKIDgen2Keg.findByGeneIdAndTaxIdAndKegId(kgiDgen2Keg.getGeneID(), 
					kgiDgen2Keg.getTaxID(), kgiDgen2Keg.getKeggID());
			if (search == null) {
				servKIDgen2Keg.save(kgiDgen2Keg);
			}
		}
		txtRead.close();
	}
	
	public void updateKeg2Ko() {
		ServKIDKeg2Ko serv = ServKIDKeg2Ko.getInstance();
		TxtReadandWrite txtRead = new TxtReadandWrite(parentPath + "idkeg2ko.txt");

		for (String content : txtRead.readlines(2)) {
			String[] ss = content.split("\t");
			KGIDkeg2Ko kgiDkeg2Ko = new KGIDkeg2Ko();
			kgiDkeg2Ko.setKeggID(ss[0]);
			kgiDkeg2Ko.setKo(ss[1]);
			kgiDkeg2Ko.setTaxID(Integer.parseInt(ss[2]));
			if (serv.findByKegIdAndKo(kgiDkeg2Ko.getKeggID(), kgiDkeg2Ko.getKo()) == null) {
				serv.save(kgiDkeg2Ko);
			}
		}
		txtRead.close();
	}
	
	public void updateEntry() {
		ServKEntry servKEntry = ServKEntry.getInstance();
		TxtReadandWrite txtRead = new TxtReadandWrite(parentPath + "entry.txt");
		int i = 0;
		for (String content : txtRead.readlines(2)) {
			i++;
			String[] ss = content.split("\t");
			KGentry kGentry = new KGentry();
			kGentry.setEntryName(ss[0]);
			kGentry.setPathName(ss[1]);
			kGentry.setEntryId(Integer.parseInt(ss[2]));
			kGentry.setType(ss[3]);
			kGentry.setReaction(ss[4]);
			kGentry.setLinkEntry(ss[5]);
			kGentry.setCompNum(Integer.parseInt(ss[6]));
			kGentry.setCompID(Integer.parseInt(ss[7]));
			kGentry.setParentID(Integer.parseInt(ss[8]));
			kGentry.setTaxID(Integer.parseInt(ss[9]));
			if (servKEntry.findByNamePathAndIdAndReaction(kGentry.getEntryName(), 
					kGentry.getPathName(), kGentry.getEntryId(), kGentry.getReaction()) == null) {
				servKEntry.save(kGentry);
			}
			if (i % 500 == 0) {
				logger.info("entry import number: " + i);
			}
		}
		txtRead.close();
	}
}

class UpdateGen2Keg implements Runnable {
	int numStart;
	int numUnit = 20;
	String parentPath = "/home/novelbio/NBCsource/database/kegg/keggtable/";

	public void setNumStart(int numStart) {
		this.numStart = numStart;
	}
	
	@Override
	public void run() {
		ServKIDgen2Keg servKIDgen2Keg = ServKIDgen2Keg.getInstance();
		TxtReadandWrite txtRead = new TxtReadandWrite(parentPath + "idgen2keg.txt");
		int i = -1;
		for (String content : txtRead.readlines(numStart)) {
			i++;
			if (i % 20 != 0) {
				continue;
			}
			String[] ss = content.split("\t");
			KGIDgen2Keg kgiDgen2Keg = new KGIDgen2Keg();
			kgiDgen2Keg.setGeneID(Long.parseLong(ss[0]));
			kgiDgen2Keg.setKeggID(ss[1]);
			kgiDgen2Keg.setTaxID(Integer.parseInt(ss[2]));
			KGIDgen2Keg search = servKIDgen2Keg.findByGeneIdAndTaxIdAndKegId(kgiDgen2Keg.getGeneID(), 
					kgiDgen2Keg.getTaxID(), kgiDgen2Keg.getKeggID());
			if (search == null) {
				servKIDgen2Keg.save(kgiDgen2Keg);
			}
		}
		txtRead.close();
		
	}
	
}
