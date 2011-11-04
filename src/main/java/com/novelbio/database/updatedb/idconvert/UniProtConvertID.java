package com.novelbio.database.updatedb.idconvert;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.annotation.genAnno.GOQuery;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.mapper.geneanno.MapGene2Go;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.mapper.geneanno.MapUniGene2Go;
import com.novelbio.database.mapper.geneanno.MapUniProtID;
import com.novelbio.database.model.modcopeid.CopeID;
import com.novelbio.database.service.ServAnno;
import com.novelbio.database.service.ServGo;

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
		TxtReadandWrite txtuniProt=new TxtReadandWrite();
		txtuniProt.setParameter(pathUniMap,false, true);
		
		TxtReadandWrite txtuniProtModify=new TxtReadandWrite();
		txtuniProtModify.setParameter(modifiedFile, true,false);
		
		
		
		BufferedReader reader=txtuniProt.readfile();
		String content="";
		reader.readLine();
		while((content=reader.readLine())!=null)
		{
			String[] tmp=content.split("\t");
			//如果geneID不存在
			if (tmp[2].trim().equals("")||tmp[2].trim().equals("-"))
			{
				//看refseq是否存在，不存在就跳过
				if (tmp[3].trim().equals("")||tmp[3].trim().equals("-")) {
					continue;
				}
				//如果refseq存在，用refseq去搜NCBIID，搜到了就把geneID装到tmp[2]里面，多个geneID用;隔开
				else {
					NCBIID ncbiid = new NCBIID();
					tmp[3] = CopeID.removeDot(tmp[3]);
					ncbiid.setAccID(tmp[3]); ncbiid.setTaxID(Integer.parseInt(tmp[13]));
					ArrayList<NCBIID> lsNcbiids = MapNCBIID.queryLsNCBIID(ncbiid);
					if (lsNcbiids != null && lsNcbiids.size() > 0) 
					{
						tmp[2] = lsNcbiids.get(0).getGeneId()+"";
						for (int i = 1; i < lsNcbiids.size(); i++) {
							tmp[2] = tmp[2] + ";"+lsNcbiids.get(i).getGeneId();
						}
					}
					else {
						continue;
					}
				}
			}
			if (tmp[13].trim().equals("")) {
				System.out.println("UniProtConvertID taxID 不存在  ");
				continue;
			}
			if (!tmp[0].trim().equals("-")&&!tmp[0].trim().equals("")) {
				String[] tmptwo=tmp[2].split(";");
				for (int i = 0; i < tmptwo.length; i++) {
					String newtmp = tmp[13]+"\t"+tmptwo[i].trim()+"\t"+tmp[0]+"\t"+NovelBioConst.DBINFO_UNIPROT_UNIID+"\n";
					txtuniProtModify.writefile(newtmp, false);
				}
			}
			if ((!tmp[1].trim().equals("-")&&!tmp[1].trim().equals("")) ) {
				String[] tmptwo=tmp[2].split(";");
				for (int i = 0; i < tmptwo.length; i++) {
					String newtmp = tmp[13]+"\t"+tmptwo[i].trim()+"\t"+tmp[1]+"\t"+NovelBioConst.DBINFO_UNIPROT_UNIPROTKB_ID+"\n";
					txtuniProtModify.writefile(newtmp, false);
				}
			}
			
			if (tmp.length<8) {
				continue;
			}
			if ((!tmp[7].trim().equals("-")&&!tmp[7].trim().equals("")) ) {
				
				String[] tmp2=tmp[7].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					String[] tmptwo=tmp[2].split(";");
					for (int j = 0; j < tmptwo.length; j++) {
						String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_IPI+"\n";;
						txtuniProtModify.writefile(newtmp, false);
					}
				}
			}
			
			if (tmp.length<12) {
				continue;
			}
			if ((!tmp[11].trim().equals("-")&&!tmp[11].trim().equals("")) ) {
				String[] tmp2=tmp[11].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					String[] tmptwo=tmp[2].split(";");
					for (int j = 0; j < tmptwo.length; j++) {
						String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_UNIPROT_UNIPARC+"\n";
						txtuniProtModify.writefile(newtmp, false);
					}
  				}
			}
			
			if (tmp.length<=12) {
				continue;
			}
			
			if ((!tmp[12].trim().equals("-")&&!tmp[12].trim().equals("")) ) {
				String[] tmp2=tmp[12].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					String[] tmptwo=tmp[2].split(";");
					for (int j = 0; j < tmptwo.length; j++) {
						String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_PIR+"\n";
						txtuniProtModify.writefile(newtmp, false);
					}
 		
				}
			}
			
			if (tmp.length<16) {
				continue;
			}
			
			if ((!tmp[15].trim().equals("-")&&!tmp[15].trim().equals("")) ) {
				String[] tmp2=tmp[15].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					String[] tmptwo=tmp[2].split(";");
					for (int j = 0; j < tmptwo.length; j++) {
						String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_UNIPROT_UNIGENE+"\n";
						txtuniProtModify.writefile(newtmp, false);
					}
 		
				}
			}
			
			if (tmp.length<18) {
				continue;
			}
 
				if ((!tmp[17].trim().equals("-")&&!tmp[17].trim().equals("")) ) {
					
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
							String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_EMBL+"\n";
							txtuniProtModify.writefile(newtmp, false);
						}
 					}
				}

		
				if (tmp.length<19) {
					continue;
				}
	 
			if ((!tmp[18].trim().equals("-")&&!tmp[18].trim().equals("")) ) {
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
						String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_EMBL_CDS+"\n";
						txtuniProtModify.writefile(newtmp, false);
					}
				}
			}
			
			if (tmp.length<20) {
				continue;
			}
 
		if ((!tmp[19].trim().equals("-")&&!tmp[19].trim().equals("")) ) {
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
					String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_ENSEMBL+"\n";
					txtuniProtModify.writefile(newtmp, false);
				}
			}
		}
			
		
		if (tmp.length<21) {
			continue;
		}
		
		if ((!tmp[20].trim().equals("-")&&!tmp[20].trim().equals("")) ) {
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
					String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_ENSEMBL_TRS+"\n";
					txtuniProtModify.writefile(newtmp, false);
				}
			}
		}
			
		
		if (tmp.length<22) {
			continue;
		}
		
		if ((!tmp[21].trim().equals("-")&&!tmp[21].trim().equals("")) ) {
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
					String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_ENSEMBL_PRO+"\n";
					txtuniProtModify.writefile(newtmp, false);
				}
			}
		}
		}
		txtuniProtModify.writefile("",true);
		txtuniProt.close();
		txtuniProtModify.close();
	}
	

	
	/**
	 * 处理Uniprot的idmapping_selected.tab表
	 *  将原始表中所有不包含geneID的行全部提取出来，格式如下：
	 * 物种 \t  SwissProtID \t  accessID \t  DataBaseInfo \n
	 * 的格式
	 * @throws Exception 
	 */
	public static void uniProtIdMapSelectDUniID(String pathUniMap,String modifiedFile) throws Exception
	{
		TxtReadandWrite txtuniProt=new TxtReadandWrite();
		txtuniProt.setParameter(pathUniMap,false, true);
		
		TxtReadandWrite txtuniProtModify=new TxtReadandWrite();
		txtuniProtModify.setParameter(modifiedFile, true,false);
		
		
		
		BufferedReader reader=txtuniProt.readfile();
		String content="";
		reader.readLine();
		while((content=reader.readLine())!=null)
		{
			String[] tmp=content.split("\t");
			//有geneID的就跳过
			if (!tmp[2].trim().equals("") && !tmp[2].trim().equals("-")) {
				continue;
			}
			if (tmp[13].trim().equals("")) {
				System.out.println("UniProtConvertID taxID 不存在  ");
				continue;
			}
			if (!tmp[0].trim().equals("-")&&!tmp[0].trim().equals("")) {
				String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmp[0]+"\t"+NovelBioConst.DBINFO_UNIPROT_UNIID+"\n";
				txtuniProtModify.writefile(newtmp, false);
			}
			if ((!tmp[1].trim().equals("-")&&!tmp[1].trim().equals("")) ) {
				String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmp[1]+"\t"+NovelBioConst.DBINFO_UNIPROT_UNIID+"\n";
				txtuniProtModify.writefile(newtmp, false);
			}
			
			if (tmp.length<8) {
				continue;
			}
			if ((!tmp[7].trim().equals("-")&&!tmp[7].trim().equals("")) ) {
				String[] tmp2=tmp[7].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_IPI+"\n";;
					txtuniProtModify.writefile(newtmp, false);
				}
			}
			
			if (tmp.length<12) {
				continue;
			}
			if ((!tmp[11].trim().equals("-")&&!tmp[11].trim().equals("")) ) {
				String[] tmp2=tmp[11].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_UNIPROT_UNIPARC+"\n";
					txtuniProtModify.writefile(newtmp, false);
  				}
			}
			
			if (tmp.length<=12) {
				continue;
			}
			
			if ((!tmp[12].trim().equals("-")&&!tmp[12].trim().equals("")) ) {
				String[] tmp2=tmp[12].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_PIR+"\n";
					txtuniProtModify.writefile(newtmp, false);
				}
			}
			
			if (tmp.length<16) {
				continue;
			}
			
			if ((!tmp[15].trim().equals("-")&&!tmp[15].trim().equals("")) ) {
				String[] tmp2=tmp[15].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_UNIPROT_UNIGENE+"\n";
					txtuniProtModify.writefile(newtmp, false);
				}
			}
			
			if (tmp.length<18) {
				continue;
			}
			if ((!tmp[17].trim().equals("-")&&!tmp[17].trim().equals("")) ) {
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
					String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_EMBL+"\n";
					txtuniProtModify.writefile(newtmp, false);
					}
			}	
		
			if (tmp.length<19) {
				continue;
			}
			
			if ((!tmp[18].trim().equals("-")&&!tmp[18].trim().equals("")) ) {
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
					String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+ NovelBioConst.DBINFO_EMBL_CDS+"\n";
					txtuniProtModify.writefile(newtmp, false);
				}
			}
			
			if (tmp.length<20) {
				continue;
			}
 
		if ((!tmp[19].trim().equals("-")&&!tmp[19].trim().equals("")) ) {
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
				String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_ENSEMBL+"\n";
				txtuniProtModify.writefile(newtmp, false);
			}
		}
		
		if (tmp.length<21) {
			continue;
		}
		if ((!tmp[20].trim().equals("-")&&!tmp[20].trim().equals("")) ) {
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
				String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_ENSEMBL_TRS+"\n";
				txtuniProtModify.writefile(newtmp, false);
			}
		}
			
		
		if (tmp.length<22) {
			continue;
		}
		
		if ((!tmp[21].trim().equals("-")&&!tmp[21].trim().equals("")) ) {
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
				String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+NovelBioConst.DBINFO_ENSEMBL_PRO+"\n";
				txtuniProtModify.writefile(newtmp, false);
		 
			}
		}
		}
		txtuniProtModify.writefile("",true);
		txtuniProt.close();
		txtuniProtModify.close();
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
		taxIDrReadandWrite.close();
	}
	
	

	
	
	
	/**
	 * 读取uniprotIDMapping_select.tab文件，获得里面第14列的taxID信息
	 * 取出所有含有有指定taxID的行
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
			if (hashTaxID.contains(ss[13])) 
			{
				outputReadandWrite.writefile(content2+"\n", false);
			}
		}
		outputReadandWrite.writefile("", true);
	}
	
	
	/**
	 * 读取taxuniprotIDMapping_select.tab文件，将里面的GO导入gene2Go或uniGen2Go
	 * 取出所有含有有NCBI geneID的行
	 * @throws Exception 
	 */
	public static void upDateUniGo(String inputFile) throws Exception 
	{
		HashMap<String, String[]> goInfo = ServGo.getHashGo2Term();
		
		TxtReadandWrite inputReadandWrite=new TxtReadandWrite();
		inputReadandWrite.setParameter(inputFile,false, true);
		
		BufferedReader inputrReader=inputReadandWrite.readfile();
		
		String content="";
		while((content=inputrReader.readLine())!=null)
		{
			String[] ss=content.split("\t");
			if (ss[6].equals("")) {
				continue;
			}
			String[] ss2 = ss[6].split(";");
			for (String string : ss2) {
				String[] thisgoInfo = goInfo.get(string.trim());
				String GoID = thisgoInfo[1];
				String goFun = thisgoInfo[3];
				String goTerm = thisgoInfo[2];
				ArrayList<String> lsAccID = ServAnno.getNCBIUni(ss[0], Integer.parseInt(ss[13]));
				if (lsAccID.get(0).equals("geneID")) {
					for (int i = 1; i < lsAccID.size(); i++) {
						Gene2Go gene2Go = new Gene2Go();
						gene2Go.setDataBase(NovelBioConst.DBINFO_UNIPROT_UNIID);
						gene2Go.setFunction(goFun);
						gene2Go.setGOID(GoID);
						gene2Go.setGOTerm(goTerm);
						gene2Go.setGeneId(Long.parseLong(lsAccID.get(i)));
						AGene2Go gene2Go2 = MapGene2Go.queryGene2Go(gene2Go);
						if (gene2Go2==null) //如果已经存在了，那么考虑下是否升级
						{
							MapGene2Go.InsertGene2Go(gene2Go);
						}
					}
				}
				else if (lsAccID.get(0).equals("uniID")) {
					for (int i = 1; i < lsAccID.size(); i++) {
						UniGene2Go uniGene2Go = new UniGene2Go();
						uniGene2Go.setDataBase(NovelBioConst.DBINFO_UNIPROT_UNIID);
						uniGene2Go.setFunction(goFun);
						uniGene2Go.setGOID(GoID);
						uniGene2Go.setGOTerm(goTerm);
						uniGene2Go.setUniProtID(lsAccID.get(i));
						AGene2Go uniGene2Go2 = MapUniGene2Go.queryUniGene2Go(uniGene2Go);
						if (uniGene2Go2==null) //如果已经存在了，那么考虑下是否升级
						{
							MapUniGene2Go.InsertUniGene2Go(uniGene2Go);
						}
					}
				}
			}
		}
	}
	
	
	
	
}
