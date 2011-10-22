package com.novelbio.analysis.annotation.pathway.kegg.kGML2DB;

import java.io.BufferedReader;
import java.util.ArrayList;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.KEGGDAO.DaoKIDKeg2Ko;
import com.novelbio.database.DAO.KEGGDAO.DaoKIDgen2Keg;
import com.novelbio.database.DAO.KEGGDAO.DaoKNCompInfo;
import com.novelbio.database.DAO.KEGGDAO.DaoKNIdKeg;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.kegg.KGIDgen2Keg;
import com.novelbio.database.entity.kegg.KGIDkeg2Ko;
import com.novelbio.database.entity.kegg.noGene.KGNCompInfo;
import com.novelbio.database.entity.kegg.noGene.KGNIdKeg;

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
	public static void upDateGen2Keg(String gen2KegFile) throws Exception 
	{
		TxtReadandWrite txtgene2Keg=new TxtReadandWrite();
		txtgene2Keg.setParameter(gen2KegFile, false, true);
		int TaxID=0;
		////////////////获得taxID////////////////////////////////////////////////////////
		BufferedReader reader=txtgene2Keg.readfile();
		String content="";
		while ((content=reader.readLine())!=null) 
		{
			String[] ss=content.split("\t"); 
			long geneID=Long.parseLong(ss[1].replace("ncbi-geneid:", "").replace("equivalent", "").trim());
			NCBIID ncbiid=new NCBIID();
			ncbiid.setGeneId(geneID);
			ArrayList<NCBIID> lsNcbiids=DaoFSNCBIID.queryLsNCBIID(ncbiid);
			if (lsNcbiids!=null&&lsNcbiids.size()>0) {
				TaxID=(int) lsNcbiids.get(0).getTaxID();
				break;
			}
		}
		while ((content=reader.readLine())!=null) 
		{
			String[] ss=content.split("\t"); 
			long geneID=Long.parseLong(ss[1].replace("ncbi-geneid:", "").replace("equivalent", "").trim());
			String accID = ss[0].split(":")[1].trim();
			NCBIID ncbiid=new NCBIID();
			ncbiid.setGeneId(geneID); ncbiid.setAccID(accID); ncbiid.setTaxID(TaxID); 
			ArrayList<NCBIID> lsNcbiids=DaoFSNCBIID.queryLsNCBIID(ncbiid);
			if (lsNcbiids==null || lsNcbiids.size() == 0) {
				ncbiid.setDBInfo("KEGG");
				DaoFSNCBIID.InsertNCBIID(ncbiid);
			}
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////////////////
		if (TaxID==0) {
			System.err.println("在NCBIID表中没有找到该物种的taxID");
			return;
		}
		BufferedReader reader2=txtgene2Keg.readfile();
		String content2="";
		while ((content2=reader2.readLine())!=null) 
		{
			String[] ss=content2.split("\t");
			String kegID=ss[0];long geneID=Long.parseLong(ss[1].replace("ncbi-geneid:", "").trim());
			KGIDgen2Keg kgiDgen2Keg=new KGIDgen2Keg();
			kgiDgen2Keg.setGeneID(geneID);kgiDgen2Keg.setKeggID(kegID);kgiDgen2Keg.setTaxID(TaxID);
			if (DaoKIDgen2Keg.queryLsKGIDgen2Keg(kgiDgen2Keg) == null || DaoKIDgen2Keg.queryLsKGIDgen2Keg(kgiDgen2Keg).size() == 0) {
				DaoKIDgen2Keg.InsertKGIDgen2Keg(kgiDgen2Keg);
			}
		}
	}
	
	
	/**
	 * 首先用kegID在gen2Keg表中找taxID
	 * 然后倒入keg2Ko表
	 * @param keg2KoFile
	 * @throws Exception 
	 */
	public static void upDateKeg2Ko(String keg2KoFile) throws Exception 
	{
		TxtReadandWrite txtKeg2Ko=new TxtReadandWrite();
		txtKeg2Ko.setParameter(keg2KoFile, false, true);
		int TaxID=0;
		////////////////获得taxID////////////////////////////////////////////////////////
		BufferedReader reader=txtKeg2Ko.readfile();
		String content="";
		while ((content=reader.readLine())!=null) 
		{
			String[] ss=content.split("\t"); 
			String kegID=ss[0].trim();
			KGIDgen2Keg kgiDgen2Keg=new KGIDgen2Keg();
			kgiDgen2Keg.setKeggID(kegID);
			KGIDgen2Keg kgiDgen2Keg2=DaoKIDgen2Keg.queryKGIDgen2Keg(kgiDgen2Keg);
			if (kgiDgen2Keg2!=null) {
				TaxID=kgiDgen2Keg2.getTaxID();
				break;
			}
		}
		///////////////////////////////////////////////////////////////////////////////////////
		if (TaxID==0) {
			System.err.println("在gene2Keg表中没有找到该物种的taxID");
			return;
		}
		BufferedReader reader2=txtKeg2Ko.readfile();
		String content2="";
		while ((content2=reader2.readLine())!=null) 
		{
			String[] ss=content2.split("\t");
			String kegID=ss[0];String ko=ss[1].trim();
			KGIDkeg2Ko kgDkeg2Ko=new KGIDkeg2Ko();
			kgDkeg2Ko.setKeggID(kegID);kgDkeg2Ko.setKo(ko);kgDkeg2Ko.setTaxID(TaxID);
			if (DaoKIDKeg2Ko.queryLsKGIDkeg2Ko(kgDkeg2Ko) == null || DaoKIDKeg2Ko.queryLsKGIDkeg2Ko(kgDkeg2Ko).size() == 0) {
				DaoKIDKeg2Ko.InsertKGIDkeg2Ko(kgDkeg2Ko);
			}
		}
	}
	
	/**
	 * 将从kegg上下载的化合物文件导入数据库
	 * @param compFile
	 * @throws Exception 
	 */
	public static void upDateKegCompound(String compFile) throws Exception
	{
		TxtReadandWrite txtComp = new TxtReadandWrite();
		txtComp.setParameter(compFile, false, true);
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
				DaoKNIdKeg.InsertKGNIdKeg(kgnIdKeg);
				
				//读取Name那一列
				content = readerComp.readLine();
				String name = content.replace("NAME", "").trim();
				String nameAll = "";
				if (name.contains(";")) {
					name = name.replace(";", "").trim();
					kgnIdKeg.setAttribute("Compound");
					kgnIdKeg.setKegID(kegID);
					kgnIdKeg.setUsualName(name);
					DaoKNIdKeg.InsertKGNIdKeg(kgnIdKeg);
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
							DaoKNIdKeg.InsertKGNIdKeg(kgnIdKeg);
							nameAll = nameAll + "//"+name;
							name = "";
						}
				}
				if (!name.trim().equals("")) {
					name = name.replace(";", "").trim();
					kgnIdKeg.setAttribute("Compound");
					kgnIdKeg.setKegID(kegID);
					kgnIdKeg.setUsualName(name);
					DaoKNIdKeg.InsertKGNIdKeg(kgnIdKeg);
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
				DaoKNCompInfo.InsertKGNCompInfo(kgnCompInfo);
				continue;
			}
		}
	}
}
