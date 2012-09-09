package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;

/**
 * �����������Ѿ��趨
 * UCSC��Ĭ���ļ�������ǿ����䣬�յ�Ϊ�����䡣ˮ�����Ͻ�Ļ�û���趨��������
 * ר�Ŷ�ȡUCSC��gene�����ļ�,��ȡʱ�ӵڶ��ж���
 * ��ȡ��Ϻ��ͳ���ں��������ӵ���Ŀ
 * group:Genes and Gene Prediction Tracks
 * track:UCSC Genes
 * table:knownGene
 * output format:all fields from selected table
 * @author zong0jie
 *
 */
public class GffHashGeneUCSC extends GffHashGeneAbs{
	/**
	 * @Override
	 * ��ײ��ȡgff�ķ�����������ֻ�ܶ�ȡUCSCknown gene<br>
	 * ����Gff�ļ��������������ϣ���һ��list��,��ȡʱ�ӵڶ��ж���<br/>
	 * �ṹ���£�<br/>
     * ����Gff�ļ��������������ϣ���һ��list��, �ṹ���£�<br>
     * <b>1.Chrhash</b><br>
     * ��ChrID��--ChrList-- GeneInforList(GffDetail��)
     * ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID, chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
     * ����LOCID����������Ŀ��ţ���UCSCkonwn gene����û��ת¼��һ˵��
	 * �����ж��LOCID����һ�����������������ж����ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene
     *  <b>3.LOCIDList</b><br>
     * ��LOCID�����List˳��洢ÿ������Ż���Ŀ�ţ��������������ȡ�������ţ�ʵ������������Ŀ��˳����룬���ǲ�����ת¼��(UCSC)�����ظ�(Peak) ���ID��locHashһһ��Ӧ�����ǲ���������ȷ��ĳ��Ŀ��ǰһ�����һ����Ŀ <br>
     * <b>4.LOCChrHashIDList </b><br>
     *   LOCChrHashIDList�б���LOCID����������Ŀ���,��Chrhash�������һ�£���ͬһ����Ķ��ת¼������һ����б�߷ָ�"/"�� NM_XXXX/NM_XXXX...<br>
	 * @throws Exception 
	 */
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		setTaxID(gfffilename);
		// ʵ�����ĸ���
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();// һ����ϣ�����洢ÿ��Ⱦɫ��
		
		TxtReadandWrite txtGffRead = new TxtReadandWrite(gfffilename, false);
		ListGff lsChromGene = null;// ˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
		String chrIDtmp = "";
		// int mm=0;//�����Ķ���
		for (String content : txtGffRead.readlines(2)) {
			content = content.replace("\"", "");
			String[] geneInfo = content.split("\t");
			String[] exonStarts = geneInfo[8].split(",");
			String[] exonEnds = geneInfo[9].split(",");
			chrIDtmp = geneInfo[1];// Сд��chrID
			String chrIDtmpLowCase = chrIDtmp.toLowerCase();
			// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// �µ�Ⱦɫ��
			if (!mapChrID2ListGff.containsKey(chrIDtmpLowCase)) // �µ�Ⱦɫ��
			{
				if (lsChromGene != null)// ����Ѿ�������LOCList��Ҳ����ǰһ��LOCList����ô�Ƚض̣�Ȼ��������gffGCtmpDetail.numberstart����
				{
					lsChromGene.trimToSize();
				}
				lsChromGene = new ListGff();// �½�һ��LOCList������Chrhash
				lsChromGene.setName(chrIDtmp);
				mapChrID2ListGff.put(chrIDtmpLowCase, lsChromGene);
			}
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// ���ת¼��
			// ���������ת¼����Ƿ�С���ϸ������ת¼�յ㣬���С�ڣ���˵�����������ϸ������һ��ת¼��
			GffDetailGene lastGffdetailUCSCgene = null; double[] overlapInfo = null;
			if (lsChromGene.size() > 0 ) {
				lastGffdetailUCSCgene = (GffDetailGene) lsChromGene.get(lsChromGene.size() - 1);
				double[] regionLast = new double[]{lastGffdetailUCSCgene.getStartAbs(), lastGffdetailUCSCgene.getEndAbs()};
				double[] regionThis = new double[]{Double.parseDouble(geneInfo[3])+getStartRegion(), Double.parseDouble(geneInfo[4])+getEndRegion() };
				overlapInfo = ArrayOperate.cmpArray(regionLast, regionThis);
			}
			
			if (lsChromGene.size() > 0 
//					&& // ���ת¼������ͬ���Ǿ��¿�һ��ת¼��
//					geneInfo[2].equals("+") == lastGffdetailUCSCgene.isCis5to3()
					&&
					//�����Ϊ�ص�1/3���ϲ���Ϊ��ͬһ������
					(overlapInfo[2] > 0.3 || overlapInfo[3] > 0.3)
					)
			{
				// �޸Ļ��������յ�
				if (Integer.parseInt(geneInfo[3]) + getStartRegion() < lastGffdetailUCSCgene.getStartAbs())
					lastGffdetailUCSCgene.setStartAbs( Integer.parseInt(geneInfo[3]) + getStartRegion() );
				if (Integer.parseInt(geneInfo[4]) + getEndRegion() > lastGffdetailUCSCgene.getEndAbs())
					lastGffdetailUCSCgene.setEndAbs( Integer.parseInt(geneInfo[4]) + getEndRegion() );
				//�����ת¼��������ֲ�һ�µģ���geneDetail��ת¼��������Ϊnull
				if (geneInfo[2].equals("+") != lastGffdetailUCSCgene.isCis5to3()) {
					lastGffdetailUCSCgene.setCis5to3( null );
				}
				
				// ��������(ת¼��)��IDװ��locString��
				lastGffdetailUCSCgene.addItemName(geneInfo[0]);
				if (Math.abs(Integer.parseInt(geneInfo[5]) - Integer.parseInt(geneInfo[6])) <= 2) {
					lastGffdetailUCSCgene.addsplitlist(geneInfo[0], GeneType.miRNA, geneInfo[2].equals("+"));
				}
				else {
					lastGffdetailUCSCgene.addsplitlist(geneInfo[0], GeneType.mRNA, geneInfo[2].equals("+"));
				}
				// ���һ��ת¼����Ȼ����Ӧ��Ϣ:
				// ��һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region
				// end,�ӵ����ʼ�Ǹ�ת¼����Exon������Ϣ
				lastGffdetailUCSCgene.setATGUAG(Integer.parseInt(geneInfo[5]) + getStartRegion(), Integer.parseInt(geneInfo[6]) + getEndRegion());
				
				int exonCount = Integer.parseInt(geneInfo[7]);
				for (int i = 0; i < exonCount; i++) {
					lastGffdetailUCSCgene.addExon(Integer.parseInt(exonStarts[i]) + getStartRegion(), Integer.parseInt(exonEnds[i]) + getEndRegion());
				}
				continue;
			}
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// ����»���
			GffDetailGene gffDetailUCSCgene = new GffDetailGene(lsChromGene, geneInfo[0], geneInfo[2].equals("+"));
			gffDetailUCSCgene.setTaxID(taxID);
			gffDetailUCSCgene.setStartAbs(  Integer.parseInt(geneInfo[3]) + getStartRegion() );
			gffDetailUCSCgene.setEndAbs(  Integer.parseInt(geneInfo[4]) + getEndRegion() );
			if (Math.abs(Integer.parseInt(geneInfo[5]) - Integer.parseInt(geneInfo[6])) <= 1) {
				gffDetailUCSCgene.addsplitlist(geneInfo[0], GeneType.miRNA);
			}
			else {
				gffDetailUCSCgene.addsplitlist(geneInfo[0], GeneType.mRNA);
			}
			// ���һ��ת¼����Ȼ����Ӧ��Ϣ:
			// ��һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region
			// end,�ӵ����ʼ�Ǹ�ת¼����Exon������Ϣ
			gffDetailUCSCgene.setATGUAG(Integer.parseInt(geneInfo[5]) + getStartRegion(), Integer.parseInt(geneInfo[6]) + getEndRegion());
			int exonCount = Integer.parseInt(geneInfo[7]);
			for (int i = 0; i < exonCount; i++) {
				gffDetailUCSCgene.addExon(Integer.parseInt(exonStarts[i]) + getStartRegion(),Integer.parseInt(exonEnds[i]) + getEndRegion());
			}
			lsChromGene.add(gffDetailUCSCgene);
		}
		lsChromGene.trimToSize();
		txtGffRead.close();
	}
	
	private void setTaxID(String gffFile) {
		if (taxID != 0) {
			return;
		}
		TxtReadandWrite txtGffRead = new TxtReadandWrite(gffFile, false);
		ArrayList<String> lsInfo = null;
		try {
			lsInfo = txtGffRead.readFirstLines(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String accID : lsInfo) {
			ArrayList<GeneID> lsCopedIDs = GeneID.createLsCopedID(accID.split("\t")[0], 0, false);
			if (lsCopedIDs.size() == 1 && lsCopedIDs.get(0).getIDtype() != GeneID.IDTYPE_ACCID) {
				taxID = lsCopedIDs.get(0).getTaxID();
				break;
			}
		}
	}
	
}
