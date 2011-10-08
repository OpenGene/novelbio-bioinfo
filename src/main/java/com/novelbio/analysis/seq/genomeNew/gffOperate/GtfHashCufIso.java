package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.base.cmd.cmdOperate2;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * ��ȡcufdif������ɵ�isoforms.fpkm_tracking
 * ��Ҫ��ÿ��ת¼���ı����Ϣ
 * @author zong0jie
 *
 */
public class GtfHashCufIso extends GffHash{

	public GtfHashCufIso(String gfffilename) {
		super(gfffilename);
		// TODO Auto-generated constructor stub
	}
	/**
	 * ֻ�����locHashtable��LOCIDList����list
	 */
	@Override
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		locHashtable = new HashMap<String, GffDetailAbs>();
		LOCIDList = new ArrayList<String>();
		TxtReadandWrite txtIso = new TxtReadandWrite();
		txtIso.setParameter(gfffilename, false, true);
		String[] head = txtIso.readFirstLine().split("\t");
		//��ӱ���
		for (int i = 10; i < head.length; i = i+3) {
			GtfDetailCufIso.addCodName(head[i].replace("_FPKM", "").trim());
		}
		ArrayList<String> lsAll = txtIso.readfileLs();	lsAll.remove(0);
		for (String string : lsAll) {
			String[] ss = string.split("\t");
			if (ss[2].equals("-")) {
				continue;
			}
			GtfDetailCufIso gtfDetailCufIso = null;
			String geneID = CopedID.removeDot(ss[2]);
			if (locHashtable.containsKey(geneID)) {
				gtfDetailCufIso = (GtfDetailCufIso) locHashtable.get(geneID);
			}
			else {
				gtfDetailCufIso = new GtfDetailCufIso("", CopedID.removeDot(ss[2]), false);
				locHashtable.put(geneID, gtfDetailCufIso);
				LOCIDList.add(geneID);
			}
			double[] exp = new double[(ss.length-10)/3]; int m=0;
			for (int i = 10; i < ss.length; i = i+3) {
				exp[m] = Double.parseDouble(ss[i]);
				m++;
			}
		
				gtfDetailCufIso.addIsoExp(ss[1], ss[2], exp);
	
			
		}
		
	}
	/**
	 * û�ã���Ҫʹ��
	 */
	@Override
	public GtfDetailCufIso searchLOC(String LOCID) {
		return (GtfDetailCufIso) locHashtable.get(LOCID);
	}
	/**
	 * û�ã���Ҫʹ��
	 */
	@Override
	public GtfDetailCufIso searchLOC(String chrID, int LOCNum) {
		return (GtfDetailCufIso) Chrhash.get(chrID).get(LOCNum);
	}
	/**
	 * û�У���Ҫʹ��
	 */
	@Override
	public GffCodAbs setGffCod(String chrID, int Coordinate) {
		// TODO Auto-generated method stub
		return null;
	}

}
