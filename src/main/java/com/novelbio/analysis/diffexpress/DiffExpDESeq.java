package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;

/**
 * ��������һ���ı��������м�����ʵ�����м����Ƕ�����
 * Ȼ����Ҳ������
 * ����DEseq�㷨����������reads�����飬Ʃ��miRNAseq��DGE
 * @author zong0jie
 *
 */
public class DiffExpDESeq {
	String rawScript = "/media/winE/Bioinformatics/R/Protocol/DESeq.txt";
	String workSpace = "";
	String fileNameRawdata = "";
	String outScript = "";
	/** ʵ���Ƿ����ظ���ò����һ���ظ��������ظ��� */
	boolean repeatExp = false;
	
	ArrayList<String[]> lsGeneInfo = new ArrayList<String[]>();
	/**
	 * һϵ�еı�ʾ������Ϣ����
	 * 0: colNum
	 * 1: SampleGroup
	 */
	ArrayList<String[]> lsSampleColumn2GroupName;
	/**����ΨһID������û���ظ� */
	int colAccID = 0;
	/**
	 * �Ƚ��飬��������һϵ����
	 * map: condition to compare group <br>
	 * list�Ƚϵ���Ϣ��ֻ������<br>
	 * 0��treatment<br>
	 * 1��control
	 */
	ArrayList<String[]> lsCompareGroup = new ArrayList<String[]>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TxtReadandWrite txtWrite = new TxtReadandWrite("Rstatistic/test",true);
		txtWrite.writefile("fesfes");
		txtWrite.close();
	}
	private void setWorkSpace() {
		workSpace = FileOperate.getProjectPath() + "Tmp/";
	}

	
	private void generateScript() {
		TxtReadandWrite txtReadScript = new TxtReadandWrite(rawScript, false);
		TxtReadandWrite txtOutScript = new TxtReadandWrite(outScript, true);
		for (String content : txtReadScript.readlines()) {
			if (content.startsWith("#workspace"))
				txtOutScript.writefileln(getWorkSpace(content));
			else if (content.startsWith("#filename"))
				txtOutScript.writefileln(getFileName(content));
			else if (repeatExp) {
				
			}
			
			
		}
	}
	
	private String getWorkSpace(String content) {
		String RworkSpace = content.split(SepSign.SEP_ID)[1];
		RworkSpace = RworkSpace.replace("$workspace", workSpace);
		return RworkSpace;
	}
	private String getFileName(String content) {
		String fileNameRawdata = content.split(SepSign.SEP_ID)[1];
		fileNameRawdata = fileNameRawdata.replace("$filename", fileNameRawdata);
		return fileNameRawdata;
	}
	private String getGroup(String content) {
		String Group = content.split(SepSign.SEP_ID)[1];
		Group = Group.replace("$Group", fileNameRawdata);
		return Group;
	}
	private String getGroupFactor() {
		String colUnKnown = "\"unknownSample\"";
		String result = "";
		//���н�������
		Collections.sort(lsSampleColumn2GroupName, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Integer col1 = Integer.parseInt(o1[0]);
				Integer col2 = Integer.parseInt(o2[0]);
				return col1.compareTo(col2);
			}
		});
		int allLine = Integer.parseInt(lsSampleColumn2GroupName.get(lsSampleColumn2GroupName.size() - 1)[0]);
		
		if (!lsSampleColumn2GroupName.get(0)[0].equals("1")) {
			result = colUnKnown;
		}
		else {
			result = "\""+lsSampleColumn2GroupName.get(0)[1]+"\"";
		}
		
		int colSample = 2; int colDetail = 1;
		while (colDetail <= allLine) {
			colSample = Integer.parseInt(lsSampleColumn2GroupName.get(colDetail)[1]);
			if (colSample > colDetail) {
				colDetail++;
				continue;
			}
			else if (colSample == colDetail) {
				result =", "+ "\""+lsSampleColumn2GroupName.get(colDetail)[1]+"\"";
			}
		}
	}
}
