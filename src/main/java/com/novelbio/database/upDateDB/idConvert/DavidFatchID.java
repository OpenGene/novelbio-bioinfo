package com.novelbio.database.upDateDB.idConvert;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataOperate.WebFetch;


public class DavidFatchID
{
	/**
	 * ���� david����������ID��ENZһһ��Ӧ��txt�ı�
	 * @param Davidhtml
	 * @param outPut
	 * @throws Exception
	 */
	public static void geturl(String Davidhtml,String Davidurl) throws Exception 
	{
		TxtReadandWrite davidReadandWritehtml=new TxtReadandWrite();
		davidReadandWritehtml.setParameter(Davidhtml,false,true);
		
		 
		 TxtReadandWrite  davidrReadandWriteurl=new TxtReadandWrite();
		 davidrReadandWriteurl.setParameter(Davidurl, true,false);
		
		
		
		
		BufferedReader davidFileReader=davidReadandWritehtml.readfile();
		String tmpcontent="";
		while ((tmpcontent=davidFileReader.readLine())!=null) 
		{
			if (tmpcontent.contains("<th width=\"15%\">Species</th>")) {
				break;
			}
		}
		ArrayList<String[]> lsAffIDtoEND=new ArrayList<String[]>();
		String content="";
 

		 
		while ((content=davidFileReader.readLine())!=null) 
		{
			String[]  affIDtoEND=new String[2];
			Pattern patternID =Pattern.compile("<td>([^<>]+)</td>", Pattern.CASE_INSENSITIVE);  //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
			Matcher matcherID;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
			Pattern patternurl =Pattern.compile("geneReportFull\\.jsp\\?rowids=(\\w+)\"", Pattern.CASE_INSENSITIVE);  //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
			Matcher matcherurl;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
			matcherID=patternID.matcher(content);
			if (matcherID.find()) 
			{
				affIDtoEND[0]=matcherID.group(1);
				davidFileReader.readLine();
				content=davidFileReader.readLine();
				matcherurl=patternurl.matcher(content);
				if (matcherurl.find()) 
				{
					affIDtoEND[1] ="http://david.abcc.ncifcrf.gov/geneReportFull.jsp?rowids="+matcherurl.group(1);
					lsAffIDtoEND.add(affIDtoEND);
				}
			}
		}
		davidrReadandWriteurl.ExcelWrite(lsAffIDtoEND, "\t", 1, 1);
	}
	
	/**
	 * ����davidurl�����������ļ���һ�����ϴ�û������ģ�һ���ǳɹ���
	 * @param Davidurl
	 * @param DavidLost
	 * @param outPut
	 * @throws Exception
	 */
	public static void getInfo(String Davidurl,String DavidLost,String outPut) throws Exception {
		 TxtReadandWrite  davidrReadandWriteurl=new TxtReadandWrite();
		 davidrReadandWriteurl.setParameter(Davidurl,false,true);
		
		 TxtReadandWrite davidReadandWritelost=new TxtReadandWrite();
		 davidReadandWritelost.setParameter(DavidLost, true,false);
		 
		 TxtReadandWrite davidReadandWriteResult=new TxtReadandWrite();
		 davidReadandWriteResult.setParameter(outPut, true,true);
		 BufferedReader davidFileReader=davidrReadandWriteurl.readfile();
			String tmpcontent="";
			
			WebFetch davidFetch=new WebFetch();
			
			while ((tmpcontent=davidFileReader.readLine())!=null) 
			{
				String[] ss=tmpcontent.split("\t");
				try {
					BufferedReader davidReader=davidFetch.GetFetch(ss[1], false);
					ss[1]=getInfo(davidReader);
					davidReadandWriteResult.writefile(ss[0]+"\t"+ss[1]+"\n");
					System.out.println(ss[0]+"\t"+ss[1]);
					System.gc();
				} catch (Exception e) {
					davidReadandWritelost.writefile(ss[0]+"\t"+ss[1]+"\n");
				}
			}
		 
	}
	
	
	
	/**
	 * ���� david��ȡ��ҳ�棬ץȡENTREZ_GENE_ID�����û�У��򷵻�null
	 * @param DavidReader
	 * @return
	 * @throws IOException
	 */
	private static String getInfo(BufferedReader DavidReader) throws IOException
	{
		 Pattern pattern =Pattern.compile("target=\"_blank\">(\\d+)</a>", Pattern.CASE_INSENSITIVE);  //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
		 Matcher matcher;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
		String content="";
		while ((content=DavidReader.readLine())!=null) 
		{
			//System.out.println(content);
			if (content.contains("ENTREZ_GENE_ID")) 
			{
				DavidReader.readLine();
				content=DavidReader.readLine();
				matcher=pattern.matcher(content);
				if (matcher.find()) 
				{
					return matcher.group(1);
				}
			}
		}
		return null;
	}
}
