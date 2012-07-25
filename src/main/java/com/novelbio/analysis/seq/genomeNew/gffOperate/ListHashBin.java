package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListAbsSearch;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;
import com.novelbio.base.dataStructure.listOperate.ListHashSearch;



/**
 * Ҳ�����Լ������ļ�
 * ��ȡ����peak������ļ���ֻ����peak��Ⱦɫ��λ�ã�������꣬�յ�����
 * @author zong0jie
 *
 */
public class ListHashBin extends ListHashSearch<ListDetailBin, ListCodAbs<ListDetailBin>, ListCodAbsDu<ListDetailBin,ListCodAbs<ListDetailBin>>, ListBin<ListDetailBin>>{
	
	boolean peakcis = true;
	int colChrID = 1;
	int colPeakstart = 2;
	int colPeakend = 3;
	int rowNum = -1;
	/**Ĭ�ϲ�����score */
	int colScore = -1;
	
	/**
     * ��ײ��ȡpeak�����ļ��ķ�������ȡ���ɵ�peak��Ϣ��ֻ��ȡpeak����Chr�У�peak������(��ö�Ϊ��)��peak����У�peak�յ��У�����ָ���ӵڼ��ж��������к��ж���ʵ���к���<br>
     * <b>peak</b> ��������ö�Ϊ���������������<br>
     * ����Gff�ļ���<b>����peak���Բ�����˳�����У������ڲ��������</b>�������������ϣ���һ��list��, �ṹ���£�<br>
     * <b>1.Chrhash</b><br>
     * ��ChrID��--ChrList-- GeneInforList(GffDetail��)
     * ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID, chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
     * ��LOCID��--GeneInforlist������LOCID����������Ŀ���,���ﲻ���Ƕ���ص���peak�� peak���_peak�յ�.<br>
     *  <b>3.LOCIDList</b><br>
     * ��LOCID��--LOCIDList����˳�򱣴�Peak,���ﲻ���Ƕ���ص���peak��������ͨ������ĳ��������,������� :peak���_peak�յ�<br>
     * <b>LOCChrHashIDList </b><br>
     *   LOCChrHashIDList�б���LOCID����������Ŀ���,��Chrhash�������һ�£�������ص���peak����һ�� peak���_peak�յ�/peak���_peak�յ�_...<br>
     * @param gfffilename
     * @param peakcis peak�����������Ǹ����ϣ�����ûʲô�ã��������ã�Ĭ��Ϊtrue�ͺ�
     * @param colChrID ʵ�����֣�Ĭ��Ϊ1
     * @param colPeakstart ʵ�����֣�Ĭ��Ϊ2
     * @param colPeakend ʵ�����֣�Ĭ��Ϊ3
     * @param rowNum ��㣬�ӵڼ��п�ʼ��
	 */
	public ListHashBin(boolean peakcis ,int colChrID,int colPeakstart,int colPeakend,int rowNum) {
		this.peakcis = peakcis;
		this.colChrID = colChrID;
		this.colPeakstart = colPeakstart;
		this.colPeakend = colPeakend;
		this.rowNum = rowNum;
	}
	public ListHashBin() {}
	/**
	 * �趨score���ڵ���
	 * @param colScore
	 */
	public void setColScore(int colScore) {
		this.colScore = colScore;
	}
	/**
	 * ���Ը���
     * ��ײ��ȡpeak�����ļ��ķ�������ȡ���ɵ�peak��Ϣ��ֻ��ȡpeak����Chr�У�peak������(��ö�Ϊ��)��peak����У�peak�յ��У�����ָ���ӵڼ��ж��������к��ж���ʵ���к���<br>
     * <b>peak</b> ��������ö�Ϊ���������������<br>
     * ����Gff�ļ���<b>����peak���Բ�����˳�����У������ڲ��������</b>�������������ϣ���һ��list��, �ṹ���£�<br>
     * <b>1.Chrhash</b><br>
     * ��ChrID��--ChrList-- GeneInforList(GffDetail��)
     * ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID, chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
     * ��LOCID��--GeneInforlist������LOCID����������Ŀ���,���ﲻ���Ƕ���ص���peak�� peak���_peak�յ�.<br>
     *  <b>3.LOCIDList</b><br>
     * ��LOCID��--LOCIDList����˳�򱣴�Peak,���ﲻ���Ƕ���ص���peak��������ͨ������ĳ��������,������� :peak���_peak�յ�<br>
     * <b>LOCChrHashIDList </b><br>
     *   LOCChrHashIDList�б���LOCID����������Ŀ���,��Chrhash�������һ�£�������ص���peak����һ�� peak���_peak�յ�/peak���_peak�յ�_...<br>
	 */
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		TxtReadandWrite txtPeakInfo=new TxtReadandWrite(gfffilename,false);
		//�Ȱ�txt�ı��е�peak��Ϣ��ȡ
		int[] colNum=new int[4];
		colNum[0]=colChrID;colNum[1]=colPeakstart;colNum[2]=colPeakend; colNum[3] = colScore; 
		ArrayList<String[]> lstmpPeakinfo =ExcelTxtRead.readLsExcelTxt(gfffilename, colNum, rowNum, -1);//(gfffilename, "\t", colNum, rowNum,  txtPeakInfo.ExcelRows());
		ReadGff(lstmpPeakinfo);
		txtPeakInfo.close();
	}
	/**
	 * ��������setIntȡһ��
	 * @param lsInterval ����ǲ��������
	 */
	public void setInterval(String name, ArrayList<int[]> lsInterval) {
		ArrayList<String[]> lsTmpInfo = new ArrayList<String[]>();
		for (int[] is : lsInterval) {
			String[] tmpInfo = new String[3];
			tmpInfo[0] = name;
			tmpInfo[1] = is[0] + "";
			tmpInfo[2] = is[1] + "";
			lsTmpInfo.add(tmpInfo);
		}
		ReadGff(lsTmpInfo);
	}
	/**
	 * ��������setIntervalȡһ��
	 * @param lsInterval ����ǲ���ĵ�����ֵ������ͳ�Ʋ���ÿ�������������
	 */
	public void setInt(String name, ArrayList<Integer> lsInterval) {
		ArrayList<String[]> lsTmpInfo = new ArrayList<String[]>();
		for (Integer is : lsInterval) {
			String[] tmpInfo = new String[3];
			tmpInfo[0] = name;
			tmpInfo[1] = is + "";
			tmpInfo[2] = is + "";
			lsTmpInfo.add(tmpInfo);
		}
		ReadGff(lsTmpInfo);
	}

	/**
	 * ���Ը�����
	 * �ڲ�������
	 * ��list����ʾpeak����Ϣ������
	 * 0��chrID ͬһ��chrID ��ʾΪͬһ�����е�ϸ��
	 * 1��start
	 * 2��end
	 * 3: score
	 * @param lsInfo
	 */
	public void ReadGff(ArrayList<String[]> lstmpPeakinfo) {
		////����ʱlist��������,���Ȱ���Chr����Ȼ���վ�����������
		Collections.sort(lstmpPeakinfo,new Comparator<String[]>(){
			public int compare(String[] arg0, String[] arg1) {
				int i=arg0[0].compareTo(arg1[0]);
				if(i==0){
					Integer a0 = Integer.parseInt(arg0[1].trim());
					Integer a1 =  Integer.parseInt(arg1[1].trim());
					return a0.compareTo(a1);
				}
				return i;
			}
		});
		//////////////////////////��ʽ��ȡ������GffUCSC�Ķ�ȡ����///////////////////////
	     //ʵ����������
	     mapChrID2ListGff=new LinkedHashMap<String, ListBin<ListDetailBin>>();
	     ListBin<ListDetailBin> LOCList=null ;//˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
	     
	     String tmpChrName = "";
	     int tmppeakstart=-1; int tmppeakend=-1; double score = -1;
	     int peakNum=lstmpPeakinfo.size();
	     
	     for (int i = 0; i < peakNum; i++) {
	    	 tmpChrName=lstmpPeakinfo.get(i)[0].toLowerCase();
	    	 tmppeakstart=Integer.parseInt(lstmpPeakinfo.get(i)[1].trim());
	    	 tmppeakend=Integer.parseInt(lstmpPeakinfo.get(i)[2].trim());
	    	 if (lstmpPeakinfo.get(i).length > 3) {
	    		 score = Double.parseDouble(lstmpPeakinfo.get(i)[3]);
	    	 }
	    	 //�����µ�Ⱦɫ��
	    	 if (!mapChrID2ListGff.containsKey(tmpChrName)) {
	    		 if(LOCList!=null) {
	    			 LOCList.trimToSize();
	    		 }
	    		 LOCList=new ListBin<ListDetailBin>();
	    		 mapChrID2ListGff.put(tmpChrName, LOCList);
	    	 }				
	    	 //����ص�peak
	    	 //����peak������Ƿ�С���ϸ�peak���յ㣬���С�ڣ���˵����peak���ϸ�peak����
	    	 ListDetailBin lastGffdetailpeak;
	    	 if(LOCList.size()>0 && tmppeakstart < (lastGffdetailpeak = LOCList.get(LOCList.size()-1)).getEndAbs() )
	    	 {   //�޸Ļ��������յ�
	    		 if(tmppeakstart < lastGffdetailpeak.getStartAbs())
	    			 lastGffdetailpeak.setStartAbs(tmppeakstart);
	    		 if(tmppeakend>lastGffdetailpeak.getEndAbs())
	    			 lastGffdetailpeak.setEndAbs(tmppeakend);
	    		 //������(ת¼��ID)װ��LOCList					
	    		 //��������(ת¼��)��IDװ��locString��
	    		 lastGffdetailpeak.addItemName(tmppeakstart+"_"+tmppeakend);
	    		 if (colScore > 0) {
	    			 lastGffdetailpeak.setScore((lastGffdetailpeak.getScore() + score)/2);
	    		 }
	    		 continue;
	    	 }
	    	 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    	 //�����peak 
	    	 ListDetailBin gffdetailpeak=new ListDetailBin(tmpChrName, tmppeakstart+"_"+tmppeakend, peakcis);
	    	 //������,����peak��һ�������
	    	 gffdetailpeak.setStartAbs(tmppeakstart);
	    	 gffdetailpeak.setEndAbs(tmppeakend);
	    	 gffdetailpeak.setScore(score);
	    	 LOCList.add(gffdetailpeak);  
	     }
	     LOCList.trimToSize();
	}
}
