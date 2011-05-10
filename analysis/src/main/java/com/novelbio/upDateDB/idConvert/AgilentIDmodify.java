package com.novelbio.upDateDB.idConvert;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.base.dataOperate.TxtReadandWrite;


/**
 * ����Agilent���ص�txt��ʽexcelת����Ϊ�������ݿ�ĸ�ʽ
 * ��ʽ����
 * ���� \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
 * ע��affyоƬ��Ҫ����оƬ�ͺ�
 * @author zong0jie
 *
 */
public class AgilentIDmodify {
	/**
	 * ����agilent���ص�cvs��ʽexcelת����Ϊ�������ݿ�ĸ�ʽ
	 * ��ʽ����
	 * ���� \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
	 * ע��agilentоƬ��Ҫ����оƬ�ͺ�
	 * @param taxID ���ֱ��ncbi��taxID
	 * @param rowstart ʵ������
	 * @param agilentDBID agilentоƬ�ͺ�,���д�����ݿ��
	 * @author zong0jie
	 *
	 */
	public static void getInfo(int taxID,String agilentInput,int rowstart, String output,String agilentDBID) throws Exception {
		TxtReadandWrite txtexcelAgilent=new TxtReadandWrite();
		txtexcelAgilent.setParameter(agilentInput, false, true);

		
		TxtReadandWrite txtAgilent=new TxtReadandWrite();
		txtAgilent.setParameter(output, true,false);
		ArrayList<String[]> result=new ArrayList<String[]>();
		String[][] agilentInfo = txtexcelAgilent.ExcelRead("\t", rowstart, 1, txtexcelAgilent.ExcelRows(), txtexcelAgilent.ExcelColumns(2, "\t"));
		
		Pattern patgbTransMambrance =Pattern.compile("([A-Z_]+?\\d+)(\\.\\d){0,1}", Pattern.CASE_INSENSITIVE);  
	    //Pattern patgbIRP =Pattern.compile("([A-Za-z_]|\\d)+", Pattern.CASE_INSENSITIVE);  
	    Matcher matID;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
		
		for (int i = 0; i < agilentInfo.length; i++) {
			if (agilentInfo[i][5]==null||agilentInfo[i][5].contains("-")||agilentInfo[i][5].trim().equals("")) {
				continue;
			}
			String[] tmp=new String[4];
			tmp[0]=taxID+"";tmp[1]=agilentInfo[i][5].trim();     tmp[2]=agilentInfo[i][0];   tmp[3]=agilentDBID;result.add(tmp);
			if (agilentInfo[i][1]!=null&&!agilentInfo[i][1].trim().equals("")) {
				String[] tmp1=new String[4];
				tmp1[0]=taxID+"";tmp1[1]=agilentInfo[i][5].trim();     tmp1[2]=agilentInfo[i][1];   tmp1[3]=agilentDBID+"_PrimaryAccession";result.add(tmp1);
			}
			if (agilentInfo[i][1]!=null&&!agilentInfo[i][2].trim().equals("")) {
				String[] tmp1=new String[4];
				tmp1[0]=taxID+"";tmp1[1]=agilentInfo[i][5].trim();     tmp1[2]=agilentInfo[i][2];   tmp1[3]=agilentDBID+"_RefSeqAccession";result.add(tmp1);
			}
			if (agilentInfo[i][1]!=null&&!agilentInfo[i][3].trim().equals("")) {
				String[] tmp1=new String[4];
				tmp1[0]=taxID+"";tmp1[1]=agilentInfo[i][5].trim();     tmp1[2]=agilentInfo[i][3];   tmp1[3]=agilentDBID+"_GenbankAccession";result.add(tmp1);
			}
			if (agilentInfo[i][1]!=null&&!agilentInfo[i][4].trim().equals("")) {
				String[] tmp1=new String[4];
				tmp1[0]=taxID+"";tmp1[1]=agilentInfo[i][5].trim();     tmp1[2]=agilentInfo[i][4];   tmp1[3]=agilentDBID+"_UniGeneID";result.add(tmp1);
			}
			if (agilentInfo[i][1]!=null&&!agilentInfo[i][6].trim().equals("")) {
				String[] tmp1=new String[4];
				tmp1[0]=taxID+"";tmp1[1]=agilentInfo[i][5].trim();     tmp1[2]=agilentInfo[i][6];   tmp1[3]=agilentDBID+"_GeneSymbol";result.add(tmp1);
			}
		}
		
		txtAgilent.ExcelWrite(result, "\t", 1, 1);
		
	}
	

}
