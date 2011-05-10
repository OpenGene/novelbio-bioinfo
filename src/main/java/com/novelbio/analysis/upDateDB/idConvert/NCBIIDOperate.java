package com.novelbio.analysis.upDateDB.idConvert;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;

import com.novelbio.base.dataOperate.TxtReadandWrite;




public class NCBIIDOperate 
{
	/**
	 * ����NCBI��gene2accessionID��,����
	 * ���� \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
	 * �ĸ�ʽ
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
			if (tmp[3].contains(".")) 
			{
				tmp[3]=tmp[3].substring(0, tmp[3].indexOf("."));
			}
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
			
			
			if (!tmp[3].equals("-")) {
				String newtmp = tmp[0]+"\t"+tmp[1]+"\t"+tmp[3]+"\t"+"rnAC"+"\n";
				gene2accIDModify.writefile(newtmp, false);
			}
			if (!tmp[5].equals("-")) {
				String newtmp = tmp[0]+"\t"+ tmp[1]+"\t"+tmp[5]+"\t"+"prAC"+"\n";
				gene2accIDModify.writefile(newtmp, false);
			}
			if (!tmp[6].equals("-")) {
				String newtmp2 = tmp[0]+"\t"+  tmp[1]+"\t"+tmp[6]+"\t"+"prGI"+"\n";
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
	 * ָ��table������һ�к���ָ��taxID���ж���ȡ����
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
	 * ����NCBI��gene2accessionID��,�����geneID2Tax
	 * ������ڲ����ˣ���Ϊ���ǰ�geneID2Taxֱ�ӷ���NCBIID��
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
	 * ����NCBI��gene2ensemblID��������
	 * ���� \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
	 * �ĸ�ʽ
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
				String newtmp = tmp[0]+"\t"+ tmp[1]+"\t"+tmp[2].trim()+"\t"+"Ensembl_Gene"+"\n";
				gene2ensebIDModify.writefile(newtmp, false);
			}
			if (!tmp[4].trim().equals("-")) {
				String newtmp = tmp[0]+"\t"+ tmp[1]+"\t"+tmp[4].trim()+"\t"+"Ensembl_RNA"+"\n";
				gene2ensebIDModify.writefile(newtmp, false);
			}
			if (!tmp[6].trim().equals("-")) {
				String newtmp = tmp[0]+"\t"+ tmp[1]+"\t"+tmp[6].trim()+"\t"+"Ensembl_PRO"+"\n";
				gene2ensebIDModify.writefile(newtmp, false);
			}
		}
		gene2ensebIDModify.writefile("",true);
	}
	
	
	/**
	 * �������в�Ϊ"-"�����ж�����
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
	 * ����NCBI��gene2refID��,�ѵ�һ�к͵ڶ�����ȡ����
	 * Ŀ���ǻ��geneID��refSeq״̬�Ķ�Ӧ��
	 * @throws Exception 
	 */
	public static void gene2ref(String pathGene2ref,String modifiedFile) throws Exception
	{
		TxtReadandWrite gene2refID=new TxtReadandWrite();
		gene2refID.setParameter(pathGene2ref, false,true);
		
		TxtReadandWrite gene2refIDModify=new TxtReadandWrite();
		gene2refIDModify.setParameter(modifiedFile, true,false);
		
		
		
		BufferedReader reader=gene2refID.readfile();
		String content="";
		reader.readLine();
		while((content=reader.readLine())!=null)
		{
			String[] tmp=content.split("\t");
 
			String newtmp = tmp[1]+"\t"+tmp[2]+"\n";
			gene2refIDModify.writefile(newtmp, false);
 
		}
		gene2refIDModify.writefile("",true);
	}
	
	
	
	
}