package com.novelbio.database.upDateDB.idConvert;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;

import com.novelbio.analysis.annotation.copeID.CopeID;
import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;




public class NCBIIDOperate 
{
	/**
	 * 处理NCBI的gene2accessionID表,按照
	 * 物种 \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
	 * 的格式
	 * @throws Exception 
	 */
	public static void gene2acID(String pathGene2accessionID,String modifiedFile) throws Exception
	{
		TxtReadandWrite gene2accID=new TxtReadandWrite();
		gene2accID.setParameter(pathGene2accessionID, false,true);
		
		TxtReadandWrite gene2accIDModify=new TxtReadandWrite();
		gene2accIDModify.setParameter(modifiedFile, true,false);
		
		
		
		BufferedReader reader=gene2accID.readfile();
		String content="";
		reader.readLine();
		while((content=reader.readLine())!=null)
		{
			String[] tmp=content.split("\t");
			tmp[3] = CopeID.removeDot(tmp[3]);
			tmp[5] = CopeID.removeDot(tmp[5]);
			tmp[6] = CopeID.removeDot(tmp[6]);
			tmp[7] = CopeID.removeDot(tmp[7]);
			
			if (!tmp[3].equals("-")) {
				String newtmp = tmp[0]+"\t"+tmp[1]+"\t"+tmp[3]+"\t"+NovelBioConst.DBINFO_NCBI_ACC_RNAAC+"\n";
				gene2accIDModify.writefile(newtmp, false);
			}
			if (!tmp[5].equals("-")) {
				String newtmp = tmp[0]+"\t"+ tmp[1]+"\t"+tmp[5]+"\t"+NovelBioConst.DBINFO_NCBI_ACC_PROAC+"\n";
				gene2accIDModify.writefile(newtmp, false);
			}
			if (!tmp[6].equals("-")) {
				String newtmp2 = tmp[0]+"\t"+  tmp[1]+"\t"+tmp[6]+"\t"+NovelBioConst.DBINFO_NCBI_ACC_PROGI+"\n";
				gene2accIDModify.writefile(newtmp2, false);
			}
			if (!tmp[7].equals("-")) {
				String newtmp3 = tmp[0]+"\t"+  tmp[1]+"\t"+tmp[7]+"\t"+"geNAc"+"\n";
				gene2accIDModify.writefile(newtmp3, false);
			}		
		}
		gene2accIDModify.writefile("",true);
	}
	
	/**
	 * 指定table，将第一列含有指定taxID的行都提取出来
	 * @throws Exception 
	 */
	public static void tableGetTaxID(String taxIDfile,String inputFIle,String outputFile) throws Exception
	{
		TxtReadandWrite txtTaxID=new TxtReadandWrite();
		txtTaxID.setParameter(taxIDfile, false, true);
		String content="";
		BufferedReader txtIDReader=txtTaxID.readfile();
		HashSet<String> hashTaxID=new HashSet<String>();
		while ((content=txtIDReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			hashTaxID.add(ss[0]);
		}
		
		TxtReadandWrite txtInputFIle=new TxtReadandWrite();
		txtInputFIle.setParameter(inputFIle, false, true);
		
		TxtReadandWrite txtOutpuFile=new TxtReadandWrite();
		txtOutpuFile.setParameter(outputFile, true, false);
		
		BufferedReader txtInputReader=txtInputFIle.readfile();
		while ((content=txtInputReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			if (hashTaxID.contains(ss[0])) 
			{
				txtOutpuFile.writefile(content+"\n",false);
			}
		}
		txtOutpuFile.writefile("",true);
	}
	
	
	/**
	  * @deprecated
	 * 处理NCBI的gene2accessionID表,最后获得geneID2Tax
	 * 这个现在不用了，因为考虑把geneID2Tax直接放在NCBIID中
	 * @throws Exception 
	 */
	public static void gene2acIDTax(String pathGene2accessionID,String modifiedFile) throws Exception
	{
		TxtReadandWrite gene2accID=new TxtReadandWrite();
		gene2accID.setParameter(pathGene2accessionID,false, true);
		
		TxtReadandWrite gene2accIDModify=new TxtReadandWrite();
		gene2accIDModify.setParameter(modifiedFile, true,false);
		
		
		
		BufferedReader reader=gene2accID.readfile();
		String content="";
		reader.readLine();
		while((content=reader.readLine())!=null)
		{
			String[] tmp=content.split("\t");
			String newtmp =tmp[0]+"\t"+tmp[1]+"\n";
			gene2accIDModify.writefile(newtmp, false);
		}
		gene2accIDModify.writefile("",true);
	}
	
	
	
	
	/**
	 * 处理NCBI的gene2ensemblID表，按照
	 * 物种 \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
	 * 的格式
	 * @throws Exception 
	 */
	public static void gene2enseb(String pathGene2enseb,String modifiedFile) throws Exception
	{
		TxtReadandWrite gene2ensebID=new TxtReadandWrite();
		gene2ensebID.setParameter(pathGene2enseb, false,true);
		
		TxtReadandWrite gene2ensebIDModify=new TxtReadandWrite();
		gene2ensebIDModify.setParameter(modifiedFile, true,false);
		
		
		
		BufferedReader reader=gene2ensebID.readfile();
		String content="";
		reader.readLine();
		while((content=reader.readLine())!=null)
		{
			String[] tmp=content.split("\t");
			/**
			if (tmp[5].contains(".")) 
			{
				tmp[5]=tmp[5].substring(0, tmp[5].indexOf("."));
			}
			if (tmp[6].contains(".")) 
			{
				tmp[6]=tmp[6].substring(0, tmp[6].indexOf("."));
			}
			if (tmp[7].contains(".")) 
			{
				tmp[7]=tmp[7].substring(0, tmp[7].indexOf("."));
			}
			*/
			
			if (!tmp[2].trim().equals("-")) {
				String newtmp = tmp[0]+"\t"+ tmp[1]+"\t"+tmp[2].trim()+"\t"+NovelBioConst.DBINFO_ENSEMBL_GENE + "\n";
				gene2ensebIDModify.writefile(newtmp, false);
			}
			if (!tmp[4].trim().equals("-")) {
				String newtmp = tmp[0]+"\t"+ tmp[1]+"\t"+tmp[4].trim()+"\t"+NovelBioConst.DBINFO_ENSEMBL_RNA + "\n";
				gene2ensebIDModify.writefile(newtmp, false);
			}
			if (!tmp[6].trim().equals("-")) {
				String newtmp = tmp[0]+"\t"+ tmp[1]+"\t"+tmp[6].trim()+"\t"+NovelBioConst.DBINFO_ENSEMBL_PRO + "\n";
				gene2ensebIDModify.writefile(newtmp, false);
			}
		}
		gene2ensebIDModify.writefile("",true);
	}
	
	
	/**
	 * 看第三列不为"-"的行有多少行
	 * @throws Exception 
	 */
	public static void getLine(String pathGene2enseb) throws Exception
	{
		TxtReadandWrite gene2ensebID=new TxtReadandWrite();
		gene2ensebID.setParameter(pathGene2enseb,false, true);

		BufferedReader reader=gene2ensebID.readfile();
		String content="";
		reader.readLine();
		long i=0;
		while((content=reader.readLine())!=null)
		{
			String[] ssStrings=content.split("\t");
			 if (!ssStrings[2].contains("-")) {
				i++;
			}
		}
	 System.out.println(i);
	}
	
	/**
	 * 
	 * 处理NCBI的gene2refID表,把第一列和第二列提取出来
	 * 目的是获得geneID与refSeq状态的对应表
	 * @param pathGene2ref 输入gene2refseq
	 * @param modifiedFile geneID与refSeq状态的对应表
	 * @param refseqIDfile taxID \t geneID \t accID \t DBINFO \n
	 * @throws Exception
	 */
	public static void gene2ref(String pathGene2ref,String modifiedFile,String refseqIDfile) throws Exception
	{
		TxtReadandWrite gene2refID = new TxtReadandWrite();
		gene2refID.setParameter(pathGene2ref, false,true);
		
		TxtReadandWrite gene2refIDModify = new TxtReadandWrite();
		gene2refIDModify.setParameter(modifiedFile, true,false);
		
		TxtReadandWrite txtgene2refID =new TxtReadandWrite();
		txtgene2refID.setParameter(refseqIDfile, true,false);
		
		
		BufferedReader reader=gene2refID.readfile();
		String content="";
		reader.readLine();
		while((content=reader.readLine())!=null)
		{
			String[] tmp=content.split("\t");
			tmp[3] = CopeID.removeDot(tmp[3]);
			tmp[5] = CopeID.removeDot(tmp[7]);
			tmp[7] = CopeID.removeDot(tmp[7]);
			String newtmp = tmp[0]+"\t"+ tmp[1]+"\t"+tmp[2]+"\n";
			gene2refIDModify.writefile(newtmp, false);
			if (!tmp[3].equals("-")) {
				String refseq = tmp[0]+"\t"+tmp[1]+"\t"+tmp[3]+"\t"+NovelBioConst.DBINFO_NCBI_ACC_REFSEQ_RNA+"\n";
				txtgene2refID.writefile(refseq);
			}
			if (!tmp[5].equals("-")) {
				String refseq = tmp[0]+"\t"+tmp[1]+"\t"+tmp[5]+"\t"+NovelBioConst.DBINFO_NCBI_ACC_REFSEQ_PROTEIN +"\n";
				txtgene2refID.writefile(refseq);
			}
			if (!tmp[7].equals("-")) {
				String refseq = tmp[0]+"\t"+tmp[1]+"\t"+tmp[7]+"\t"+NovelBioConst.DBINFO_NCBI_ACC_REFSEQ_DNA+"\n";
				txtgene2refID.writefile(refseq);
			}
		}
		gene2refIDModify.writefile("",true);
		txtgene2refID.close();
		gene2refID.close();
		gene2refIDModify.close();
	}
	
	
	
	
}
