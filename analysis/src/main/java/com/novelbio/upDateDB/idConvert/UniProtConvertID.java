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
	 * 处理Uniprot的idmapping_selected.tab表中含有geneID的行
	 * 导出其中的ID信息，按照
	 * 物种 \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
	 * 的格式
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
	 * 处理Uniprot的idmapping_selected.tab表
	 * 导出其中的ID信息，按照
	 * 物种 \t  SwissProtID \t  accessID \t  DataBaseInfo \n
	 * 的格式
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
	 * 读取uniprotIDMapping_select.tab文件，获得里面第14列的taxID信息
	 * 取出所有没有NCBI geneID的行
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
	 * 读取gene_association.goa_uniprot文件，获得里面第13列的taxID信息
	 * 将所有含有需要TaxID的行全部提取出来
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
	 * 将gene_association.goa_uniprot文件去重复、提取TaxID、去掉文件最开始的IPI后，做以下工作
	 * 将每一行的， 第2列：基因的UniProtID，第3列：Symbol，第10列Description，第11列Synonym, 连同第13列Taxon_ID<br>
	 * 向NCBIID和UniProtID两个表比对，比上NCBI后，将本列所有数据整理为两个文件 1. taxID \t geneID \t accessID \t DataBase \n  和    2. taxID \t geneID \t symbol \t discription \t Synonym \n 
	 * 较表 UniProtID, 将本列所有数据整理为两个文件 1. taxID \t UniProtID \t accessID \t DataBase \n 装入UniProtID和   2. taxID \t geneID \t symbol \t discription \t Synonym \n 装入UniGeneInfo
	 * 如果有一个swiss对应多个geneID的情况，多个geneID都更新，UniProt也一样
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
					lsResultNcbiid=ncbiidDao.queryLsNCBIID(ncbiid);
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

				if (NCBIflag==1)//在ncbi表中找到了唯一的一条记录
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
		
		
		
	
	
	
	
	
	/**
	 * 读取uniprotIDMapping_select.tab文件，获得里面第14列的taxID信息
	 * 取出所有含有有NCBI geneID的行
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
