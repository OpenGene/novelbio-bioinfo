package com.novelbio.analysis.annotation.GO.queryDB;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.DAO.FriceDAO.DaoFCGene2GoInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSGo2Term;
import com.novelbio.database.entity.friceDB.Gene2Go;
import com.novelbio.database.entity.friceDB.Gene2GoInfo;
import com.novelbio.database.entity.friceDB.Go2Term;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.Uni2GoInfo;
import com.novelbio.database.entity.friceDB.UniGene2Go;
import com.novelbio.database.entity.friceDB.UniProtID;

/**
 * ����GeneID2GoInfo����Ϣ
 * affy̽�뻹�Ƕ�Ӧaffy̽��
 * ����ʲôID���������ʲôID
 * @author zong0jie
 *
 */
public class QGenID2GoInfoSepID 
{

	ArrayList<Gene2GoInfo> lsGene2GoInfos=null;
	ArrayList<Uni2GoInfo> lsUni2GoInfos=null;
	
	/**
	 * ��������geneID2Go����UniProtID2Go�������ļ�
	 * ��Ҫ����д�뱳��:<br>
	 * geneID \t GOID,GOID,GOID
	 */
	ArrayList<String[]> lsGene2Go=null;
	
	/**
	 * ��������geneID2Go����UniProtID2Go�������ļ�
	 * ��Ҫ����д�뱳��
	 * geneID \t GOID,GOID,GOID
	 */
	ArrayList<String[]> lsUniGene2Go=null;
	
	
	//Hashtable<String, ArrayList<String>> Go2LsGene;
	//Hashtable<String, ArrayList<String>> Go2LsGeneAll;
	
	
	
	
	
	/**
	 * ���������ļ��������ļ���taxID�ļ������������б�,����elimfisher��
	 * 1. GeneID��GO�б�
	 * 2. BackGroundGO�б���ʽΪ geneID \t GOID \t GOID....\n
	 * 3. GeneID���б�  ��ʽΪ geneID \n
	 * @param geneFile
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 * @param backGroundFile
	 * @param taxIDfile
	 * @return object[2] 0:strGeneID  1:lsGoResult,������
	 	title[0] = "accessID";<br>
		title[1] = "geneID";<br>
		title[2] = "Symbol";<br>
		title[3] = "GOID";<br>
		title[4] = "GOTerm";;<br>
	 * @throws Exception 
	 */
	public Object[] goAnalysis(String[] geneID,String GOClass,String backGroundFile,int taxID, String resultGeneGofile,String resultBGGofile,String resultGeneIDfile) throws Exception 
	{
		/**
		 * GeneID�Ĵ���
		 */
		getGenID2GoInfo(geneID, taxID);
		ArrayList<String[]> lsGoResult=copeGene2GoInfo(GOClass);
		ArrayList<String[]> lsUniGoResult=copeUni2GoInfo(GOClass);
		  for (String[] strings : lsUniGoResult) {
				lsGoResult.add(strings);
			}
	 
		  String[] title = new String[5];
		  title[0] = "AccessID";
		  title[1] = "GeneID";
		  title[2] = "GeneSymbol";
		  title[3] = "GOID";
		  title[4] = "GOTerm";
		  lsGoResult.add(0, title);
		
		
		//GeneIDд����
		//GeneIDȥ�ظ�
		HashSet<String> hashGeneID=new HashSet<String>();
		for (int i = 0; i < lsGoResult.size(); i++) {
			hashGeneID.add(lsGoResult.get(i)[0]);
		}
		for (int i = 0; i < lsUniGoResult.size(); i++) {
			hashGeneID.add(lsUniGoResult.get(i)[0]);
		}
		  Iterator<String> it = hashGeneID.iterator();
		  String[] strGeneID = new String[hashGeneID.size()];int i=0;
		  while(it.hasNext())
		  {
			  strGeneID[i]=it.next();
			  i++;
		  }

			Object[] objGeneID_GeneInfo = new Object[2];
			objGeneID_GeneInfo[0] = strGeneID;
			objGeneID_GeneInfo[1] = lsGoResult;
		TxtReadandWrite txtGeneID=new TxtReadandWrite(); txtGeneID.setParameter(resultGeneIDfile, true, false);
		txtGeneID.Rwritefile(strGeneID);

		//GeneGoд����
		txtGeneID.setParameter(resultGeneGofile, true, false);
		txtGeneID.ExcelWrite(lsGoResult, "\t", 1, 1);
		/**
		 * BackGroudID�Ĵ���
		 */
		String[] BackGroundID=KGprepare.getAccID(backGroundFile, 1, 1);
		getGenID2GoInfo(BackGroundID, taxID);
		ArrayList<String[]> lsBGGoResult=copeGene2GoInfo(GOClass);
		ArrayList<String[]> lsBGUniGoResult=copeUni2GoInfo(GOClass);
		//GeneID2Goд����
		TxtReadandWrite txtBGGeneID=new TxtReadandWrite(); txtBGGeneID.setParameter(resultBGGofile, true, false);
		txtBGGeneID.ExcelWrite(lsGene2Go, "\t", 1, 1);
		txtBGGeneID.ExcelWrite(lsUniGene2Go, "\t", 1, 1);
		return objGeneID_GeneInfo;
	}
	
	
	/**
	 * ���������ļ��������ļ���taxID�ļ�������GO������б����ڼ���pvalue��FDR
	 * �Լ�ÿ��GO������Щ����
	 * ���������object[2]��
	 * 0��lsGOInfo ׼����fisher�ı��  GOID	GOTerm	difGene	AllDifGene	GeneInGoID	AllGene	//����ӵ�Pvalue	FDR	enrichment	logP <br>
	 * 1��lsGoResult GO�ľ�����Ϣ 0: queryID 1: geneID 2: symbol 3: GOID 4: GOTerm
	 * @param geneID geneID
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 * @param backGroundFile
	 * @param taxIDfile
	 * @throws Exception 
	 */
	public Object[] goAnalysis(int taxID,String[] geneID,String GOClass,String backGroundFile) throws Exception 
//	public void goAnalysis(String geneFile,String backGroundFile,String taxIDfile,   String writeFIle) throws Exception 
	{
		/**
		 * GeneID�Ĵ���
		 */
		getGenID2GoInfo(geneID, taxID);
		ArrayList<String[]> lsGoResult=copeGene2GoInfo(GOClass);
		ArrayList<String[]> lsUniGoResult=copeUni2GoInfo(GOClass);

		//��hash������gene2Go�����Ϣȥ�ظ���
		Hashtable<String, String> hashGene2Go=new Hashtable<String, String>();
		for (int j = 0; j < lsGene2Go.size(); j++) {
			hashGene2Go.put(lsGene2Go.get(j)[0], lsGene2Go.get(j)[1]);
		}
		for (int j = 0; j < lsUniGene2Go.size(); j++) {
			hashGene2Go.put(lsUniGene2Go.get(j)[0], lsUniGene2Go.get(j)[1]);
		}
		
		//////////////////////////����������Gene2Go����Ϣת��ΪGo2Gene����Ϣ//////////////////////////////
		Hashtable<String, ArrayList<String>> hashGo2Gene = new Hashtable<String, ArrayList<String>>();
		
		//��gene2go����Ϣת��hashGo2Gene
		Enumeration keys = hashGene2Go.keys();
		while(keys.hasMoreElements())
		{
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
		
		//GeneIDд����
		//GeneIDȥ�ظ�
		HashSet<String> hashGeneID=new HashSet<String>();
		for (int i = 0; i < lsGoResult.size(); i++) {
			hashGeneID.add(lsGoResult.get(i)[0]);
		}
		for (int i = 0; i < lsUniGoResult.size(); i++) {
			hashGeneID.add(lsUniGoResult.get(i)[0]);
		}
		  Iterator<String> it = hashGeneID.iterator();
		  String[] strGeneID = new String[hashGeneID.size()];int i=0;
		  while(it.hasNext())
		  {
			  strGeneID[i]=it.next();
			  i++;
		  }
		  /**
			 * BackGroudID�Ĵ���
			 */
			String[] BackGroundID = KGprepare.getAccID(backGroundFile, 1, 1);
			getGenID2GoInfo(BackGroundID, taxID);
			ArrayList<String[]> lsBGGoResult = copeGene2GoInfo(GOClass);
			ArrayList<String[]> lsBGUniGoResult = copeUni2GoInfo(GOClass);
			//��hash������gene2Go�����Ϣȥ�ظ���
			Hashtable<String, String> hashGene2GoAll=new Hashtable<String, String>();
			for (int j = 0; j < lsGene2Go.size(); j++) {
				hashGene2GoAll.put(lsGene2Go.get(j)[0], lsGene2Go.get(j)[1]);
			}
			for (int j = 0; j < lsUniGene2Go.size(); j++) {
				hashGene2GoAll.put(lsUniGene2Go.get(j)[0], lsUniGene2Go.get(j)[1]);
			}
	 
			//////////////////////////�����������Gene2Go����Ϣת��ΪGo2Gene����Ϣ//////////////////////////////
			Hashtable<String, ArrayList<String>> hashGo2GeneAll=new Hashtable<String, ArrayList<String>>();
			long start=System.currentTimeMillis(); //��ȡ���ʱ��
			
			//��gene2go����Ϣת��hashGo2Gene
			Enumeration keysAll=hashGene2GoAll.keys();

			while(keysAll.hasMoreElements())
			{
				String geneIDthis=(String)keysAll.nextElement();
			    String goIDthis =hashGene2GoAll.get(geneIDthis);
			    String[] ss=goIDthis.split(",");
			    for (int j = 0; j < ss.length; j++) 
			    {
			    	String GOID=ss[j].trim();
			    	if (hashGo2GeneAll.containsKey(GOID))
			    	{
						ArrayList<String> lsgeneID=hashGo2GeneAll.get(GOID);
						if (!lsgeneID.contains(geneIDthis)) {
							lsgeneID.add(geneIDthis);
						}
					}
			    	else
			    	{
						ArrayList<String> lsgeneID=new ArrayList<String>();
						lsgeneID.add(geneIDthis);
						hashGo2GeneAll.put(GOID, lsgeneID);
					}
				}
			}
			
			long end=System.currentTimeMillis(); //��ȡ���н���ʱ��
			System.out.println("��������ʱ�䣺 "+(end-start)+"ms"); 
			int NumDifGene=hashGene2Go.size();
			int NumAllGene=hashGene2GoAll.size();
			ArrayList<String[]> lsGOInfo=cope2HashForPvalue(hashGo2Gene, NumDifGene, hashGo2GeneAll, NumAllGene);
			//TxtReadandWrite goInfo=new TxtReadandWrite();
			//goInfo.setParameter(writeFIle, true, false);
			//goInfo.ExcelWrite(lsGOInfo, "\t", 1, 1);
			
		//	TxtReadandWrite txtGeneID=new TxtReadandWrite();
			//GeneGoд����
		//	txtGeneID.setParameter(resultGeneGofile, true, false);
		//	txtGeneID.ExcelWrite(lsGoResult, "\t", 1, 1);
		//	txtGeneID.ExcelWrite(lsUniGoResult, "\t", 1, 1);
			
			for (String[] strings : lsUniGoResult) {
				lsGoResult.add(strings);
			}
			//���������object[2]��
			//0��lsGOInfo ׼����fisher�ı�� 
			//1��lsGoResult GO�ľ�����Ϣ accessID  GeneSymbol  Blast2Symbol  GOID  GOTerm  GOEvidence
			Object[] objResult = new Object[2];
			objResult[0] = lsGOInfo;
			objResult[1] = lsGoResult;
			return objResult;
	}
	
	
	
	
	

	/**
	 * ��������hashtable��һ��Ϊ�������
	 * GOTerm--list-GeneID[]
	 * һ��Ϊ�ܻ���
	 * GOTerm---list-GeneID[]
	 * ע��������������ܻ�����
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
	private ArrayList<String[]> cope2HashForPvalue(Hashtable<String, ArrayList<String>> hashDif,int NumDif,Hashtable<String, ArrayList<String>> hashAll ,int NumAll) 
	{
		ArrayList<String[]> lsResult=new ArrayList<String[]>();
		Enumeration keys=hashDif.keys();
		//����ǽ���ļ������������½����
		//GOID,GOterm,��GOID�Ĳ������Num���ܲ������Num����GOID���ܻ���Num���ܻ����Num,
		while(keys.hasMoreElements()){
			String GOID=(String)keys.nextElement();
		    ArrayList<String> lsGeneID = hashDif.get(GOID);
		    String[] tmpResult=new String[6];
		    tmpResult[0]=GOID;
		    if (GOID.trim().equals("")) {
				continue;
			}
		    Go2Term go2Term=new Go2Term(); go2Term.setGoIDQuery(GOID);
		    Go2Term go2Term2=DaoFSGo2Term.queryGo2Term(go2Term);
		    tmpResult[1]=go2Term2.getGoTerm();
		    tmpResult[2]=lsGeneID.size()+"";
		    tmpResult[3]=NumDif+"";
		    if (hashAll.get(GOID)==null) {
				System.out.println(GOID);
			}
		    try {
		    	tmpResult[4]=hashAll.get(GOID).size()+"";
			} catch (Exception e) {
				continue;
			}
		    
		    tmpResult[5]=NumAll+"";
		    lsResult.add(tmpResult);
		}
		return lsResult;
	}

	/**
	 * ��������list��	ArrayList<Gene2GoInfo> lsGene2GoInfos
	 * �� ArrayList<Uni2GoInfo> lsUni2GoInfos=null;
	 * @param geneID
	 * @param taxID
	 */
	private void getGenID2GoInfo(String[] geneID,int taxID)
	{
		lsGene2GoInfos=new ArrayList<Gene2GoInfo>(); 
		 lsUni2GoInfos=new ArrayList<Uni2GoInfo>();
		for (int i = 0; i < geneID.length; i++)
		{
			NCBIID ncbiid=new NCBIID();
			ncbiid.setAccID(geneID[i]);ncbiid.setTaxID(taxID);
			UniProtID uniProtID=new UniProtID();
			uniProtID.setAccID(geneID[i]);uniProtID.setTaxID(taxID);
			ArrayList<Gene2GoInfo> lsGene2GoInfo=null;
			try {
				lsGene2GoInfo=DaoFCGene2GoInfo.queryLsGeneDetail(ncbiid);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (lsGene2GoInfo!=null&& lsGene2GoInfo.size()>0)
				lsGene2GoInfos.add(lsGene2GoInfo.get(0));
			else
			{
				ArrayList<Uni2GoInfo> lsUni2GoInfo=DaoFCGene2GoInfo.queryLsUniDetail(uniProtID);
				if (lsUni2GoInfo!=null && lsUni2GoInfo.size()>0) 
				{
					@SuppressWarnings("unused")
					ArrayList<Uni2GoInfo> lsUni2GoInfo2=DaoFCGene2GoInfo.queryLsUniDetail(uniProtID);
					lsUni2GoInfos.add(lsUni2GoInfo.get(0));
				}
			}
		}
	}
	
	/**
	 * ����Gene2GoInfo���б���������Ϣ����Ϊ
	 * ArrayList-String[5]
	 * ���У�0: queryID
	 * 1: geneID
	 * 2: symbol
	 * 3: GOID
	 * 4: GOTerm
	 * ͬʱ����һ�� lsGene2Go
	 * @param lsGene2GoInfos
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 */
	private ArrayList<String[]> copeGene2GoInfo(String GOClass)
	{
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		lsGene2Go=new ArrayList<String[]>();
		
		for (int i = 0; i < lsGene2GoInfos.size(); i++) 
		{
			Gene2GoInfo gene2GoInfo=lsGene2GoInfos.get(i);
			//���û���ҵ����� ���� �û���û��Go����Ϣ��������
			if (gene2GoInfo==null||gene2GoInfo.getLsGOInfo()==null||gene2GoInfo.getLsGOInfo().size()==0) 
				continue;
			
			ArrayList<Gene2Go> lsGoInfos=gene2GoInfo.getLsGOInfo();
			//����GeneID2Go�Ļ�����
			String[] strGene2Go=new String[2];
			strGene2Go[0]=gene2GoInfo.getQuaryID();
			strGene2Go[1]="";
			for (int j = 0; j < lsGoInfos.size(); j++) {
				//�������biologicalProcess����������
				if (!lsGoInfos.get(j).getFunction().equals(GOClass)) {
					continue;
				}
				
				if (strGene2Go[1].trim().equals("")) 
					strGene2Go[1]=lsGoInfos.get(j).getGOID();
				else
					strGene2Go[1]=strGene2Go[1]+", "+lsGoInfos.get(j).getGOID();
				
				//ÿ�������Ӧ��Go��Ϣ��һ�������Ӧһ��GO��Ȼ��һ�Զ�Ļ��źܶ���
				String[] gene2Go=new String[5];
				for (int k = 0; k < gene2Go.length; k++)
					gene2Go[k]="";//����ֵ����Ϊ��ֵ
				
				gene2Go[0]=gene2GoInfo.getQuaryID();gene2Go[1]=gene2GoInfo.getGeneId()+"";
				String symbol=null;
				try {
					symbol=gene2GoInfo.getGeneInfo().getSymbol();
				} catch (Exception e) {
					// TODO: handle exception
				}
				if ( symbol!=null) {
					gene2Go[2]=symbol.split("//")[0];
				}
				else {
					gene2Go[2]="";
				}
				gene2Go[3]=lsGoInfos.get(j).getGOID();gene2Go[4]=lsGoInfos.get(j).getGOTerm();
				lsGoResult.add(gene2Go);
			}
			
			lsGene2Go.add(strGene2Go);
		}
		return lsGoResult;
	}
	
	/**
	 * ����Uni2GoInfo���б���������Ϣ����Ϊ
	 * ArrayList-String[5]
	 * ���У�0: queryID
	 * 1: uniID
	 * 2: symbol
	 * 3: GOID
	 * 4: GOTerm
	 * @param lsUni2GoInfos
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 */
	private ArrayList<String[]>  copeUni2GoInfo(String GOClass)
	{
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		lsUniGene2Go = new ArrayList<String[]>();
		
		for (int i = 0; i < lsUni2GoInfos.size(); i++) 
		{
			Uni2GoInfo uni2GoInfo=lsUni2GoInfos.get(i);
			//���û���ҵ����� ���� �û���û��Go����Ϣ��������
			if (uni2GoInfo==null||uni2GoInfo.getLsUniGOInfo()==null||uni2GoInfo.getLsUniGOInfo().size()==0) 
				continue;
			ArrayList<UniGene2Go> lsUniGoInfos=uni2GoInfo.getLsUniGOInfo();
			//����GeneID2Go�Ļ�����
			String[] strGene2Go=new String[2];
			strGene2Go[0]=uni2GoInfo.getQuaryID();
			strGene2Go[1]="";
			for (int j = 0; j < lsUniGoInfos.size(); j++) {
				if (!lsUniGoInfos.get(j).getFunction().equals(GOClass)) {
					continue;
				}
				if (strGene2Go[1].trim().equals("")) 
					strGene2Go[1]=lsUniGoInfos.get(j).getGOID();
				else
					strGene2Go[1]=strGene2Go[1]+", "+lsUniGoInfos.get(j).getGOID();
				
				String[] uni2Go=new String[5];
				for (int k = 0; k < uni2Go.length; k++) {
					uni2Go[k]="";//����ֵ����Ϊ��ֵ
				}
				
				uni2Go[0]=uni2GoInfo.getQuaryID();uni2Go[1]=uni2GoInfo.getUniID();
				String symbol=uni2GoInfo.getUniGeneInfo().getSymbol();
				if ( symbol!=null) {
					uni2Go[2]=symbol.split("//")[0];
				}
				else {
					uni2Go[2]="";
				}
				uni2Go[3]=lsUniGoInfos.get(j).getGOID();uni2Go[4]=lsUniGoInfos.get(j).getGOTerm();
				lsGoResult.add(uni2Go);
			}
			lsUniGene2Go.add(strGene2Go);
		}
		return lsGoResult;
	}
}
