package com.novelbio.analysis.annotation.pathway.kegg.kGML2DB;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathRelation;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.domain.kegg.KGreaction;
import com.novelbio.database.domain.kegg.KGrelation;
import com.novelbio.database.domain.kegg.KGsubstrate;
import com.novelbio.database.service.servgeneanno.ServTaxID;
import com.novelbio.database.service.servkegg.ServKEntry;
import com.novelbio.database.service.servkegg.ServKPathRelation;
import com.novelbio.database.service.servkegg.ServKPathway;
import com.novelbio.database.service.servkegg.ServKReaction;
import com.novelbio.database.service.servkegg.ServKRelation;


 
/**
 * 将读取的KGML文件导入数据库
 * @author zong0jie
 *
 */
public class KGML2DB 
{
	private static Logger logger = Logger.getLogger(KGML2DB.class);
	/**
	 * 将KGML导入数据库
	 * @param filePath
	 * @throws Exception
	 */
	public static void readKGML(String filePath)
	{
		ArrayList<String[]> lsKGML=FileOperate.getFoldFileName(filePath, "*", "xml");
		Serializer serializer = new Persister();
		for (int i = 0; i < lsKGML.size(); i++) {
			File source = new File(filePath+"/"+lsKGML.get(i)[0]+"."+lsKGML.get(i)[1]);
			System.out.println(source.getAbsolutePath());
			KGML example = null;
			try {
				example = serializer.read(KGML.class, source);
			} catch (Exception e) {
				logger.error("文件出错："+source.getAbsolutePath());
			}
			kgml2DB(example);
		}
	}
	
	private static void kgml2DB(KGML kgml) 
	{
		ServKEntry servKEntry = new ServKEntry();
		ServKPathRelation servKPathRelation = new ServKPathRelation();
		ServKPathway servKPathway = new ServKPathway();
		ServKReaction servKReaction = new ServKReaction();
		ServKRelation servKRelation = new ServKRelation();
		ServTaxID servTaxID = new ServTaxID();
		//获得具体物种
		String taxAbbr=kgml.getSpecies();
		TaxInfo taxInfo = new TaxInfo(); taxInfo.setAbbr(taxAbbr);
		TaxInfo taxInfo2= servTaxID.queryTaxInfo(taxInfo);
		int taxID=0;
		if (taxInfo2 != null) {
			taxID = taxInfo2.getTaxID();
		}
		
		///////////////////装入entry/////////////////////////////////////////////////////////////////
		///////////////////注意，如果entry的type为group，那么将后面里面的component装入relation中//////////////////////////////////////////////////////////////////////////////
		ArrayList<Entry> lsEntry=kgml.getLsEntries();
		for (int i = 0; i < lsEntry.size(); i++)
		{
			
			KGentry kGentry=new KGentry();
			kGentry.setTaxID(taxID);
			kGentry.setPathName(kgml.getPathName());
			kGentry.setID(lsEntry.get(i).getID());
			kGentry.setType(lsEntry.get(i).getType());
			kGentry.setLinkEntry(lsEntry.get(i).getLinkEntry());
			//kGentry.setReaction(lsEntry.get(i).getReaction());
			///////////////////////////////////////////////////////////////////////////////////
			if (kGentry.getType().equals("group"))
			{
				kGentry.setEntryName(lsEntry.get(i).getEntryName());//仅仅是为了下面检查名字是不是"undefined"
				//如果是group的话，先看看name是不是undefined，不是的话报错
				if (!kGentry.getEntryName().equals("undefined")) 
				{
					System.out.println(kGentry.getPathName()+"的entry的type为group，但是name不是undefined"+"   是"+kGentry.getEntryName());
					continue;
				}
				else 
				{
					////////////////////////////////////////将所有component分别对数据库的parentID进行升级///////////////////////////////////////////////////////////////////////////
					int compNum= lsEntry.get(i).getLsComponent().size();
					for (int j = 0; j < compNum; j++)
					{
						int entryID=lsEntry.get(i).getLsComponent().get(j).getComID();//获得该component中一个entry的ID
						//获得那个子类的entryID
						Entry entryComp=kgml.getEntry(entryID);
						//将子类的信息赋值给kgentry
						kGentry.setID(entryComp.getID());
						kGentry.setLinkEntry(entryComp.getLinkEntry());
						kGentry.setType(entryComp.getType());
						/////
						String[] ss2=new String[1]; ss2[0]="";

						if (!entryComp.getReaction().equals("")) {
							 ss2=entryComp.getReaction().trim().split(" +"); 
						}
						if (!lsEntry.get(i).getReaction().equals("")) {
							ss2=lsEntry.get(i).getReaction().trim().split(" +"); 
						}
						//不知道会不会有这种情况，group中的reaction和其中的component的reaction不一致，这个就是来测试的，如果发现不一致了，那么就报错。
						if (!entryComp.getReaction().equals("")&&
								!lsEntry.get(i).getReaction().equals("")
										&&!lsEntry.get(i).getReaction().equals(    entryComp.getReaction()   	)
								)
						{
							System.out.println(kGentry.getPathName()+"Component Error");
						}
						//子类赋值component信息
						kGentry.setCompID(j);
						kGentry.setCompNum(compNum);//component的数量
						
						////名字是"sma:SAV_2461 sma:SAV_3026 sma:SAV_3027"空格分开
						String[] ss=entryComp.getEntryName().trim().split(" +");
						for (int k = 0; k < ss.length; k++) 
						{
							for (int k2 = 0; k2 < ss2.length; k2++) {
								kGentry.setEntryName(ss[k]);
								kGentry.setReaction(ss2[k2]);
								//因为本循环中kGentry一直没有new，所以前一次的ParentID会继续存在，从而干扰查询，所以要先清零
								kGentry.setParentID(0);
								//先用不包含parentID的kgentry查找数据库，没找到就插入，找到就升级，实际也就是将parentID加上去
								if (servKEntry.queryKGentry(kGentry)!=null) 
								{
									
									kGentry.setParentID(lsEntry.get(i).getID());
									//这里可能会报错，这是由于前面单个组分已经输入了一遍，所以这个错误没关系可以忽略
									servKEntry.updateKGentry(kGentry);
								}
								else 
								{
									kGentry.setReaction(ss2[k2]);
									kGentry.setParentID(lsEntry.get(i).getID());
									servKEntry.insertKGentry(kGentry);
								}
							}
						}
					}
				}
			}
			else if (kGentry.getType().equals("map")) {
				String[] ss=lsEntry.get(i).getEntryName().trim().split(" +");
				String[] ss2=lsEntry.get(i).getReaction().trim().split(" +"); 
				for (int j = 0; j < ss.length; j++) 
				{
					//如果是本pathway，则跳过，否则的话装入pathRelation表中
					if (ss[j].trim().equals("")||ss[j].trim().equals(kgml.getPathName())) {
						continue;
					}
					for (int k2 = 0; k2 < ss2.length; k2++) {
						kGentry.setEntryName(ss[j]);kGentry.setReaction(ss2[k2]);
						if (servKEntry.queryKGentry(kGentry)==null) 
						{
							servKEntry.insertKGentry(kGentry);
						}
					}
					/////////////////////////////可能做一个单独的map关系网络会更好
					KGpathRelation kGpathRelation=new KGpathRelation();
					kGpathRelation.setPathName(kgml.getPathName());
					kGpathRelation.setScrPath(kgml.getPathName());
					kGpathRelation.setTrgPath(ss[j]);
					if (servKPathRelation.queryKGpathRelation(kGpathRelation)==null) {
						kGpathRelation.setType("relate");
						servKPathRelation.insertKGpathRelation(kGpathRelation);
					}
				}
			}
			else 
			{
				String[] ss=lsEntry.get(i).getEntryName().trim().split(" +");
				String[] ss2=lsEntry.get(i).getReaction().trim().split(" +"); 
				for (int j = 0; j < ss.length; j++) 
				{
					if (ss[j].trim().equals("")) {
						continue;
					}
					for (int j2 = 0; j2 < ss2.length; j2++) {
						kGentry.setEntryName(ss[j]);kGentry.setReaction(ss2[j2]);
						if (servKEntry.queryKGentry(kGentry)==null) 
						{
							servKEntry.insertKGentry(kGentry);
						}
					}
			
				}
			}
		}
		
		///////////////////装入pathway/////////////////////////////////////////////////////////////////
		KGpathway kGpathway=new KGpathway();
		kGpathway.setTaxID(taxID);
		kGpathway.setPathName(kgml.getPathName());
		kGpathway.setSpecies(kgml.getSpecies());
		kGpathway.setMapNum(kgml.getMapNum());
		kGpathway.setTitle(kgml.getTitle());
		kGpathway.setLinkUrl(kgml.getLinkUrl());
		if (servKPathway.queryKGpathway(kGpathway)==null)
		{
			servKPathway.insertKGpathway(kGpathway);
		}
		
		//////////////////装入reaction和substrate//////////////////////////////////////////////////////////////////
		ArrayList<Reaction> lsReactions=kgml.getLsrReactions();
		if (lsReactions!=null) {
			for (int i = 0; i < lsReactions.size(); i++)
			{
				KGreaction kGreaction=new KGreaction();
				kGreaction.setPathName(kgml.getPathName());
				kGreaction.setID(lsReactions.get(i).getID());
			
				kGreaction.setType(lsReactions.get(i).getType());
				if (lsReactions.get(i).getAlt()!=null) {
					kGreaction.setAlt(lsReactions.get(i).getAlt().getName());
				}
				String[] ss2=lsReactions.get(i).getName().trim().split(" +");
				for (int j = 0; j < ss2.length; j++) {
					kGreaction.setName(ss2[j]);
					if (servKReaction.queryKGreaction(kGreaction)==null)
					{
						servKReaction.insertKGreaction(kGreaction);
					}
				}
				ArrayList<Substrate> lsSubstrates=lsReactions.get(i).getLsSubstrate();
				if (lsSubstrates!=null&&lsSubstrates.size()>0) 
				{
					KGsubstrate kGsubstrate=new KGsubstrate();
					kGsubstrate.setPathName(kgml.getPathName());
					kGsubstrate.setReactionID(kGreaction.getID());
					for (int j = 0; j < lsSubstrates.size(); j++) {
						kGsubstrate.setID(lsSubstrates.get(j).getID());
						kGsubstrate.setName(lsSubstrates.get(j).getName());
						kGsubstrate.setType("substrate");
					}
				}
				ArrayList<Product> lsProduct=lsReactions.get(i).getLsProduct();
				if (lsProduct!=null&&lsProduct.size()>0) 
				{
					KGsubstrate kGsubstrate=new KGsubstrate();
					kGsubstrate.setPathName(kgml.getPathName());
					kGsubstrate.setReactionID(kGreaction.getID());
					for (int j = 0; j < lsProduct.size(); j++) 
					{
						kGsubstrate.setID(lsProduct.get(j).getID());
						kGsubstrate.setName(lsProduct.get(j).getName());
						kGsubstrate.setType("product");
					}
				}
			}
		}
		
		
		/////////////////装入relation/////////////////////////////////////////////////////////////////////
		ArrayList<Relation> lsRelations=kgml.getLsRelations();
		if (lsRelations!=null) {
			for (int i = 0; i < lsRelations.size(); i++)
			{
				KGrelation kGrelation=new KGrelation();
				kGrelation.setPathName(kgml.getPathName());
				kGrelation.setEntry1ID(lsRelations.get(i).getEntry1ID());
				kGrelation.setEntry2ID(lsRelations.get(i).getEntry2ID());
				kGrelation.setType(lsRelations.get(i).getType());
				/////////如果一个relation中有多种相互作用的类型，那么现在的处理就是将这么多种类型拆成多列来存入数据库。考虑将多种结合写入一行用//隔开，如active//binding//expression////////////////////////////////////////////////////////////////////////////////////////////
				if (lsRelations.get(i).getLsSubtype()!=null) {
					for (int j = 0; j < lsRelations.get(i).getLsSubtype().size(); j++) {
						kGrelation.setSubtypeName(lsRelations.get(i).getLsSubtype().get(j).getName());
						kGrelation.setSubtypeValue(lsRelations.get(i).getLsSubtype().get(j).getValue());
						if (servKRelation.queryKGrelation(kGrelation)==null)
						{
							servKRelation.insertKGrelation(kGrelation);
						}
					}
				}
		
			}
		}
	}
}
