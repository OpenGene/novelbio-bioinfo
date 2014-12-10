package com.novelbio.analysis.annotation.pathway.kegg.kGML2DB;

import java.io.BufferedReader;
import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.kegg.KGIDgen2Keg;
import com.novelbio.database.domain.kegg.KGIDkeg2Ko;
import com.novelbio.database.domain.kegg.noGene.KGNCompInfo;
import com.novelbio.database.domain.kegg.noGene.KGNIdKeg;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servkegg.ServKIDKeg2Ko;
import com.novelbio.database.service.servkegg.ServKIDgen2Keg;
import com.novelbio.database.service.servkegg.ServKNCompInfo;
import com.novelbio.database.service.servkegg.ServKNIdKeg;

/**
 * 将KEGGID与geneID和KEGGID与KO的关系等导入数据库
 * @author zong0jie
 *
 */
public class KeggIDcvt {
	/**
	 * 首先用geneID在NCBIID表中获得taxID，然后导入gen2Keg表
	 * @param gen2KegFile
	 * @throws Exception 
	 */
	public static void upDateGen2Keg(String gen2KegFile) throws Exception {
		ServKIDgen2Keg servKIDgen2Keg = ServKIDgen2Keg.getInstance();
		TxtReadandWrite txtgene2Keg=new TxtReadandWrite(gen2KegFile);
		int TaxID=0;
		////////////////获得taxID////////////////////////////////////////////////////////
		for (String content : txtgene2Keg.readlines()) {
			String[] ss=content.split("\t"); 
			long geneID=Long.parseLong(ss[1].replace("ncbi-geneid:", "").replace("equivalent", "").trim());
			GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, geneID + "", 0);
			if (copedID.getTaxID() > 0) {
				TaxID = copedID.getTaxID();
				break;
			}
		}
//		while ((content=reader.readLine())!=null) 
//		{
//			String[] ss=content.split("\t"); 
//			long geneID=Long.parseLong(ss[1].replace("ncbi-geneid:", "").replace("equivalent", "").trim());
//			String accID = ss[0].split(":")[1].trim();
//			CopedID copedID = new CopedID(accID, TaxID);
//			copedID.setUpdateGeneID(geneID+"", CopedID.IDTYPE_GENEID);
//			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_KEGG, false);
//			copedID.update(false);
//		}
		///////////////////////////////////////////////////////////////////////////////////////
		if (TaxID==0) {
			System.err.println("在NCBIID表中没有找到该物种的taxID");
			return;
		}
		for (String content2 : txtgene2Keg.readlines()) {
			String[] ss=content2.split("\t");
			String kegID=ss[0];long geneID=Long.parseLong(ss[1].replace("ncbi-geneid:", "").trim());
			KGIDgen2Keg kgiDgen2Keg=new KGIDgen2Keg();
			kgiDgen2Keg.setGeneID(geneID);kgiDgen2Keg.setKeggID(kegID);kgiDgen2Keg.setTaxID(TaxID);
			
			KGIDgen2Keg ls = servKIDgen2Keg.findByGeneIdAndTaxIdAndKegId(geneID, TaxID, kegID);
			if (ls == null) {
				servKIDgen2Keg.save(kgiDgen2Keg);
			}
		}
	}
	
	
	/**
	 * 首先用kegID在gen2Keg表中找taxID
	 * 然后倒入keg2Ko表
	 * @param keg2KoFile
	 * @throws Exception 
	 */
	public static void upDateKeg2Ko(String keg2KoFile) throws Exception {
		ServKIDgen2Keg servKIDgen2Keg = ServKIDgen2Keg.getInstance();
		ServKIDKeg2Ko servKIDKeg2Ko = ServKIDKeg2Ko.getInstance();
		TxtReadandWrite txtKeg2Ko=new TxtReadandWrite(keg2KoFile);
		int TaxID=0;
		////////////////获得taxID////////////////////////////////////////////////////////
		for (String content : txtKeg2Ko.readlines()) {
			String[] ss=content.split("\t"); 
			String kegID=ss[0].trim();
			KGIDgen2Keg kgiDgen2Keg=new KGIDgen2Keg();
			kgiDgen2Keg.setKeggID(kegID);
			
			KGIDgen2Keg kgiDgen2Keg2 = servKIDgen2Keg.findByKegId(kegID);
			
			if (kgiDgen2Keg2 != null) {
				TaxID = kgiDgen2Keg2.getTaxID();
				break;
			}
		}
		
		///////////////////////////////////////////////////////////////////////////////////////
		if (TaxID == 0) {
			System.err.println("在gene2Keg表中没有找到该物种的taxID");
			return;
		}
		
		for (String content2 : txtKeg2Ko.readlines()) {
			String[] ss=content2.split("\t");
			String kegID=ss[0];String ko=ss[1].trim();
			KGIDkeg2Ko kgDkeg2Ko=new KGIDkeg2Ko();
			kgDkeg2Ko.setKeggID(kegID);kgDkeg2Ko.setKo(ko);kgDkeg2Ko.setTaxID(TaxID);
			
			List<KGIDkeg2Ko> ls = servKIDKeg2Ko.findLsByKegIdAndTaxId(kegID, TaxID);
			
			if (ls == null || ls.size() == 0) {
				servKIDKeg2Ko.save(kgDkeg2Ko);
			}
		}
	}
	
	/**
	 * 将从kegg上下载的化合物文件导入数据库
	 * @param compFile
	 * @throws Exception 
	 */
	public static void upDateKegCompound(String compFile) throws Exception {
		ServKNIdKeg servKNIdKeg = ServKNIdKeg.getInstance();
		ServKNCompInfo servKNCompInfo = ServKNCompInfo.getInstance();
		
		TxtReadandWrite txtComp = new TxtReadandWrite(compFile);
		BufferedReader readerComp = txtComp.readfile();
		String content = "";
		while ((content = readerComp.readLine())!=null) 
		{
			if (content.startsWith("ENTRY")) 
			{
				String[] ss = content.split("\\s+");
				KGNCompInfo kgnCompInfo = new KGNCompInfo();
				String kegID = ss[1];
				kgnCompInfo.setKegID(kegID);
				//将keggID装入
				KGNIdKeg kgnIdKeg = new KGNIdKeg();
				kgnIdKeg.setAttribute("Compound");
				kgnIdKeg.setKegID(kegID);
				kgnIdKeg.setUsualName(kegID);
				servKNIdKeg.save(kgnIdKeg);
				
				//读取Name那一列
				content = readerComp.readLine();
				String name = content.replace("NAME", "").trim();
				String nameAll = "";
				if (name.contains(";")) {
					name = name.replace(";", "").trim();
					kgnIdKeg.setAttribute("Compound");
					kgnIdKeg.setKegID(kegID);
					kgnIdKeg.setUsualName(name);
					servKNIdKeg.save(kgnIdKeg);
					nameAll = name;
					name = "";
				}
				while ((content = readerComp.readLine()).startsWith(" ")) {
						name = name + content.trim();
						if (name.contains(";")) {
							name = name.replace(";", "").trim();
							kgnIdKeg.setAttribute("Compound");
							kgnIdKeg.setKegID(kegID);
							kgnIdKeg.setUsualName(name);
							servKNIdKeg.save(kgnIdKeg);
							nameAll = nameAll + "//"+name;
							name = "";
						}
				}
				if (!name.trim().equals("")) {
					name = name.replace(";", "").trim();
					kgnIdKeg.setAttribute("Compound");
					kgnIdKeg.setKegID(kegID);
					kgnIdKeg.setUsualName(name);
					servKNIdKeg.save(kgnIdKeg);
					if (nameAll.trim().equals("")) {
						nameAll = name;
					}
					else {
						nameAll = nameAll + "//"+name;
					}
					name = "";
				}
				kgnCompInfo.setUsualName(nameAll);
				if (content.startsWith("FORMULA")) {
					String formula = content.replace("FORMULA", "").trim();
					kgnCompInfo.setFormula(formula);
					content = readerComp.readLine();
				}
				if (content.startsWith("MASS")) {
					double mass = Double.parseDouble(content.replace("MASS", "").trim());
					kgnCompInfo.setMass(mass);
					content = readerComp.readLine();
				}
				if (content.startsWith("REMARK")) {
					String remark = content.substring(content.indexOf(":")+1).trim();
					if (!remark.trim().equals("")) {
						kgnCompInfo.setRemark(remark);
					}
					content = readerComp.readLine();
				}
				servKNCompInfo.save(kgnCompInfo);
				continue;
			}
		}
		txtComp.close();
	}
}
