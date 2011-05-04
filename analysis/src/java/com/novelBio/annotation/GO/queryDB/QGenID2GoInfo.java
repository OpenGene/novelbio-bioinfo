package com.novelBio.annotation.GO.queryDB;

import java.io.BufferedReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.novelBio.annotation.pathway.kegg.prepare.KGprepare;
import com.novelBio.base.dataOperate.ExcelTxtRead;
import com.novelBio.base.dataOperate.TxtReadandWrite;



import DAO.FriceDAO.DaoFCGene2GoInfo;
import DAO.FriceDAO.DaoFSGo2Term;


import entity.friceDB.Gene2Go;
import entity.friceDB.Gene2GoInfo;
import entity.friceDB.Go2Term;
import entity.friceDB.NCBIID;
import entity.friceDB.Uni2GoInfo;
import entity.friceDB.UniGene2Go;
import entity.friceDB.UniProtID;

/**
 * 查找GeneID2GoInfo的信息,
 * 将输入的ID进行整合，相同的ID整合为一个ID
 * 需要修正，暂时没有办法区分P、C、F
 * @author zong0jie
 *
 */
public class QGenID2GoInfo 
{

	ArrayList<Gene2GoInfo> lsGene2GoInfos=null;
	ArrayList<Uni2GoInfo> lsUni2GoInfos=null;
	
	/**
	 * 用来保存geneID2Go，和UniProtID2Go的两个文件
	 * 主要用来写入背景
	 * 格式为：<br>
	 * geneID	GO:0004835, GO:0006464, GO:0016874

	 */
	ArrayList<String[]> lsGene2Go=null;
	ArrayList<String[]> lsUniGene2Go=null;
	
	/**
	 * 给定基因文件，背景文件，taxID文件，返回GO整理的列表，用于计算pvalue和FDR<b>有问题，数据库查找GO时感觉不对，查到的比较少</b>
	 * 以及每个Gene的GO信息0: queryID 1: geneID 2: symbol 3: GOID 4: GOTerm
	 * 将结果放入object[2]中
	 * 0：lsGOInfo 准备做fisher的表格  GOID	GOTerm	difGene	AllDifGene	GeneInGoID	AllGene	//后面加的Pvalue	FDR	enrichment	logP <br>
	 * 1：0: queryID 1: geneID 2: symbol 3: GOID 4: GOTerm
	 * @param geneFile
	 * @param backGroundFile
	 * @param taxIDfile
	 * @throws Exception 
	 * @return arrayList-string[6] 0:GOID 1:GOTerm 2:difInGoNum 3:difNum 4:GeneGoNum 5:GeneNum
	 */
	public Object[]  goAnalysis(int taxID,String[] geneID,String backGroundFile) throws Exception 
	//public void goAnalysis(String geneFile,String backGroundFile,String taxIDfile,String writeFIle) throws Exception 
	{
		int NumDifGene=0;
		int NumAllGene=0;
		/**
		 * GeneID的处理
		 */
		getGenID2GoInfo(geneID, taxID);
		ArrayList<String[]> lsGoResult=copeGene2GoInfo();
		ArrayList<String[]> lsUniGoResult=copeUni2GoInfo();
		
		//用hash表来将gene2Go里的信息去重复。
		Hashtable<String, String> hashGene2Go=new Hashtable<String, String>();
		for (int j = 0; j < lsGene2Go.size(); j++) {
			hashGene2Go.put(lsGene2Go.get(j)[0], lsGene2Go.get(j)[1]);
		}
		for (int j = 0; j < lsUniGene2Go.size(); j++) {
			hashGene2Go.put(lsUniGene2Go.get(j)[0], lsUniGene2Go.get(j)[1]);
		}
		
		//////////////////////////将差异基因的Gene2Go的信息转换为Go2Gene的信息//////////////////////////////
		Hashtable<String, ArrayList<String>> hashGo2Gene=new Hashtable<String, ArrayList<String>>();
		
		//将gene2go的信息转入hashGo2Gene
		Enumeration keys=hashGene2Go.keys();
		while(keys.hasMoreElements()){
			String key=(String)keys.nextElement();
		    String val = (String) hashGene2Go.get(key);
		    String[] ss=val.split(",");
		    for (int j = 0; j < ss.length; j++) 
		    {
		    	String GOID=ss[j].trim();
		    	if (hashGo2Gene.containsKey(GOID))
		    	{
					ArrayList<String> lsgeneID=hashGo2Gene.get(GOID);
					if (!lsgeneID.contains(key))
					{
						lsgeneID.add(key);
					}
				}
		    	else
		    	{
					ArrayList<String> lsgeneID=new ArrayList<String>();
					lsgeneID.add(key);
					hashGo2Gene.put(GOID, lsgeneID);
				}
			}
		}
		//GeneID写入结果
		//GeneID去重复，并获得去重复后的基因列表
		HashSet<String> hashGeneID=new HashSet<String>();
		for (int i = 0; i < lsGoResult.size(); i++) {
			hashGeneID.add(lsGoResult.get(i)[1]);
		}
		for (int i = 0; i < lsUniGoResult.size(); i++) {
			hashGeneID.add(lsUniGoResult.get(i)[1]);
		}
		  Iterator<String> it = hashGeneID.iterator();
		  //去重复后的基因列表
		  String[] strGeneID = new String[hashGeneID.size()];int i=0;
		  while(it.hasNext())
		  {
			  strGeneID[i]=it.next();
			  i++;
		  }
		/**
		 * BackGroudID的处理
		 */
		String[] BackGroundID = KGprepare.getAccID(backGroundFile, 1, 1);
		getGenID2GoInfo(BackGroundID, taxID);
		ArrayList<String[]> lsBGGoResult=copeGene2GoInfo();
		ArrayList<String[]> lsBGUniGoResult=copeUni2GoInfo();
		//用hash表来将gene2Go里的信息去重复。
		Hashtable<String, String> hashGene2GoAll=new Hashtable<String, String>();
		for (int j = 0; j < lsGene2Go.size(); j++) {
			hashGene2GoAll.put(lsGene2Go.get(j)[0], lsGene2Go.get(j)[1]);
		}
		for (int j = 0; j < lsUniGene2Go.size(); j++) {
			hashGene2GoAll.put(lsUniGene2Go.get(j)[0], lsUniGene2Go.get(j)[1]);
		}
		
		//////////////////////////将背景基因的Gene2Go的信息转换为Go2Gene的信息//////////////////////////////
		Hashtable<String, ArrayList<String>> hashGo2GeneAll=new Hashtable<String, ArrayList<String>>();
		//将gene2go的信息转入hashGo2Gene
		Enumeration keysAll=hashGene2GoAll.keys();

		while(keys.hasMoreElements()){
			String geneIDthis=(String)keysAll.nextElement();
		    String goIDthis =hashGene2GoAll.get(geneIDthis);
		    String[] ss=goIDthis.split(",");
		    for (int j = 0; j < ss.length; j++) 
		    {
		    	String GOID=ss[j].trim();
		    	if (hashGo2GeneAll.containsKey(GOID))
		    	{
					ArrayList<String> lsgeneID=hashGo2GeneAll.get(GOID);
					//if (!lsgeneID.contains(geneIDthis)) {
					lsgeneID.add(geneIDthis);
			//		}
				}
		    	else
		    	{
					ArrayList<String> lsgeneID=new ArrayList<String>();
					lsgeneID.add(geneIDthis);
					hashGo2GeneAll.put(GOID, lsgeneID);
				}
			}
		}
		NumDifGene=hashGene2Go.size();
		NumAllGene=hashGene2GoAll.size();
		ArrayList<String[]> lsGOInfo=cope2HashForPvalue(hashGo2Gene, NumDifGene, hashGo2GeneAll, NumAllGene);
	//	TxtReadandWrite goInfo=new TxtReadandWrite();
		//goInfo.setParameter(writeFIle, true, false);
	//	goInfo.ExcelWrite(lsGOInfo, "\t", 1, 1);
		
		
		for (String[] strings : lsUniGoResult) {
			lsGoResult.add(strings);
		}
		//将结果放入object[2]中
		//0：lsGOInfo 准备做fisher的表格 
		//1：lsGoResult GO的具体信息 accessID  GeneSymbol  Blast2Symbol  GOID  GOTerm  GOEvidence
		Object[] objResult = new Object[2];
		objResult[0] = lsGOInfo;
		objResult[1] = lsGoResult;
		
		return objResult;		
	}
	
	
	/**
	 * 给定两个hashtable，一个为差异基因：
	 * GOTerm--list-GeneID[]
	 * 一个为总基因
	 * GOTerm---list-GeneID[]
	 * 注意差异基因必须在总基因中
	 * 
	 * @return
	 * arrayList-string[6]
	 * 0:GOID
	 * 1:GOTerm
	 * 2:difInGoNum
	 * 3:difNum
	 * 4:GeneGoNum
	 * 5:GeneNum
	 */
	private ArrayList<String[]> cope2HashForPvalue(Hashtable<String, ArrayList<String>> hashDif,int NumDif,Hashtable<String, ArrayList<String>> hashAll,int NumAll) {
		ArrayList<String[]> lsResult=new ArrayList<String[]>();
	
		Enumeration keys=hashDif.keys();
		//这个是结果文件，保存了如下结果：
		//GOID,GOterm,该GOID的差异基因Num，总差异基因Num，该GOID的总基因Num，总基因的Num,
		while(keys.hasMoreElements()){
			String GOID=(String)keys.nextElement();
		    ArrayList<String> lsGeneID = hashDif.get(GOID);
		    String[] tmpResult=new String[6];
		    tmpResult[0]=GOID;
		    Go2Term go2Term=new Go2Term(); go2Term.setGoIDQuery(GOID);
		    Go2Term go2Term2=DaoFSGo2Term.queryGo2Term(go2Term);
		    tmpResult[1]=go2Term2.getGoTerm();
		    tmpResult[2]=lsGeneID.size()+"";
		    tmpResult[3]=NumDif+"";
		    tmpResult[4]=hashAll.get(GOID).size()+"";
		    tmpResult[5]=NumAll+"";
		    lsResult.add(tmpResult);
		}
		
		return lsResult;
	}
	


	/**
	 * 给定基因文件，背景文件，taxID文件，返回三个列表,用于R计算GO关系，<b>注意去重复功能没有做好</b><br>
	 * 1. GeneID的GO列表<br>
	 * 2. BackGroundGO列表，格式为 geneID \t GOID \t GOID....\n<br>
	 * 3. GeneID的列表  格式为 geneID \n<br>
	 * @param geneFile 第一行是accID，自动去除XM_00102.1的.1 <br>
	 * @param backGroundFile <br>
	 * @param taxIDfile <br>
	 * @throws Exception <br>
	 */
	public void goAnalysis(String geneFile,String backGroundFile,String taxIDfile,   String resultGeneGofile,String resultBGGofile,String resultGeneIDfile) throws Exception 
	{
		//获得TaxID，TaxID写在taxIDfile的第一行，是数字
		TxtReadandWrite txtTaxID=new TxtReadandWrite();
		txtTaxID.setParameter(taxIDfile, false, true);
		BufferedReader taxIDReader=txtTaxID.readfile();
		int taxID=Integer.parseInt((taxIDReader.readLine().trim()));
		
		/**
		 * GeneID的处理
		 */
		String[] geneID = KGprepare.getAccID(geneFile, 1, 1);
		getGenID2GoInfo(geneID, taxID);
		ArrayList<String[]> lsGoResult = copeGene2GoInfo();
		ArrayList<String[]> lsUniGoResult = copeUni2GoInfo();
		//GeneID写入结果
		//GeneID去重复
		HashSet<String> hashGeneID=new HashSet<String>();
		for (int i = 0; i < lsGoResult.size(); i++) {
			hashGeneID.add(lsGoResult.get(i)[1]);
		}
		for (int i = 0; i < lsUniGoResult.size(); i++) {
			hashGeneID.add(lsUniGoResult.get(i)[1]);
		}
		  Iterator<String> it = hashGeneID.iterator();
		  String[] strGeneID = new String[hashGeneID.size()];int i=0;
		  while(it.hasNext())
		  {
			  strGeneID[i]=it.next();
			  i++;
		  }
		TxtReadandWrite txtGeneID=new TxtReadandWrite(); txtGeneID.setParameter(resultGeneIDfile, true, false);
		txtGeneID.Rwritefile(strGeneID);
		//GeneGo写入结果
		txtGeneID.setParameter(resultGeneGofile, true, false);
		txtGeneID.ExcelWrite(lsGoResult, "\t", 1, 1);
		txtGeneID.ExcelWrite(lsUniGoResult, "\t", 1, 1);

		/**
		 * BackGroudID的处理
		 */
		String[] BackGroundID =  KGprepare.getAccID(backGroundFile, 1, 1);
		getGenID2GoInfo(BackGroundID, taxID);
		ArrayList<String[]> lsBGGoResult=copeGene2GoInfo();
		ArrayList<String[]> lsBGUniGoResult=copeUni2GoInfo();
		//GeneID2Go写入结果
		TxtReadandWrite txtBGGeneID=new TxtReadandWrite(); txtBGGeneID.setParameter(resultBGGofile, true, false);
		txtBGGeneID.ExcelWrite(lsGene2Go, "\t", 1, 1);
		txtBGGeneID.ExcelWrite(lsUniGene2Go, "\t", 1, 1);
	}
	
	/**
	 * 生成两个list，	ArrayList<Gene2GoInfo> lsGene2GoInfos
	 * 和 ArrayList<Uni2GoInfo> lsUni2GoInfos
	 * @param geneID
	 * @param taxID
	 */
	private void getGenID2GoInfo(String[] geneID,int taxID) {
		lsGene2GoInfos=new ArrayList<Gene2GoInfo>(); 
		 lsUni2GoInfos=new ArrayList<Uni2GoInfo>();
		for (int i = 0; i < geneID.length; i++)
		{
			NCBIID ncbiid=new NCBIID();
			ncbiid.setAccID(geneID[i]);ncbiid.setTaxID(taxID);
			UniProtID uniProtID=new UniProtID();
			uniProtID.setAccID(geneID[i]);uniProtID.setTaxID(taxID);
			Gene2GoInfo gene2GoInfo=DaoFCGene2GoInfo.queryGeneDetail(ncbiid);
			if (gene2GoInfo!=null)
				lsGene2GoInfos.add(gene2GoInfo);
			else
			{
				Uni2GoInfo uni2GoInfo=DaoFCGene2GoInfo.queryUniDetail(uniProtID);
				if (uni2GoInfo!=null) 
				{
					lsUni2GoInfos.add(uni2GoInfo);
				}
			}
		}
	}
	
	/**
	 * 给定Gene2GoInfo的列表，将其中信息整理为
	 * ArrayList-String[5]
	 * 其中：0: queryID
	 * 1: geneID
	 * 2: symbol
	 * 3: GOID
	 * 4: GOTerm
	 * 同时填充lsGene2Go
	 * @param lsGene2GoInfos
	 */
	private ArrayList<String[]> copeGene2GoInfo()
	{
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		lsGene2Go=new ArrayList<String[]>();
		
		for (int i = 0; i < lsGene2GoInfos.size(); i++) 
		{
			Gene2GoInfo gene2GoInfo=lsGene2GoInfos.get(i);
			//如果没有找到基因 或者 该基因没有Go的信息，则跳过
			if (gene2GoInfo==null||gene2GoInfo.getLsGOInfo()==null||gene2GoInfo.getLsGOInfo().size()==0) 
				continue;
			
			ArrayList<Gene2Go> lsGoInfos=gene2GoInfo.getLsGOInfo();
			//设置GeneID2Go的基因列
			String[] strGene2Go=new String[2];
			strGene2Go[0]=gene2GoInfo.getGeneId()+"";
			strGene2Go[1]="";
			for (int j = 0; j < lsGoInfos.size(); j++) {
				if (j==0) 
					strGene2Go[1]=lsGoInfos.get(j).getGOID();
				else
					strGene2Go[1]=strGene2Go[1]+", "+lsGoInfos.get(j).getGOID();
				
				//每个基因对应的Go信息，一个基因对应一个GO，然后一对多的话排很多列
				String[] gene2Go=new String[5];
				for (int k = 0; k < gene2Go.length; k++)
					gene2Go[k]="";//赋初值，都为空值
				
				gene2Go[0]=gene2GoInfo.getQuaryID();gene2Go[1]=gene2GoInfo.getGeneId()+"";gene2Go[2]=gene2GoInfo.getGeneInfo().getSymbol();
				gene2Go[3]=lsGoInfos.get(j).getGOID();gene2Go[4]=lsGoInfos.get(j).getGOTerm();
				lsGoResult.add(gene2Go);
			}
			
			lsGene2Go.add(strGene2Go);
		}
		return lsGoResult;
	}
	
	/**
	 * 给定Uni2GoInfo的列表，将其中信息整理为
	 * ArrayList-String[5]
	 * 其中：0: queryID
	 * 1: uniID
	 * 2: symbol
	 * 3: GOID
	 * 4: GOTerm
	 * 同时填充lsUni2GoInfos
	 * @param lsUni2GoInfos
	 */
	private ArrayList<String[]>  copeUni2GoInfo()
	{
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		lsUniGene2Go = new ArrayList<String[]>();
		
		for (int i = 0; i < lsUni2GoInfos.size(); i++) 
		{
			Uni2GoInfo uni2GoInfo=lsUni2GoInfos.get(i);
			//如果没有找到基因 或者 该基因没有Go的信息，则跳过
			if (uni2GoInfo==null||uni2GoInfo.getLsUniGOInfo()==null||uni2GoInfo.getLsUniGOInfo().size()==0) 
				continue;
			ArrayList<UniGene2Go> lsUniGoInfos=uni2GoInfo.getLsUniGOInfo();
			//设置GeneID2Go的基因列
			String[] strGene2Go=new String[2];
			strGene2Go[0]=uni2GoInfo.getUniID();
			strGene2Go[1]="";
			for (int j = 0; j < lsUniGoInfos.size(); j++) {
				if (j==0) 
					strGene2Go[1]=lsUniGoInfos.get(j).getGOID();
				else
					strGene2Go[1]=strGene2Go[1]+", "+lsUniGoInfos.get(j).getGOID();
				
				String[] uni2Go=new String[5];
				for (int k = 0; k < uni2Go.length; k++) {
					uni2Go[k]="";//赋初值，都为空值
				}
				uni2Go[0]=uni2GoInfo.getQuaryID();uni2Go[1]=uni2GoInfo.getUniID();uni2Go[2]=uni2GoInfo.getUniGeneInfo().getSymbol();
				uni2Go[3]=lsUniGoInfos.get(j).getGOID();uni2Go[4]=lsUniGoInfos.get(j).getGOTerm();
				lsGoResult.add(uni2Go);
			}
			lsUniGene2Go.add(strGene2Go);
		}
		return lsGoResult;
	}
}
