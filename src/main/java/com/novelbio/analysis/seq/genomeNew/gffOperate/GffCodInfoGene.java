package com.novelbio.analysis.seq.genomeNew.gffOperate;
 
import java.util.ArrayList;
 

 

/**
 * ��������������ҵ��������Ϣ����GffSearch�����
 * @author Zong jie
 *
 */
public class GffCodInfoGene extends GffCodAbs
{
	GffCodInfoGene(String chrID, int Coordinate) {
		super(chrID, Coordinate, gffHash);
		// TODO Auto-generated constructor stub
	}


	/**
	 * ��Coordinate�ڻ����ʱ��
	 * ��list����װ��Coordinate���һ������ͬת¼���������յ�ľ���
	 * @��������Ϊint[2]
	 * �������λ�ں�һ�������ͷ��<br/>
	 * 0: ת¼�����<br/>
	 * 1: �ͺ�һ������ATG����<br/>
	 * �������λ�ں�һ�������β����<br/>
	 * 0��-1<br/>
	 * 1���ͺ�һ������β���ľ���<br/>
	 */
	public ArrayList<int[]> enddistance=new ArrayList<int[]>();	

	/**
	 * ��������ڻ����,
	 * ����������һ������ľ���
	 * @int[2]��һ������
	 * 0��ת¼�����<br/>
	 * 1���ͺ�һ������ľ��룬����ں����ͷ��endcis5to3=true������Ϊ����ATGλ�ã����ں����β����endcis5to3=false������Ϊ��������յ�λ��
	 */
	public void addenddistance(int splitID,int distanceend)
	    {   int[] distance=new int[2] ;//װ�ص����ɱ���ӵ���Ϣ,����ֻ��cds����Ϣ��������GffHash��װ��
	        
	        distance[0]=splitID;//ת¼�����
	        distance[1]=distanceend;
	        enddistance.add(distance);
	    }

	
	/**
	 * ��Coordinate�ڻ����ʱ��
	 * ��list����װ��Coordinate��ǰһ������ͬת¼���������յ�ľ���
	 * ��������Ϊint[2]<br/>
	 * @�������λ��ǰһ�������ͷ��<br/>
	 * 0: ת¼�����<br/>
	 * 1: ��ǰһ������ATG����<br/>
	 * @�������λ��ǰһ�������β����<br/>
	 * 0��-1<br/>
	 * 1����ǰһ������β���ľ���<br/>
	 */
	public ArrayList<int[]> begindistance=new ArrayList<int[]>();	
	 
	/**
	 * ��������ڻ����
	 * ���������ǰһ������ľ���
	 * int[2]��һ������
	 * 0��ת¼�����
	 * 1����ǰһ������ľ��룬�����ǰ����ͷ��begincis5to3=true������Ϊ����ATGλ�ã�����ǰ����β����begincis5to3=false������Ϊ��������յ�λ��
	 */
	public void addbegindistance(int splitID,int distancebegin)
	    {   int[] distance=new int[2] ;//װ�ص����ɱ���ӵ���Ϣ,����ֻ��cds����Ϣ��������GffHash��װ��
	        
	        distance[0]=splitID;//ת¼�����
	        distance[1]=distancebegin;
	        begindistance.add(distance);
	    }
	
	
	
	
	/**
	 * ��Coordinate�ڻ�����ʱ��
	 * ��list����װ��Coordinate�Ĳ�ͬת¼��λ����Ϣ
	 * @list��Ϊint[5]����
     * 0��ת¼�����<br/>
     * 1���������ھ���λ�� 1. 5��UTR 2.������ 3.�ں��� 4. 3��UTR<br/>
     * 2: �û����ں���/�����ӵ�λ�� ��5��UTRΪ-1��3��UTRΪ-2<br/>
	 * 3�������ں���/������ �������룬5��UTRΪ��gene�����룬 3��UTRΪ�����һ��CDS����<br/>
	 * 4�������ں���/�����ӵ��յ���룬��5��UTRΪ��ATG�� 3��UTRΪ��geneβ������<br/>
	 */
	public ArrayList<int[]> GeneInfo=new ArrayList<int[]>();	
	
	/**
	 * ��������ڻ�����
	 * ��������ڲ�ͬת¼���е�����
	 * @param splitID:ת¼����Ŀ
	 * @param position���������ڴ��λ�� 1. 5��UTR 2.������ 3.�ں��� 4. 3��UTR 
	 * @param ExIntronnum: �û����ں���/�����ӵ�λ�� ��5��UTRΪ-1��3��UTRΪ-2
	 * @param start:�����ں���/������ �������룬5��UTRΪ��gene�����룬 3��UTRΪ�����һ��CDS����
	 * @param end:�����ں���/�����ӵ��յ���룬��5��UTRΪ��ATG�� 3��UTRΪ��geneβ������
	 * <br>
	 * 
	 * @���װ���int[5]����
     * 0��ת¼�����<br>
     * 1���������ھ���λ�� 1. 5��UTR 2.������ 3.�ں��� 4. 3��UTR  <br>
     * 2: �û����ں���/�����ӵ�λ�� ��5��UTRΪ-1��3��UTRΪ-2<br>
	 * 3�������ں���/������ �������룬5��UTRΪ��gene�����룬 3��UTRΪ�����һ��CDS����<br>
	 * 4�������ں���/�����ӵ��յ���룬��5��UTRΪ��ATG�� 3��UTRΪ��geneβ������
	 */
    public void addingeneinfo(int splitID, int position ,int ExIntronnum,int start,int end)
    {   int[] CordtGeneInfo=new int[5] ;//װ�ص����ɱ���ӵ���Ϣ,����ֻ��cds����Ϣ��������GffHash��װ��
        
        CordtGeneInfo[0]=splitID;//ת¼�����
		CordtGeneInfo[1]=position;//�������ڴ��λ�� 1. 5��UTR 2.������ 3.�ں��� 4. 3��UTR  
        CordtGeneInfo[2]=ExIntronnum;//�û����ں���/�����ӵ�λ�� ��5��UTRΪ-1��3��UTRΪ-2
        CordtGeneInfo[3]=start;//�����ں���/������ �������룬5��UTRΪ��gene�����룬 3��UTRΪ�����һ��CDS����
        CordtGeneInfo[4]=end;//�����ں���/�����ӵ��յ���룬��5��UTRΪ��ATG�� 3��UTRΪ��geneβ������
        GeneInfo.add(CordtGeneInfo);//װ��list
    }

	@Override
	protected void SearchLOCinside(ArrayList<GffDetailAbs> loclist, int i, int j) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void SearchLOCoutside(ArrayList<GffDetailAbs> loclist, int i, int j) {
		// TODO Auto-generated method stub
		
	}
}
