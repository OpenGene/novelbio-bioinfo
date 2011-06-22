package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;



/**
 * ��ȡ����peak������ļ���ֻ����peak��Ⱦɫ��λ�ã�������꣬�յ�����
 * @author zong0jie
 *
 */
public class GffHashPeak extends GffHash{
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
     * @param colChrID
     * @param colPeakstart
     * @param colPeakend
     * @param rowNum
	 */
	public Hashtable<String, ArrayList<GffDetail>> ReadGffarray(String gfffilename,boolean peakcis ,int colChrID,int colPeakstart,int colPeakend,int rowNum) throws Exception 
	{		
		TxtReadandWrite txtPeakInfo=new TxtReadandWrite();
		//�Ȱ�txt�ı��е�peak��Ϣ��ȡ
		int[] colNum=new int[3];
		colNum[0]=colChrID;colNum[1]=colPeakstart;colNum[2]=colPeakend;
		txtPeakInfo.setParameter(gfffilename, false,true);
		String[][] peakInfo=ExcelTxtRead.readtxtExcel(gfffilename, "\t", colNum, rowNum,  txtPeakInfo.ExcelRows());
		/////////װ����ʱlist
		LinkedList<String[]> lstmpPeakinfo=new LinkedList<String[]>();
		for (int i = 0; i < peakInfo.length; i++) {
			lstmpPeakinfo.add(peakInfo[i]);
		}
		////����ʱlist��������,���Ȱ���Chr����Ȼ���վ�����������
	     Collections.sort(lstmpPeakinfo,new Comparator<String[]>(){
	            public int compare(String[] arg0, String[] arg1) {
	            	int i=arg0[0].compareTo(arg1[0]);
	            	if(i==0){
	            		 if( Integer.parseInt(arg0[1])< Integer.parseInt(arg1[1]))
	 	                {	return -1;}
	 	                else if (Integer.parseInt(arg0[1])== Integer.parseInt(arg1[1])) 
	 	                { 	return 0;}
	 	                else
	 	                {   return 1;}
	            	}
	               return i;
	            }
	        });
		//////////////////////////��ʽ��ȡ������GffUCSC�Ķ�ȡ����///////////////////////
	 	//ʵ����������
			locHashtable =new Hashtable<String, GffDetail>();//�洢ÿ��LOCID���������Ϣ�Ķ��ձ�
			Chrhash=new Hashtable<String, ArrayList<GffDetail>>();//һ����ϣ�����洢ÿ��Ⱦɫ��
			LOCIDList=new ArrayList<String>();//˳��洢ÿ��peak�ţ������Ƿ��ص�
			LOCChrHashIDList=new ArrayList<String>();//
			ArrayList<GffDetail> LOCList=null ;//˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
			
			String chrnametmpString="";
			int tmppeakstart=-1;
			int tmppeakend=-1;

			int peakNum=peakInfo.length;
			for (int i = 0; i < peakNum; i++) {
				chrnametmpString=lstmpPeakinfo.get(i)[0];
				tmppeakstart=Integer.parseInt(lstmpPeakinfo.get(i)[1]);
				tmppeakend=Integer.parseInt(lstmpPeakinfo.get(i)[2]);
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//�µ�Ⱦɫ��
				if (!Chrhash.containsKey(chrnametmpString)) //�µ�Ⱦɫ��
				{
					if(LOCList!=null)//����Ѿ�������LOCList��Ҳ����ǰһ��LOCList����ô�Ƚض̣�Ȼ��������gffGCtmpDetail.numberstart����
					{
						LOCList.trimToSize();
						 //��peak����˳��װ��LOCIDList
						   for (GffDetail gffDetail : LOCList) {
							   LOCChrHashIDList.add(gffDetail.locString);
						   }
					}
					LOCList=new ArrayList<GffDetail>();//�½�һ��LOCList������Chrhash
					Chrhash.put(chrnametmpString, LOCList);
				}
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				
				//����ص�peak
				//����peak������Ƿ�С���ϸ�peak���յ㣬���С�ڣ���˵����peak���ϸ�peak����
				GffDetail lastGffdetailpeak;
				LOCIDList.add(tmppeakstart+"_"+tmppeakend);//�����LOCIDList
				if(LOCList.size()>0 && tmppeakstart < (lastGffdetailpeak = LOCList.get(LOCList.size()-1)).numberend )
				{   //�޸Ļ��������յ�
					if(tmppeakstart<lastGffdetailpeak.numberstart)
						lastGffdetailpeak.numberstart=tmppeakstart;
					if(tmppeakend>lastGffdetailpeak.numberend)
						lastGffdetailpeak.numberend=tmppeakend;
  
					//������(ת¼��ID)װ��LOCList
				
					
					//��������(ת¼��)��IDװ��locString��
					lastGffdetailpeak.locString=lastGffdetailpeak.locString+"/"+tmppeakstart+"_"+tmppeakend;
					//����ֵװ��locHashtable	

					//��locHashtable����Ӧ����ĿҲ�޸ģ�ͬʱ�����µ���Ŀ
					//��ΪUCSC����û��ת¼��һ˵��ֻ������LOCID����һ����������������ֻ�ܹ�������ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene
					String[] allPeakID=lastGffdetailpeak.locString.split("/");
					for (int m= 0; m < allPeakID.length; m++) {
						locHashtable.put(allPeakID[m], lastGffdetailpeak);
					}
					continue;
				}
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//�����peak 
				GffDetail gffdetailpeak=new GffDetail();
				gffdetailpeak.ChrID=chrnametmpString;
				//������,����peak��һ�������
				gffdetailpeak.cis5to3=peakcis;

				gffdetailpeak.locString=tmppeakstart+"_"+tmppeakend;
				gffdetailpeak.numberstart=tmppeakstart;
				gffdetailpeak.numberend=tmppeakend;
				
				LOCList.add(gffdetailpeak);  
				locHashtable.put(gffdetailpeak.locString, gffdetailpeak);
			}
			LOCList.trimToSize();
			for (GffDetail gffDetail : LOCList) {
				LOCChrHashIDList.add(gffDetail.locString);
			}
			txtPeakInfo.close();
			//System.out.println(mm);
			return Chrhash;

	}

	/**
     * ��ײ��ȡpeak�����ļ��ķ�������ȡ���ɵ�peak��Ϣ������peakInfo����peak����Chr��peak��㣬peak�յ�<br>
     * �����ڲ��������</b>�������������ϣ���һ��list��, �ṹ���£�<br>
     * <b>1.Chrhash</b><br>
     * ��ChrID��--ChrList-- GeneInforList(GffDetail��)
     * ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID, chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
     * ��LOCID��--GeneInforlist������LOCID����������Ŀ���,���ﲻ���Ƕ���ص���peak�� peak���_peak�յ�.<br>
     *  <b>3.LOCIDList</b><br>
     * ��LOCID��--LOCIDList����˳�򱣴�Peak,���ﲻ���Ƕ���ص���peak��������ͨ������ĳ��������,������� :peak���_peak�յ�<br>
     * <b>LOCChrHashIDList </b><br>
     *   LOCChrHashIDList�б���LOCID����������Ŀ���,��Chrhash�������һ�£�������ص���peak����һ�� peak���_peak�յ�/peak���_peak�յ�_...<br>
     * @param peakInfo 
	 */
	public Hashtable<String, ArrayList<GffDetail>> ReadGffarray(String[][] peakInfo) throws Exception 
	{		
		/////////װ����ʱlist
		LinkedList<String[]> lstmpPeakinfo=new LinkedList<String[]>();
		for (int i = 0; i < peakInfo.length; i++) {
			lstmpPeakinfo.add(peakInfo[i]);
		}
		////����ʱlist��������,���Ȱ���Chr����Ȼ���վ�����������
	     Collections.sort(lstmpPeakinfo,new Comparator<String[]>(){
	            public int compare(String[] arg0, String[] arg1) {
	            	int i=arg0[0].compareTo(arg1[0]);
	            	if(i==0){
	            		 if( Integer.parseInt(arg0[1])< Integer.parseInt(arg1[1]))
	 	                {	return -1;}
	 	                else if (Integer.parseInt(arg0[1])== Integer.parseInt(arg1[1])) 
	 	                { 	return 0;}
	 	                else
	 	                {   return 1;}
	            	}
	               return i;
	            }
	        });
		//////////////////////////��ʽ��ȡ������GffUCSC�Ķ�ȡ����///////////////////////
	 	//ʵ����������
			locHashtable =new Hashtable<String, GffDetail>();//�洢ÿ��LOCID���������Ϣ�Ķ��ձ�
			Chrhash=new Hashtable<String, ArrayList<GffDetail>>();//һ����ϣ�����洢ÿ��Ⱦɫ��
			LOCIDList=new ArrayList<String>();//˳��洢ÿ��peak�ţ������Ƿ��ص�
			LOCChrHashIDList=new ArrayList<String>();//
			ArrayList<GffDetail> LOCList=null ;//˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
			
			String chrnametmpString="";
			int tmppeakstart=-1;
			int tmppeakend=-1;

			int peakNum=peakInfo.length;
			for (int i = 0; i < peakNum; i++) {
				chrnametmpString=lstmpPeakinfo.get(i)[0];
				tmppeakstart=Integer.parseInt(lstmpPeakinfo.get(i)[1]);
				tmppeakend=Integer.parseInt(lstmpPeakinfo.get(i)[2]);
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//�µ�Ⱦɫ��
				if (!Chrhash.containsKey(chrnametmpString)) //�µ�Ⱦɫ��
				{
					if(LOCList!=null)//����Ѿ�������LOCList��Ҳ����ǰһ��LOCList����ô�Ƚض̣�Ȼ��������gffGCtmpDetail.numberstart����
					{
						LOCList.trimToSize();
						 //��peak����˳��װ��LOCIDList
						   for (GffDetail gffDetail : LOCList) {
							   LOCChrHashIDList.add(gffDetail.locString);
						   }
					}
					LOCList=new ArrayList<GffDetail>();//�½�һ��LOCList������Chrhash
					Chrhash.put(chrnametmpString, LOCList);
				}
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				
				//����ص�peak
				//����peak������Ƿ�С���ϸ�peak���յ㣬���С�ڣ���˵����peak���ϸ�peak����
				GffDetail lastGffdetailpeak;
				LOCIDList.add(tmppeakstart+"_"+tmppeakend);//�����LOCIDList
				if(LOCList.size()>0 && tmppeakstart < (lastGffdetailpeak = LOCList.get(LOCList.size()-1)).numberend )
				{   //�޸Ļ��������յ�
					if(tmppeakstart<lastGffdetailpeak.numberstart)
						lastGffdetailpeak.numberstart=tmppeakstart;
					if(tmppeakend>lastGffdetailpeak.numberend)
						lastGffdetailpeak.numberend=tmppeakend;
  
					//������(ת¼��ID)װ��LOCList
				
					
					//��������(ת¼��)��IDװ��locString��
					lastGffdetailpeak.locString=lastGffdetailpeak.locString+"/"+tmppeakstart+"_"+tmppeakend;
					//����ֵװ��locHashtable	

					//��locHashtable����Ӧ����ĿҲ�޸ģ�ͬʱ�����µ���Ŀ
					//��ΪUCSC����û��ת¼��һ˵��ֻ������LOCID����һ����������������ֻ�ܹ�������ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene
					String[] allPeakID=lastGffdetailpeak.locString.split("/");
					for (int m= 0; m < allPeakID.length; m++) {
						locHashtable.put(allPeakID[m], lastGffdetailpeak);
					}
					continue;
				}
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//�����peak 
				GffDetail gffdetailpeak=new GffDetail();
				gffdetailpeak.ChrID=chrnametmpString;
				//������,����peak��һ�������
				gffdetailpeak.cis5to3=true;

				gffdetailpeak.locString=tmppeakstart+"_"+tmppeakend;
				gffdetailpeak.numberstart=tmppeakstart;
				gffdetailpeak.numberend=tmppeakend;
				
				LOCList.add(gffdetailpeak);  
				locHashtable.put(gffdetailpeak.locString, gffdetailpeak);
			}
			LOCList.trimToSize();
			for (GffDetail gffDetail : LOCList) {
				LOCChrHashIDList.add(gffDetail.locString);
			}
			//System.out.println(mm);
			return Chrhash;

	}
	
	
	
	/**
	 * ��������÷������ã�ֱ�ӷ���null������һ��
	 */
	@Override
	public Hashtable<String, ArrayList<GffDetail>> ReadGffarray(
			String gfffilename) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
}
