package com.novelbio.upDateDB.idConvert;

import java.io.BufferedReader;

import com.novelBio.base.dataOperate.TxtReadandWrite;


/**
 * 给定gene_info的表，将其中的symbol全部提取出来，按照
 * 物种 \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
 * 的格式
 * @author zong0jie
 *
 */
public class GeneInfoTaxIDgetSymbol {
	/**
	 * 	
	 * @param filePath
	 * @throws Exception 
	 */
	public static void getSymbol(String geneInfoFile,String symbolFile) throws Exception
	{
		TxtReadandWrite txtGeneInfo=new TxtReadandWrite();
		txtGeneInfo.setParameter(geneInfoFile,false, true);
		BufferedReader geneInfoReader=txtGeneInfo.readfile();
		String content="";
		
		TxtReadandWrite txtGeneSymbol=new TxtReadandWrite();
		txtGeneSymbol.setParameter(symbolFile, true,false);
		
		while ((content=geneInfoReader.readLine())!=null) {
			String[] ss=content.split("\t");
			
			
			String[] ss2=ss[2].split("\\||/");
			if (ss2.length>1) {
				for (int i = 0; i < ss2.length; i++) {
					txtGeneSymbol.writefile(ss[0]+"\t"+ss[1]+"\t"+ss2[i]+"\tsymbol\n", false);
				}
			}
			txtGeneSymbol.writefile(ss[0]+"\t"+ss[1]+"\t"+ss[2]+"\tsymbol\n", false);
		}
		txtGeneSymbol.writefile("", true);
	}
}
