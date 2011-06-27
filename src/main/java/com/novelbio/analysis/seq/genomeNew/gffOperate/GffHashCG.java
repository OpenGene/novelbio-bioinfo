package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.TxtReadandWrite;



/**
 * ���UCSC��CpG��Gff����Ŀ��Ϣ,�������ʵ��������ʹ��<br/>
 * ����Gff�ļ��������������ϣ���һ��list��,
 * �ṹ���£�<br/>
 * 1.hash��ChrID��--ChrList--GeneInforList(GffDetail��)<br/>
 *   ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID
 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br/>
 * 
 * 2.hash��LOCID��--GeneInforlist������LOCID�������Ļ�����,�����ж���Ϊ��107_chr1_CpG: 128_36568608 <br/>
	 * ����Ϊ��#bin_chrom_name_chromStart
 * 
 * 3.list��LOCID��--LOCList����˳�򱣴�LOCID<br/>
 * 
 * ÿ�����������յ��CDS������յ㱣����GffDetailList����<br/>
 */
public class GffHashCG extends GffHash
{	
	public GffHashCG(String gfffilename) throws Exception {
		super(gfffilename);
	}

	/**
	 * ��ײ��ȡgff�ķ���<br>
	 * ����Gff�ļ��������������ϣ���һ��list��,
	 * �ṹ���£�<br/>
	 * @3.LOCIDList
	 * ��LOCID��--LOCIDList����˳�򱣴�LOCID<br/>
	 ** <b>1.Chrhash</b><br>
     * ��ChrID��--ChrList-- GeneInforList(GffDetail��,,ʵ����GffDetailCG����)
     * ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID, chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
     * ��LOCID��--GffDetail������LOCID����������Ŀ���,,�����ж���Ϊ��107_chr1_CpG: 128_36568608 <br/>
     *  * ����Ϊ��#bin_chrom_name_chromStart<br>
     *  <b>3.LOCIDList</b><br>
     * ��LOCID��--LOCIDList����˳�򱣴�LOCID<br>
     * <b>LOCChrHashIDList </b><br>
     *  LOCChrHashIDList�б���LOCID����������Ŀ���,��Chrhash�������һ��<br>
     *
	 * @throws Exception 
	 */
	public void ReadGffarray(String gfffilename) throws Exception 
	{
		  //ʵ����������
		   locHashtable =new Hashtable<String, GffDetailAbs>();//�洢ÿ��LOCID���������Ϣ�Ķ��ձ�
		   Chrhash = new Hashtable<String, ArrayList<GffDetailAbs>>();//һ����ϣ�����洢ÿ��Ⱦɫ��
		   LOCIDList = new ArrayList<String>();//˳��洢ÿ������ţ��������������ȡ��������
		   LOCChrHashIDList = new ArrayList<String>();//˳��洢ChrHash�е�ID���������ChrHash��ʵ�ʴ洢��ID
		   //Ϊ���ļ���׼��
		   TxtReadandWrite txtgff=new TxtReadandWrite();
		   txtgff.setParameter(gfffilename, false,true);
		   BufferedReader reader=txtgff.readfile();//open gff file
	       
		   String[] ss = null;//�洢�ָ��������ʱ����
		   String content="";
		   //��ʱ����
		   ArrayList<GffDetailAbs> LOCList=null ;//˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
		   String chrnametmpString=""; //Ⱦɫ�����ʱ����
		   
		   reader.readLine();//������һ��
		   while((content=reader.readLine())!=null)//������β
		   {
			   ss=content.split("\t");
			   chrnametmpString=ss[1].toLowerCase();//Сд
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			    //�µ�Ⱦɫ��
			   if (!Chrhash.containsKey(chrnametmpString)) //�µ�Ⱦɫ��
			   {
				   if(LOCList!=null)//����Ѿ�������LOCList��Ҳ����ǰһ��LOCList����ô�Ƚض̣�Ȼ��������gffGCtmpDetail.numberstart����
				   {
					   LOCList.trimToSize();
					   //���ռ���һ��list/array����ķ������ܼ�����
					   Collections.sort(LOCList,new Comparator<GffDetailAbs>(){
				            public int compare(GffDetailAbs arg0, GffDetailAbs arg1) {
				                int Compareresult;
				            	if(arg0.numberstart<arg1.numberstart)
				            		Compareresult=-1;
				            	else if (arg0.numberstart==arg1.numberstart) 
				            		Compareresult=0;
				            	else 
				            		Compareresult=1;
				            	return Compareresult;
				            }
				        });
					   //��������CG��װ��LOCIDList
					   for (GffDetailAbs gffDetail : LOCList) {
						   LOCIDList.add(gffDetail.locString);
						   LOCChrHashIDList.add(gffDetail.locString);
					   }
				   }
				   LOCList=new ArrayList<GffDetailAbs>();//�½�һ��LOCList������Chrhash
				   Chrhash.put(chrnametmpString, LOCList);
			   }
			  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			   //ÿһ�о���һ��CG
			   GffDetailCG gffGCtmpDetail=new GffDetailCG();
			   gffGCtmpDetail.ChrID=chrnametmpString;//��Сд��
			   gffGCtmpDetail.cis5to3=true;
			   gffGCtmpDetail.locString=ss[0]+"_"+ss[1]+"_"+ss[4]+"_"+ss[2];
			   gffGCtmpDetail.numberstart=Integer.parseInt(ss[2]);
			   gffGCtmpDetail.numberend=Integer.parseInt(ss[3]);
			   gffGCtmpDetail.lengthCpG=Integer.parseInt(ss[5]);
			   gffGCtmpDetail.numCpG=Integer.parseInt(ss[6]);
			   gffGCtmpDetail.numGC=Integer.parseInt(ss[7]);
			   gffGCtmpDetail.perCpG=Double.parseDouble(ss[8]);
			   gffGCtmpDetail.perGC=Double.parseDouble(ss[9]);
			   gffGCtmpDetail.obsExp=Double.parseDouble(ss[10]);
			   //װ��LOCList��locHashtable
			   LOCList.add(gffGCtmpDetail);  
			   locHashtable.put(gffGCtmpDetail.locString, gffGCtmpDetail);
		   }
		   /////////////////////////////////////////////////////////////////////////////////////////////
		   LOCList.trimToSize();
		   //�����������Ÿ���
		   Collections.sort(LOCList,new Comparator<GffDetailAbs>(){
	            public int compare(GffDetailAbs arg0, GffDetailAbs arg1) {
	                int Compareresult;
	            	if(arg0.numberstart<arg1.numberstart)
	            		Compareresult=-1;
	            	else if (arg0.numberstart==arg1.numberstart) 
	            		Compareresult=0;
	            	else 
	            		Compareresult=1;
	            	return Compareresult;
	            }});
		 //�������װ��LOCIDList
		   for (GffDetailAbs gffDetail : LOCList) {
			   LOCIDList.add(gffDetail.locString);
			   LOCChrHashIDList.add(gffDetail.locString);
		}
		   txtgff.close();
		 /////////////////////////////////////////////////////////////////////////////////////////////////
	}

	/**
	 * ����CG��������hash����ʽ����
	 * @return
	 */
	public Hashtable<String, Integer> getLength() 
	{
		int LOCNum=LOCIDList.size();
		Hashtable<String, Integer> hashCGLength=new Hashtable<String, Integer>();
		
		for (int i = 0; i < LOCNum; i++) 
		{
			GffDetailCG gffDetailCG=(GffDetailCG)locHashtable.get(LOCIDList.get(i));
			int tmpLength=gffDetailCG.numberend-gffDetailCG.numberstart;
			String tmpCGClass="CpG";//��ֻ��һ��CpG
			if (hashCGLength.containsKey(tmpCGClass)) //������֪��repeat�����repeat�ĳ����ۼ���ȥ
			{
				tmpLength=tmpLength+hashCGLength.get(tmpCGClass);
				hashCGLength.put(tmpCGClass, tmpLength);
			}
			else//����������µ�repeat�ӽ�ȥ 
			{
				hashCGLength.put(tmpCGClass,tmpLength);
			}
		}
		return hashCGLength;
	}

	
	
	/**
	 * ��λ�㴦�ڻ����ⲿʱ�ľ������,����GffCodInfoʵ��������
	 * @param Coordinate ����
	 * @param Genlist ĳ��Ⱦɫ���list��
	 * @param beginnum����������
	 * @param endnum��һ����������
	 * �������ǰ/��û����Ӧ�Ļ�����ô��Ӧ��LOCIDΪnull
	 */
	@Override
	protected  GffCodAbs SearchLOCoutside(ArrayList<GffDetailAbs> Genlist,int beginnum,int endnum,String chrID, int Coordinate)
	{
		GffCodCG gffCodCG = new GffCodCG(chrID, Coordinate);
		GffDetailCG beginnumlist=null;
		GffDetailCG endnumlist=null;		
		gffCodCG.result=true;
		gffCodCG.insideLOC=false;
		
		if (beginnum!=-1) {
			beginnumlist=(GffDetailCG) Genlist.get(beginnum);
			gffCodCG.LOCID[1]=beginnumlist.locString;//�ϸ������ID
			gffCodCG.begincis5to3=beginnumlist.cis5to3;//һֱΪ��
			gffCodCG.distancetoLOCEnd[0]=Math.abs(Coordinate-beginnumlist.numberend);
			gffCodCG.distancetoLOCStart[0]=Math.abs(Coordinate-beginnumlist.numberstart);
		}
		if (endnum!=-1) {
			endnumlist=(GffDetailCG) Genlist.get(endnum);
			gffCodCG.LOCID[2]=endnumlist.locString;//�¸������ID
			gffCodCG.endcis5to3=endnumlist.cis5to3;//һֱΪ��
			gffCodCG.distancetoLOCEnd[1]=-Math.abs(Coordinate-endnumlist.numberend);
			gffCodCG.distancetoLOCStart[1]=-Math.abs(Coordinate-endnumlist.numberstart);
		}
		return gffCodCG;
	}
	
	/**
	 * ��λ�㴦�ڻ����ڲ�ʱ�ľ������,����GffCodInfoʵ��������
	 * @param Coordinate ����
	 * @param Genlist ĳ��Ⱦɫ���list��
	 * @param beginnum����������
	 * @param endnum��һ����������
	 */
	@Override
	protected  GffCodAbs SearchLOCinside(ArrayList<GffDetailAbs> Genlist,int beginnum,int endnum,String chrID, int Coordinate)
	{
		GffCodCG gffCodCG = new GffCodCG(chrID, Coordinate);
		GffDetailCG LOCdetial=(GffDetailCG) Genlist.get(beginnum);		
		gffCodCG.result=true;
		gffCodCG.insideLOC=true;
		
		gffCodCG.LOCID[0]=LOCdetial.locString;//�������ID
		gffCodCG.begincis5to3=LOCdetial.cis5to3;//һֱΪ��
		
		gffCodCG.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberstart);//������������λ��
		gffCodCG.distancetoLOCStart[1]=-1;
		
		gffCodCG.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberend);//������������λ��
		gffCodCG.distancetoLOCEnd[1]=-1;
		return gffCodCG;
	}

	@Override
	public GffDetailCG LOCsearch(String LOCID) {
		return (GffDetailCG) locHashtable.get(LOCID);
	}

	@Override
	public GffDetailCG LOCsearch(String chrID, int LOCNum) {
		return (GffDetailCG) Chrhash.get(chrID).get(LOCNum);
	}
	/**
	 * ��������� ����ChrID���������꣬�Լ�GffHash��<br>
	 * ,chr����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд, chr1,chr2,chr11<br>
	 * @param chrID
	 * @param Coordinate
	 * @return
	 * û�ҵ��ͷ���null
	 */
	public GffCodCG searchLoc(String chrID, int Coordinate) {
		return (GffCodCG) searchLocation(chrID, Coordinate); 
	}
}
