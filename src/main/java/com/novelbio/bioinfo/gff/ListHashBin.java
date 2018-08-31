package com.novelbio.bioinfo.gff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.listoperate.ListBin;
import com.novelbio.listoperate.ListCodAbs;
import com.novelbio.listoperate.ListCodAbsDu;
import com.novelbio.listoperate.ListHashSearch;



/**
 * 也可以自己输入文件
 * 读取那种peak坐标的文件，只输入peak的染色体位置，起点坐标，终点坐标
 * @author zong0jie
 *
 */
public class ListHashBin extends ListHashSearch<ListDetailBin, ListCodAbs<ListDetailBin>, ListCodAbsDu<ListDetailBin,ListCodAbs<ListDetailBin>>, ListBin<ListDetailBin>>{
	
	boolean peakcis = true;
	int colChrID = 1;
	int colPeakstart = 2;
	int colPeakend = 3;
	int rowNum = -1;
	/**默认不设置score */
	int colScore = -1;
	
	/**
     * 最底层读取peak坐标文件的方法，读取生成的peak信息，只读取peak所在Chr列，peak正反向(最好都为正)，peak起点列，peak终点列，并且指定从第几行读起，所有行和列都是实际行和列<br>
     * <b>peak</b> 正反向最好都为正，方便后续处理<br>
     * 输入Gff文件，<b>其中peak可以不按照顺序排列，本类内部会给排序</b>，最后获得两个哈希表和一个list表, 结构如下：<br>
     * <b>1.Chrhash</b><br>
     * （ChrID）--ChrList-- GeneInforList(GffDetail类)
     * 其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID, chr格式，全部小写 chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
     * （LOCID）--GeneInforlist，其中LOCID代表具体的条目编号,这里不考虑多个重叠的peak： peak起点_peak终点.<br>
     *  <b>3.LOCIDList</b><br>
     * （LOCID）--LOCIDList，按顺序保存Peak,这里不考虑多个重叠的peak，不建议通过其获得某基因的序号,具体情况 :peak起点_peak终点<br>
     * <b>LOCChrHashIDList </b><br>
     *   LOCChrHashIDList中保存LOCID代表具体的条目编号,与Chrhash里的名字一致，将多个重叠的peak放在一起： peak起点_peak终点/peak起点_peak终点_...<br>
     * @param gfffilename
     * @param peakcis peak是在正链还是负链上，不过没什么用，不用设置，默认为true就好
     * @param colChrID 实际数字，默认为1
     * @param colPeakstart 实际数字，默认为2
     * @param colPeakend 实际数字，默认为3
     * @param rowNum 起点，从第几行开始读
	 */
	public ListHashBin(boolean peakcis ,int colChrID,int colPeakstart,int colPeakend,int rowNum) {
		this.peakcis = peakcis;
		this.colChrID = colChrID;
		this.colPeakstart = colPeakstart;
		this.colPeakend = colPeakend;
		this.rowNum = rowNum;
	}

	public ListHashBin() {}
	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}
	/**
	 * 设定score所在的列
	 * @param colScore
	 */
	public void setColScore(int colScore) {
		this.colScore = colScore;
	}
	/**
	 * 可以覆盖
     * 最底层读取peak坐标文件的方法，读取生成的peak信息，只读取peak所在Chr列，peak正反向(最好都为正)，peak起点列，peak终点列，并且指定从第几行读起，所有行和列都是实际行和列<br>
     * <b>peak</b> 正反向最好都为正，方便后续处理<br>
     * 输入Gff文件，<b>其中peak可以不按照顺序排列，本类内部会给排序</b>，最后获得两个哈希表和一个list表, 结构如下：<br>
     * <b>1.Chrhash</b><br>
     * （ChrID）--ChrList-- GeneInforList(GffDetail类)
     * 其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID, chr格式，全部小写 chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
     * （LOCID）--GeneInforlist，其中LOCID代表具体的条目编号,这里不考虑多个重叠的peak： peak起点_peak终点.<br>
     *  <b>3.LOCIDList</b><br>
     * （LOCID）--LOCIDList，按顺序保存Peak,这里不考虑多个重叠的peak，不建议通过其获得某基因的序号,具体情况 :peak起点_peak终点<br>
     * <b>LOCChrHashIDList </b><br>
     *   LOCChrHashIDList中保存LOCID代表具体的条目编号,与Chrhash里的名字一致，将多个重叠的peak放在一起： peak起点_peak终点/peak起点_peak终点_...<br>
	 */
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		TxtReadandWrite txtPeakInfo=new TxtReadandWrite(gfffilename,false);
		//先把txt文本中的peak信息读取
		int[] colNum=new int[4];
		colNum[0]=colChrID;colNum[1]=colPeakstart;colNum[2]=colPeakend; colNum[3] = colScore; 
		ArrayList<String[]> lstmpPeakinfo =ExcelTxtRead.readLsExcelTxt(gfffilename, colNum, rowNum, -1);//(gfffilename, "\t", colNum, rowNum,  txtPeakInfo.ExcelRows());
		ReadGff(lstmpPeakinfo);
		txtPeakInfo.close();
	}
	/**
	 * 本方法和setInt取一个
	 * @param lsInterval 这个是插入的区间
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
	 * 本方法和setInterval取一个
	 * @param lsInterval 这个是插入的单个数值，用于统计测序每个碱基的数量等
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
	 * 可以覆盖它
	 * 内部有排序
	 * 用list来表示peak的信息，必须
	 * 0：chrID 同一个chrID 表示为同一类型中的细分
	 * 1：start
	 * 2：end
	 * 3: score
	 * @param lsInfo
	 */
	public void ReadGff(List<String[]> lstmpPeakinfo) {
		sortLsPeak(lstmpPeakinfo);
		//////////////////////////正式读取，类似GffUCSC的读取方法///////////////////////
	     mapChrID2ListGff=new LinkedHashMap<String, ListBin<ListDetailBin>>();
	     ListBin<ListDetailBin> LOCList=null ;//顺序存储每个loc的具体信息，一条染色体一个LOCList，最后装入Chrhash表中
	     
	     String tmpChrName = "";
	     int tmppeakstart=-1; int tmppeakend=-1; double score = -1;
	     for (String[] strings : lstmpPeakinfo) {
	    	 tmpChrName=strings[0].toLowerCase();
	    	 try {
		    	 tmppeakstart=Integer.parseInt(strings[1].trim());
		    	 tmppeakend=Integer.parseInt(strings[2].trim());
			} catch (Exception e) { continue; }

	    	 if (strings.length > 3) {
	    		 score = Double.parseDouble(strings[3]);
			}
	    	 //出现新的染色体
	    	 if (!mapChrID2ListGff.containsKey(tmpChrName)) {
	    		 if(LOCList!=null) {
	    			 LOCList.trimToSize();
	    		 }
	    		 LOCList=new ListBin<ListDetailBin>();
	    		 mapChrID2ListGff.put(tmpChrName, LOCList);
	    	 }				
	    	 //添加重叠peak，看本peak的起点是否小于上个peak的终点，如果小于，则说明本peak和上个peak连续
	    	 ListDetailBin lastGffdetailpeak;
	    	 if(LOCList.size()>0 && tmppeakstart < (lastGffdetailpeak = LOCList.get(LOCList.size()-1)).getEndAbs() ) {   
	    		 //修改基因起点和终点
	    		 if(tmppeakstart < lastGffdetailpeak.getStartAbs())
	    			 lastGffdetailpeak.setStartAbs(tmppeakstart);
	    		 if(tmppeakend>lastGffdetailpeak.getEndAbs())
	    			 lastGffdetailpeak.setEndAbs(tmppeakend);
	    		 //将基因(转录本ID)装入LOCList					
	    		 //将本基因(转录本)的ID装入locString中
	    		 lastGffdetailpeak.addItemName(tmppeakstart+"_"+tmppeakend);
	    		 if (colScore > 0) {
	    			 lastGffdetailpeak.setScore((lastGffdetailpeak.getScore() + score)/2);
	    		 }
	    		 continue;
	    	 }
	    	 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    	 //添加新peak 
	    	 ListDetailBin gffdetailpeak=new ListDetailBin(tmpChrName, tmppeakstart+"_"+tmppeakend, peakcis);
	    	 //正反向,所有peak都一个方向的
	    	 gffdetailpeak.setStartAbs(tmppeakstart);
	    	 gffdetailpeak.setEndAbs(tmppeakend);
	    	 gffdetailpeak.setScore(score);
	    	 LOCList.add(gffdetailpeak);  
	     }
	     LOCList.trimToSize();
	}
	
	private void sortLsPeak(List<String[]> lstmpPeakinfo) {
		////对临时list进行排序,首先按照Chr排序，然后按照具体坐标排序
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
	}
}
