package com.novelbio.upDateDB.idConvert;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.base.dataOperate.TxtReadandWrite;


/**
 * 将从affy下载的cvs格式excel转化成为导入数据库的格式
 * 格式如下
 * 物种 \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
 * 注意affy芯片需要给出芯片型号
 * @author zong0jie
 *
 */
public class AffyIDmodify 
{
	/**
	 * 首先删除control探针
	 * 将从affy下载的cvs格式excel转化成为导入数据库的格式
	 * 格式如下
	 * 物种 \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
	 * 注意affy芯片需要给出芯片型号
	 * @param taxID 物种编号ncbi的taxID
	 * @param affyDBID affy芯片型号,最后写入数据库
	 * @author zong0jie
	 *
	 */
	public static void getInfo(int taxID,String affyInput,int rowstart, String output,String affyDBID) throws Exception {
		ExcelOperate excelAffy = new ExcelOperate();
		excelAffy.openExcel(affyInput);
		
		TxtReadandWrite txtAffyInfo=new TxtReadandWrite();
		txtAffyInfo.setParameter(output, true,false);
		ArrayList<String[]> result=new ArrayList<String[]>();
		String[][] affyInfo = excelAffy.ReadExcel(rowstart, 1, excelAffy.getRowCount(), excelAffy.getColCount());
		
		Pattern patgbTransMambrance =Pattern.compile("([A-Z_]+?\\d+?)(\\.\\d){0,1}\\s//", Pattern.CASE_INSENSITIVE);  
	    //Pattern patgbIRP =Pattern.compile("([A-Za-z_]|\\d)+", Pattern.CASE_INSENSITIVE);  
	    Matcher matID;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
		
		
		
		
		for (int i = 0; i < affyInfo.length; i++) {
			if (affyInfo[i][18]==null||affyInfo[i][18].contains("-")||affyInfo[i][18].trim().equals("")) {
				continue;
			}
			
			String[] tmp1=new String[4];
			tmp1[0]=taxID+"";tmp1[1]=affyInfo[i][18].split("///")[0].trim();     tmp1[2]=affyInfo[i][0];   tmp1[3]=affyDBID;result.add(tmp1);
	
			
			
			if (affyInfo[i][8]!=null&&!affyInfo[i][8].contains("-")&&!affyInfo[i][8].trim().equals("")) {
				if (affyInfo[i][8].contains("///")) {
					String[] ss=affyInfo[i][8].split("///");
					for (int j = 0; j < ss.length; j++) {
						String[] tmp=new String[4];
						tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=ss[j].trim() ;tmp[3]="PublicID";result.add(tmp);
					}
				}
				else {
					String[] tmp=new String[4];
					tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=affyInfo[i][8].trim(); tmp[3]="PublicID";result.add(tmp);
				}
			}
			
			if (affyInfo[i][10]!=null&&!affyInfo[i][10].contains("-")&&!affyInfo[i][10].trim().equals("")) {
				if (affyInfo[i][10].contains("///")) {
					String[] ss=affyInfo[i][10].split("///");
					for (int j = 0; j < ss.length; j++) {
						String[] tmp=new String[4];
						tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=ss[j].trim() ;tmp[3]="UniGene";result.add(tmp);
					}
				}
				else {
					String[] tmp=new String[4];
					tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=affyInfo[i][10].trim();   tmp[3]="UniGene";result.add(tmp);
				}
			}
			
			if (affyInfo[i][14]!=null&&!affyInfo[i][14].contains("-")&&!affyInfo[i][14].trim().equals("")) {
				if (affyInfo[i][14].contains("///")) {
					String[] ss=affyInfo[i][14].split("///");
					for (int j = 0; j < ss.length; j++) {
						String[] tmp=new String[4];
						tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=ss[j].trim() ;tmp[3]="symbol";result.add(tmp);
					}
				}
				else {
					String[] tmp=new String[4];
					tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=affyInfo[i][14].trim();   tmp[3]="symbol";result.add(tmp);
				}
			}
		 
			if (affyInfo[i][17]!=null&&!affyInfo[i][17].contains("-")&&!affyInfo[i][17].trim().equals("")) {
				if (affyInfo[i][17].contains("///")) {
					String[] ss=affyInfo[i][17].split("///");
					for (int j = 0; j < ss.length; j++) {
						String[] tmp=new String[4];
						tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=ss[j].trim() ;tmp[3]="ensembl";result.add(tmp);
					}
				}
				else {
					String[] tmp=new String[4];
					tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=affyInfo[i][17].trim();   tmp[3]="ensembl";result.add(tmp);
				}
			}
			
			if (affyInfo[i][19]!=null&&!affyInfo[i][19].contains("-")&&!affyInfo[i][19].trim().equals("")) {
				if (affyInfo[i][19].contains("///")) {
					String[] ss=affyInfo[i][19].split("///");
					for (int j = 0; j < ss.length; j++) {
						String[] tmp=new String[4];
						tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=ss[j].trim() ;tmp[3]="SwissProt";result.add(tmp);
					}
				}
				else {
					String[] tmp=new String[4];
					tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=affyInfo[i][19].trim();   tmp[3]="SwissProt";result.add(tmp);
				}
			}
			
			if (affyInfo[i][22]!=null&&!affyInfo[i][22].contains("-")&&!affyInfo[i][22].trim().equals("")) {
				if (affyInfo[i][22].contains("///")) {
					String[] ss=affyInfo[i][22].split("///");
					for (int j = 0; j < ss.length; j++) {
						String[] tmp=new String[4];
						tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=ss[j].trim() ;tmp[3]="RefSeqPrID";result.add(tmp);
					}
				}
				else {
					String[] tmp=new String[4];
					tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=affyInfo[i][22].trim();   tmp[3]="RefSeqPrID";result.add(tmp);
				}
			}
			
			if (affyInfo[i][23]!=null&&!affyInfo[i][23].contains("-")&&!affyInfo[i][23].trim().equals("")) {
				if (affyInfo[i][23].contains("///")) {
					String[] ss=affyInfo[i][23].split("///");
					for (int j = 0; j < ss.length; j++) {
						String[] tmp=new String[4];
						tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=ss[j].trim() ;tmp[3]="RefSeqRNAID";result.add(tmp);
					}
				}
				else {
					String[] tmp=new String[4];
					tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=affyInfo[i][23].trim();   tmp[3]="RefSeqRNAID";result.add(tmp);
				}
			}
			
			//InterPro: IPR000048
			  matID=patgbTransMambrance.matcher(affyInfo[i][34]);
			while (matID.find()) {
				String[] tmp=new String[4];
				tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=matID.group(1);   tmp[3]="InterPro";result.add(tmp);
			}
			
			
			
			//TransMambrance: NM_001100
			  matID=patgbTransMambrance.matcher(affyInfo[i][35]);
				while (matID.find()) {
					String[] tmp=new String[4];
					tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=matID.group(1);   tmp[3]="TransMambrance";result.add(tmp);
				}
			
			//TranscriptID: NM_001100
			  matID=patgbTransMambrance.matcher(affyInfo[i][39]);
			  while (matID.find()) {
				String[] tmp=new String[4];
				tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=matID.group(1);   tmp[3]="Transcript";result.add(tmp);
			}
			  
				//TranscriptID: NM_001100
			  matID=patgbTransMambrance.matcher(affyInfo[i][40]);
			  while (matID.find()) {
				String[] tmp=new String[4];
				tmp[0]=taxID+"";tmp[1]=affyInfo[i][18].split("///")[0].trim();     tmp[2]=matID.group(1);   tmp[3]="Annotation";result.add(tmp);
			}
		}
		txtAffyInfo.ExcelWrite(result, "\t", 1, 1);
		}
		
}
