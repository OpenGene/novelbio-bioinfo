package com.novelbio.database.updatedb.idConvert;

import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;


public class PigAffyID
{
	public static void getID(String filepath,String output) throws Exception 
	{
		ExcelOperate pigRead=new ExcelOperate();
		pigRead.openExcel(filepath);
		String[][] pigInfo = pigRead.ReadExcel(2, 1, pigRead.getRowCount(), pigRead.getColCount());
		
		TxtReadandWrite pigWrite=new TxtReadandWrite();
		pigWrite.setParameter(output, true,false);
		
	    //Pattern patunprot =Pattern.compile("(.+?)\\.\\d\\b", Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
	    Pattern patgbID =Pattern.compile("gb:(\\w+?)(\\.\\d){0,1}\\b", Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
	    Pattern patgbTransMambrance =Pattern.compile("(\\w+?)(\\.\\d){0,1}\\b", Pattern.CASE_INSENSITIVE);  
	    Pattern patgbTranscriptID =Pattern.compile("([A-Za-z_]|\\d)+", Pattern.CASE_INSENSITIVE);  
	    Matcher matID;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
		
		
		for (int i = 0; i < pigInfo.length; i++) {
 
			if (pigInfo[i][8]==null||pigInfo[i][8].trim().equals("")) {
				
				continue;
			}
			//AffID
			String ss1="9823\t"+pigInfo[i][8].trim()+"\t"+pigInfo[i][0].trim()+"\tAffID\n";
			pigWrite.writefile(ss1, false);
			
			//NCBI gb:
			
	         matID=patgbID.matcher(pigInfo[i][2]);
			if (matID.find()) {
				String ss2="9823\t"+pigInfo[i][8].trim()+"\t"+matID.group(1)+"\tGeneBank\n";
				pigWrite.writefile(ss2, false);
			}
	
			//Ssc.15838
	         
			if (!pigInfo[i][4].contains("-")) {
				String sse=pigInfo[i][4].split("///")[0].trim();
				String ss2="9823\t"+pigInfo[i][8].trim()+"\t"+sse+"\tUniGeneID\n";
				pigWrite.writefile(ss2, false);
			}
	
 
			//swissport: Q153Y6
			if (!pigInfo[i][9].contains("-")) {
				String sse=pigInfo[i][9].split("///")[0].trim();
				String ss2="9823\t"+pigInfo[i][8].trim()+"\t"+sse+"\tSwissPort\n";
				pigWrite.writefile(ss2, false);
			}

			//RefProteinID: NM_001100
			if (!pigInfo[i][10].contains("-")) {
				String sse=pigInfo[i][10].split("///")[0].trim();
				if (sse.contains(".")) 
				{
					sse=sse.substring(0, sse.indexOf("."));
				}
				String ss2="9823\t"+pigInfo[i][8].trim()+"\t"+sse+"\tRefProteinID\n";
				pigWrite.writefile(ss2, false);
			}
			
			//RefTranscriptID: NM_001100
			if (!pigInfo[i][11].contains("-")) {
				String sse=pigInfo[i][11].split("///")[0].trim();
				if (sse.contains(".")) 
				{
					sse=sse.substring(0, sse.indexOf("."));
				}
				String ss2="9823\t"+pigInfo[i][8].trim()+"\t"+sse+"\tRefTranscriptID\n";
				pigWrite.writefile(ss2, false);
			}
			//InterPro: IPR000048
			  matID=patgbTranscriptID.matcher(pigInfo[i][15]);
			if (matID.find()) {
				String ss2="9823\t"+pigInfo[i][8].trim()+"\t"+matID.group()+"\tInterPro\n";
				pigWrite.writefile(ss2, false);
			}
			
			
			
			//TransMambrance: NM_001100
			  matID=patgbTransMambrance.matcher(pigInfo[i][16]);
			if (matID.find()) {
				String ss2="9823\t"+pigInfo[i][8].trim()+"\t"+matID.group(1)+"\tTransMambrance\n";
				pigWrite.writefile(ss2, false);
			}
			
			//TranscriptID: NM_001100
			  matID=patgbTranscriptID.matcher(pigInfo[i][18]);
			if (matID.find()) {
				String ss2="9823\t"+pigInfo[i][8].trim()+"\t"+matID.group()+"\tTranscriptID\n";
				pigWrite.writefile(ss2, false);
			}
		}
		pigWrite.writefile("", true);
	}
}
