package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * ��������һ���ı��������м�����ʵ�����м����Ƕ�����
 * Ȼ����Ҳ������
 * ����DEseq�㷨����������reads�����飬Ʃ��miRNAseq��DGE
 * @author zong0jie
 *
 */
public class DiffExpDESeq {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TxtReadandWrite txtWrite = new TxtReadandWrite("Rstatistic/test",true);
		txtWrite.writefile("fesfes");
		txtWrite.close();
	}
	ArrayList<String[]> lsGeneInfo = new ArrayList<String[]>();
	/**
	 * һϵ�еı�ʾ������Ϣ����
	 */
	ArrayList<Integer> lsColAccID;
	/**����ΨһID������û���ظ� */
	int colAccID = 0;
	/**
	 * �Ƚ��飬��������һϵ����
	 * map: condition to compare group <br>
	 * list�Ƚϵ���Ϣ��ֻ������<br>
	 * 0��treatment<br>
	 * 1��control
	 */
	HashMap<String, ArrayList<int[]>> mapCond2CompareGroup = new HashMap<String, ArrayList<int[]>>();
	
//	private void 
	
	
}
