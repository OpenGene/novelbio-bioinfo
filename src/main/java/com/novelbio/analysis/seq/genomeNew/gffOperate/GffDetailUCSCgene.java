package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodInfoUCSCgene;
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
public class GffDetailUCSCgene extends GffDetailAbs
{


	  
	/**
	 * ˳�򴢴�ͬһ����ͬת¼�������֣���splitList���Ӧ
	 */
	private ArrayList<String> lsIsoName=new ArrayList<String>();
	/**
	 * ˳�򴢴�ͬһ����Ĳ�ͬת¼�����꣬��Ӧת¼�����ֱ�����splitName��
	 */
	private ArrayList<ArrayList<Integer>> lsIsoform=new ArrayList<ArrayList<Integer>>();//�洢�ɱ���ӵ�mRNA
	/**
	 * ˳��洢ÿ��ת¼���ķ��������Ҫ���ˣ���Ϊ�����һ�������ת¼��������µ�gene��ȥ
	 */
	private ArrayList<Boolean> lsSplitCis5to3 = new ArrayList<Boolean>();


	public GffDetailUCSCgene(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
		// TODO Auto-generated constructor stub
	}
	/**
	 * ָ�����һ��ת¼���ķ���
	 * �����Ҫ��UCSC��ʹ�ã���ΪUCSC���м��ٲ���һ��������ͬʱ��������ͷ������У���������������ÿ��ת¼���ķ���
	 */
	protected void addCis5to3(boolean cis5to3)
	{
		lsSplitCis5to3.add(cis5to3);
	}
	
	/**
	 * �����һ��ת¼�����exon���꣬����exonList�ĵ�һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end.<br>
	 * ע������������������޹أ���Զ��һ��С�ڵڶ���<br>
     *�ӵ����ſ�ʼ��Exon����Ϣ<br>
     *������ô�Ӷ��Ǵ�С�ӵ���
	 */
	protected void addExon(int locnumber)
	{
		ArrayList<Integer> exonList=lsIsoform.get(lsIsoform.size()-1);//include one special loc start number to end number	
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
		ArrayList<Integer> exonList=lsIsoform.get(lsIsoform.size()-1);//include one special loc start number to end number	
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
  	lsIsoform.add(exonList);
  }
	/**
	 * ˳�򴢴�ͬһ����ͬת¼�������֣���splitList���Ӧ
	 */
	public void addSplitName(String splitName) {
		lsIsoName.add(splitName);
	}
	/**
   * ����ת¼������Ŀ
    * @return
    */
    public int getSplitlistNumber()
    {  
    	return lsIsoName.size();
    }
	
    /**
     * ����ת¼�����Ƶ�List������˳���getExonlist��˳����ͬ
     */
	public ArrayList<String> getLsSplitename() {
		return lsIsoName;
	}
	
    /**
     * �������(��0��ʼ����Ų���ת¼���ľ���ID)<br>
     * ����ĳ��ת¼�������е�һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end.ע������������������޹أ���Զ��һ��С�ڵڶ���<br>
     * �ӵ����ʼ��exon����Ϣ��exon�ɶԳ��֣���һ��exon�����Ǹ�ת¼����ת¼��㣬���һ��exon�����Ǹ�ת¼����ת¼�յ�<br>
     * ������ô�Ӷ��Ǵ�С�ӵ���<br>
     */
    public ArrayList<Integer> getExonlist(int splitnum)
    {  
    	return lsIsoform.get(splitnum);//include one special loc start number to end number	
    }
    
    /**
     * ����ת¼����(UCSC��ʵ�����ǻ�����)<br>
     * ����ĳ��ת¼�������е�һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end.ע������������������޹أ���Զ��һ��С�ڵڶ���<br>
     * �ӵ����ʼ��exon����Ϣ��exon�ɶԳ��֣���һ��exon�����Ǹ�ת¼����ת¼��㣬���һ��exon�����Ǹ�ת¼����ת¼�յ�<br>
     * ������ô�Ӷ��Ǵ�С�ӵ���<br>
     */
    public ArrayList<Integer> getExonlist(String splitID)
    {  
    	return lsIsoform.get(lsIsoName.indexOf(splitID));//include one special loc start number to end number	
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
		String splitName=lsIsoName.get(longsplitID);
		ArrayList<Integer> splitresult=lsIsoform.get(longsplitID);
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
    	return lsSplitCis5to3.get(lsIsoName.indexOf(splitID));//include one special loc start number to end number	
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
		if(lsIsoform.size()==1)
		{
			return 0;
		}
		ArrayList<Integer> lslength=new ArrayList<Integer>();
		for(int i=0;i<lsIsoform.size();i++)
		{
			ArrayList<Integer>  subsplit=lsIsoform.get(i);
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
	///////////////////// �� coord ��ص����Ժͷ��� ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ˳��洢ÿ��ת¼���ĵ��������
	 */
	ArrayList<CodIsoInfo> lsCodIsoInfos = new ArrayList<CodIsoInfo>();
	/**
	 * ����ÿ��ת¼�����������������涨λ����������������lsCodIsoInfos��
	 */
	private void searchCoordInSplice()
	{
		if (!lsCodIsoInfos.isEmpty()) {
			return;
		}
	}
	
}
/**
 * �����ڵ���ת¼���еĶ�λ���
 * @author zong0jie
 *
 */
abstract class CodIsoInfo
{
	/**
	 * ���codInExon������������
	 */
	public static final int COD_LOC_EXON = 100;
	/**
	 * ���codInExon�����ں�����
	 */
	public static final int COD_LOC_INTRON = 200;
	/**
	 * ���codInExon����ת¼����
	 */
	public static final int COD_LOC_OUT = 300;
	/**
	 * ���codInExon����5UTR��
	 */
	public static final int COD_LOCUTR_5UTR = 5000;
	/**
	 * ���codInExon����3UTR��
	 */
	public static final int COD_LOCUTR_3UTR = 3000;
	/**
	 * ���codInExon����UTR��
	 */
	public static final int COD_LOCUTR_OUT = 0;
	
	public CodIsoInfo(String IsoName, ArrayList<Integer> lsIsoform) {
		this.IsoName = IsoName;
		this.lsIsoform = lsIsoform;
	}
	/**
	 * ת¼��������
	 */
	protected String IsoName = "";
	/**
	 * ����
	 */
	protected int coord = -100;
	/**
	 * ת¼������
	 */
	protected boolean cis5to3;
	/**
	 * ת¼��������Ϣ
	 */
	protected ArrayList<Integer> lsIsoform;
	
	/**
	 * ���굽��ת¼�����ľ��룬����������
	 * �������������Ϊ����������Ϊ����
	 */
	protected int cod2start = -1000000000;
	/**
	 * ���굽��ת¼���յ�ľ��룬����������
	 * �������յ�����Ϊ����������Ϊ����
	 */
	protected int cod2end = -1000000000;
	/**
	 * �����ڵڼ��������ӻ��ں����У�������ھ�Ϊ����
	 */
	protected int exIntronNum = -1;
	/**
	 * �����������ӡ��ں��ӻ����ڸ�ת¼����
	 * ��codLocExon��codLocIntron�Ƚϼ���
	 */
	protected int codLoc = 0;
	
	/**
	 * ������5UTR��3UTR���ǲ���
	 */
	protected int codLocUTR = 0;
	/**
	 * ʹ��ǰ���ж���UTR��
	 * ���������UTR�У��������UTR����㣬ע�������ȥ���ں���
	 * ��ȥ���ں��ӵ�ֱ����cod2start/cod2cdsEnd
	 */
	protected int UTRstart = -100000000;
	/**
	 * ʹ��ǰ���ж���UTR��
	 * ���������UTR�У��������UTR���յ㣬ע�������ȥ���ں���
	 * ��ȥ���ں��ӵ�ֱ����cod2atg/cod2End
	 */
	protected int UTRend = -100000000;
	/**
	 * ���������������/�ں����У�
	 * �������������/�ں������ľ���
	 * ��Ϊ����
	 */
	protected int cod2ExInStart = -1000000000;
	/**
	 * ���������������/�ں����У�
	 * �������������/�ں����յ�ľ���
	 * ��Ϊ����
	 */
	protected int cod2ExInEnd = -1000000000;
	/**
	 * �������ATG�ľ���
	 */
	protected int cod2ATG = -1000000000;
	/**
	 * ������CDSend�ľ���
	 */
	protected int cod2cdsEnd = -1000000000;
	/**
	 * ��ת¼���ĳ���
	 */
	protected int lengthIso = -100;
	
	public void setCoord(int coord) {
		this.coord = coord;
	}
	/**
	 * ��ת¼�����ĸ�λ��
	 * ��COD_LOC_EXON��COD_LOC_INTRON��COD_LOC_OUT����
	 * @return
	 */
	public int getCodLoc() {
		return codLoc;
	}
	/**
	 * ��ת¼�����ĸ�λ��
	 * ��COD_LOC_EXON��COD_LOC_INTRON��COD_LOC_OUT����
	 * @return
	 */
	public int getCodLocUTR() {
		return codLoc;
	}
	/**
	 * ���굽��ת¼�����ľ��룬����������
	 * @return
	 */
	public int getCod2IsoStart() {
		return cod2start;
	}
	/**
	 * ���굽��ת¼���յ�ľ��룬����������
	 * @return
	 */
	public int getCod2IsoEnd() {
		return cod2end;
	}
	/**
	 * �����ڵڼ��������ӻ��ں����У�������ھ�Ϊ����
	 * @return
	 */
	public int getExInNum() {
		return exIntronNum;
	}
	/**
	 * ���굽��������/�ں������ľ��룬����������
	 * @return
	 */
	public int getCod2ExInStart() {
		return cod2ExInStart;
	}
	/**
	 * ���굽��������/�ں����յ�ľ��룬����������
	 * @return
	 */
	public int getCod2ExInEnd() {
		return cod2ExInEnd;
	}
	/**
	 * ���굽ATG�ľ��룬����������
	 * @return
	 */
	public int getCod2ATG() {
		return cod2ATG;
	}
	/**
	 * ʹ��ǰ���ж���UTR��
	 * ���������UTR�У��������UTR����㣬ע�������ȥ���ں���
	 * ��ȥ���ں��ӵ�ֱ����cod2start/cod2cdsEnd
	 */
	public int getCod2UTRstart() {
		return UTRstart;
	}
	/**
	 * ʹ��ǰ���ж���UTR��
	 * ���������UTR�У��������UTR���յ㣬ע�������ȥ���ں���
	 * ��ȥ���ں��ӵ�ֱ����cod2atg/cod2End
	 */
	public int getCod2UTRend() {
		return UTRend;
	}
	public abstract void searchCoord();
	
}
/**
 *   tss>---coord-----atg--------->-----------tes---->
 * @author zong0jie
 *
 */
class CodIsoInfoCis extends CodIsoInfo
{
	private static final Logger logger = Logger.getLogger(CodIsoInfoCis.class);
	public CodIsoInfoCis(String IsoName, ArrayList<Integer> lsIsoform) {
		super(IsoName, lsIsoform);
		// TODO Auto-generated constructor stub
	}
	/**
	 * ��Ҫ���һ��
	 */
	@Override
	public void searchCoord()
	{
		  /**
	     * �������(��0��ʼ����Ų���ת¼���ľ���ID)<br>
	     * ����ĳ��ת¼�������е�һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end.ע������������������޹أ���Զ��һ��С�ڵڶ���<br>
	     * �ӵ����ʼ��exon����Ϣ��exon�ɶԳ��֣���һ��exon�����Ǹ�ת¼����ת¼��㣬���һ��exon�����Ǹ�ת¼����ת¼�յ�<br>
	     * ������ô�Ӷ��Ǵ�С�ӵ���<br>
	     */
		if (    coord < lsIsoform.get(2) || 
				coord > lsIsoform.get(lsIsoform.size()-1)  	)
		{
			codLoc = COD_LOC_OUT;
		}
		cod2ATG = coord - lsIsoform.get(0);
		cod2cdsEnd = coord - lsIsoform.get(1);
		cod2start = coord - lsIsoform.get(2);
		cod2end = coord - lsIsoform.get(lsIsoform.size() - 1);
		boolean flag=false; //false��ʾcoord�����һ�������ӵ����һλ����Ҳ���������ѭ��û���ܽ�flag����Ϊtrue,�Ǿ���3UTR��������Ϊ��������
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (codLoc == COD_LOC_OUT) {
			return;
		}
		for(int j=2; j<lsIsoform.size(); j++)  //һ��һ��Exon�ļ��
		{
			if(coord<lsIsoform.get(j) && j%2==0)//��������֮ǰ���ں����У���������Ϊ�� 2,3  4,5  6,7  8,9   0��ת¼����ת¼��㣬1��ת¼����ת¼�յ�
			{
				flag=true;
				//���²���
			   if(j==2)//��5��UTR��,Ҳ��������������   tss cod ��2 3 �� 4 5 �� 6 7 �� 8 9 
			   {
				   logger.error("coord is out of the isoform, but the codLoc is: "+codLoc+" coord: "+ coord + IsoName);
			   }
			   else // ���ں�����
			   {
				   // tss ��2 3 �� 4 5 ��cod 6 7 �� 8 9
				   codLoc = COD_LOC_INTRON;
				   exIntronNum = j / 2 - 1;// �ڵ�j/2-1���ں�����
				   cod2ExInEnd = lsIsoform.get(j) - coord;// ���һ��������
				   cod2ExInStart = coord - lsIsoform.get(j - 1);// ��ǰһ��������
				   break; // ������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��
			   }
			}
			else if(coord <= lsIsoform.get(j) && j%2 == 1) //��������֮�У�������Ϊ��2,3  4,5  6,7  8,9   0��ת¼����ת¼��㣬1��ת¼����ת¼�յ�
			{
				flag=true;
				codLoc = COD_LOC_EXON;
				exIntronNum = (j-1)/2;//�ڵ�(j-1)/2����������
				cod2ExInEnd = lsIsoform.get(j) - coord;//���뱾��������ֹ
				cod2ExInStart = coord - lsIsoform.get(j-1);//���뱾��������ʼ
				if(coord<lsIsoform.get(0))//����С��atg����5��UTR��,Ҳ������������
				{
					codLocUTR = COD_LOCUTR_5UTR;
					UTRstart=0;UTRend=0;
					// tss  2 3,   4 5,   6 cod 7,   8 0 9
					for (int k = 3; k <= j-2; k=k+2) {
						UTRstart = UTRstart + lsIsoform.get(k) - lsIsoform.get(k-1);
					}
					UTRstart = UTRstart + cod2ExInStart;
					// tss  2 3,   4 5,   6 cod  0 7
					if (lsIsoform.get(0) <= lsIsoform.get(j)) //һ��ҪС�ڵ���
					{
						UTRend = lsIsoform.get(0) - coord;
					}
					// tss  2 3,   4 5,   6 cod 7,   8  9,   10 0 11
					else 
					{
						UTRend = lsIsoform.get(j) - coord;
						int m = j+2;
						while (m < lsIsoform.size() && lsIsoform.get(0) > lsIsoform.get(m)) 
						{
							UTRend = UTRend + lsIsoform.get(m) - lsIsoform.get(m-1);
							m=m+2;
						}
						UTRend = UTRend + lsIsoform.get(0) - lsIsoform.get(m-1);
					}
					break;//����
				}
				// tss  2 3,   4 0 5,   6 1 7,   8  9,   10 11
				if(coord > lsIsoform.get(1))//����cds��ʼ������3��UTR��
				{
					codLocUTR = COD_LOCUTR_3UTR; UTRstart=0;UTRend=0;
					// tss  2 3,   4 0 5,   6 1 cod 7,   8  9,   10 11
					if (lsIsoform.get(1)>=lsIsoform.get(j-1))//һ��Ҫ���ڵ��� 
					{
						UTRstart=coord-lsIsoform.get(1);
					}
					// tss  2 3,   4 0 5,   6 1 7,   8  9,   10 cod 11
					else 
					{
						UTRstart=coord-lsIsoform.get(j-1);
						int m=j-3;
						while (m>=2&&lsIsoform.get(m)>lsIsoform.get(1)) 
						{
							UTRstart=UTRstart+lsIsoform.get(m+1)-lsIsoform.get(m);
							m=m-2;
						}
						UTRstart=UTRstart+lsIsoform.get(m+1)-lsIsoform.get(1);
					}
					/////////////////////utrend//////////////////
					// tss  2 3,   4 0 5,   6 1 7,   8  cod 9,   10 11,  12 13
					for (int k = lsIsoform.size() - 1; k >= j+2; k=k-2) {
						UTRend=UTRend+lsIsoform.get(k)-lsIsoform.get(k-1);
					}
					UTRend=UTRend+cod2ExInEnd;
					break;//����
				}
				break;//������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��
			}
		}
		if (flag == false)//�����һ�������ӵ����һλ����Ҳ���������ѭ��û���ܽ�flag����Ϊtrue,�Ǿ���3UTR��������Ϊ��������
		{
			logger.error("coord is out of the isoform, but the codLoc is: "+codLoc+" coord: "+ coord + IsoName);
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
}

class CodIsoInfoTrans extends CodIsoInfo
{

	public CodIsoInfoTrans(String IsoName, ArrayList<Integer> lsIsoform) {
		super(IsoName, lsIsoform);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void searchCoord() {
		// TODO Auto-generated method stub
		
	}
	


}