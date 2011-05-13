package com.novelbio.database.upDateDB.dataBase;


import java.io.BufferedReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
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


public class UpDateNBCDBFile {
	/**
	 * ר�Ŵ���Gene2Go�ı�
	 * �����й̶�TaxID��gene2Go��������ݿ⣬����ʱ�������·���
	 * ���ȿ����ݿ����Ƿ���ָ��geneID��GOID����
	 * ���û�У�ֱ�Ӳ���
	 * ����У���Evidence��һ���Ƿ�һ����һ���Ļ�������������һ������updata
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
			
			if (ss[7].equals("Function")) {
				ss[7] = "F";
			}
			else if (ss[7].equals("Process")) {
				ss[7] = "P";
			}
			else if (ss[7].equals("Component")) {
				ss[7] = "C";
			}
			gene2GoInfo.setFunction(ss[7]);
			gene2GoInfo.setDataBase("NCBI");
			Gene2Go gene2GoInfo2=DaoFSGene2Go.queryGene2Go(gene2GoInfo);
			if (gene2GoInfo2==null) 
			{
				DaoFSGene2Go.InsertGene2Go(gene2GoInfo);
			}
			else {
				//����Ѿ�������Ӧ��GOID����ô��evidence�Ƿ�Ҳ�����ˣ�û�еĻ���upDate
				if(gene2GoInfo2.getEvidence() !=null 
						&& !gene2GoInfo2.getEvidence().trim().equals("")
						&& !gene2GoInfo2.getEvidence().contains(gene2GoInfo.getEvidence()))
				{
					gene2GoInfo.setEvidence(gene2GoInfo.getEvidence()+"/"+gene2GoInfo2.getEvidence());
					DaoFSGene2Go.upDateGene2Go(gene2GoInfo);
				}
			}
			i++;
			if (i%10000==0) {
				System.out.println(i);
			}
		}
		txtgene2Go.close();
	}
	
	
	
	/**
	 * ��NCBIID���͵ı�������ݿ�
	 * ���� \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
	 * ������Ľ���е�geneID���ܺ��У� geneID//geneID//geneID<br>
	 * ע��һ��Ҫ�ֿ���Ҳ�������һ��accID��Ӧ���geneID����ô�ͽ�������geneID������NCBIID
	 * ��geneID��accessIDһ��ȥ����NCBIID��
	 * ���û�У�ֱ�Ӳ���
	 * ���������
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
			//���geneID�ֱ���һ��
			for (String string : ssGeneID) 
			{
				NCBIID ncbiid = new NCBIID();
				ncbiid.setTaxID(Integer.parseInt(ss[0]));
				ncbiid.setGeneId((long)Double.parseDouble(string));
				ncbiid.setAccID(ss[2]); 
				//����ѵ��ˣ���ô���Ƿ���Ҫ����DBINFO
				ArrayList<NCBIID> lsNcbiids = DaoFSNCBIID.queryLsNCBIID(ncbiid);
				if (lsNcbiids != null && lsNcbiids.size() > 0) 
				{
					//�����Ҫ����dbinfo����ô�����е�dbinfo�Ƿ���ϵ�һ���������һ���Ļ����Ÿ���
					if (updateDBINFO && !lsNcbiids.get(0).getDBInfo().equals(ss[3])) 
					{
						ncbiid.setDBInfo(ss[3]);
						DaoFSNCBIID.upDateNCBIID(ncbiid);
						i++;
					}
					else
						continue;
				}
				else
				{
					ncbiid.setDBInfo(ss[3]);
					DaoFSNCBIID.InsertNCBIID(ncbiid);
					i++;
				}
			}
			if (i%10000==0) {
				System.out.println(i);
			}
		}
		System.out.println(i);
		txtNCBIID.close();
	}
	
	/**
	 * ��NCBIID���͵ı�������ݿ⣬����ǵ�NCBIID��ȫ���ʱ����ʱ��affyоƬ������һЩgeneID���Ѿ����ڵ�ID����ʱ������ID��Ҫ��ȥ
	 * ʱ�������·���
	 * <b>����GeneIDȥ����NCBIID��</b>������ҵ���
	 * ��geneID��accessIDһ��ȥ����NCBIID��
	 * ���û�У�ֱ�Ӳ���<br>
	 * <b>���û�ҵ�����ô��û�ҵ�����д��һ���ļ���</b>
	 * ���������
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
			
			ncbiid.setGeneId((long)Double.parseDouble(ss[1]));//����geneID��
			ArrayList<NCBIID> lsncbiid=DaoFSNCBIID.queryLsNCBIID(ncbiid);
			if (lsncbiid==null||lsncbiid.size()==0) 
			{
				txtnoFindFile.writefile(content+"\n");
				continue;
			}

			ncbiid.setAccID(ss[2]);
			
			//geneID�� accID����һ��鿴�Ƿ��ܲ鵽
			NCBIID ncbiid2=DaoFSNCBIID.queryNCBIID(ncbiid);
			if (ncbiid2==null) 
			{
				ncbiid.setTaxID(Integer.parseInt(ss[0]));
				ncbiid.setDBInfo(ss[3]);
				DaoFSNCBIID.InsertNCBIID(ncbiid);
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
	 * ��gene_refseq_uniprotkb_collab.txt �ı�������ݿ⣬������refseq��NCBIID���ѵ��ĵõ���geneID��Ȼ����uniIDһ�����NCBIID������<br>
	 * ʱ�������·���<br>
	 * <b>������refseq��NCBIID</b>������ҵ���
	 * ��geneID��uniIDһ��ȥ����NCBIID�����û�ҵ��������<br>
	 * 
	 * <b>�����refseqû�ҵ�����ô����</b><br>
	 * ���������
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
			
			ncbiid.setAccID(ss[0]);//����refseq��
			ArrayList<NCBIID> lsncbiid=DaoFSNCBIID.queryLsNCBIID(ncbiid);
			if (lsncbiid==null||lsncbiid.size()==0) 
			{
				continue;
			}
			long geneID = lsncbiid.get(0).getGeneId();
			int taxID = lsncbiid.get(0).getTaxID();
			ncbiid.setGeneId(geneID);
			ncbiid.setAccID(ss[1]);
			
			//geneID�� uniID����һ��鿴�Ƿ��ܲ鵽���ܲ鵽���������鲻���Ͳ���
			NCBIID ncbiid2=DaoFSNCBIID.queryNCBIID(ncbiid);
			if (ncbiid2==null) 
			{
				ncbiid.setTaxID(taxID);
				ncbiid.setDBInfo(NovelBioConst.DBINFO_UNIPROT_UNIID);
				DaoFSNCBIID.InsertNCBIID(ncbiid);
				i++;
			}
			else {
				continue;
			}
			
			if (i%10000==0) {
				System.out.println(i);
			}
		}
		System.out.println(i);
	}
	
	
	/**
	 * ��UniProtID���͵ı�������ݿ�
	 * @param UniProtIDfile
	 * @param NCBIID �Ƿ����NCBIID��
	 * true����UniProtID���accID����NCBI���Ҳ�����д���ı�
	 * false��ֱ�ӽ�UniProtID�����uniID��
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
				//�Ȳ���NCBIID��
				NCBIID ncbiid = new NCBIID();
				ncbiid.setTaxID(taxID);
				ncbiid.setAccID(ss[1]);
				ArrayList<NCBIID> lsNcbiids = DaoFSNCBIID.queryLsNCBIID(ncbiid);
				if (lsNcbiids != null && lsNcbiids.size()>0) 
				{
					//�鵽�Ļ������ñ���������accIDȥ��NCBIID�����û�鵽����ô����
					long geneID = lsNcbiids.get(0).getGeneId();
					ncbiid.setGeneId(geneID);
					ncbiid.setAccID(ss[2]);
					ArrayList<NCBIID> lsNcbiids2 = DaoFSNCBIID.queryLsNCBIID(ncbiid);
					if (lsNcbiids2 == null || lsNcbiids2.size() == 0) {
						ncbiid.setDBInfo(ss[3]);
						DaoFSNCBIID.InsertNCBIID(ncbiid);
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
				ArrayList<UniProtID> lsuniProtid2=DaoFSUniProtID.queryLsUniProtID(uniProtid);
				if (lsuniProtid2==null || lsuniProtid2.size() == 0) 
				{
					uniProtid.setDBInfo(ss[3]);
					DaoFSUniProtID.InsertUniProtID(uniProtid);
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
	 * ��geneInfo���͵ı�������ݿ�
	 * ʱ�������·���
	 * ��geneIDȥ����geneInfo��
	 * ���û�У�ֱ�Ӳ���
	 * ���������
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

			GeneInfo geneInfo2=DaoFSGeneInfo.queryGeneInfo(geneInfo);
			if (geneInfo2==null)
			{
				DaoFSGeneInfo.InsertGeneInfo(geneInfo);
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
	 * ��UniProt��unfilter��Go�ļ�����4����󣬽����е�GeneInfo����geneInfo���ݿ�
	 * geneInfo���͵ı�������ݿ�
	 * ʱ�������·���
	 * ��geneIDȥ����GeneInfo��
	 * ���û�У�ֱ�Ӳ��룬����У�����discription�ǲ���һ������һ���͸���
	 * ���������
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
	 * ��UniProt��unfilter��Go�ļ�����4����󣬽����е�UniProtInfo����UnigeneInfo���ݿ�
	 * geneInfo���͵ı�������ݿ�
	 * ʱ�������·���
	 * ��geneIDȥ����UniGeneInfo��
	 * ���û�У�ֱ�Ӳ���,����У�����discription�ǲ���һ������һ���͸���
	 * ���������
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
	 * ��UniProtgene_associationgoa_uniprot����а���TaxID��ȡ�����ļ�(���700����)�е�
	 * GO���뵽Gene2Go��� UniGene2Go��
	 * ��������:���Ȳ���NCBIID��������ҵ�������ȡNCBIID�е�geneID��Ȼ��GO����Gene2Go��
	 * ����ֱ�Ӳ���UniGene2Go��
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
			int taxID=Integer.parseInt(ss[12].split("\\|")[0].split(":")[1]);//���е�taxID
			ncbiid.setAccID(ss[1]);ncbiid.setTaxID(taxID);
			ArrayList<NCBIID> lsNcbiids=ncbiidDao.queryLsNCBIID(ncbiid);
			if (lsNcbiids.size()>0)//�ܺ�NCBIID��Ӧ��ȥ��Ҳ����˵�ܹ��ҵ�GeneID����ô����װ��gene2Go��
			{
				for (int i = 0; i < lsNcbiids.size(); i++)   //һ��������ܻ��ж��geneID
				{
					Gene2Go gene2Go=new Gene2Go();
					gene2Go.setGeneId(lsNcbiids.get(i).getGeneId());
					gene2Go.setGOID(ss[4].trim());gene2Go.setQualifier(ss[3].trim());gene2Go.setReference(ss[5]);gene2Go.setEvidence(ss[6]);gene2Go.setFunction(ss[8]);gene2Go.setDataBase("UniProt");
					Gene2Go gene2Go2 = gene2GoDao.queryGene2Go(gene2Go);
					if (gene2Go2!=null) //����Ѿ������ˣ���ô�������Ƿ�����
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
					else //���û�У�������
					{
						gene2GoDao.InsertGene2Go(gene2Go);
					}
				}
			}
			else //����װ��UniGene2Go��
			{
				UniGene2Go uniGene2Go=new UniGene2Go();
				uniGene2Go.setUniProtID(ss[1].trim());uniGene2Go.setGOID(ss[4].trim());uniGene2Go.setQualifier(ss[3].trim());uniGene2Go.setReference(ss[5]);
				uniGene2Go.setEvidence(ss[6]);uniGene2Go.setFunction(ss[8]);uniGene2Go.setDataBase("UniProt");
				
				UniGene2Go uniGene2Go2 = uniGene2GoDao.queryUniGene2Go(uniGene2Go);
				if (uniGene2Go2!=null) //����Ѿ������ˣ���ô�������Ƿ�����
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
				else //���û�У�������
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
	 * ����amiGo�����ص�Go��������ݿ�
	 * ʱ�������·���
	 * ��GoIDquery,GoID,GoFunction����ȥ����Go2Term��
	 * ���û�У�ֱ�Ӳ���
	 * ���������
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
			if (ss[1].trim().equals("")) //û�б���
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
				//װ�����
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
	 * ��Blast����õĽ������BlastInfo��
	 * ����ʱ�������·���
	 * ��queryID��queryTax��subjectTax�������ݿ⣬���û�ҵ�������롣����ҵ������ȿ�subjectID�Ƿ�һ�£������һ�£���Ƚ�evalue������¼����evalue��С�����������ݿ�
	 * ���������
	 * @author zong0jie
	 * @param blast2InfoFile blast�ļ�
	 * @param subjectTab blast����Ŀ�����������ĸ����У���NCBIID����UniProtID
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
			blastInfo.setBlastDate(ss[8]);//����������ڲ�ѯ
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
	 * ��TaxID����õĽ������taxID����
	 * ����ʱ�������·���
	 * ��taxID�������ݿ⣬���û�ҵ�������롣����ҵ���������
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











