package com.novelbio.upDateDB.idConvert;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniProtID;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniProtID;

public class UniProtConvertID {

	/**
	 * ����Uniprot��idmapping_selected.tab���к���geneID����
	 * �������е�ID��Ϣ������
	 * ���� \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
	 * �ĸ�ʽ
	 * @throws Exception 
	 */
	public static void uniProtIdMapSelectGeneID(String pathUniMap,String modifiedFile) throws Exception
	{
		TxtReadandWrite uniProt=new TxtReadandWrite();
		uniProt.setParameter(pathUniMap,false, true);
		
		TxtReadandWrite uniProtModify=new TxtReadandWrite();
		uniProtModify.setParameter(modifiedFile, true,false);
		
		
		
		BufferedReader reader=uniProt.readfile();
		String content="";
		reader.readLine();
		while((content=reader.readLine())!=null)
		{
			String[] tmp=content.split("\t");
			
			if (tmp[2].trim().equals("")||tmp[2].trim().equals("-"))
			{
				continue;
			}
			
			if (!tmp[0].trim().equals("-")&&!tmp[0].trim().equals("")) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				String[] tmptwo=tmp[2].split(";");
				for (int i = 0; i < tmptwo.length; i++) {
					String newtmp = tmp[13]+"\t"+tmptwo[i].trim()+"\t"+tmp[0]+"\t"+"unpAC"+"\n";
					uniProtModify.writefile(newtmp, false);
				}
			}
			if ((!tmp[1].trim().equals("-")&&!tmp[1].trim().equals("")) ) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
				String[] tmptwo=tmp[2].split(";");
				for (int i = 0; i < tmptwo.length; i++) {
					String newtmp = tmp[13]+"\t"+tmptwo[i].trim()+"\t"+tmp[1]+"\t"+"unpID"+"\n";
					uniProtModify.writefile(newtmp, false);
				}
			}
			
			if (tmp.length<8) {
				continue;
			}
			if ((!tmp[7].trim().equals("-")&&!tmp[7].trim().equals("")) ) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
				
				String[] tmp2=tmp[7].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					
					String[] tmptwo=tmp[2].split(";");
					for (int j = 0; j < tmptwo.length; j++) {
						String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+"IPI"+"\n";;
						uniProtModify.writefile(newtmp, false);
					}
 
				}
			}
			
			if (tmp.length<12) {
				continue;
			}
			if ((!tmp[11].trim().equals("-")&&!tmp[11].trim().equals("")) ) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
				String[] tmp2=tmp[11].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					String[] tmptwo=tmp[2].split(";");
					for (int j = 0; j < tmptwo.length; j++) {
						String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+"UniParc"+"\n";
						uniProtModify.writefile(newtmp, false);
					}
  				}
			}
			
			if (tmp.length<=12) {
				continue;
			}
			
			if ((!tmp[12].trim().equals("-")&&!tmp[12].trim().equals("")) ) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
				String[] tmp2=tmp[12].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					String[] tmptwo=tmp[2].split(";");
					for (int j = 0; j < tmptwo.length; j++) {
						String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+"PIR"+"\n";
						uniProtModify.writefile(newtmp, false);
					}
 		
				}
			}
			
			if (tmp.length<16) {
				continue;
			}
			
			if ((!tmp[15].trim().equals("-")&&!tmp[15].trim().equals("")) ) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
				String[] tmp2=tmp[15].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					String[] tmptwo=tmp[2].split(";");
					for (int j = 0; j < tmptwo.length; j++) {
						String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+"UniGene"+"\n";
						uniProtModify.writefile(newtmp, false);
					}
 		
				}
			}
			
			if (tmp.length<18) {
				continue;
			}
 

				if ((!tmp[17].trim().equals("-")&&!tmp[17].trim().equals("")) ) {
					if (tmp[13].trim().equals("")) {
						System.out.println("tax=  ");
						continue;
					}
					
					
					String[] tmp2=tmp[17].split(";");
					for (int i = 0; i < tmp2.length; i++) {
						String tmpp=tmp2[i].trim();
						if (tmpp.equals("")||tmpp.equals("-")) 
						{
							continue;
						}
						if (tmpp.contains(".")) 
						{
							tmpp=tmpp.substring(0, tmpp.indexOf("."));
						}
						String[] tmptwo=tmp[2].split(";");
						for (int j = 0; j < tmptwo.length; j++) {
							String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+"EMBL"+"\n";
							uniProtModify.writefile(newtmp, false);
						}
 					}
				}

		
				if (tmp.length<19) {
					continue;
				}
	 
			if ((!tmp[18].trim().equals("-")&&!tmp[18].trim().equals("")) ) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
				
				String[] tmp2=tmp[18].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					if (tmpp.contains(".")) 
					{
						tmpp=tmpp.substring(0, tmpp.indexOf("."));
					}
					String[] tmptwo=tmp[2].split(";");
					for (int j = 0; j < tmptwo.length; j++) {
						String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+"EMBLCDS"+"\n";
						uniProtModify.writefile(newtmp, false);
					}
				}
			}
			
			if (tmp.length<20) {
				continue;
			}
 
		if ((!tmp[19].trim().equals("-")&&!tmp[19].trim().equals("")) ) {
			if (tmp[13].trim().equals("")) {
				System.out.println("tax=  ");
				continue;
			}
			
			
			String[] tmp2=tmp[19].split(";");
			for (int i = 0; i < tmp2.length; i++) {
				String tmpp=tmp2[i].trim();
				if (tmpp.equals("")||tmpp.equals("-")) 
				{
					continue;
				}
				if (tmpp.contains(".")) 
				{
					tmpp=tmpp.substring(0, tmpp.indexOf("."));
				}
				String[] tmptwo=tmp[2].split(";");
				for (int j = 0; j < tmptwo.length; j++) {
					String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+"Ensembl_Gene"+"\n";
					uniProtModify.writefile(newtmp, false);
				}
			}
		}
			
		
		if (tmp.length<21) {
			continue;
		}
		
		if ((!tmp[20].trim().equals("-")&&!tmp[20].trim().equals("")) ) {
			if (tmp[13].trim().equals("")) {
				System.out.println("tax=  ");
				continue;
			}
			
			
			String[] tmp2=tmp[20].split(";");
			for (int i = 0; i < tmp2.length; i++) {
				String tmpp=tmp2[i].trim();
				if (tmpp.equals("")||tmpp.equals("-")) 
				{
					continue;
				}
				if (tmpp.contains(".")) 
				{
					tmpp=tmpp.substring(0, tmpp.indexOf("."));
				}
				String[] tmptwo=tmp[2].split(";");
				for (int j = 0; j < tmptwo.length; j++) {
					String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+"Ensembl_RNA"+"\n";
					uniProtModify.writefile(newtmp, false);
				}
			}
		}
			
		
		if (tmp.length<22) {
			continue;
		}
		
		if ((!tmp[21].trim().equals("-")&&!tmp[21].trim().equals("")) ) {
			if (tmp[13].trim().equals("")) {
				System.out.println("tax=  ");
				continue;
			}
			
			
			String[] tmp2=tmp[21].split(";");
			for (int i = 0; i < tmp2.length; i++) {
				String tmpp=tmp2[i].trim();
				if (tmpp.equals("")||tmpp.equals("-")) 
				{
					continue;
				}
				if (tmpp.contains(".")) 
				{
					tmpp=tmpp.substring(0, tmpp.indexOf("."));
				}
				String[] tmptwo=tmp[2].split(";");
				for (int j = 0; j < tmptwo.length; j++) {
					String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+"Ensembl_PRO"+"\n";
					uniProtModify.writefile(newtmp, false);
				}
			}
		}
		}
		uniProtModify.writefile("",true);
	}
	

	
	/**
	 * ����Uniprot��idmapping_selected.tab��
	 * �������е�ID��Ϣ������
	 * ���� \t  SwissProtID \t  accessID \t  DataBaseInfo \n
	 * �ĸ�ʽ
	 * @throws Exception 
	 */
	public static void uniProtIdMapSelectDSwissPort(String pathUniMap,String modifiedFile) throws Exception
	{
		TxtReadandWrite uniProt=new TxtReadandWrite();
		uniProt.setParameter(pathUniMap,false, true);
		
		TxtReadandWrite uniProtModify=new TxtReadandWrite();
		uniProtModify.setParameter(modifiedFile, true,false);
		
		
		
		BufferedReader reader=uniProt.readfile();
		String content="";
		reader.readLine();
		while((content=reader.readLine())!=null)
		{
			String[] tmp=content.split("\t");
			
			if (!tmp[0].trim().equals("-")&&!tmp[0].trim().equals("")) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
		

					String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmp[0]+"\t"+"unpAC"+"\n";
					uniProtModify.writefile(newtmp, false);
				
			}
			if ((!tmp[1].trim().equals("-")&&!tmp[1].trim().equals("")) ) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
 
					String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmp[1]+"\t"+"unpID"+"\n";
					uniProtModify.writefile(newtmp, false);
			 
			}
			
			if (tmp.length<8) {
				continue;
			}
			if ((!tmp[7].trim().equals("-")&&!tmp[7].trim().equals("")) ) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
				
				String[] tmp2=tmp[7].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					
	 
						String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+"IPI"+"\n";;
						uniProtModify.writefile(newtmp, false);
				 
 
				}
			}
			
			if (tmp.length<12) {
				continue;
			}
			if ((!tmp[11].trim().equals("-")&&!tmp[11].trim().equals("")) ) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
				String[] tmp2=tmp[11].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
	 
						String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+"UniParc"+"\n";
						uniProtModify.writefile(newtmp, false);
				 
  				}
			}
			
			if (tmp.length<=12) {
				continue;
			}
			
			if ((!tmp[12].trim().equals("-")&&!tmp[12].trim().equals("")) ) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
				String[] tmp2=tmp[12].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
	 
						String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+"PIR"+"\n";
						uniProtModify.writefile(newtmp, false);
				 
 		
				}
			}
			
			if (tmp.length<16) {
				continue;
			}
			
			if ((!tmp[15].trim().equals("-")&&!tmp[15].trim().equals("")) ) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
				String[] tmp2=tmp[15].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
 
						String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+"UniGene"+"\n";
						uniProtModify.writefile(newtmp, false);
		 
 		
				}
			}
			
			if (tmp.length<18) {
				continue;
			}
 

				if ((!tmp[17].trim().equals("-")&&!tmp[17].trim().equals("")) ) {
					if (tmp[13].trim().equals("")) {
						System.out.println("tax=  ");
						continue;
					}
					
					
					String[] tmp2=tmp[17].split(";");
					for (int i = 0; i < tmp2.length; i++) {
						String tmpp=tmp2[i].trim();
						if (tmpp.equals("")||tmpp.equals("-")) 
						{
							continue;
						}
						if (tmpp.contains(".")) 
						{
							tmpp=tmpp.substring(0, tmpp.indexOf("."));
						}
	 
							String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+"EMBL"+"\n";
							uniProtModify.writefile(newtmp, false);
					 
 					}
				}

		
				if (tmp.length<19) {
					continue;
				}
	 
			if ((!tmp[18].trim().equals("-")&&!tmp[18].trim().equals("")) ) {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
				
				String[] tmp2=tmp[18].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					if (tmpp.contains(".")) 
					{
						tmpp=tmpp.substring(0, tmpp.indexOf("."));
					}
 
						String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+"EMBLCDS"+"\n";
						uniProtModify.writefile(newtmp, false);
			 
				}
			}
			
			if (tmp.length<20) {
				continue;
			}
 
		if ((!tmp[19].trim().equals("-")&&!tmp[19].trim().equals("")) ) {
			if (tmp[13].trim().equals("")) {
				System.out.println("tax=  ");
				continue;
			}
			
			
			String[] tmp2=tmp[19].split(";");
			for (int i = 0; i < tmp2.length; i++) {
				String tmpp=tmp2[i].trim();
				if (tmpp.equals("")||tmpp.equals("-")) 
				{
					continue;
				}
				if (tmpp.contains(".")) 
				{
					tmpp=tmpp.substring(0, tmpp.indexOf("."));
				}
 
					String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+"Ensembl_Gene"+"\n";
					uniProtModify.writefile(newtmp, false);
		 
			}
		}
			
		
		if (tmp.length<21) {
			continue;
		}
		
		if ((!tmp[20].trim().equals("-")&&!tmp[20].trim().equals("")) ) {
			if (tmp[13].trim().equals("")) {
				System.out.println("tax=  ");
				continue;
			}
			
			
			String[] tmp2=tmp[20].split(";");
			for (int i = 0; i < tmp2.length; i++) {
				String tmpp=tmp2[i].trim();
				if (tmpp.equals("")||tmpp.equals("-")) 
				{
					continue;
				}
				if (tmpp.contains(".")) 
				{
					tmpp=tmpp.substring(0, tmpp.indexOf("."));
				}
 
					String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+"Ensembl_RNA"+"\n";
					uniProtModify.writefile(newtmp, false);
		 
			}
		}
			
		
		if (tmp.length<22) {
			continue;
		}
		
		if ((!tmp[21].trim().equals("-")&&!tmp[21].trim().equals("")) ) {
			if (tmp[13].trim().equals("")) {
				System.out.println("tax=  ");
				continue;
			}
			
			
			String[] tmp2=tmp[21].split(";");
			for (int i = 0; i < tmp2.length; i++) {
				String tmpp=tmp2[i].trim();
				if (tmpp.equals("")||tmpp.equals("-")) 
				{
					continue;
				}
				if (tmpp.contains(".")) 
				{
					tmpp=tmpp.substring(0, tmpp.indexOf("."));
				}
 
					String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+"Ensembl_PRO"+"\n";
					uniProtModify.writefile(newtmp, false);
		 
			}
		}
		}
		uniProtModify.writefile("",true);
	}
	
	
	
	/**
	 * ��ȡuniprotIDMapping_select.tab�ļ�����������14�е�taxID��Ϣ
	 * ȡ������û��NCBI geneID����
	 * @throws Exception 
	 */
	public static void getUniProtIdMappingInfo(String taxIDfile, String inputFile, String outputFile) throws Exception 
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
		
		String content2="";
		while((content2=inputrReader.readLine())!=null)
		{
			String[] ss=content2.split("\t");
			if (ss[2].trim().equals("")&&hashTaxID.contains(ss[13])) 
			{
				outputReadandWrite.writefile(content2+"\n", false);
			}
		}
		outputReadandWrite.writefile("", true);
	}
	
	
	/**
	 * ��ȡgene_association.goa_uniprot�ļ�����������13�е�taxID��Ϣ
	 * �����к�����ҪTaxID����ȫ����ȡ����
	 * @throws Exception 
	 */
	public static void getUniProtGoInfoTaxID(String taxIDfile, String inputFile, String outputFile) throws Exception 
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
	}
	
	/**
	 * ��gene_association.goa_uniprot�ļ�ȥ�ظ�����ȡTaxID��ȥ���ļ��ʼ��IPI�������¹���
	 * ��ÿһ�еģ� ��2�У������UniProtID����3�У�Symbol����10��Description����11��Synonym, ��ͬ��13��Taxon_ID<br>
	 * ��NCBIID��UniProtID������ȶԣ�����NCBI�󣬽�����������������Ϊ�����ļ� 1. taxID \t geneID \t accessID \t DataBase \n  ��    2. taxID \t geneID \t symbol \t discription \t Synonym \n 
	 * �ϱ� UniProtID, ������������������Ϊ�����ļ� 1. taxID \t UniProtID \t accessID \t DataBase \n װ��UniProtID��   2. taxID \t geneID \t symbol \t discription \t Synonym \n װ��UniGeneInfo
	 * �����һ��swiss��Ӧ���geneID����������geneID�����£�UniProtҲһ��
	 * @throws Exception 
	 */
	public static void getUniProtGoInfo(String inputFile,String outNCBIID,String outGeneInfo, String outUniProtID, String outUniGeneInfo,String remain) throws Exception 
	{
		TxtReadandWrite txtInput=new TxtReadandWrite(); txtInput.setParameter(inputFile, false, true);
		TxtReadandWrite txtOutNCBIID=new TxtReadandWrite();txtOutNCBIID.setParameter(outNCBIID, true, false);
		TxtReadandWrite txtOutGeneInfo=new TxtReadandWrite();txtOutGeneInfo.setParameter(outGeneInfo, true, false);
		TxtReadandWrite txtOutUniProtID=new TxtReadandWrite();txtOutUniProtID.setParameter(outUniProtID, true, false);
		TxtReadandWrite txtOutUniGeneInfo=new TxtReadandWrite();txtOutUniGeneInfo.setParameter(outUniGeneInfo, true, false);
		TxtReadandWrite txtRemain=new TxtReadandWrite();txtRemain.setParameter(remain, true, false);
		BufferedReader inputReader=txtInput.readfile();
		
		DaoFSNCBIID ncbiidDao=new DaoFSNCBIID();
		DaoFSUniProtID uniProtIDDao=new DaoFSUniProtID();
		
		String content="";
		int[] index=new int[3];index[0]=1;index[1]=2;index[2]=10;
		String[] DBInfo=new String[3]; DBInfo[0]="UniProtID";DBInfo[1]="symbol";DBInfo[2]="Synonym";
		ArrayList<NCBIID> lsResultNcbiid=null;
		ArrayList<NCBIID> tmplsResultNcbiid=null;
		ArrayList<UniProtID> lsResultUniProtID=null;
		ArrayList<UniProtID> tmplsResultUniProtID=null;
		while ((content=inputReader.readLine())!=null) {
			String ss[]=content.split("\t");
			int taxID=Integer.parseInt(ss[12].split("\\|")[0].split(":")[1]);//���е�taxID
			int NCBIflag=0;//����Ƿ�鵽NCBIID����taxID��accessID��NCBIID�����û�ҵ�����Ϊ0���ҵ�һ��1���ҵ����2
			int UniProtflag=0;//����Ƿ�鵽UniProt����taxID��accessID��UniProtIID�����û�ҵ�����Ϊ0���ҵ�һ��1���ҵ����2
			tmplsResultNcbiid=null;//�����
			tmplsResultUniProtID=null;
			//////////////////////���Ȳ���NCBIID��/////////////////////////////////////
			for (int i = 0; i < 3; i++) {
				String[] ssTmp=ss[index[i]].split("\\|");
		
				for (int j = 0; j < ssTmp.length; j++) 
				{
					String sstmpid=ssTmp[j].trim();
					//�����е�
					if (sstmpid.equals("")) {
						continue;
					}
					if (sstmpid.contains("Em:")) {
						sstmpid=sstmpid.substring(sstmpid.indexOf(":")+1, sstmpid.indexOf("."));
					}
					NCBIID ncbiid=new NCBIID();
					ncbiid.setTaxID(taxID);ncbiid.setAccID(sstmpid);
					lsResultNcbiid=ncbiidDao.queryLsNCBIID(ncbiid);
					if (lsResultNcbiid.size()==1)
					{
						NCBIflag=1;break;
					}
					else if (lsResultNcbiid.size()>1)
					{
						NCBIflag=2;System.out.println(taxID+" "+sstmpid+"           "+ss[1]+"    NCBI");
						tmplsResultNcbiid=lsResultNcbiid;//���ҵ���>=2������ʱ��������������  tmplsResultNcbiid ���棬�����������������ԭ�򣺵�lsResultNcbiid.size()=1ʱ
						//ֱ������������Ҳ���Խ��У��������������>=2�����򣬲���û�У���ô���� 	NCBIID ncbiidRes=lsResultNcbiid.get(0); �����ܻ������ʱ����� tmplsResultNcbiid �����棬ע�������=����Ȼ�����ô��ݣ����Ǻ���lsResultNcbiid�ḳ����ֵ
					}
				}

				if (NCBIflag==1)//��ncbi�����ҵ���Ψһ��һ����¼
					break;
			}
			if (NCBIflag==2) {
				System.out.println(taxID+"  Reallyaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "+"    NCBI");
			}
			if (NCBIflag>=1) 
			{
				NCBIID ncbiidRes=null;
				if (lsResultNcbiid.size()>0) {
					ncbiidRes=lsResultNcbiid.get(0);
					String resultTmp=ncbiidRes.getTaxID()+"\t"+ncbiidRes.getGeneId();
					for (int i = 0; i < 3; i++)
					{
						String[] ssTmp=ss[index[i]].split("\\|");
						for (int j = 0; j < ssTmp.length; j++) 
						{
							String sstmpid=ssTmp[j].trim();
							//�����е�
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
				else {
					for (int m = 0; m < tmplsResultNcbiid.size(); m++) {
						ncbiidRes=tmplsResultNcbiid.get(m);
						String resultTmp=ncbiidRes.getTaxID()+"\t"+ncbiidRes.getGeneId();
						for (int i = 0; i < 3; i++)
						{
							String[] ssTmp=ss[index[i]].split("\\|");
							for (int j = 0; j < ssTmp.length; j++) 
							{
								String sstmpid=ssTmp[j].trim();
								//�����е�
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
				continue;//��ִ�������UniProt�������ݿ���
			}
			
			
			
			/////////////////////////���NCBIID��û�в鵽����ô��UniProt��/////////////////////////////////////////
			for (int i = 0; i < 3; i++) {
				String[] ssTmp=ss[index[i]].split("\\|");
		
				for (int j = 0; j < ssTmp.length; j++) 
				{
					String sstmpid=ssTmp[j].trim();
					//�����е�
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
						tmplsResultUniProtID=lsResultUniProtID;//���ҵ���>=2������ʱ��������������  tmplsResultNcbiid ���棬�����������������ԭ�򣺵�lsResultNcbiid.size()=1ʱ
						//ֱ������������Ҳ���Խ��У��������������>=2�����򣬲���û�У���ô���� 	NCBIID ncbiidRes=lsResultNcbiid.get(0); �����ܻ������ʱ����� tmplsResultNcbiid �����棬ע�������=����Ȼ�����ô��ݣ����Ǻ���lsResultNcbiid�ḳ����ֵ
			
					}
				}

				if (UniProtflag==1)//��ncbi�����ҵ���Ψһ��һ����¼
					break;
			}
			if (UniProtflag==2) {
				System.out.println(taxID+"  Reallyaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "+"    UniProt");
			}
			if (UniProtflag>=1) 
			{
				
				UniProtID  uniProtIDRes=null;
				if (lsResultUniProtID.size()>0) {
					 uniProtIDRes=lsResultUniProtID.get(0);
						String resultTmp=uniProtIDRes.getTaxID()+"\t"+uniProtIDRes.getUniID();
						for (int i = 0; i < 3; i++)
						{
							String[] ssTmp=ss[index[i]].split("\\|");
							for (int j = 0; j < ssTmp.length; j++) 
							{
								String sstmpid=ssTmp[j].trim();
								//�����е�
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
				else {
					for (int m = 0; m < tmplsResultUniProtID.size(); m++) {
						uniProtIDRes=tmplsResultUniProtID.get(m);
						String resultTmp=uniProtIDRes.getTaxID()+"\t"+uniProtIDRes.getUniID();
						for (int i = 0; i < 3; i++)
						{
							String[] ssTmp=ss[index[i]].split("\\|");
							for (int j = 0; j < ssTmp.length; j++) 
							{
								String sstmpid=ssTmp[j].trim();
								//�����е�
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
		
		
		
	
	
	
	
	
	/**
	 * ��ȡuniprotIDMapping_select.tab�ļ�����������14�е�taxID��Ϣ
	 * ȡ�����к�����NCBI geneID����
	 * @throws Exception 
	 */
	public static void getUniProtTaxID(String taxIDfile, String inputFile, String outputFile) throws Exception 
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
		
		String content2="";
		while((content2=inputrReader.readLine())!=null)
		{
			String[] ss=content2.split("\t");
			if (!ss[2].trim().equals("")&&hashTaxID.contains(ss[13])) 
			{
				outputReadandWrite.writefile(content2+"\n", false);
			}
		}
		outputReadandWrite.writefile("", true);
	}
	
	
	
	
	
	
}
