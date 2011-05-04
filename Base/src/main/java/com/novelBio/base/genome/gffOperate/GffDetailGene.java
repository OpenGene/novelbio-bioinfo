package com.novelBio.base.genome.gffOperate;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * GffDetailList���б���ÿ�����������յ��CDS������յ� 
 * @GffHashGene��ȡGff�ļ���ÿ��������Ի��������Ϣ
 * ������<br>
 * ���������<br>
 * �������յ�<br>
 * ����������Ⱦɫ����<br>
 * ������Ĳ�ͬת¼��<br>
 * ������ת¼����<br>
 * �����еļ�����������Gff�����й�<br>
 * @GffHashItem��ȡGff�ļ���ÿ����Ŀ���Ի��������Ϣ
 * ��Ŀ��<br>
 * ��Ŀ���<br>
 * ��Ŀ�յ�<br>
 * ��Ŀ����Ⱦɫ����<br>
 * ��Ŀ��ת¼���������һ�����У����û�еĻ���Ĭ�Ͼ�������<br>
 */
public class GffDetailGene extends GffDetail
{
	/**
	 * ����ͬһ����Ĳ�ͬת¼��
	 */
	public ArrayList<LinkedList<Integer>> splitlist=new ArrayList<LinkedList<Integer>>();//�洢�ɱ���ӵ�mRNA


	/**
	 * �����һ��ת¼�����cds������cdslist�ĵ�һ��洢mRNAת¼�������
	 */
	public void addcds(int locnumber)
	{
		LinkedList<Integer> cdslist=splitlist.get(splitlist.size()-1);//include one special loc start number to end number	
		cdslist.add(locnumber);
	}	

	/**
	 * ���ת¼��
	 */
  public void addsplitlist()
  {   /**
       *װ�ص����ɱ���ӵ���Ϣ<br>
       *���е�һ����ת¼����ID���ӵڶ��ſ�ʼ��CDS����Ϣ<br>
       *������ô�Ӷ��Ǵӵ�һ��cds��ʼ�ӵ����һ��cds������Ļ����Ǵ�С�ӵ��󣬷�����ǴӴ�ӵ�С��
       */	
  	LinkedList<Integer> cdslist=new LinkedList<Integer>();
      splitlist.add(cdslist);
  }
  
 /**
 * ����ת¼������Ŀ
  * @return
  */
  public int getSplitlistNumber()
  {  
  	return splitlist.size();
  }

  /**
   * �������(��0��ʼ����Ų���ת¼���ľ���ID)<br>
   * ����ĳ��ת¼�������е�һ����ת¼����ID���ӵڶ��ſ�ʼ��CDS����Ϣ<br>
   * ������ô�Ӷ��Ǵӵ�һ��cds��ʼ�ӵ����һ��cds������Ļ����Ǵ�С�ӵ��󣬷�����ǴӴ�ӵ�С��<br>
   */
  public LinkedList<Integer> getcdslist(int splitnum)
  {  
  	return splitlist.get(splitnum);//include one special loc start number to end number	
  }
  
  
  /**
   * ��øû��������һ��ת¼�����ƺ;�����Ϣ
   * @return ����һ��ArrayList-object
   * ��һ���� String ���գ��Ǹ�ת¼��������
   * �ڶ�����ArrayList-Integer���գ��Ǹ�ת¼���ľ�����Ϣ
   * ���е�һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end.ע������������������޹أ���Զ��һ��С�ڵڶ���<br>
   * �ӵ����ʼ��exon����Ϣ��exon�ɶԳ��֣���һ��exon�����Ǹ�ת¼����ת¼��㣬���һ��exon�����Ǹ�ת¼����ת¼�յ�<br>
   * ������ô�Ӷ��Ǵ�С�ӵ���<br>
   */
	public ArrayList<Object> getLongestSplit() 
	{
		ArrayList<Object> result=new ArrayList<Object>();
		if(splitlist.size()==1)
		{
			result.add(lssplitName.get(0));
			result.add(splitlist.get(0));
			return result;
		}
		
		
		ArrayList<Integer> lslength=new ArrayList<Integer>();
		for(int i=0;i<splitlist.size();i++)
		{
			ArrayList<Integer>  subsplit=splitlist.get(i);
			lslength.add(subsplit.get(subsplit.size()-1)-subsplit.get(2));
		}
		int max=lslength.get(0);
		for (int i = 0; i < lslength.size(); i++) {
			if(lslength.get(i)>max)
				max=lslength.get(i);
		}
		
		int longsplitID=lslength.indexOf(max);
		ArrayList<Integer> splitresult=splitlist.get(longsplitID);
		int splitID=splitlist.indexOf(splitresult);
		String splitName=lssplitName.get(splitID);
		result.add(splitName);
		result.add(splitresult);
		return result;
	}
	
  
  
  
  
  
}