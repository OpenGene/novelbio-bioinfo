package com.novelBio.annotation.pathway.kegg.kGML2DB;

import java.io.File;
import java.util.ArrayList;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.novelBio.base.fileOperate.FileOperate;

import DAO.FriceDAO.DaoFSTaxID;
import DAO.KEGGDAO.DaoKEntry;
import DAO.KEGGDAO.DaoKPathRelation;
import DAO.KEGGDAO.DaoKPathway;
import DAO.KEGGDAO.DaoKReaction;
import DAO.KEGGDAO.DaoKRealtion;

import entity.friceDB.TaxInfo;
import entity.kegg.KGentry;
import entity.kegg.KGpathRelation;
import entity.kegg.KGpathway;
import entity.kegg.KGreaction;
import entity.kegg.KGrelation;
import entity.kegg.KGsubstrate;

 
/**
 * ����ȡ��KGML�ļ��������ݿ�
 * @author zong0jie
 *
 */
public class KGML2DB 
{
	/**
	 * ��KGML�������ݿ�
	 * @param filePath
	 * @throws Exception
	 */
	public static void readKGML(String filePath) throws Exception 
	{
		ArrayList<String[]> lsKGML=FileOperate.getFoldFileName(filePath, "*", "xml");
		Serializer serializer = new Persister();
		for (int i = 0; i < lsKGML.size(); i++) {
			File source = new File(filePath+"/"+lsKGML.get(i)[0]+"."+lsKGML.get(i)[1]);
			KGML example = serializer.read(KGML.class, source);
			kgml2DB(example);
		}
	}
	
	private static void kgml2DB(KGML kgml) 
	{
		//��þ�������
		String taxAbbr=kgml.getSpecies();
		TaxInfo taxInfo = new TaxInfo(); taxInfo.setAbbr(taxAbbr);
		TaxInfo taxInfo2= DaoFSTaxID.queryTaxInfo(taxInfo);
		int taxID=0;
		if (taxInfo2 != null) {
			taxID = taxInfo2.getTaxID();
		}
		
		///////////////////װ��entry/////////////////////////////////////////////////////////////////
		///////////////////ע�⣬���entry��typeΪgroup����ô�����������componentװ��relation��//////////////////////////////////////////////////////////////////////////////
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
				kGentry.setEntryName(lsEntry.get(i).getEntryName());//������Ϊ�������������ǲ���"undefined"
				//�����group�Ļ����ȿ���name�ǲ���undefined�����ǵĻ�����
				if (!kGentry.getEntryName().equals("undefined")) 
				{
					System.out.println(kGentry.getPathName()+"��entry��typeΪgroup������name����undefined"+"   ��"+kGentry.getEntryName());
					continue;
				}
				else 
				{
					////////////////////////////////////////������component�ֱ�����ݿ��parentID��������///////////////////////////////////////////////////////////////////////////
					int compNum= lsEntry.get(i).getLsComponent().size();
					for (int j = 0; j < compNum; j++)
					{
						int entryID=lsEntry.get(i).getLsComponent().get(j).getComID();//��ø�component��һ��entry��ID
						//����Ǹ������entryID
						Entry entryComp=kgml.getEntry(entryID);
						//���������Ϣ��ֵ��kgentry
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
						//��֪���᲻�������������group�е�reaction�����е�component��reaction��һ�£�������������Եģ�������ֲ�һ���ˣ���ô�ͱ�����
						if (!entryComp.getReaction().equals("")&&
								!lsEntry.get(i).getReaction().equals("")
										&&!lsEntry.get(i).getReaction().equals(    entryComp.getReaction()   	)
								)
						{
							System.out.println(kGentry.getPathName()+"Component Error");
						}
						//���ำֵcomponent��Ϣ
						kGentry.setCompID(j);
						kGentry.setCompNum(compNum);//component������
						
						////������"sma:SAV_2461 sma:SAV_3026 sma:SAV_3027"�ո�ֿ�
						String[] ss=entryComp.getEntryName().trim().split(" +");
						for (int k = 0; k < ss.length; k++) 
						{
							for (int k2 = 0; k2 < ss2.length; k2++) {
								kGentry.setEntryName(ss[k]);
								kGentry.setReaction(ss2[k2]);
								//��Ϊ��ѭ����kGentryһֱû��new������ǰһ�ε�ParentID��������ڣ��Ӷ����Ų�ѯ������Ҫ������
								kGentry.setParentID(0);
								//���ò�����parentID��kgentry�������ݿ⣬û�ҵ��Ͳ��룬�ҵ���������ʵ��Ҳ���ǽ�parentID����ȥ
								if (DaoKEntry.queryKGentry(kGentry)!=null) 
								{
									
									kGentry.setParentID(lsEntry.get(i).getID());
									//������ܻᱨ������������ǰ�浥������Ѿ�������һ�飬�����������û��ϵ���Ժ���
									DaoKEntry.upDateKGentry(kGentry);
								}
								else 
								{
									kGentry.setReaction(ss2[k2]);
									kGentry.setParentID(lsEntry.get(i).getID());
									DaoKEntry.InsertKGentry(kGentry);
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
					//����Ǳ�pathway��������������Ļ�װ��pathRelation����
					if (ss[j].trim().equals("")||ss[j].trim().equals(kgml.getPathName())) {
						continue;
					}
					for (int k2 = 0; k2 < ss2.length; k2++) {
						kGentry.setEntryName(ss[j]);kGentry.setReaction(ss2[k2]);
						if (DaoKEntry.queryKGentry(kGentry)==null) 
						{
							DaoKEntry.InsertKGentry(kGentry);
						}
					}
					/////////////////////////////������һ��������map��ϵ��������
					KGpathRelation kGpathRelation=new KGpathRelation();
					kGpathRelation.setPathName(kgml.getPathName());
					kGpathRelation.setScrPath(kgml.getPathName());
					kGpathRelation.setTrgPath(ss[j]);
					if (DaoKPathRelation.queryKGpathRelation(kGpathRelation)==null) {
						kGpathRelation.setType("relate");
						DaoKPathRelation.InsertKGpathRelation(kGpathRelation);
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
						if (DaoKEntry.queryKGentry(kGentry)==null) 
						{
							DaoKEntry.InsertKGentry(kGentry);
						}
					}
			
				}
			}
		}
		
		///////////////////װ��pathway/////////////////////////////////////////////////////////////////
		KGpathway kGpathway=new KGpathway();
		kGpathway.setTaxID(taxID);
		kGpathway.setPathName(kgml.getPathName());
		kGpathway.setSpecies(kgml.getSpecies());
		kGpathway.setMapNum(kgml.getMapNum());
		kGpathway.setTitle(kgml.getTitle());
		kGpathway.setLinkUrl(kgml.getLinkUrl());
		if (DaoKPathway.queryKGpathway(kGpathway)==null)
		{
			DaoKPathway.InsertKGpathway(kGpathway);
		}
		
		//////////////////װ��reaction��substrate//////////////////////////////////////////////////////////////////
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
					if (DaoKReaction.queryKGreaction(kGreaction)==null)
					{
						DaoKReaction.InsertKGreaction(kGreaction);
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
		
		
		/////////////////װ��relation/////////////////////////////////////////////////////////////////////
		ArrayList<Relation> lsRelations=kgml.getLsRelations();
		if (lsRelations!=null) {
			for (int i = 0; i < lsRelations.size(); i++)
			{
				KGrelation kGrelation=new KGrelation();
				kGrelation.setPathName(kgml.getPathName());
				kGrelation.setEntry1ID(lsRelations.get(i).getEntry1ID());
				kGrelation.setEntry2ID(lsRelations.get(i).getEntry2ID());
				kGrelation.setType(lsRelations.get(i).getType());
				/////////���һ��relation���ж����໥���õ����ͣ���ô���ڵĴ������ǽ���ô�������Ͳ�ɶ������������ݿ⡣���ǽ����ֽ��д��һ����//��������active//binding//expression////////////////////////////////////////////////////////////////////////////////////////////
				if (lsRelations.get(i).getLsSubtype()!=null) {
					for (int j = 0; j < lsRelations.get(i).getLsSubtype().size(); j++) {
						kGrelation.setSubtypeName(lsRelations.get(i).getLsSubtype().get(j).getName());
						kGrelation.setSubtypeValue(lsRelations.get(i).getLsSubtype().get(j).getValue());
						if (DaoKRealtion.queryKGrelation(kGrelation)==null)
						{
							DaoKRealtion.InsertKGrelation(kGrelation);
						}
					}
				}
		
			}
		}

	}

}














