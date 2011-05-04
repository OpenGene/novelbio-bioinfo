package com.novelBio.base.genome.gffOperate;

import java.util.ArrayList;



/**
 * UCSC konwn gene�Ļ���������Ϣ
 * @author zong0jie
 *
 */
public class GffCodInfoUCSCgene extends GffCodInfo
{
	/**
	 * �������굽�ת¼����ATG�ľ���
	 * ������ATG���Σ�Ϊ����
	 * ������ATG���Σ�Ϊ����
	 * 0: ���굽������/�ϸ����� ATG����
	 * 1: ��������ڻ����:���굽�¸�����ATG�ľ���.����Ϊ-1000000000
	 * @return
	 */
	public int[] codToATG=new int[2];
	
	
	
	/**
	 * ��Coordinate�ڻ�����ʱ��
	 * ��list����װ��Coordinate�Ĳ�ͬת¼��λ����Ϣ
	 * @list��Ϊint[7]����
     * 0���������ھ���λ�� 1..������ 2.�ں��� <br/>
     * 1: �û����ں���/�����ӵ�λ�á�ע��UCSC������������ں��ӣ�����ʱ5UTR��3UTRҲ���������ӡ� <br/>
	 * 2�������ں���/������ ��������<br/>
	 * 3�������ں���/�����ӵ��յ����<br/>
	 * 4������������ӣ������Ƿ���5UTR��3UTR�ڣ�0: ����  5:5UTR    3:3UTR<br/>
	 * 5�������UTR�����ڣ�5��UTRΪ��gene�����룬  3��UTRΪ�����������룬�������ں��Ӽ�����롣������ڣ���Ϊ-1<br/>
	 * 6�������UTR�����ڣ�5��UTRΪ��ATG�� 3��UTRΪ��geneβ�����룬�������ں��Ӽ�����롣������ڣ���Ϊ-1<br/>
	 */
	public ArrayList<int[]> GeneInfo=new ArrayList<int[]>();	
	
	/**
	 * ��Coordinate�ڻ�����ʱ��
	 * ��list����װ��Coordinate�Ĳ�ͬת¼��������,������GeneInfoһһ��Ӧ
	 */
	public ArrayList<String> GeneID=new ArrayList<String>();
	/**
	 * ��������ڻ�����
	 * ��������ڲ�ͬת¼���е�����
	 * @param splitID ת¼������
	 * @param position �������ڴ��λ�� 1.������ 2.�ں���
	 * @param ExIntronnum �û����ں���/�����ӵ�λ�ã�ע��UCSC������������ں��ӣ�����ʱ5UTR��3UTRҲ���������ӡ�
	 * @param start �����ں���/������ �������룬5��UTRΪ��gene�����룬  3��UTRΪ������������
	 * @param end �����ں���/�����ӵ��յ���룬��5��UTRΪ��ATG�� 3��UTRΪ��geneβ������
	 * @param UTRinfo ����������ӣ������Ƿ���5UTR��3UTR�ڣ�0: ����  5:5UTR    3:3UTR
	 * @param UTRstart �����UTR�����ڣ�5��UTRΪ��gene������,3��UTRΪ�����������룬�������ں��Ӽ�����롣������ڣ���Ϊ-1
	 * @param UTRend �����UTR�����ڣ�5��UTRΪ��ATG�� 3��UTRΪ��geneβ�����룬�������ں��Ӽ�����롣������ڣ���Ϊ-1
	 * <br>
	 * 
	 * @���װ���int[7]����
     * 0���������ھ���λ�� 1..������ 2.�ں��� <br/>
     * 1: �û����ں���/�����ӵ�λ�á�ע��UCSC������������ں��ӣ�����ʱ������5UTR��3UTR�� <br/>
	 * 2�������ں���/������ ��������<br/>
	 * 3�������ں���/�����ӵ��յ����<br/>
	 * 4������������ӣ������Ƿ���5UTR��3UTR�ڣ�0: ����  5:5UTR    3:3UTR<br/>
	 * 5�������UTR�����ڣ�5��UTRΪ��gene�����룬  3��UTRΪ�����������룬�������ں��Ӽ�����롣������ڣ���Ϊ-1<br/>
	 * 6�������UTR�����ڣ�5��UTRΪ��ATG�� 3��UTRΪ��geneβ�����룬�������ں��Ӽ�����롣������ڣ���Ϊ-1<br/>
	 */
    public void addingeneinfo(String splitID, int position ,int ExIntronnum,int start,int end,int UTRinfo,int UTRstart, int UTRend)
    {   int[] CordtGeneInfo=new int[7] ;//װ�ص����ɱ���ӵ���Ϣ,����ֻ��cds����Ϣ��������GffHash��װ��
        
    	GeneID.add(splitID);//ת¼�����
		CordtGeneInfo[0]=position;//�������ڴ��λ�� 1. 5��UTR 2.������ 3.�ں��� 4. 3��UTR  
        CordtGeneInfo[1]=ExIntronnum;//�û����ں���/�����ӵ�λ�� ��5��UTRΪ-1��3��UTRΪ-2
        CordtGeneInfo[2]=start;//�����ں���/������ �������룬5��UTRΪ��gene�����룬 3��UTRΪ�����һ��CDS����
        CordtGeneInfo[3]=end;//�����ں���/�����ӵ��յ���룬��5��UTRΪ��ATG�� 3��UTRΪ��geneβ������
        CordtGeneInfo[4]=UTRinfo;
        CordtGeneInfo[5]=UTRstart;
        CordtGeneInfo[6]=UTRend;
        
        
        GeneInfo.add(CordtGeneInfo);//װ��list
    }
}
