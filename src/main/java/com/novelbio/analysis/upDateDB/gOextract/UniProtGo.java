package com.novelbio.analysis.upDateDB.gOextract;

import java.io.BufferedReader;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.DAO.FriceDAO.DaoFSGene2Go;
import com.novelbio.database.entity.friceDB.Gene2Go;

public class UniProtGo {
	/**
	 * 提取uniProt的idmapping_select的GO
	 * 注意其中必须含有geneID的行才会被提取
	 * @param idmapping_select
	 * @param output
	 * @throws Exception
	 */
	public static void uniGoExtractGeneID(String idmapping_select,String output) throws Exception
	{
		TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
		txtReadandWrite.setParameter(idmapping_select, false, true);
		BufferedReader uniReader=txtReadandWrite.readfile();
		
		TxtReadandWrite txtUniProtGO=new TxtReadandWrite();
		txtUniProtGO.setParameter(output, true, false);
 
		
		
		String content="";
		while ((content=uniReader.readLine())!=null) 
		{
			String[] tmp=content.split("\t");
			if (tmp[2].trim().equals("")||tmp[2].trim().equals("-"))
			{
				continue;
			}
			if (tmp[6]!=null&&!tmp[6].trim().equals("-")&&!tmp[6].trim().equals(""))  {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
				
				String[] tmp2=tmp[6].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					
					String[] tmptwo=tmp[2].split(";");
					for (int j = 0; j < tmptwo.length; j++) {
						String newtmp = tmp[13]+"\t"+tmptwo[j].trim()+"\t"+tmpp+"\t"+"UniProt"+"\n";;
						txtUniProtGO.writefile(newtmp, false);
					}
 
				}
			}
		}
		txtUniProtGO.writefile("",true);
	}
	
	/**
	 * 提取uniProt的idmapping_select的GO
	 * 注意只提取不含有geneID的行
	 * @param idmapping_select
	 * @param output
	 * @throws Exception
	 */
	public static void uniGoExtractSwissProt(String idmapping_select,String output) throws Exception
	{
		TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
		txtReadandWrite.setParameter(idmapping_select, false, true);
		BufferedReader uniReader=txtReadandWrite.readfile();
		
		TxtReadandWrite txtUniProtGO=new TxtReadandWrite();
		txtUniProtGO.setParameter(output, true, false);
 
		
		
		String content="";
		while ((content=uniReader.readLine())!=null) 
		{
			String[] tmp=content.split("\t");
			if (tmp[6]!=null&&!tmp[6].trim().equals("-")&&!tmp[6].trim().equals(""))  {
				if (tmp[13].trim().equals("")) {
					System.out.println("tax=  ");
					continue;
				}
				
				
				String[] tmp2=tmp[6].split(";");
				for (int i = 0; i < tmp2.length; i++) {
					String tmpp=tmp2[i].trim();
					if (tmpp.equals("")||tmpp.equals("-")) 
					{
						continue;
					}
					
 
						String newtmp = tmp[13]+"\t"+tmp[0].trim()+"\t"+tmpp+"\t"+"UniProt"+"\n";;
						txtUniProtGO.writefile(newtmp, false);
			 
 
				}
			}
		}
		txtUniProtGO.writefile("",true);
	}
	
	
	/**
	 * 专门将UniProt idmapping提取的go插入gene2Go数据库中
	 * @author zong0jie
	 *
	 */
	public static void upDateGenetoGo(String UniprotIdmapping2GOtaxID) throws Exception
	{
		TxtReadandWrite txtgene2Go=new TxtReadandWrite();
		txtgene2Go.setParameter(UniprotIdmapping2GOtaxID,false,true);
		BufferedReader gene2GoReader=txtgene2Go.readfile();
		
		DaoFSGene2Go friceDAO=new DaoFSGene2Go();
		
		String content="";
		int i=0;
		while ((content=gene2GoReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			Gene2Go gene2GoInfo=new Gene2Go();
			gene2GoInfo.setGeneId((long)Double.parseDouble(ss[1]));
			gene2GoInfo.setGOID(ss[2]);
			gene2GoInfo.setDataBase(ss[3]);
			
			Gene2Go gene2GoInfo2=friceDAO.queryGene2Go(gene2GoInfo);
			if (gene2GoInfo2==null)
			{
				friceDAO.InsertGene2Go(gene2GoInfo);
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
	 * 专门提取从GO网站上下载的UniProt
	 * UniProt [multispecies] GO Annotations @ EBI
	 * Unfiltered Annotation File Downloads
	 * 文件名为：gene_association.goa_uniprot.gz
	 * 从这个文件中可以提取很多有用信息，首先是要把所需要的TaxID行提取出来，然后
	 */
	public void uniGoExtractGoFile() {
		
	}

}
