package com.novelbio.database.updatedb.database;


import java.io.BufferedReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.annotation.genAnno.GOQuery;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.mapper.geneanno.MapBlastInfo;
import com.novelbio.database.mapper.geneanno.MapFSTaxID;
import com.novelbio.database.mapper.geneanno.MapGene2Go;
import com.novelbio.database.mapper.geneanno.MapGeneInfo;
import com.novelbio.database.mapper.geneanno.MapGo2Term;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.mapper.geneanno.MapUniGene2Go;
import com.novelbio.database.mapper.geneanno.MapUniGeneInfo;
import com.novelbio.database.mapper.geneanno.MapUniProtID;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.service.ServGo;


public class UpDateNBCDBFile {
	private static Logger logger = Logger.getLogger(UpDateNBCDBFile.class);
	/**
	 * 专门处理Gene2Go的表
	 * 将含有固定TaxID的gene2Go表插入数据库，插入时采用以下方法
	 * 首先看数据库中是否含有指定geneID和GOID的行
	 * 如果没有，直接插入
	 * 如果有，看Evidence那一项是否一样，一样的话，就跳过，不一样，就updata
	 * @author zong0jie
	 *
	 */
	public static void upDateGene2Go(String gene2GoFile) throws Exception
	{
		TxtReadandWrite txtgene2Go=new TxtReadandWrite();
		txtgene2Go.setParameter(gene2GoFile,false,true);
		BufferedReader gene2GoReader=txtgene2Go.readfile();
		String content="";
		int i=0;
		HashMap<String, String[]> goInfo = ServGo.getHashGo2Term();
		
		while ((content=gene2GoReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			String[] thisgoInfo = goInfo.get(ss[2].trim());
			if (thisgoInfo == null) {
				continue;
			}
			String goID = thisgoInfo[1];
			String goFunction = thisgoInfo[3];
			String goTerm = thisgoInfo[2];
			Gene2Go gene2GoInfo=new Gene2Go();
			gene2GoInfo.setGeneId(Long.parseLong(ss[1]));
			gene2GoInfo.setGOID(goID);
			gene2GoInfo.setEvidence(ss[3]);
			gene2GoInfo.setQualifier(ss[4]);
			gene2GoInfo.setGOTerm( goTerm);
			if(ss[6].equals("-"))
				gene2GoInfo.setReference(ss[6]);
			else 
				gene2GoInfo.setReference("PMID:"+ss[6]);
			
			gene2GoInfo.setFunction(goFunction);
			gene2GoInfo.setDataBase("NCBI");
			Gene2Go gene2GoInfo2=(Gene2Go) MapGene2Go.queryGene2Go(gene2GoInfo);
			if (gene2GoInfo2==null) 
			{
				MapGene2Go.InsertGene2Go(gene2GoInfo);
				i++;
			}
			else {
				//如果已经含有相应的GOID，那么看evidence是否也含有了，没有的话就upDate
				if(gene2GoInfo2.getEvidence() !=null 
						&& !gene2GoInfo2.getEvidence().trim().equals("")
						&& !gene2GoInfo2.getEvidence().contains(gene2GoInfo.getEvidence()))
				{
					gene2GoInfo.setEvidence(gene2GoInfo.getEvidence()+"//"+gene2GoInfo2.getEvidence());
					MapGene2Go.upDateGene2Go(gene2GoInfo);
					i++;
				}
			}
			
			if (i>0 && i%10000==0) {
				System.out.println(i);
			}
		}
		txtgene2Go.close();
	}
	
	
	
	/**
	 * 将NCBIID类型的表插入数据库
	 * 物种 \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
	 * 待导入的结果中的geneID可能含有： geneID//geneID//geneID<br>
	 * 注意一定要分开。也就是如果一个accID对应多个geneID，那么就将这两个geneID都插入NCBIID
	 * 用geneID和accessID一起去查找NCBIID表
	 * 如果没有，直接插入
	 * 如果有跳过
	 * @author zong0jie
	 *
	 */
	public static void upDateNCBIID(String NCBIIDfile,boolean updateDBINFO) throws Exception
	{
		TxtReadandWrite txtNCBIID=new TxtReadandWrite();
		txtNCBIID.setParameter(NCBIIDfile,false,true);
		BufferedReader ncbiReader=txtNCBIID.readfile();
		
		String content="";
		int i=0;
		while ((content=ncbiReader.readLine())!=null) 
		{
			String[] ss = content.split("\t");
			String[] ssGeneID = ss[1].split("//");
			//多个geneID分别都试一遍
			for (String string : ssGeneID) 
			{
				NCBIID ncbiid = new NCBIID();
				ncbiid.setTaxID(Integer.parseInt(ss[0]));
				ncbiid.setGeneId((long)Double.parseDouble(string));
				ncbiid.setAccID(ss[2]); 
				//如果搜到了，那么看是否需要更新DBINFO
				ArrayList<NCBIID> lsNcbiids = MapNCBIID.queryLsNCBIID(ncbiid);
				if (lsNcbiids != null && lsNcbiids.size() > 0) 
				{
					//如果需要更新dbinfo，那么看现有的dbinfo是否和老的一样，如果不一样的话，才更新
					if (updateDBINFO && !lsNcbiids.get(0).getDBInfo().equals(ss[3])) 
					{
						ncbiid.setDBInfo(ss[3]);
						MapNCBIID.upDateNCBIID(ncbiid);
						i++;
					}
					else
						continue;
				}
				else
				{
					ncbiid.setDBInfo(ss[3]);
					MapNCBIID.insertNCBIID(ncbiid);
					i++;
				}
			}
			if (i>0 && i%10000==0) {
				System.out.println(i);
			}
		}
		System.out.println(i);
		txtNCBIID.close();
	}
	/**
	 * 将芯片探针和nr数据库blast后处理得到的NCBIID类型的表插入数据库
	 * 物种 \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
	 * 待导入的结果中的geneID可能含有： geneID//geneID//geneID<br>
	 * 注意一定要分开。也就是如果一个accID对应多个geneID，那么就将这两个geneID都插入NCBIID
	 * 用geneID和accessID一起去查找NCBIID表
	 * 如果没有，直接插入
	 * 如果有跳过
	 * @author zong0jie
	 *
	 */
	public static void upDateNCBIIDBlast(String NCBIIDfile) throws Exception
	{
		TxtReadandWrite txtNCBIID = new TxtReadandWrite();
		txtNCBIID.setParameter(NCBIIDfile,false,true);
		BufferedReader ncbiReader = txtNCBIID.readfile();
		
		String content="";
		int i=0;
		while ((content = ncbiReader.readLine())!=null) 
		{
			String[] ss = content.split("\t");
			String[] geneID = ss[1].split("//");
			NCBIID ncbiid = new NCBIID();
			ncbiid.setTaxID(Integer.parseInt(ss[0]));
			ncbiid.setAccID(ss[2]); 
			//如果搜到了，那么看是否需要更新DBINFO
			ArrayList<NCBIID> lsNcbiids = MapNCBIID.queryLsNCBIID(ncbiid);
			if (lsNcbiids != null && lsNcbiids.size() > 0) 
			{
				continue;
			}
			for (String string : geneID) 
			{
				long ssGeneID = (long)Double.parseDouble(string);
				//多个geneID分别都试一遍
				ncbiid.setGeneId(ssGeneID);
				ncbiid.setDBInfo(ss[3]);
				MapNCBIID.insertNCBIID(ncbiid);
				i++;
				if (i%10000==0) {
					System.out.println(i);
				}
			}
		}
		System.out.println(i);
		txtNCBIID.close();
	}
	/**
	 * 将NCBIID类型的表插入数据库，这个是当NCBIID很全面的时候，有时候affy芯片里面有一些geneID是已经过期的ID，这时候这种ID就要舍去
	 * 时采用以下方法
	 * <b>先用GeneID去查找NCBIID表</b>，如果找到了
	 * 用geneID和accessID一起去查找NCBIID表
	 * 如果没有，直接插入<br>
	 * <b>如果没找到，那么将没找到的行写入一个文件中</b>
	 * 如果有跳过
	 * @author zong0jie
	 *
	 */
	public static void upDateNCBIID(String NCBIIDfile,String noFindFile) throws Exception
	{
		TxtReadandWrite txtNCBIID=new TxtReadandWrite();
		txtNCBIID.setParameter(NCBIIDfile,false,true);
		BufferedReader ncbiReader=txtNCBIID.readfile();
		
		TxtReadandWrite txtnoFindFile=new TxtReadandWrite();
		txtnoFindFile.setParameter(noFindFile, true, false);
			
		String content="";
		int i=0;
		while ((content=ncbiReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			NCBIID ncbiid=new NCBIID();
			
			ncbiid.setGeneId((long)Double.parseDouble(ss[1]));//先用geneID搜
			ArrayList<NCBIID> lsncbiid=MapNCBIID.queryLsNCBIID(ncbiid);
			if (lsncbiid==null||lsncbiid.size()==0) 
			{
				txtnoFindFile.writefile(content+"\n");
				continue;
			}

			ncbiid.setAccID(ss[2]);
			
			//geneID和 accID两个一起查看是否能查到
			NCBIID ncbiid2=MapNCBIID.queryNCBIID(ncbiid);
			if (ncbiid2==null) 
			{
				ncbiid.setTaxID(Integer.parseInt(ss[0]));
				ncbiid.setDBInfo(ss[3]);
				MapNCBIID.insertNCBIID(ncbiid);
			}
			else {
				continue;
			}
			i++;
			if (i%10000==0) {
				System.out.println(i);
			}
		}
		txtNCBIID.close();
		txtnoFindFile.close();
	}
	
	/**
	 * 将gene_refseq_uniprotkb_collab.txt 的表插入数据库，首先用refseq搜NCBIID，搜到的得到其geneID，然后与uniID一起查找NCBIID表并插入<br>
	 * 时采用以下方法<br>
	 * <b>首先用refseq搜NCBIID</b>，如果找到了
	 * 用geneID和uniID一起去查找NCBIID表，如果没找到，则插入<br>
	 * 
	 * <b>如果用refseq没找到，那么跳过</b><br>
	 * 如果有跳过
	 * @author zong0jie
	 *
	 */
	public static void upDateNCBIIDRef2Uni(String Ref2Unifile) throws Exception
	{
		TxtReadandWrite txtRef2Uni=new TxtReadandWrite();
		txtRef2Uni.setParameter(Ref2Unifile,false,true);
		BufferedReader ncbiReader=txtRef2Uni.readfile();
			
		String content="";
		int i=0;
		while ((content=ncbiReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			NCBIID ncbiid=new NCBIID();
			
			ncbiid.setAccID(ss[0]);//先用refseq搜
			ArrayList<NCBIID> lsncbiid=MapNCBIID.queryLsNCBIID(ncbiid);
			if (lsncbiid==null||lsncbiid.size()==0) 
			{
				continue;
			}
			for (NCBIID ncbiid3 : lsncbiid) {
//				ncbiid.setTaxID(ncbiid3.getTaxID());
				ncbiid.setGeneId(ncbiid3.getGeneId());
				ncbiid.setAccID(ss[1]);
				
				//geneID和 uniID两个一起查看是否能查到，能查到就跳过，查不到就插入
				NCBIID ncbiid2=MapNCBIID.queryNCBIID(ncbiid);
				if (ncbiid2==null) 
				{
					ncbiid.setTaxID(ncbiid3.getTaxID());
					ncbiid.setDBInfo(NovelBioConst.DBINFO_UNIPROT_UNIID);
					MapNCBIID.insertNCBIID(ncbiid);
					i++;
				}
				else {
					continue;
				}
			}
			
			
			if (i%10000==0) {
				System.out.println(i);
			}
		}
		System.out.println(i);
	}
	
	
	/**
	 * 将UniProtID类型的表插入数据库
	 * @param UniProtIDfile
	 * @param NCBIID 是否插入NCBIID表，
	 * true：将UniProtID表的accID查找NCBI表，找不到的写入文本
	 * false：直接将UniProtID表插入uniID表
	 * @throws Exception
	 */
	public static void upDateUniProtID(String UniProtIDfile,boolean NCBIID,String outUniProtIDfile) throws Exception
	{
		TxtReadandWrite txtUniProtID=new TxtReadandWrite();
		txtUniProtID.setParameter(UniProtIDfile,false,true);
		BufferedReader uniProtReader=txtUniProtID.readfile();
				
		TxtReadandWrite txtOutUniProt = new TxtReadandWrite();
		if (NCBIID) {
			txtOutUniProt.setParameter(outUniProtIDfile, true,false);
		}
		String content="";
		int i=0;
		while ((content=uniProtReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			int taxID = Integer.parseInt(ss[0]);
			if (NCBIID)
			{
				//先查找NCBIID表
				NCBIID ncbiid = new NCBIID();
				ncbiid.setTaxID(taxID);
				ncbiid.setAccID(ss[1]);
				ArrayList<NCBIID> lsNcbiids = MapNCBIID.queryLsNCBIID(ncbiid);
				if (lsNcbiids != null && lsNcbiids.size()>0) 
				{
					//查到的话，再用本列真正的accID去查NCBIID表，如果没查到，那么插入
					long geneID = lsNcbiids.get(0).getGeneId();
					ncbiid.setGeneId(geneID);
					ncbiid.setAccID(ss[2]);
					ArrayList<NCBIID> lsNcbiids2 = MapNCBIID.queryLsNCBIID(ncbiid);
					if (lsNcbiids2 == null || lsNcbiids2.size() == 0) {
						ncbiid.setDBInfo(ss[3]);
						MapNCBIID.insertNCBIID(ncbiid);
						i++;
					}
					continue;
				}
				else {
					txtOutUniProt.writefile(content + "\n");
				}
			}
			else 
			{
				UniProtID uniProtid=new UniProtID();
				uniProtid.setTaxID(Integer.parseInt(ss[0]));
				uniProtid.setUniID(ss[1]);
				uniProtid.setAccID(ss[2]);
				ArrayList<UniProtID> lsuniProtid2=MapUniProtID.queryLsUniProtID(uniProtid);
				if (lsuniProtid2==null || lsuniProtid2.size() == 0) 
				{
					uniProtid.setDBInfo(ss[3]);
					MapUniProtID.InsertUniProtID(uniProtid);
					i++;
				}
				else {
					continue;
				}
			}
			if (i>0&&i%10000==0) {
				System.out.println(i);
			}
		}
		System.out.println(i);
	}
	
	
	/**
	 * 将geneInfo类型的表插入数据库
	 * 时采用以下方法
	 * 用geneID去查找geneInfo表
	 * 如果没有，直接插入
	 * 如果有跳过
	 * @author zong0jie
	 *
	 */
	public static void upDateGeneInfo(String GeneInfoFile) throws Exception
	{
		TxtReadandWrite txtGeneInfo=new TxtReadandWrite();
		txtGeneInfo.setParameter(GeneInfoFile,false,true);
		BufferedReader ncbiReader=txtGeneInfo.readfile();
		
		String content="";
		int i=0;
		while ((content=ncbiReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			GeneInfo geneInfo=new GeneInfo();
			geneInfo.setGeneID((long)Double.parseDouble(ss[1]));
			geneInfo.setSymbol(ss[2]);	geneInfo.setLocusTag(ss[3]);geneInfo.setSynonyms(ss[4]);geneInfo.setDbXrefs(ss[5]);
			geneInfo.setChromosome(ss[6]);geneInfo.setMapLocation(ss[7]);geneInfo.setDescription(ss[8]);geneInfo.setTypeOfGene(ss[9]);
			geneInfo.setSymNome(ss[10]);geneInfo.setFullName(ss[11]);geneInfo.setNomStat(ss[12]);geneInfo.setOtherDesign(ss[13]);
			geneInfo.setModDate(ss[14]);

			GeneInfo geneInfo2=MapGeneInfo.queryGeneInfo(geneInfo);
			if (geneInfo2==null)
			{
				MapGeneInfo.InsertGeneInfo(geneInfo);
			}
			else {
				continue;
			}
			i++;
			if (i%10000==0) {
				System.out.println(i);
			}
		}
		System.out.println(i);
	}
	
	/**
	 * 将UniProt的unfilter的Go文件生成4个表后，将其中的GeneInfo导入geneInfo数据库
	 * geneInfo类型的表插入数据库
	 * 时采用以下方法
	 * 用geneID去查找GeneInfo表
	 * 如果没有，直接插入，如果有，看看discription是不是一样，不一样就附上
	 * 如果有跳过
	 * @author zong0jie
	 *
	 */
	public static void upDateGeneInfoUniProtgene_associationgoa_uniprot(String GeneInfoFile) throws Exception
	{
		TxtReadandWrite txtGeneInfo=new TxtReadandWrite();
		txtGeneInfo.setParameter(GeneInfoFile,false,true);
		BufferedReader ncbiReader=txtGeneInfo.readfile();
				
		String content="";
		int i=0;
		while ((content=ncbiReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			GeneInfo geneInfo=new GeneInfo();
			geneInfo.setGeneID((long)Double.parseDouble(ss[1]));
			geneInfo.setSymbol(ss[2]);
 
	
			if (ss.length>3) {
				geneInfo.setDescription(ss[3]);
			}
			else {
				geneInfo.setDescription("");
			}
			
			if (ss.length>4) {
				geneInfo.setSynonyms(ss[4]);
			}
			else {
				geneInfo.setSynonyms("");
			}
			
			GeneInfo geneInfo2=MapGeneInfo.queryGeneInfo(geneInfo);
			
			if (geneInfo2==null)
			{
				MapGeneInfo.InsertGeneInfo(geneInfo);
			}
			else {
				boolean flag=false;
				if (!geneInfo2.getSymbol().contains(geneInfo.getSymbol().trim()))
				{
					if (!geneInfo2.getSymbol().trim().equals("")) {
						geneInfo2.setSymbol(geneInfo2.getSymbol().trim()+"//"+geneInfo.getSymbol().trim());
					}
					else {
						geneInfo2.setSymbol(geneInfo.getSymbol().trim());
					}
					flag=true;
				}
				if (!geneInfo2.getDescription().contains(geneInfo.getDescription().trim())) {
					if (!geneInfo2.getDescription().trim().equals("")) {
						geneInfo2.setDescription(geneInfo2.getDescription().trim()+"//"+geneInfo.getDescription().trim());
					}
					else {
						geneInfo2.setDescription(geneInfo.getDescription().trim());
					}
					 flag=true;
				}
				if (geneInfo.getSynonyms()!=null) {
					String[] synonyms=geneInfo.getSynonyms().split("\\|");
					for (int j = 0; j < synonyms.length; j++) {
						if (!geneInfo2.getSynonyms().contains(synonyms[j].trim()))
						{
							if (!geneInfo2.getSynonyms().trim().equals("")) {
								geneInfo2.setSynonyms(geneInfo2.getSynonyms().trim()+"|"+synonyms[j].trim());
							}
							else {
								geneInfo2.setSynonyms(synonyms[j].trim());
							}
							if (synonyms[j].trim().equals("HLA-")) {
								System.out.println("test");
								
							}
							flag=true;
						}
					}
				}
				if(flag)
				{
					MapGeneInfo.upDateGeneInfo(geneInfo2);
					i++;
				}
			}
			
			if (i%10000==0) {
				System.out.println(i);
			}
		}
		System.out.println(i);
	}
	
	/**
	 * 将UniProt的unfilter的Go文件生成4个表后，将其中的UniProtInfo导入UnigeneInfo数据库
	 * geneInfo类型的表插入数据库
	 * 时采用以下方法
	 * 用geneID去查找UniGeneInfo表
	 * 如果没有，直接插入,如果有，看看discription是不是一样，不一样就附上
	 * 如果有跳过
	 * @author zong0jie
	 *
	 */
	public static void upDateUniGeneInfoUniProtgene_associationgoa_uniprot(String GeneInfoFile) throws Exception
	{
		TxtReadandWrite txtGeneInfo=new TxtReadandWrite();
		txtGeneInfo.setParameter(GeneInfoFile,false,true);
		BufferedReader ncbiReader=txtGeneInfo.readfile();
		
		String content="";
		int i=0;
		while ((content=ncbiReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			UniGeneInfo uniGeneInfo=new UniGeneInfo();
			uniGeneInfo.setUniProtID(ss[1]);
			uniGeneInfo.setSymbol(ss[2]);
			if (ss.length>3) {
				uniGeneInfo.setDescription(ss[3]);
			}
			else {
				uniGeneInfo.setDescription("");
			}
			if (ss.length>4) {
				uniGeneInfo.setSynonyms(ss[4]);
			}
			else {
				uniGeneInfo.setSynonyms("");
			}
			
			UniGeneInfo uniGeneInfo2=MapUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
			
			if (uniGeneInfo2==null)
			{
				MapUniGeneInfo.InsertUniGeneInfo(uniGeneInfo);
			}
			else 
			{
				if (uniGeneInfo2.getDescription()==null) {
					uniGeneInfo2.setDescription("");
				}
				if (uniGeneInfo2.getSynonyms()==null) {
					uniGeneInfo2.setSynonyms("");
				}
				boolean flag=false;
				if (!uniGeneInfo2.getSymbol().contains(uniGeneInfo.getSymbol().trim())) {
					if (uniGeneInfo2.getSymbol().trim().equals("")) {
						uniGeneInfo2.setSymbol(uniGeneInfo.getSymbol().trim());
					}
					else {
						uniGeneInfo2.setSymbol(uniGeneInfo2.getSymbol().trim()+"//"+uniGeneInfo.getSymbol().trim());
					}
					flag=true;
				}
				if (!uniGeneInfo2.getDescription().contains(uniGeneInfo.getDescription().trim())) {
					if (uniGeneInfo2.getDescription().trim().equals("")) {
						uniGeneInfo2.setDescription(uniGeneInfo.getDescription().trim());
					}
					else {
						uniGeneInfo2.setDescription(uniGeneInfo2.getDescription().trim()+"//"+uniGeneInfo.getDescription().trim());
					}
					 flag=true;
				}
				if (uniGeneInfo.getSynonyms()!=null) {
					String[] synonyms=uniGeneInfo.getSynonyms().split("\\|");
					for (int j = 0; j < synonyms.length; j++) {
						if (!uniGeneInfo2.getSynonyms().contains(synonyms[j].trim()))
						{
							if (uniGeneInfo2.getSynonyms().trim().equals("")) {
								uniGeneInfo2.setSynonyms(synonyms[j].trim());
							}
							else {
								uniGeneInfo2.setSynonyms(uniGeneInfo2.getSynonyms().trim()+"|"+synonyms[j].trim());
							}
							flag=true;
						}
					}
				}
				if(flag)
				{
					MapUniGeneInfo.upDateUniGeneInfo(uniGeneInfo2);
					i++;
				}
			}
		
			if (i%10000==0) {
				System.out.println(i);
			}
		}
		System.out.println(i);
	}
	
	/**
	 * 将UniProtgene_associationgoa_uniprot表格中按照TaxID提取出来文件(大概700多兆)中的
	 * GO插入到Gene2Go表和 UniGene2Go表
	 * 规则如下:首先查找NCBIID表，如果能找到，则提取NCBIID中的geneID，然后将GO插入Gene2Go表，
	 * 否则直接插入UniGene2Go表
	 * @throws Exception 
	 */
	public static void upDateGene2GoUniProtgene_associationgoa_uniprot(String uniGOfile) throws Exception {
		HashMap<String, String[]> hashGoInfo = ServGo.getHashGo2Term();
		TxtReadandWrite txtGene2GO=new TxtReadandWrite();
		txtGene2GO.setParameter(uniGOfile,false,true);
		BufferedReader ncbiReader=txtGene2GO.readfile();
		String content="";
		int mm=0;
		int noneGoInfo = 0; //GoTerm中没有的GOID的数量
		while ((content=ncbiReader.readLine())!=null) 
		{
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss=content.split("\t");
			if (ss.length == 0) {
				continue;
			}
			String[] GoInfo = hashGoInfo.get(ss[4].trim());
			if (GoInfo == null) {
				noneGoInfo++;
				System.out.println(noneGoInfo);
				continue;
			}

			String GoID = GoInfo[1];
			String goTerm = GoInfo[2];
			String goFuncition = GoInfo[3];

			NCBIID ncbiid=new NCBIID();
			int taxID=Integer.parseInt(ss[12].split("\\|")[0].split(":")[1]);//本列的taxID
			ncbiid.setAccID(ss[1]);ncbiid.setTaxID(taxID);
			ArrayList<NCBIID> lsNcbiids=MapNCBIID.queryLsNCBIID(ncbiid);
			if (lsNcbiids.size()>0)//能和NCBIID对应上去，也就是说能够找到GeneID，那么将其装入gene2Go表
			{
				for (int i = 0; i < lsNcbiids.size(); i++)   //一个基因可能会有多个geneID
				{
					

					Gene2Go gene2Go=new Gene2Go();
					gene2Go.setGeneId(lsNcbiids.get(i).getGeneId());
					gene2Go.setGOID(GoID);gene2Go.setQualifier(ss[3].trim());gene2Go.setReference(ss[5]);gene2Go.setEvidence(ss[6]);gene2Go.setFunction(goFuncition);
					gene2Go.setGOTerm(goTerm);
					gene2Go.setDataBase(NovelBioConst.DBINFO_UNIPROT_UNIID);
					AGene2Go gene2Go2 = MapGene2Go.queryGene2Go(gene2Go);
					if (gene2Go2!=null) //如果已经存在了，那么考虑下是否升级
					{
						boolean update=false;
						if (gene2Go2.getEvidence()!=null&&gene2Go.getEvidence()!=null&&!gene2Go2.getEvidence().contains(gene2Go.getEvidence())) {
							gene2Go2.setEvidence(gene2Go2.getEvidence()+"//"+gene2Go.getEvidence());
							update=true;
						}
						if (gene2Go2.getReference()!=null&&gene2Go.getReference()!=null&&!gene2Go2.getReference().contains(gene2Go.getReference())) {
							gene2Go2.setReference(gene2Go2.getReference()+"//"+gene2Go.getReference());
							update=true;
						}
						if (update) {
							MapGene2Go.upDateGene2Go((Gene2Go) gene2Go2);
						}
					}
					else //如果没有，则升级
					{
						MapGene2Go.InsertGene2Go(gene2Go);
					}
				}
			}
			else //将其装入UniGene2Go表
			{
				UniGene2Go uniGene2Go=new UniGene2Go();
				uniGene2Go.setUniProtID(ss[1].trim());uniGene2Go.setGOID(GoID);uniGene2Go.setQualifier(ss[3].trim());uniGene2Go.setReference(ss[5]);
				uniGene2Go.setEvidence(ss[6]);uniGene2Go.setFunction(goFuncition);uniGene2Go.setDataBase("UniProt");
				uniGene2Go.setGOTerm(goTerm);
				AGene2Go uniGene2Go2 = MapUniGene2Go.queryUniGene2Go(uniGene2Go);
				if (uniGene2Go2!=null) //如果已经存在了，那么考虑下是否升级
				{
					boolean update=false;
					if (uniGene2Go.getEvidence()!=null&&uniGene2Go.getEvidence()!=null&&!uniGene2Go2.getEvidence().contains(uniGene2Go.getEvidence())) {
						uniGene2Go2.setEvidence(uniGene2Go2.getEvidence()+"//"+uniGene2Go.getEvidence());
						update=true;
					}
					if (uniGene2Go2.getReference()!=null&&uniGene2Go.getReference()!=null&&!uniGene2Go2.getReference().contains(uniGene2Go.getReference())) {
						uniGene2Go2.setReference(uniGene2Go2.getReference()+"//"+uniGene2Go.getReference());
						update=true;
					}
					if (update) {
						MapUniGene2Go.upDateUniGene2Go((UniGene2Go) uniGene2Go2);
					}
				}
				else //如果没有，则升级
				{
					MapUniGene2Go.InsertUniGene2Go(uniGene2Go);
				}
			}
			
			mm++;
			if (mm%10000==0) {
				System.out.println(mm);
			}
		}
		
	}
	
	
	/**
	 * 将从amiGo上下载的Go表插入数据库
	 * 时采用以下方法
	 * 用GoIDquery,GoID,GoFunction三个去查找Go2Term表
	 * 如果没有，直接插入
	 * 如果有跳过
	 * @author zong0jie
	 *
	 */
	public static void upDateGoTerm(String Go2Termfile) throws Exception
	{
		TxtReadandWrite txtGoTerm=new TxtReadandWrite();
		txtGoTerm.setParameter(Go2Termfile,false,true);
		BufferedReader go2TermReader=txtGoTerm.readfile();
				
		String content="";
		int i=0;
		while ((content=go2TermReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");ss[1].trim();
			String[] ss2=ss[1].split(" +");
			if (ss[1].trim().equals("")) //没有别名
			{
				Go2Term go2Term=new Go2Term();
				go2Term.setGoIDQuery(ss[0].trim());
				Go2Term go2Term2=MapGo2Term.queryGo2Term(go2Term);
				if (go2Term2==null) {
					go2Term.setGoID(ss[0].trim());
					go2Term.setGoIDQuery(ss[0].trim());
					go2Term.setGoTerm(ss[2].trim());
					go2Term.setGoFunction(ss[3].trim());
					MapGo2Term.InsertGo2Term(go2Term);
					i++;
				}
			}
			else 
			{
				//装入别名
				for (int j = 0; j < ss2.length; j++) 
				{
					Go2Term go2Term=new Go2Term();
					go2Term.setGoIDQuery(ss2[j].trim());
					Go2Term go2Term2=MapGo2Term.queryGo2Term(go2Term);
					if (go2Term2==null) {
						go2Term.setGoID(ss[0].trim());
						go2Term.setGoIDQuery(ss2[j].trim());
						go2Term.setGoTerm(ss[2].trim());
						go2Term.setGoFunction(ss[3].trim());
						MapGo2Term.InsertGo2Term(go2Term);
						i++;
					}
				}
				Go2Term go2Term=new Go2Term();
				go2Term.setGoIDQuery(ss[0].trim());
				Go2Term go2Term2=MapGo2Term.queryGo2Term(go2Term);
				if (go2Term2==null) {
					go2Term.setGoID(ss[0].trim());
					go2Term.setGoIDQuery(ss[0].trim());
					go2Term.setGoTerm(ss[2].trim());
					go2Term.setGoFunction(ss[3].trim());
					MapGo2Term.InsertGo2Term(go2Term);
					i++;
				}
			}
			
			if (i%10000==0) {
				System.out.println(i);
			}
		}
	}
	
	
	/**
	 * 将Blast整理好的结果插入BlastInfo中
	 * 插入时采用如下方法
	 * 用queryID，queryTax和subjectTax搜索数据库，如果没找到，则插入。如果找到，则先看subjectID是否一致，如果不一致，则比较evalue。如果新加入的evalue更小，则升级数据库
	 * 如果有跳过
	 * @author zong0jie
	 * @param blast2InfoFile blast文件
	 * @param subjectTab blast到的目的物种是在哪个表中，是NCBIID还是UniProtID
	 *
	 */
	public static void upDateBlastInfo(String blast2InfoFile,String subjectTab) throws Exception
	{
		TxtReadandWrite txtBInfo=new TxtReadandWrite(blast2InfoFile, false);
		BufferedReader readerBInfo=txtBInfo.readfile();
		String content="";
		while ((content=readerBInfo.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			BlastInfo blastInfo=new BlastInfo();
			blastInfo.setQueryID(ss[0]);blastInfo.setQueryTax(Integer.parseInt(ss[1]));blastInfo.setSubjectTax(Integer.parseInt(ss[4]));
			//Date date=(Date) new SimpleDateFormat("yyyy-MM-dd").parse(ss[8]);
			blastInfo.setBlastDate(ss[8]);//这个不会用于查询
			BlastInfo blastInfo2=MapBlastInfo.queryBlastInfo(blastInfo);
			blastInfo.setQueryDB(ss[2]);
			blastInfo.setSubjectID(ss[3]);
			blastInfo.setSubjectDB(ss[5]);
			blastInfo.setIdentities(Double.parseDouble(ss[6]));
			blastInfo.setEvalue(Double.parseDouble(ss[7]));
			blastInfo.setSubjectTab(subjectTab);
			if (blastInfo2!=null)
			{
				if(!blastInfo2.getSubjectID().equals(blastInfo.getSubjectID())&&blastInfo2.getEvalue()>blastInfo.getEvalue()) 
				{
					MapBlastInfo.upDateBlastInfo(blastInfo);
				}
				continue;
			}
			MapBlastInfo.InsertBlastInfo(blastInfo);
		}
	}
	
	/**
	 * 将Blast整理好的结果插入BlastInfo中
	 * 插入时采用如下方法
	 * 用queryID，queryTax和subjectTax搜索数据库，如果没找到，则插入。如果找到，则先看subjectID是否一致，如果不一致，则比较evalue。如果新加入的evalue更小，则升级数据库
	 * 如果有跳过
	 * @author zong0jie
	 * @param blast2InfoFile blast文件
	 *
	 */
	public static void upDateBlastInfo(String blast2InfoFile) throws Exception
	{
		TxtReadandWrite txtBInfo=new TxtReadandWrite(blast2InfoFile, false);
		BufferedReader readerBInfo=txtBInfo.readfile();
		String content="";
		while ((content=readerBInfo.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			CopedID copedIDQ = new CopedID(ss[0], Integer.parseInt(ss[1]), false);
			CopedID copedIDS = new CopedID(ss[3], Integer.parseInt(ss[4]), false);
			
			BlastInfo blastInfo=new BlastInfo();
			blastInfo.setQueryID(copedIDQ.getGenUniID());blastInfo.setQueryTax(copedIDQ.getTaxID());blastInfo.setSubjectTax(copedIDS.getTaxID());
			//Date date=(Date) new SimpleDateFormat("yyyy-MM-dd").parse(ss[8]);
			blastInfo.setBlastDate(ss[8]);//这个不会用于查询
			
			BlastInfo blastInfo2=MapBlastInfo.queryBlastInfo(blastInfo);

			blastInfo.setQueryDB(ss[2]);
			blastInfo.setSubjectID(copedIDS.getGenUniID());
			blastInfo.setSubjectDB(ss[5]);
			blastInfo.setIdentities(Double.parseDouble(ss[6]));
			blastInfo.setEvalue(Double.parseDouble(ss[7]));
			if (copedIDS.getIDtype().equals(CopedID.IDTYPE_GENEID)) {
				blastInfo.setSubjectTab(BlastInfo.SUBJECT_TAB_NCBIID);
			}
			else if (copedIDS.getIDtype().equals(CopedID.IDTYPE_UNIID)) {
				blastInfo.setSubjectTab(BlastInfo.SUBJECT_TAB_UNIPROTID);
			}
			else {
				logger.error("subject have unknown id: "+copedIDS.getAccID());
			}
			
			if (blastInfo2!=null)
			{
				if(!blastInfo2.getSubjectID().equals(blastInfo.getSubjectID())&&blastInfo2.getEvalue()>blastInfo.getEvalue()) 
				{
					MapBlastInfo.upDateBlastInfo(blastInfo);
				}
				continue;
			}
			MapBlastInfo.InsertBlastInfo(blastInfo);
		}
	}
	
	
	/**
	 * 将TaxID整理好的结果插入taxID表中
	 * 插入时采用如下方法
	 * 用taxID搜索数据库，如果没找到，则插入。如果找到，则升级
	 * @author zong0jie
	 *
	 */
	public static void upDateTaxID(String taxIDfile) throws Exception
	{
		TxtReadandWrite txtTaxID=new TxtReadandWrite();
		txtTaxID.setParameter(taxIDfile, false, true);
		String[][] taxInfo=txtTaxID.ExcelRead("\t", 1, 1, txtTaxID.ExcelRows(), txtTaxID.ExcelColumns("\t"));
		for (int i = 0; i < taxInfo.length; i++) {
			TaxInfo taxID=new TaxInfo();
			taxID.setTaxID(Integer.parseInt(taxInfo[i][0]));taxID.setChnName(taxInfo[i][1]);taxID.setLatin(taxInfo[i][2]);
			taxID.setComName(taxInfo[i][3]);taxID.setAbbr(taxInfo[i][4]);
			if (MapFSTaxID.queryTaxInfo(taxID)==null) {
				MapFSTaxID.InsertTaxInfo(taxID);
			}
			else {
				MapFSTaxID.upDateTaxInfo(taxID);
			}
		}
	}
		
	/**
	 * 读取gene_association.goa_uniprot文件，获得里面第13列的taxID信息
	 * 将所有含有需要TaxID的行全部提取出来
	 * @throws Exception 
	 */
	public static void getUniProtGoInfoTaxIDgene_associationgoa_uniprot(String taxIDfile, String inputFile, String outputFile) throws Exception 
	{
		TxtReadandWrite taxIDrReadandWrite=new TxtReadandWrite();
		taxIDrReadandWrite.setParameter(taxIDfile, false,true);
		HashSet<String> hashTaxID=new HashSet<String>();
		BufferedReader Taxreader=taxIDrReadandWrite.readfile();
		String content="";
		while ((content=Taxreader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			hashTaxID.add(ss[0]);
		}
		
		TxtReadandWrite inputReadandWrite=new TxtReadandWrite();
		inputReadandWrite.setParameter(inputFile,false, true);
		
		TxtReadandWrite outputReadandWrite=new TxtReadandWrite();
		outputReadandWrite.setParameter(outputFile, true,false);
		
		BufferedReader inputrReader=inputReadandWrite.readfile();
		inputrReader.readLine();
		String content2="";
		while((content2=inputrReader.readLine())!=null)
		{
			String[] ss=content2.split("\t");
			String[]ss2=null;
			if (ss[0].equals("IPI")) {
				continue;
			}
			
			if (ss[12].contains("|")) 
			{
				System.out.println(content2);
				ss2=ss[12].split("\\|");
				ss2=ss2[0].split(":");
			}
			else {
				ss2=ss[12].split(":");
			}
			String ss3=null;
			try {
				 ss3=Integer.parseInt(ss2[1])+"";
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("error   "+content2);
			}
			
			if (hashTaxID.contains(ss3)) 
			{
				outputReadandWrite.writefile(content2+"\n", false);
			}
		}
		outputReadandWrite.writefile("", true);
		taxIDrReadandWrite.close();
	}
	
	/**
	 * 将gene_association.goa_uniprot文件去重复、提取TaxID、去掉文件最开始的IPI后，做以下工作<br>
	 * 将每一行的， 第2列：基因的UniProtID，第3列：Symbol，第10列Description，第11列Synonym, 连同第13列Taxon_ID<br>
	 * 向NCBIID和UniProtID两个表比对，比上NCBI后，将本列所有数据整理为两个文件<br>
	 *  1. taxID \t geneID \t accessID \t DataBase \n  和<br>
	 *  2. taxID \t geneID \t symbol \t discription \t Synonym \n <br>
	 * 较表 UniProtID, 将本列所有数据整理为两个文件 <br>
	 * 1. taxID \t UniProtID \t accessID \t DataBase \n 装入UniProtID和  <br>
	 *  2. taxID \t geneID \t symbol \t discription \t Synonym \n 装入UniGeneInfo<br>
	 * 如果有一个swiss对应多个geneID的情况，多个geneID都更新，UniProt也一样<br>
	 * @throws Exception 
	 */
	public static void getUniProtGoInfogene_associationgoa_uniprot(String inputFile,String outNCBIID,String outGeneInfo, String outUniProtID, String outUniGeneInfo,String remain) throws Exception 
	{
		TxtReadandWrite txtInput=new TxtReadandWrite(); txtInput.setParameter(inputFile, false, true);
		TxtReadandWrite txtOutNCBIID=new TxtReadandWrite();txtOutNCBIID.setParameter(outNCBIID, true, false);
		TxtReadandWrite txtOutGeneInfo=new TxtReadandWrite();txtOutGeneInfo.setParameter(outGeneInfo, true, false);
		TxtReadandWrite txtOutUniProtID=new TxtReadandWrite();txtOutUniProtID.setParameter(outUniProtID, true, false);
		TxtReadandWrite txtOutUniGeneInfo=new TxtReadandWrite();txtOutUniGeneInfo.setParameter(outUniGeneInfo, true, false);
		TxtReadandWrite txtRemain=new TxtReadandWrite();txtRemain.setParameter(remain, true, false);
		BufferedReader inputReader=txtInput.readfile();
		
		MapUniProtID uniProtIDDao=new MapUniProtID();
		
		String content="";
		int[] index=new int[3];index[0]=1;index[1]=2;index[2]=10;
		String[] DBInfo=new String[3]; DBInfo[0]=NovelBioConst.DBINFO_UNIPROT_UNIID;DBInfo[1]=NovelBioConst.DBINFO_SYMBOL;DBInfo[2]=NovelBioConst.DBINFO_SYNONYMS;
		ArrayList<NCBIID> lsResultNcbiid=null;
		ArrayList<NCBIID> tmplsResultNcbiid=null;
		ArrayList<UniProtID> lsResultUniProtID=null;
		ArrayList<UniProtID> tmplsResultUniProtID=null;
		while ((content=inputReader.readLine())!=null) {
			String ss[]=content.split("\t");
			int taxID=Integer.parseInt(ss[12].split("\\|")[0].split(":")[1]);//本列的taxID
			int NCBIflag=0;//标记是否查到NCBIID表，用taxID和accessID找NCBIID表。如果没找到，设为0，找到一个1，找到多个2
			int UniProtflag=0;//标记是否查到UniProt表，用taxID和accessID找UniProtIID表。如果没找到，设为0，找到一个1，找到多个2
			tmplsResultNcbiid=null;//先清空
			tmplsResultUniProtID=null;
			//////////////////////首先查找NCBIID表/////////////////////////////////////
			for (int i = 0; i < 3; i++) {
				String[] ssTmp=ss[index[i]].split("\\|");
		
				for (int j = 0; j < ssTmp.length; j++) 
				{
					String sstmpid=ssTmp[j].trim();
					//将其中的
					if (sstmpid.equals("")) {
						continue;
					}
					if (sstmpid.contains("Em:")) {
						sstmpid=sstmpid.substring(sstmpid.indexOf(":")+1, sstmpid.indexOf("."));
					}
					NCBIID ncbiid=new NCBIID();
					ncbiid.setTaxID(taxID);ncbiid.setAccID(sstmpid);
					lsResultNcbiid=MapNCBIID.queryLsNCBIID(ncbiid);
					if (lsResultNcbiid.size()==1)
					{
						NCBIflag=1;break;
					}
					else if (lsResultNcbiid.size()>1)
					{
						NCBIflag=2;System.out.println(taxID+" "+sstmpid+"           "+ss[1]+"    NCBI");
						tmplsResultNcbiid=lsResultNcbiid;//当找到了>=2个基因时，将结果放在这个  tmplsResultNcbiid 里面，并不跳出，本步设计原因：当lsResultNcbiid.size()=1时
						//直接跳出，下面也可以进行，但是如果部分是>=2个基因，部分没有，那么后面 	NCBIID ncbiidRes=lsResultNcbiid.get(0); 语句可能会出错，这时候就用 tmplsResultNcbiid 来代替，注意可以用=，虽然是引用传递，但是后面lsResultNcbiid会赋给新值
					}
				}

				if (NCBIflag == 1)//在ncbi表中找到了唯一的一条记录
					break;
			}
			if (NCBIflag == 2) {
				System.out.println(taxID+"  Reallyaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "+"    NCBI");
			}
			if (NCBIflag >= 1) 
			{
				if (lsResultNcbiid.size()>0) 
				{
					for (NCBIID ncbiidRes : lsResultNcbiid) {
						//数据库是DBINFO_SYNONYMS都跳过，这个会有问题
						if (lsResultNcbiid.size()>1 && ncbiidRes.getDBInfo().equals(NovelBioConst.DBINFO_SYNONYMS)) {
							continue;
						}
						
						ncbiidRes = lsResultNcbiid.get(0);
						String resultTmp = ncbiidRes.getTaxID()+"\t" + ncbiidRes.getGeneId();
						for (int i = 0; i < 3; i++)
						{
							String[] ssTmp = ss[index[i]].split("\\|");
							for (int j = 0; j < ssTmp.length; j++) 
							{
								String sstmpid = ssTmp[j].trim();
								//将其中的
								if (sstmpid.equals("")) {
									continue;
								}
								if (sstmpid.contains("Em:")) {
									sstmpid = sstmpid.substring(sstmpid.indexOf(":")+1, sstmpid.indexOf("."));
								}
								String resultID=resultTmp+"\t"+sstmpid+"\t"+DBInfo[i]+"\n";
								txtOutNCBIID.writefile(resultID, false);
							}
						}
						String resultInfo=resultTmp+"\t"+ss[2]+"\t"+ss[9]+"\t"+ss[10]+"\n";
						txtOutGeneInfo.writefile(resultInfo,false);
					}
				
				}
				else {
					for (NCBIID ncbiidRes : tmplsResultNcbiid) {
						if (ncbiidRes.getDBInfo().equals(NovelBioConst.DBINFO_SYNONYMS)) {
							continue;
						}
						String resultTmp=ncbiidRes.getTaxID()+"\t"+ncbiidRes.getGeneId();
						for (int i = 0; i < 3; i++)
						{
							String[] ssTmp=ss[index[i]].split("\\|");
							for (int j = 0; j < ssTmp.length; j++) 
							{
								String sstmpid=ssTmp[j].trim();
								//将其中的
								if (sstmpid.equals("")) {
									continue;
								}
								if (sstmpid.contains("Em:")) {
									sstmpid=sstmpid.substring(sstmpid.indexOf(":")+1, sstmpid.indexOf("."));
								}
								String resultID=resultTmp+"\t"+sstmpid+"\t"+DBInfo[i]+"\n";
								txtOutNCBIID.writefile(resultID, false);
							}
						}
						String resultInfo=resultTmp+"\t"+ss[2]+"\t"+ss[9]+"\t"+ss[10]+"\n";
						txtOutGeneInfo.writefile(resultInfo,false);
					}
				}
				continue;//不执行下面的UniProt查找数据库了
			}

			/////////////////////////如果NCBIID表没有查到，那么查UniProt表/////////////////////////////////////////
			for (int i = 0; i < 3; i++) 
			{
				String[] ssTmp=ss[index[i]].split("\\|");
		
				for (int j = 0; j < ssTmp.length; j++) 
				{
					String sstmpid=ssTmp[j].trim();
					//将其中的
					if (sstmpid.equals("")) {
						continue;
					}
					if (sstmpid.contains("Em:")) {
						sstmpid=sstmpid.substring(sstmpid.indexOf(":")+1, sstmpid.indexOf("."));
					}
					UniProtID uniProtID=new UniProtID();
					uniProtID.setTaxID(taxID);uniProtID.setAccID(sstmpid);
					lsResultUniProtID=uniProtIDDao.queryLsUniProtID(uniProtID);
					if (lsResultUniProtID.size()==1)
					{
						UniProtflag=1;break;
					}
					else if (lsResultUniProtID.size()>1)
					{
						UniProtflag=2;System.out.println(taxID+"   "+sstmpid+"           "+ss[1]+"     uniprot");
						tmplsResultUniProtID=lsResultUniProtID;//当找到了>=2个基因时，将结果放在这个  tmplsResultNcbiid 里面，并不跳出，本步设计原因：当lsResultNcbiid.size()=1时
						//直接跳出，下面也可以进行，但是如果部分是>=2个基因，部分没有，那么后面 	NCBIID ncbiidRes=lsResultNcbiid.get(0); 语句可能会出错，这时候就用 tmplsResultNcbiid 来代替，注意可以用=，虽然是引用传递，但是后面lsResultNcbiid会赋给新值
			
					}
				}

				if (UniProtflag==1)//在ncbi表中找到了唯一的一条记录
					break;
			}
			if (UniProtflag==2) {
				System.out.println(taxID+"  Reallyaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "+"    UniProt");
			}
			if (UniProtflag>=1) 
			{
				if (lsResultUniProtID.size()>0) {
					for (UniProtID uniProtIDRes : lsResultUniProtID) {
						//数据库是DBINFO_SYNONYMS都跳过，这个会有问题
						if (lsResultUniProtID.size()>1 && uniProtIDRes.getDBInfo().equals(NovelBioConst.DBINFO_SYNONYMS)) {
							continue;
						}
						
							String resultTmp=uniProtIDRes.getTaxID()+"\t"+uniProtIDRes.getUniID();
							for (int i = 0; i < 3; i++)
							{
								String[] ssTmp=ss[index[i]].split("\\|");
								for (int j = 0; j < ssTmp.length; j++) 
								{
									String sstmpid=ssTmp[j].trim();
									//将其中的
									if (sstmpid.equals("")) {
										continue;
									}
									if (sstmpid.contains("Em:")) {
										sstmpid=sstmpid.substring(sstmpid.indexOf(":")+1, sstmpid.indexOf("."));
									}
									String resultID=resultTmp+"\t"+sstmpid+"\t"+DBInfo[i]+"\n";
									txtOutUniProtID.writefile(resultID, false);
								}
							}
							String resultInfo=resultTmp+"\t"+ss[2]+"\t"+ss[9]+"\t"+ss[10]+"\n";
							txtOutUniGeneInfo.writefile(resultInfo,false);
					}
		
				}
				else {
					for (UniProtID uniProtIDRes : tmplsResultUniProtID) {
						if (uniProtIDRes.getDBInfo().equals(NovelBioConst.DBINFO_SYNONYMS)) {
							continue;
						}
						String resultTmp=uniProtIDRes.getTaxID()+"\t"+uniProtIDRes.getUniID();
						for (int i = 0; i < 3; i++)
						{
							String[] ssTmp=ss[index[i]].split("\\|");
							for (int j = 0; j < ssTmp.length; j++) 
							{
								String sstmpid=ssTmp[j].trim();
								//将其中的
								if (sstmpid.equals("")) {
									continue;
								}
								if (sstmpid.contains("Em:")) {
									sstmpid=sstmpid.substring(sstmpid.indexOf(":")+1, sstmpid.indexOf("."));
								}
								String resultID=resultTmp+"\t"+sstmpid+"\t"+DBInfo[i]+"\n";
								txtOutUniProtID.writefile(resultID, false);
							}
						}
						String resultInfo=resultTmp+"\t"+ss[2]+"\t"+ss[9]+"\t"+ss[10]+"\n";
						txtOutUniGeneInfo.writefile(resultInfo,false);
					}
				}
				continue;
			}
			txtRemain.writefile(content+"\n");
		}
		txtOutGeneInfo.writefile("", true);
		txtOutNCBIID.writefile("", true);
		txtOutUniGeneInfo.writefile("", true);
		txtOutUniProtID.writefile("", true);
		txtRemain.writefile("", true);
	}
		
		
		
	
	
	
}











