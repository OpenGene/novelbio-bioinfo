package com.novelBio.base.genome.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.apache.ibatis.migration.commands.NewCommand;



/**
 * ר�Ŵ洢UCSC��gene�����ļ�
 * group:Genes and Gene Prediction Tracks
 * track:UCSC Genes
 * table:knownGene
 * output format:all fields from selected table
 * GffDetailList���б���ÿ�����������յ��CDS������յ� 
 * @author zong0jie
 * @GffHashGene��ȡGff�ļ���ÿ��������Ի��������Ϣ
 * ������<br>
 * ��������㣬����UCSC konwn geneĳλ�����л�����ǰ��exon�����<br>
 * �������յ㣬����UCSC konwn geneĳλ�����л��������intron���յ�<br>
 * ����������Ⱦɫ����<br>
 * ������Ĳ�ͬת¼��<br>
 * ������ת¼����<br>
 * �����еļ�����������Gff�����й�<br>
 */
public class GffDetailUCSCgene extends GffDetail
{
	/**
	 * ˳�򴢴�ͬһ����Ĳ�ͬת¼�����꣬��Ӧת¼�����ֱ�����splitName��
	 */
	private ArrayList<ArrayList<Integer>> splitList=new ArrayList<ArrayList<Integer>>();//�洢�ɱ���ӵ�mRNA
	
	private ArrayList<Boolean> lsSplitCis5to3 = new ArrayList<Boolean>();

	/**
	 * ָ�����һ��ת¼���ķ���
	 * �����Ҫ��UCSC��ʹ�ã���ΪUCSC���м��ٲ���һ��������ͬʱ��������ͷ������У���������������ÿ��ת¼���ķ���
	 */
	public void addCis5to3(boolean cis5to3)
	{
		lsSplitCis5to3.add(cis5to3);
	}
	
	/**
	 * �����һ��ת¼�����exon���꣬����exonList�ĵ�һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end.<br>
	 * ע������������������޹أ���Զ��һ��С�ڵڶ���<br>
     *�ӵ����ſ�ʼ��Exon����Ϣ<br>
     *������ô�Ӷ��Ǵ�С�ӵ���
	 */
	public void addExon(int locnumber)
	{
		ArrayList<Integer> exonList=splitList.get(splitList.size()-1);//include one special loc start number to end number	
		exonList.add(locnumber);
	}	
	
	/**
	 * �����һ��ת¼�����exon���꣬����exonList�ĵ�һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end.<br>
	 * ע������������������޹أ���Զ��һ��С�ڵڶ���<br>
     *�ӵ����ſ�ʼ��Exon����Ϣ<br>
     *������ô�Ӷ��Ǵ�С�ӵ���<br>
     *���������Ҫ�Ƕ�ȡgff�ļ�ʱʹ�ã���Ϊgff�ļ���exon�ڷ����ʱ���� 7��8  5��6  3��4  1��2���ָ�ʽ������Ҫ���ż�
     *��ʱ��num=0��ÿ��exon�ȼӺ�һ���ټ�ǰһ��
     *@param num ���num<0,�ͽ�ֵ�����������numһ���ȡ����ֵ��Ҫô�����exon��ȡ0��Ҫô�����exon��ȡ-1
     *@param locnumber
     *@param replace �Ƿ��滻��һ��ֵ�������Ҫ����TIGR��TAIR��gff�ļ���������Ѻ���atg��cds�ָ��������
     *��Ϊ�ǳɶԳ��ֵ�exon���������꣬�������replaceΪtrue�Ļ�����������Ƚϲ���λ�õ�ֵ��������ֵ�Ƿ�ֻ���1������ǵĻ����Ὣnum����λ�õ�һ��Ԫ�س�ȥ
	 */
	public void addExon(int num, int locnumber,boolean replace)
	{
		//˵����exon�Ƿ������е�
		ArrayList<Integer> exonList=splitList.get(splitList.size()-1);//include one special loc start number to end number	
		if (num >= 0)
		{
			if (replace)
			{
				int tmpLocnumber = exonList.get(num);
				if (Math.abs(locnumber-tmpLocnumber) <= 1) {
					exonList.remove(num);//
				}
				else {
					exonList.add(num,locnumber);
				}
			}
			else {
				exonList.add(num,locnumber);
			}
		}
		//��exon���������е�
		else 
		{
			num = exonList.size()-1;
			if (replace)
			{
				int tmpLocnumber = exonList.get(num);
				if (Math.abs(locnumber-tmpLocnumber) <= 1) {
					exonList.remove(num);//
				}
				else {
					exonList.add(locnumber);
				}
			}
			else {
				exonList.add(locnumber);
			}
		}
		
	}	
	/**
	 * ֱ�����ת¼����֮����addcds()��������ת¼�����exon
	 */
  public void addsplitlist()
  {   /**
       *װ�ص����ɱ���ӵ���Ϣ<br>
       *���е�һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end.ע������������������޹أ���Զ��һ��С�ڵڶ���<br>
       *�ӵ����ſ�ʼ��Exon����Ϣ<br>
       *������ô�Ӷ��Ǵ�С�ӵ���
       */	
  	ArrayList<Integer> exonList=new ArrayList<Integer>();
  	splitList.add(exonList);
  }
  
  
	/**
	 * ˳�򴢴�ͬһ����ͬת¼�������֣���splitList���Ӧ
	 */
	private ArrayList<String> lssplitName=new ArrayList<String>();
	/**
	 * ˳�򴢴�ͬһ����ͬת¼�������֣���splitList���Ӧ
	 */
	public void addSplitName(String splitName) {
		lssplitName.add(splitName);
	}
	
	
	
  /**
   * ����ת¼������Ŀ
    * @return
    */
    public int getSplitlistNumber()
    {  
    	return lssplitName.size();
    }
	
    /**
     * ����ת¼�����Ƶ�List������˳���getExonlist��˳����ͬ
     */
	public ArrayList<String> getLsSplitename() {
		return lssplitName;
	}
	
    /**
     * �������(��0��ʼ����Ų���ת¼���ľ���ID)<br>
     * ����ĳ��ת¼�������е�һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end.ע������������������޹أ���Զ��һ��С�ڵڶ���<br>
     * �ӵ����ʼ��exon����Ϣ��exon�ɶԳ��֣���һ��exon�����Ǹ�ת¼����ת¼��㣬���һ��exon�����Ǹ�ת¼����ת¼�յ�<br>
     * ������ô�Ӷ��Ǵ�С�ӵ���<br>
     */
    public ArrayList<Integer> getExonlist(int splitnum)
    {  
    	return splitList.get(splitnum);//include one special loc start number to end number	
    }
    
    /**
     * ����ת¼����(UCSC��ʵ�����ǻ�����)<br>
     * ����ĳ��ת¼�������е�һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end.ע������������������޹أ���Զ��һ��С�ڵڶ���<br>
     * �ӵ����ʼ��exon����Ϣ��exon�ɶԳ��֣���һ��exon�����Ǹ�ת¼����ת¼��㣬���һ��exon�����Ǹ�ת¼����ת¼�յ�<br>
     * ������ô�Ӷ��Ǵ�С�ӵ���<br>
     */
    public ArrayList<Integer> getExonlist(String splitID)
    {  
    	return splitList.get(lssplitName.indexOf(splitID));//include one special loc start number to end number	
    }
    /**
     * ��øû��������һ��ת¼�����ƺͷ���
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
		int longsplitID = getLongestSplitNum();
		String splitName=lssplitName.get(longsplitID);
		ArrayList<Integer> splitresult=splitList.get(longsplitID);
		result.add(splitName);
		result.add(splitresult);
		return result;
	}
   
	
    /**
     * �������(��0��ʼ����Ų���ת¼���ľ���ID)<br>
     * ����ĳ��ת¼���ķ���	�������Ҫ��UCSC��ʹ�ã�
     * ��ΪUCSC���м��ٲ���һ��������ͬʱ��������ͷ������У���������������ÿ��ת¼���ķ���
     */
    public boolean getCis5to3(int splitnum)
    {  
    	return lsSplitCis5to3.get(splitnum);//include one special loc start number to end number	
    }
    
    /**
     * ����ת¼����(UCSC��ʵ�����ǻ�����)<br>
     * ����ĳ��ת¼���ķ���	�������Ҫ��UCSC��ʹ�ã�
     * ��ΪUCSC���м��ٲ���һ��������ͬʱ��������ͷ������У���������������ÿ��ת¼���ķ���
     */
    public boolean getCis5to3(String splitID)
    {  
    	return lsSplitCis5to3.get(lssplitName.indexOf(splitID));//include one special loc start number to end number	
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
	public boolean getLongestSplitCis5to3() 
	{
		int longsplitID = getLongestSplitNum();
		return lsSplitCis5to3.get(longsplitID);
	}
	
	
	  /**
     * ��øû��������һ��ת¼����ţ��ɸñ���ܹ���splitList�л����Ӧ��ת¼����Ϣ
     * @return ����һ��ArrayList-object
     * ��һ���� String ���գ��Ǹ�ת¼��������<br>
     * �ڶ�����ArrayList-Integer���գ��Ǹ�ת¼���ľ�����Ϣ<br>
     * ���е�һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end.ע������������������޹أ���Զ��һ��С�ڵڶ���<br>
     * �ӵ����ʼ��exon����Ϣ��exon�ɶԳ��֣���һ��exon�����Ǹ�ת¼����ת¼��㣬���һ��exon�����Ǹ�ת¼����ת¼�յ�<br>
     */
	public int getLongestSplitNum() 
	{
		if(splitList.size()==1)
		{
			return 0;
		}
		ArrayList<Integer> lslength=new ArrayList<Integer>();
		for(int i=0;i<splitList.size();i++)
		{
			ArrayList<Integer>  subsplit=splitList.get(i);
			lslength.add(subsplit.get(subsplit.size()-1)-subsplit.get(2));
		}
		int max=lslength.get(0);
		for (int i = 0; i < lslength.size(); i++) {
			if(lslength.get(i)>max)
				max=lslength.get(i);
		}
		return lslength.indexOf(max);
	}
	
	
    /**
     * 
     * ��øû��������һ��ת¼���Ĳ����������Ϣ
     * @param type ָ��Ϊ"Intron","Exon","5UTR","3UTR"
     * @param num ���typeΪ"Intron"��"Exon"��ָ���ڼ���������������򷵻�0
     * @return
     */
	public int getTypeLength(String type,int num)  
	{
		ArrayList<Object>  lstmpSplitInfo=getLongestSplit();
		ArrayList<Integer> lstmpSplit=(ArrayList<Integer>)lstmpSplitInfo.get(1);
		int exonNum=lstmpSplit.size();
		//TODO ���������Ҫ����0
		if (type.equals("Intron")) 
		{
			int IntronLength=0;
			if (cis5to3) //2,3 4,5 6,7 8,9
			{
				IntronLength=lstmpSplit.get(num*2+2)-lstmpSplit.get(num*2+1);
			}
			else 
			{
				IntronLength=lstmpSplit.get(exonNum-num*2)-lstmpSplit.get(exonNum-num*2-1);
			}
			return IntronLength;
		}
		if (type.equals("Exon")) 
		{
			int ExonLength=0;
			if (cis5to3) //2,3 4,5 6,7 8,9
			{
				//ת¼�����յ㶼��������֮��
				//if(lstmpSplit.get(num*2)>=lstmpSplit.get(0)&&lstmpSplit.get(num*2+1)<=lstmpSplit.get(1))
					ExonLength=lstmpSplit.get(num*2+1)-lstmpSplit.get(num*2);
				/**
				//ת¼�����յ㶼��������֮��
				else if (lstmpSplit.get(num*2)<=lstmpSplit.get(0)&&lstmpSplit.get(num*2+1)>lstmpSplit.get(1)) 
					ExonLength=lstmpSplit.get(1)-lstmpSplit.get(0);
				//ת¼������������ڣ��յ�����������
				else if (lstmpSplit.get(num*2)<lstmpSplit.get(0)&&lstmpSplit.get(num*2+1)>lstmpSplit.get(0)&&lstmpSplit.get(num*2+1)<=lstmpSplit.get(1)) 
					ExonLength=lstmpSplit.get(num*2+1)-lstmpSplit.get(0);
				//ת¼������������⣬�յ�����������
				else if (lstmpSplit.get(num*2)>=lstmpSplit.get(0)&&lstmpSplit.get(num*2)<lstmpSplit.get(1)&&lstmpSplit.get(num*2+1)>=lstmpSplit.get(1)) 
					ExonLength=lstmpSplit.get(1)-lstmpSplit.get(num*2);
				*/
			}
			else //2,3 4,5 6,7 8,9
			{
				//ת¼�����յ㶼��������֮��
				//if(lstmpSplit.get(exonNum-num*2)>=lstmpSplit.get(0)&&lstmpSplit.get(exonNum-num*2+1)<=lstmpSplit.get(1))
					ExonLength=lstmpSplit.get(exonNum-num*2+1)-lstmpSplit.get(exonNum-num*2);
			/**
					//ת¼�����յ㶼��������֮��
				else if (lstmpSplit.get(exonNum-num*2)<=lstmpSplit.get(0)&&lstmpSplit.get(exonNum-num*2+1)>lstmpSplit.get(1)) 
					ExonLength=lstmpSplit.get(1)-lstmpSplit.get(0);
				//ת¼������������⣬�յ�����������
				else if (lstmpSplit.get(exonNum-num*2)<lstmpSplit.get(0)&&lstmpSplit.get(exonNum-num*2+1)>lstmpSplit.get(0)&&lstmpSplit.get(exonNum-num*2+1)<=lstmpSplit.get(1)) 
					ExonLength=lstmpSplit.get(exonNum-num*2+1)-lstmpSplit.get(0);
				//ת¼������������ڣ��յ�����������
				else if (lstmpSplit.get(exonNum-num*2)>=lstmpSplit.get(0)&&lstmpSplit.get(exonNum-num*2)<lstmpSplit.get(1)&&lstmpSplit.get(exonNum-num*2+1)>=lstmpSplit.get(1)) 
					ExonLength=lstmpSplit.get(1)-lstmpSplit.get(exonNum-num*2);
			*/
			}
			return ExonLength;
		}
		if (type.equals("5UTR")) 
		{
			int FUTR=0;
			if (cis5to3) //2,3 4,5 6,7 8,9
			{	
				FUTR=lstmpSplit.get(2)-numberstart;
				for (int i = 3; i <exonNum; i=i+2) 
				{
					if(lstmpSplit.get(i)<=lstmpSplit.get(0))
						FUTR=FUTR+(lstmpSplit.get(i)-lstmpSplit.get(i-1));
					else if (lstmpSplit.get(i-1)<=lstmpSplit.get(0)&&lstmpSplit.get(i)>lstmpSplit.get(0))
						FUTR=FUTR+lstmpSplit.get(0)-lstmpSplit.get(i-1);
					else if (lstmpSplit.get(i-1)>lstmpSplit.get(0)) 
						break;
				}
			}
			else 
			{
				FUTR=numberend-lstmpSplit.get(exonNum-1);
				for (int i = exonNum-2; i >=2; i=i-2) 
				{
					if(lstmpSplit.get(i)>=lstmpSplit.get(1))
						FUTR=FUTR+(lstmpSplit.get(i+1)-lstmpSplit.get(i));
					else if (lstmpSplit.get(i)<lstmpSplit.get(1)&&lstmpSplit.get(i+1)>=lstmpSplit.get(1))
						FUTR=FUTR+lstmpSplit.get(i+1)-lstmpSplit.get(1);
					else if (lstmpSplit.get(i+1)<lstmpSplit.get(1))
						break;
				}
			}
			return FUTR;
		}
		if (type.equals("3UTR")) 
		{
			int TUTR=0;
			if (cis5to3) //2,3 4,5 6,7 8,9
			{	
				TUTR=numberend-lstmpSplit.get(exonNum-1);
				for (int i = exonNum-2; i >=2; i=i-2) 
				{
					if(lstmpSplit.get(i)>=lstmpSplit.get(1))
						TUTR=TUTR+(lstmpSplit.get(i+1)-lstmpSplit.get(i));
					else if (lstmpSplit.get(i)<lstmpSplit.get(1)&&lstmpSplit.get(i+1)>=lstmpSplit.get(1))
						TUTR=TUTR+lstmpSplit.get(i+1)-lstmpSplit.get(1);
					else if (lstmpSplit.get(i+1)<lstmpSplit.get(1))
						break;
				}
			}
			else 
			{
				TUTR=lstmpSplit.get(2)-numberstart;
				for (int i = 3; i <exonNum; i=i+2) {
					if(lstmpSplit.get(i)<=lstmpSplit.get(0))
						TUTR=TUTR+(lstmpSplit.get(i)-lstmpSplit.get(i-1));
					else if (lstmpSplit.get(i-1)<=lstmpSplit.get(0)&&lstmpSplit.get(i)>lstmpSplit.get(0))
						TUTR=TUTR+lstmpSplit.get(0)-lstmpSplit.get(i-1);
					else if (lstmpSplit.get(i-1)>lstmpSplit.get(0)) 
						break;
				}
			}
			return TUTR;
		}

		return -1000000;
	}
	


}
