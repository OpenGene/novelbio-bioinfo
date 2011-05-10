package com.novelbio.upDateDB.dataBase;


import java.io.BufferedReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.DAO.FriceDAO.DaoFSBlastInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSGene2Go;
import com.novelbio.database.DAO.FriceDAO.DaoFSGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSGo2Term;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.FriceDAO.DaoFSTaxID;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniGene2Go;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniProtID;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.Gene2Go;
import com.novelbio.database.entity.friceDB.GeneInfo;
import com.novelbio.database.entity.friceDB.Go2Term;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.TaxInfo;
import com.novelbio.database.entity.friceDB.UniGene2Go;
import com.novelbio.database.entity.friceDB.UniGeneInfo;
import com.novelbio.database.entity.friceDB.UniProtID;


public class UpDateFriceDBFile {
	/**
	 * 专门处理Gene2Go的表
	 * 将含有固定TaxID的gene2Go表插入数据库，插入时采用以下方法
	 * 首先看数据库中是否含有指定geneID和GOID的行
	 * 如果没有，直接插入
	 * 如果有，看Evidence那一项是否一样，一样的话，就跳过，不一样，就updata
	 * @author zong0jie
	 *
	 */
	public static void upDateGenetoGo(String gene2GoFile) throws Exception
	{
		TxtReadandWrite txtgene2Go=new TxtReadandWrite();
		txtgene2Go.setParameter(gene2GoFile,false,true);
		BufferedReader gene2GoReader=txtgene2Go.readfile();
		
		
		DaoFSGene2Go friceDAO=new DaoFSGene2Go();
		
		String content="";
		int i=0;
		while ((content=gene2GoReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			Gene2Go gene2GoInfo=new Gene2Go();
			gene2GoInfo.setGeneId(Long.parseLong(ss[1]));
			gene2GoInfo.setGOID(ss[2]);
			gene2GoInfo.setEvidence(ss[3]);
			gene2GoInfo.setQualifier(ss[4]);
			gene2GoInfo.setGOTerm(ss[5]);
			if(ss[6].equals("-"))
				gene2GoInfo.setReference(ss[6]);
			else 
				gene2GoInfo.setReference("PMID:"+ss[6]);
			
	
			gene2GoInfo.setFunction(ss[7]);
			gene2GoInfo.setDataBase("NCBI");
			Gene2Go gene2GoInfo2=friceDAO.queryGene2Go(gene2GoInfo);
			if (gene2GoInfo2==null) 
			{
				friceDAO.InsertGene2Go(gene2GoInfo);
			}
			else {
				//如果已经含有相应的GOID，那么看evidence是否也含有了，没有的话就upDate
				if(!gene2GoInfo2.getEvidence().contains(gene2GoInfo.getEvidence()))
				{
					gene2GoInfo.setEvidence(gene2GoInfo.getEvidence()+"/"+gene2GoInfo2.getEvidence());
					friceDAO.upDateGene2Go(gene2GoInfo);
				}
			}
			i++;
			if (i%10000==0) {
				System.out.println(i);
			}
			
		}
	}
	
	
	
	/**
	 * 将NCBIID类型的表插入数据库
	 * 待导入的结果中的geneID可能含有： geneID//geneID//geneID<br>
	 * 注意一定要分开。也就是如果一个accID对应多个geneID，那么就将这两个geneID都插入NCBIID
	 * 用geneID和accessID一起去查找NCBIID表
	 * 如果没有，直接插入
	 * 如果有跳过
	 * @author zong0jie
	 *
	 */
	public static void upDateNCBIID(String NCBIIDfile) throws Exception
	{
		TxtReadandWrite txtNCBIID=new TxtReadandWrite();
		txtNCBIID.setParameter(NCBIIDfile,false,true);
		BufferedReader ncbiReader=txtNCBIID.readfile();
		DaoFSNCBIID friceDAO=new DaoFSNCBIID();
		
		String content="";
		int i=0;
		while ((content=ncbiReader.readLine())!=null) 
		{
			String[] ss = content.split("\t");
			NCBIID ncbiid = new NCBIID();
			ncbiid.setAccID(ss[2]);
			ArrayList<NCBIID> lsNcbiids =friceDAO.queryLsNCBIID(ncbiid);
			if (lsNcbiids != null && lsNcbiids.size() > 0) //如果数据库中已经有了
			{
				continue;
			}
			String[] ssGeneID = ss[1].split("//");
			for (String string : ssGeneID) 
			{
				ncbiid.setTaxID(Integer.parseInt(ss[0]));
				ncbiid.setGeneId((long)Double.parseDouble(string));
				ncbiid.setDBInfo(ss[3]);
				friceDAO.InsertNCBIID(ncbiid);
				i++;
			}
			
			if (i%10000==0) {
				System.out.println(i);
			}
		}
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
		
		DaoFSNCBIID friceDAO=new DaoFSNCBIID();
		
		String content="";
		int i=0;
		while ((content=ncbiReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			NCBIID ncbiid=new NCBIID();
			
			ncbiid.setGeneId((long)Double.parseDouble(ss[1]));//先用geneID搜
			ArrayList<NCBIID> lsncbiid=friceDAO.queryLsNCBIID(ncbiid);
			if (lsncbiid==null||lsncbiid.size()==0) 
			{
				txtnoFindFile.writefile(content+"\n");
				continue;
			}
			
			
			
			ncbiid.setAccID(ss[2]);
			
			//geneID和 accID两个一起查看是否能查到
			NCBIID ncbiid2=friceDAO.queryNCBIID(ncbiid);
			if (ncbiid2==null) 
			{
				ncbiid.setTaxID(Integer.parseInt(ss[0]));
				ncbiid.setDBInfo(ss[3]);
				friceDAO.InsertNCBIID(ncbiid);
			}
			else {
				continue;
			}
			i++;
			if (i%10000==0) {
				System.out.println(i);
			}
		}
	}
	
	
	
	/**
	 * 将UniProtID类型的表插入数据库
	 * 时采用以下方法
	 * 用geneID和accessID一起去查找NCBIID表
	 * 如果没有，直接插入
	 * 如果有跳过
	 * @author zong0jie
	 *
	 */
	public static void upDateUniProtID(String UniProtIDfile) throws Exception
	{
		TxtReadandWrite txtUniProtID=new TxtReadandWrite();
		txtUniProtID.setParameter(UniProtIDfile,false,true);
		BufferedReader uniProtReader=txtUniProtID.readfile();
		
		DaoFSUniProtID friceDAO=new DaoFSUniProtID();
		
		String content="";
		int i=0;
		while ((content=uniProtReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			UniProtID uniProtid=new UniProtID();
			uniProtid.setTaxID(Integer.parseInt(ss[0]));
			uniProtid.setUniID(ss[1]);
			uniProtid.setAccID(ss[2]);
			uniProtid.setDBInfo(ss[3]);
  
			UniProtID uniProtid2=friceDAO.queryUniProtID(uniProtid);
			if (uniProtid2==null) 
			{
				friceDAO.InsertUniProtID(uniProtid);
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
		
		
		DaoFSGeneInfo friceDAO=new DaoFSGeneInfo();
		
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

			GeneInfo geneInfo2=friceDAO.queryGeneInfo(geneInfo);
			if (geneInfo2==null)
			{
				friceDAO.InsertGeneInfo(geneInfo);
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
		
		
		DaoFSGeneInfo friceDAO=new DaoFSGeneInfo();
		
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
			
			GeneInfo geneInfo2=friceDAO.queryGeneInfo(geneInfo);
			
			if (geneInfo2==null)
			{
				friceDAO.InsertGeneInfo(geneInfo);
			}
			else {
				boolean flag=false;
				if (!geneInfo2.getSymbol().contains(geneInfo.getSymbol())) {
					geneInfo2.setSymbol(geneInfo2.getSymbol()+"//"+geneInfo.getSymbol().trim());
					flag=true;
				}
				if (!geneInfo2.getDescription().contains(geneInfo.getDescription())) {
					 geneInfo2.setDescription(geneInfo2.getDescription()+"//"+geneInfo.getDescription());
					 flag=true;
				}
				if (geneInfo.getSynonyms()!=null) {
					String[] synonyms=geneInfo.getSynonyms().split("\\|");
					for (int j = 0; j < synonyms.length; j++) {
						if (!geneInfo2.getSynonyms().contains(synonyms[j].trim()))
						{
							geneInfo2.setSynonyms(geneInfo2.getSynonyms()+"|"+synonyms[j].trim());
							if (synonyms[j].trim().equals("HLA-")) {
								System.out.println("test");
								
							}
							flag=true;
						}
					}
				}
				if(flag)
				{
					 friceDAO.upDateGeneInfo(geneInfo2);
				}
			}
			i++;
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
		
		
		DaoFSUniGeneInfo friceDAO=new DaoFSUniGeneInfo();
		
		String content="";
		int i=0;
		while ((content=ncbiReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			UniGeneInfo uniGeneInfo=new UniGeneInfo();
			uniGeneInfo.setGeneID(ss[1]);
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
			
			UniGeneInfo uniGeneInfo2=friceDAO.queryUniGeneInfo(uniGeneInfo);
			
			if (uniGeneInfo2==null)
			{
				friceDAO.InsertUniGeneInfo(uniGeneInfo);
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
				if (!uniGeneInfo2.getSymbol().contains(uniGeneInfo.getSymbol())) {
					uniGeneInfo2.setSymbol(uniGeneInfo2.getSymbol()+"//"+uniGeneInfo.getSymbol());
					flag=true;
				}
				if (!uniGeneInfo2.getDescription().contains(uniGeneInfo.getDescription())) {
					uniGeneInfo2.setDescription(uniGeneInfo2.getDescription()+"//"+uniGeneInfo.getDescription());
					 flag=true;
				}
				if (uniGeneInfo.getSynonyms()!=null) {
					String[] synonyms=uniGeneInfo.getSynonyms().split("\\|");
					for (int j = 0; j < synonyms.length; j++) {
						if (!uniGeneInfo2.getSynonyms().contains(synonyms[j].trim()))
						{
							uniGeneInfo2.setSynonyms(uniGeneInfo2.getSynonyms()+"|"+synonyms[j].trim());
							flag=true;
						}
					}
				}
				if(flag)
				{
					 friceDAO.upDateUniGeneInfo(uniGeneInfo2);
				}
			}
			i++;
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
		TxtReadandWrite txtGene2GO=new TxtReadandWrite();
		txtGene2GO.setParameter(uniGOfile,false,true);
		BufferedReader ncbiReader=txtGene2GO.readfile();
		String content="";
		DaoFSNCBIID ncbiidDao=new DaoFSNCBIID();
		DaoFSGene2Go gene2GoDao=new DaoFSGene2Go();
		DaoFSUniGene2Go uniGene2GoDao=new DaoFSUniGene2Go();
		int mm=0;
		while ((content=ncbiReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			NCBIID ncbiid=new NCBIID();
			int taxID=Integer.parseInt(ss[12].split("\\|")[0].split(":")[1]);//本列的taxID
			ncbiid.setAccID(ss[1]);ncbiid.setTaxID(taxID);
			ArrayList<NCBIID> lsNcbiids=ncbiidDao.queryLsNCBIID(ncbiid);
			if (lsNcbiids.size()>0)//能和NCBIID对应上去，也就是说能够找到GeneID，那么将其装入gene2Go表
			{
				for (int i = 0; i < lsNcbiids.size(); i++)   //一个基因可能会有多个geneID
				{
					Gene2Go gene2Go=new Gene2Go();
					gene2Go.setGeneId(lsNcbiids.get(i).getGeneId());
					gene2Go.setGOID(ss[4].trim());gene2Go.setQualifier(ss[3].trim());gene2Go.setReference(ss[5]);gene2Go.setEvidence(ss[6]);gene2Go.setFunction(ss[8]);gene2Go.setDataBase("UniProt");
					Gene2Go gene2Go2 = gene2GoDao.queryGene2Go(gene2Go);
					if (gene2Go2!=null) //如果已经存在了，那么考虑下是否升级
					{
						boolean update=false;
						if (gene2Go2.getEvidence()!=null&&gene2Go.getEvidence()!=null&&!gene2Go2.getEvidence().contains(gene2Go.getEvidence())) {
							gene2Go2.setEvidence(gene2Go2.getEvidence()+"/"+gene2Go.getEvidence());
							update=true;
						}
						if (gene2Go2.getReference()!=null&&gene2Go.getReference()!=null&&!gene2Go2.getReference().contains(gene2Go.getReference())) {
							gene2Go2.setReference(gene2Go2.getReference()+"/"+gene2Go.getReference());
							update=true;
						}
						if (update) {
							gene2GoDao.upDateGene2Go(gene2Go2);
						}
					}
					else //如果没有，则升级
					{
						gene2GoDao.InsertGene2Go(gene2Go);
					}
				}
			}
			else //将其装入UniGene2Go表
			{
				UniGene2Go uniGene2Go=new UniGene2Go();
				uniGene2Go.setUniProtID(ss[1].trim());uniGene2Go.setGOID(ss[4].trim());uniGene2Go.setQualifier(ss[3].trim());uniGene2Go.setReference(ss[5]);
				uniGene2Go.setEvidence(ss[6]);uniGene2Go.setFunction(ss[8]);uniGene2Go.setDataBase("UniProt");
				
				UniGene2Go uniGene2Go2 = uniGene2GoDao.queryUniGene2Go(uniGene2Go);
				if (uniGene2Go2!=null) //如果已经存在了，那么考虑下是否升级
				{
					boolean update=false;
					if (uniGene2Go.getEvidence()!=null&&uniGene2Go.getEvidence()!=null&&!uniGene2Go2.getEvidence().contains(uniGene2Go.getEvidence())) {
						uniGene2Go2.setEvidence(uniGene2Go2.getEvidence()+"/"+uniGene2Go.getEvidence());
						update=true;
					}
					if (uniGene2Go2.getReference()!=null&&uniGene2Go.getReference()!=null&&!uniGene2Go2.getReference().contains(uniGene2Go.getReference())) {
						uniGene2Go2.setReference(uniGene2Go2.getReference()+"/"+uniGene2Go.getReference());
						update=true;
					}
					if (update) {
						uniGene2GoDao.upDateUniGene2Go(uniGene2Go2);
					}
				}
				else //如果没有，则升级
				{
					uniGene2GoDao.InsertUniGene2Go(uniGene2Go);
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
		
		DaoFSGo2Term friceDAO=new DaoFSGo2Term();
		
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
				Go2Term go2Term2=friceDAO.queryGo2Term(go2Term);
				if (go2Term2==null) {
					go2Term.setGoID(ss[0].trim());
					go2Term.setGoIDQuery(ss[0].trim());
					go2Term.setGoTerm(ss[2].trim());
					go2Term.setGoFunction(ss[3].trim());
					friceDAO.InsertGo2Term(go2Term);
				}
			}
			else 
			{
				//装入别名
				for (int j = 0; j < ss2.length; j++) 
				{
					Go2Term go2Term=new Go2Term();
					go2Term.setGoIDQuery(ss2[j].trim());
					Go2Term go2Term2=friceDAO.queryGo2Term(go2Term);
					if (go2Term2==null) {
						go2Term.setGoID(ss[0].trim());
						go2Term.setGoIDQuery(ss2[j].trim());
						go2Term.setGoTerm(ss[2].trim());
						go2Term.setGoFunction(ss[3].trim());
						friceDAO.InsertGo2Term(go2Term);
					}
				}
				Go2Term go2Term=new Go2Term();
				go2Term.setGoIDQuery(ss[0].trim());
				Go2Term go2Term2=friceDAO.queryGo2Term(go2Term);
				if (go2Term2==null) {
					go2Term.setGoID(ss[0].trim());
					go2Term.setGoIDQuery(ss[0].trim());
					go2Term.setGoTerm(ss[2].trim());
					go2Term.setGoFunction(ss[3].trim());
					friceDAO.InsertGo2Term(go2Term);
				}
			}
			
	
			i++;
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
		TxtReadandWrite txtBInfo=new TxtReadandWrite();
		txtBInfo.setParameter(blast2InfoFile, false, true);
		BufferedReader readerBInfo=txtBInfo.readfile();
		String content="";
		while ((content=readerBInfo.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			BlastInfo blastInfo=new BlastInfo();
			blastInfo.setQueryID(ss[0]);blastInfo.setQueryTax(Integer.parseInt(ss[1]));blastInfo.setSubjectTax(Integer.parseInt(ss[4]));
			//Date date=(Date) new SimpleDateFormat("yyyy-MM-dd").parse(ss[8]);
			blastInfo.setBlastDate(ss[8]);//这个不会用于查询
			BlastInfo blastInfo2=DaoFSBlastInfo.queryBlastInfo(blastInfo);
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
					DaoFSBlastInfo.upDateGene2Go(blastInfo);
				}
				continue;
			}
			DaoFSBlastInfo.InsertGene2Go(blastInfo);
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
		BufferedReader readerTaxID=txtTaxID.readfile();
		String content="";
		DaoFSBlastInfo daoSBlastInfo=new DaoFSBlastInfo();
		String[][] taxInfo=txtTaxID.ExcelRead("\t", 1, 1, txtTaxID.ExcelRows(), txtTaxID.ExcelColumns("\t"));
		for (int i = 0; i < taxInfo.length; i++) {
			TaxInfo taxID=new TaxInfo();
			taxID.setTaxID(Integer.parseInt(taxInfo[i][0]));taxID.setChnName(taxInfo[i][1]);taxID.setLatin(taxInfo[i][2]);
			taxID.setComName(taxInfo[i][3]);taxID.setAbbr(taxInfo[i][4]);
			if (DaoFSTaxID.queryTaxInfo(taxID)==null) {
				DaoFSTaxID.InsertTaxInfo(taxID);
			}
			else {
				DaoFSTaxID.upDateTaxInfo(taxID);
			}
		}
	}
	
	
	
	
	
	
}











