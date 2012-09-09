package com.novelbio.analysis.seq.genomeNew.gffOperate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.generalConf.NovelBioConst;

/**
 * ���Gff�Ļ���������Ϣ,�������ʵ��������ʹ��<br/>
 * ����Gff�ļ��������������ϣ���һ��list��,
 * �ṹ���£�<br/>
 * 1.hash��ChrID��--ChrList--GffDetail(GffDetail��,ʵ����GffDetailUCSCgene����)<br/>
 *   ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID
 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br/>
 * 
 * 2.hash��LOCID��--GffDetail������LOCID�������Ļ����� <br/>
 * 
 * 3.list��LOCID��--LOCList����˳�򱣴�LOCID<br/>
 * 
 * ÿ�����������յ��CDS������յ㱣����GffDetailList����<br/>
 */
public class GffHashGenePlant extends GffHashGeneAbs{
	public static void main(String[] args) {
		GffHashGenePlant gffHashGenePlant = new GffHashGenePlant(NovelBioConst.GENOME_GFF_TYPE_PLANT);
		gffHashGenePlant.ReadGffarray("/media/winE/Bioinformatics/genome/rice/tigr7/all.gff3");
		GffCodGene gffCodGene = gffHashGenePlant.searchLocation("chr1", 6790);
		System.out.println(gffCodGene.getGffDetailThis().getLongestSplit().getATGsite());
	}
	/**
	 * �������ֵ����򣬿��Ըĳ�ʶ�������������,���������Ͻ棬Ĭ��  "AT\\w{1}G\\d{5}"
	 * ˮ���� "LOC_Os\\d{2}g\\d{5}";
	 */
	protected String GeneName="AT\\w{1}G\\d{5}";
	/** �ɱ����mRNA������ˮ���ǣ�"(?<=LOC_Os\\d{2}g\\d{5}\\.)\\d";��Ĭ�����Ͻ�" (?<=AT\\w{1}G\\d{5}\\.)\\d" */
	protected String splitmRNA="(?<=AT\\w{1}G\\d{5}\\.)\\d";
	/** mRNA������ */
	private static HashMap<String, GeneType> mapMRNA2GeneType = new HashMap<String, GeneType>();
	/** gene������ */
	private static HashSet<String> hashgene = new HashSet<String>();
	
	/**
	 * @param gfffilename
	 * @param Species ˮ���������Ͻ棬ֻ��������ѡ�� ��Species���еĳ���
	 * Species.ARABIDOPSIS��DB.equals(Species.RICE
	 * @throws Exception
	 */
	public GffHashGenePlant(String DB) {
		setDB(DB);
	}
	
	private void setDB(String DB) {
		if (DB.equals(NovelBioConst.GENOME_GFF_TYPE_PLANT)) {
			GeneName= "(?<=Name\\=)\\w+";
			splitmRNA= "(?<=Name\\=)\\w+";
		}
		else if (DB.equals(NovelBioConst.GENOME_GFF_TYPE_TIGR)) {
			GeneName = "(?<=Alias\\=)\\w+";
			splitmRNA = "(?<=Alias\\=)\\w+";
		}
	}
	/** �趨mRNA��gene������������gff�ļ�������ֵ� */
	private void setHashName() {
		if (mapMRNA2GeneType.isEmpty()) {
			

			mapMRNA2GeneType.put("mRNA_TE_gene",GeneType.mRNA_TE);
			mapMRNA2GeneType.put("mRNA",GeneType.mRNA);
			mapMRNA2GeneType.put("miRNA",GeneType.miRNA);
			mapMRNA2GeneType.put("tRNA",GeneType.tRNA);
			mapMRNA2GeneType.put("pseudogenic_transcript", GeneType.PSEU_TRANSCRIPT);
			mapMRNA2GeneType.put("snoRNA", GeneType.snoRNA);
			mapMRNA2GeneType.put("snRNA", GeneType.snRNA);
			mapMRNA2GeneType.put("rRNA", GeneType.rRNA);
			mapMRNA2GeneType.put("ncRNA", GeneType.ncRNA);
			mapMRNA2GeneType.put("transcript",GeneType.miscRNA);
			mapMRNA2GeneType.put("miscRNA",GeneType.miscRNA);
		}
		
		if (hashgene.isEmpty()) {
			hashgene.add("gene");
			hashgene.add("transposable_element_gene");
			hashgene.add("pseudogene");
		}
	}
	
	/**
	 * ��ײ��ȡgff�ķ�����������ֻ�ܶ�ȡUCSCknown gene<br>
	 * ����Gff�ļ��������������ϣ���һ��list��<br/>
	 * �ṹ���£�<br/>
     * ����Gff�ļ��������������ϣ���һ��list��, �ṹ���£�<br>
     * <b>1.Chrhash</b><br>
     * ��ChrID��--ChrList-- GeneInforList(GffDetail��)
     * ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID, chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
    * ����LOCID����������Ŀ��ţ���UCSCkonwn gene����û��ת¼��һ˵��
	 * ֻ������LOCID����һ����������������ֻ�ܹ�������ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene
     *  <b>3.LOCIDList</b><br>
     * ��LOCID��--LOCIDList����˳�򱣴�LOCID,���ﲻ���Ƕ��ת¼����ÿһ��ת¼������һ��������LOCID <br>
     * <b>4. LOCChrHashIDList </b><br>
     *   LOCChrHashIDList�б���LOCID����������Ŀ���,��Chrhash�������һ�£���ͬһ����Ķ��ת¼������һ�� NM_XXXX/NM_XXXX...<br>
	 * @throws Exception 
	 */
   protected void ReadGffarrayExcep(String gfffilename) throws Exception {
	   setHashName();
		// ʵ�����ĸ���
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();// һ����ϣ�����洢ÿ��Ⱦɫ��
		
	   TxtReadandWrite txtgff=new TxtReadandWrite(gfffilename, false);
	   
	   ListGff LOCList = null;//˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
	   //��������
	   Pattern genepattern =Pattern.compile(GeneName, Pattern.CASE_INSENSITIVE);//to catch the LOC
	   Matcher genematcher;
      
	   //mRNA�ɱ���ӵ����
	   Pattern mRNApattern =Pattern.compile(splitmRNA, Pattern.CASE_INSENSITIVE);//to catch the LOC
	   Matcher mRNAmatcher;
	   String chrIDtmp=""; //Ⱦɫ�����ʱ����
	   boolean UTR5start = false; boolean UTR3start = false; boolean UTR5end = false; boolean UTR3end = false;
	   boolean CDSstart = false; boolean CDSend = false; boolean mRNAsplit = false;//�Ƿ������һ��mRNA
	   int cdsStart = -100; int cdsEnd = -100; int mRNAstart = -100;  int mRNAend = -100; 
	   boolean ncRNA = false;
	   GffDetailGene gffDetailLOC= null;
	   for (String content : txtgff.readlines()) {
		   if(content.length() == 0 || content.charAt(0)=='#')
			   continue;
		   ////////////////// ��Ҫ�����滻�ĵط� /////////////////////////////////////////////////////////////
		   if (ncRNA) {
			   content = content.replace("pseudogenic_exon", "CDS");
			   content = content.replace("exon", "CDS");
		   }
		   String[] ss = content.split("\t");//����tab�ֿ�
		   chrIDtmp=ss[0];//Сд��chrID
		   String chrIDtmpLowCase = chrIDtmp.toLowerCase();
		 //�µ�Ⱦɫ��
			if (!mapChrID2ListGff.containsKey(chrIDtmpLowCase)) //�µ�Ⱦɫ��
			{
				LOCList=new ListGff();//�½�һ��LOCList������Chrhash
				LOCList.setName(chrIDtmp);
				mapChrID2ListGff.put(chrIDtmpLowCase, LOCList);
			}
		   /**
		    * ����ȡ��geneʱ�����Ƕ�����һ���µĻ�����ô������������㣬�յ��ÿ��CDS�ĳ��ȶ�����list������
		    */
		   if (hashgene.contains(ss[2])) //when read the # and the line contains gene, it means the new LOC
			{
				if (mRNAsplit) {
					// ����һ��mRNA����Ϣװ��
					// �����һ��mRNAû��CDS����ôCDS�ĳ���ʵ���Ͼ���0����ôCDS�������յ����һ���ģ�����mRNA��endλ��
					if (cdsStart < 0 && cdsEnd < 0) {
						cdsStart = mRNAend;
						cdsEnd = mRNAend;
					}
					gffDetailLOC.setATGUAG(cdsStart, cdsEnd);
					if (cdsStart < 0 || cdsEnd < 0 || cdsStart > cdsEnd) {
						System.out.println("GffHashPlantGeneError: �ļ�  " + gfffilename + "  �����������������⣬cdsStart��cdsEnd����  " + gffDetailLOC.getName());
					}
					mRNAsplit = false;// ȫ�µĻ��򣬽����λfalse\
					if (!ss[2].equals("gene")) {
						ncRNA = true;
					} else {
						ncRNA = false;
					}
				}
			   /**
			    * ÿ������һ���µ�Chr����ô�ͽ����Chr�����ϣ��
			    * chr��ʽ��ȫ��Сд chr1,chr2,chr11
			    */
			   genematcher = genepattern.matcher(content);//���һ�������
      		   if(genematcher.find()) {
      			   gffDetailLOC=new GffDetailGene(LOCList, genematcher.group(), ss[6].equals("+"));//�½�һ��������
      			   gffDetailLOC.setTaxID(taxID);
      			   gffDetailLOC.setStartAbs(  Integer.parseInt(ss[3].toLowerCase()) ); gffDetailLOC.setEndAbs( Integer.parseInt(ss[4]));//������ֹ      		
      			   LOCList.add(gffDetailLOC);//��ӽ���LOClist
      		   }
      		   else {
      			   System.out.println("GffHashPlantGeneError: �ļ�  "+gfffilename+"  �ڱ��п���û��ָ���Ļ���ID  "+ splitmRNA + " " +content);
      		   }
      		   //���ñ�ǩ����ʾ��5UTR��CDS��ǰ���ˣ���ô�ں��� if �����ĵľ��ǵ�һ��UTR���һ��CDS
      		   UTR5start = true; 
      		   CDSstart = true;
      	   }
		   /**
      	    * ����ȡ��mRNAʱ������˵�ǿɱ����ʱ�����һ���µĿɱ����list
      	    * ������ô�Ӷ��Ǵӵ�һ��cds��ʼ�ӵ����һ��cds������Ļ����Ǵ�С�ӵ��󣬷�����ǴӴ�ӵ�С��
      	    * һ��������mRNA����Ҫ��ʼָ��5UTR��3UTR��CDS��������ֹ
      	    */
		   else if (mapMRNA2GeneType.containsKey(ss[2])) 
		   {
			   if (!ss[2].equals("mRNA")) {
				ncRNA = true;
			   }
			   //����ոն�ȡ����һ��mRNA�Ļ�
			   if (mRNAsplit) {
				   //����һ��mRNA����Ϣװ��
				   if (cdsStart < 0 && cdsEnd <0) {
					   cdsStart = mRNAend;
					   cdsEnd = mRNAend;
				   }
				   gffDetailLOC.setATGUAG(cdsStart, cdsEnd);
				   if (cdsStart <0 || cdsEnd<0 || cdsStart >= cdsEnd) {
					   System.out.println("GffHashPlantGeneError: �ļ�  "+gfffilename+"  �����������������⣬cdsStart��cdsEnd����  " +gffDetailLOC.getName());
				   }
				   mRNAsplit =false;
			   }
			   mRNAmatcher = mRNApattern.matcher(content);
			   if(mRNAmatcher.find())
			   {
				   //ÿ����һ��mRNA�����һ���ɱ����,��Ҫ����ת��Ϊ����
				   gffDetailLOC.addsplitlist(mRNAmatcher.group(), mapMRNA2GeneType.get(ss[2]));	   
				   //����UCSC�������������һ���Ǳ����mRNA����ôcdsStart = cdsEnd = mRNAend
				   mRNAstart = Integer.parseInt(ss[3]); mRNAend = Integer.parseInt(ss[4]); 
				   cdsStart = -100; cdsEnd = -100;
			   }
			   else {
				   System.out.println("GffHashPlantGeneError: �ļ�  "+gfffilename+"  �ڱ��п���û��ָ���Ļ���ID  " +content);
			   }
			   //���ñ�ǩ����ʾ��5UTR��CDS��ǰ���ˣ���ô�ں��� if �����ľ��ǵ�һ��UTR���һ��CDS
      		   UTR5start = true; 
      		   CDSstart = true;
    	   }
		   
		   //����5UTR
		   else if (ss[2].equals("five_prime_UTR")) 
		   {
			   gffDetailLOC.addExon(Integer.parseInt(ss[3]),Integer.parseInt(ss[4]));
			   //5UTR��ȥ��
			   UTR5start = false;
			   UTR5end = true;//5UTR���н���
			   mRNAsplit = true;//��ת¼�������Ҫ�ܽ�
			   CDSstart = true; 
			   ncRNA = false;
		   }
		   else if (ss[2].equals("CDS"))
		   {
			   if (CDSstart)
			   {
				   if (UTR5end) 
				   {
					   gffDetailLOC.addExonGFFCDSUTR(Integer.parseInt(ss[3]),Integer.parseInt(ss[4]));//���������,��С�ӵ���
					   UTR5start = false;
					   UTR5end = false;
				   }
				   else {
					   gffDetailLOC.addExon(Integer.parseInt(ss[3]),Integer.parseInt(ss[4]));
				   }
				   CDSstart = false;
				   CDSend = true;
				   cdsStart = Integer.parseInt(ss[3]);
				   cdsEnd = Integer.parseInt(ss[4]);
			   }
			   else {
				   gffDetailLOC.addExon(Integer.parseInt(ss[3]),Integer.parseInt(ss[4]));
				   if (gffDetailLOC.isCis5to3()) 
					   cdsEnd = Integer.parseInt(ss[4]);
				   else //����װ
					   cdsStart = Integer.parseInt(ss[3]);
				   CDSend = true;
			   }
			   mRNAsplit = true;//��ת¼�������Ҫ�ܽ�
		   }
		   else if (ss[2].equals("three_prime_UTR")) 
		   {
			   if (UTR5end || CDSend) {//���������һ��CDS��
				   gffDetailLOC.addExonGFFCDSUTR(Integer.parseInt(ss[3]),Integer.parseInt(ss[4]));
				   UTR5end = false; 
				   CDSend = false; //�Ѿ����ǽ��������һ��CDS��
			   }
			   else {
				   gffDetailLOC.addExon(Integer.parseInt(ss[3]),Integer.parseInt(ss[4]));
			   }
			   //5UTR��ȥ��
			   UTR3start = false;
			   UTR3end = true;//5UTR���н���
			   mRNAsplit = true;//ȫ�µĻ��򣬽����λfalse
			   ncRNA = false;
		   }
		   else if (!ss[2].equals("protein") && !ss[2].equals("exon")) {
			   System.out.println(ss[2]);
		   }
	   }
	   if (mRNAsplit) {
		   //����һ��mRNA����Ϣװ��
		   if (cdsStart < 0 && cdsEnd <0) {
			   cdsStart = mRNAend;
			   cdsEnd = mRNAend;
		   }
		   gffDetailLOC.setATGUAG(cdsStart,cdsEnd);
		   if (cdsStart <0 || cdsEnd<0 || cdsStart > cdsEnd) {
			   System.out.println("GffHashPlantGeneError: �ļ�  "+gfffilename+"  �����������������⣬cdsStart��cdsEnd����  " +gffDetailLOC.getName());
		   }
		   mRNAsplit = false;//ȫ�µĻ��򣬽����λfalse
	   }
	   LOCList.trimToSize();
	   txtgff.close();
   }
}
