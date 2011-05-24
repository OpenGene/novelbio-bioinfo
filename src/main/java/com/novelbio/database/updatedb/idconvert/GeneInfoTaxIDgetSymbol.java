package com.novelbio.database.updatedb.idconvert;

import java.io.BufferedReader;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;


/**
 * ����gene_info�ı������е�symbolȫ����ȡ����������
 * ���� \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
 * �ĸ�ʽ
 * @author zong0jie
 *
 */
public class GeneInfoTaxIDgetSymbol {
	/**
	 * ����gene_info�ı������е�Symbol,Synonyms�������е�Symbolȫ����ȡ����������
	 * ���� \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
	 * �ĸ�ʽ
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
					txtGeneSymbol.writefile(ss[0]+"\t"+ss[1]+"\t"+ss2[i]+"\t"+NovelBioConst.DBINFO_SYMBOL+"\n", false);
				}
			}
			txtGeneSymbol.writefile(ss[0]+"\t"+ss[1]+"\t"+ss[2]+"\t"+NovelBioConst.DBINFO_SYMBOL+"\n", false);
			if (!ss[4].trim().equals("-")) {
				String[] ss3=ss[4].split("\\|");
				for (int i = 0; i < ss3.length; i++) {
					txtGeneSymbol.writefile(ss[0]+"\t"+ss[1]+"\t"+ss3[i]+"\t"+NovelBioConst.DBINFO_SYNONYMS+"\n", false);
				}
			}
			if (!ss[10].trim().equals("-")) {
				String[] ss3=ss[10].split("\\|");
				for (int i = 0; i < ss3.length; i++) {
					txtGeneSymbol.writefile(ss[0]+"\t"+ss[1]+"\t"+ss3[i]+"\t"+NovelBioConst.DBINFO_SYMBOL+"\n", false);
				}
			}
		}
		txtGeneSymbol.writefile("", true);
		txtGeneInfo.close();
		txtGeneSymbol.close();
	}
}
