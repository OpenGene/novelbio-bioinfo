package com.novelbio.nbcgui.controlquery;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.nbcgui.GUI.GuiBlastJpanel;


public class CtrlBlastPath extends SwingWorker<ArrayList<String[]>, ProgressDataPath> {


	/**
	 * �Ƿ���Ҫblast
	 */
	boolean blast = false;
	/**
	 * ��������
	 */
	int taxID = 0;
	/**
	 * blast����
	 */
	int StaxID = 0;
	/**
	 * blast��evalue
	 */
	double evalue = 100;
	
	/**
	 * Go������
	 */
	String GoClass = "P";
	/**
	 * �������
	 */
//	GUIBlast guiBlast;
	GuiBlastJpanel guiBlast;
	/**
	 * 
	 * @param blast
	 * @param taxID
	 * @param StaxID
	 * @param evalue
	 * @param guiBlast
	 */
//	public CtrlBlastPath(boolean blast, int taxID, int StaxID, double evalue,GUIBlast guiBlast) {
	public CtrlBlastPath(boolean blast, int taxID, int StaxID, double evalue,GuiBlastJpanel guiBlast) {
		this.blast = blast;
		this.taxID = taxID;
		this.StaxID = StaxID;
		this.evalue = evalue;
		this.guiBlast =guiBlast;
		
	}
	
	List<String> lsGeneID = null;
 
	
	/**
	 * ׼����������geneID�����ڴ�ͬʱ׼�����ң�ͬʱ�����ܹ����ҵ�������������������
	 * @return
	 * @throws Exception
	 */
	public int prepare(List<String> lsGeneID) {
		this.lsGeneID = lsGeneID;
		return lsGeneID.size();
	}
	
	/**
	 * �����ļ������ļ��ָ�����Լ��ڼ��У���ø��еĻ���ID
	 * @param fileName
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<String[]> doInBackground() throws Exception 
	{
		System.out.println("run");
		ArrayList<String[]> lsDesp = new ArrayList<String[]>();
		int length = 0;
		if (blast) 
			length = 13;
		else 
			length = 5; 
		
		for (int i = 0; i<lsGeneID.size(); i++) {
			String geneID = lsGeneID.get(i).trim();
		
			try {
				ArrayList<String[]> lstmpAnno = getGenPath(geneID, taxID, blast, StaxID, evalue);
				if (lstmpAnno == null || lstmpAnno.size()<1) {
					String[] tmp = new String[length];
					for (int j = 0; j < tmp.length; j++) {
						tmp[j] = "";
					}
					tmp[0] = geneID;
					lstmpAnno = new ArrayList<String[]>();
					lstmpAnno.add(tmp);
				}
	
				ProgressDataPath progressData  = new ProgressDataPath();
				progressData.rowNum = i;
				progressData.lsInfo = lstmpAnno;
				publish(progressData);
				lsDesp.addAll(lstmpAnno);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return lsDesp;
	}
	
	
	
	/**
	 * ����accID����ѯ��accID����Ӧ��pathway
	 * Ŀǰֻ����NCBIID�в�ѯ��������UniProt�в�ѯ
	 * @param accID accID ��Ҫȥ�ո����Լ��ж�accID�Ƿ�Ϊ��
	 * @param taxID
	 * @param blast
	 * @param subTaxID
	 * @param evalue
	 * @return ���û�鵽�򷵻�null
	 * ���blast��
	 * 0:accID
	 * 1:Symbol/AccID
	 * 2:PathID
	 * 3:PathName
	 * 4:evalue
	 * 5:SubjectSymbol
	 * 6:PathID
	 * 7:PathName
	 * ���û��blast
	 * 0:accID
	 * 1:Symbol/AccID
	 * 2:PathID
	 * 3:PathName
	 */
	public static ArrayList<String[]> getGenPath(String accID,int taxID,boolean blast,int subTaxID,double evalue)
	{
		String[] tmpAccIDInfo = new String[] { accID, ""};
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		CopedID copedID = new CopedID(accID, taxID);
		if (copedID.getIDtype().equals(CopedID.IDTYPE_ACCID)) {
			return null;
		}
		tmpAccIDInfo[1] = copedID.getSymbol();
		copedID.setBlastInfo(evalue, subTaxID);
		// �������GO��Ϣ
		ArrayList<KGentry> lsKGentrythis = copedID.getKegEntity(false);
		HashSet<String> hashPathID = new HashSet<String>();
		for (KGentry kGentry : lsKGentrythis) {
			String[] tmpResult = ArrayOperate.copyArray(tmpAccIDInfo, 4);
			if (hashPathID.contains(kGentry.getPathName())) {
				continue;
			}
			tmpResult[2] = kGentry.getPathName();
			tmpResult[3] = kGentry.getPathTitle();
			lsResult.add(tmpResult);
		}
		if (!blast) {
			if (lsResult.size() == 0) {
				return null;
			}
			return lsResult;
		}
		// blast�����GO��Ϣ
		ArrayList<String[]> lsResultBlast = new ArrayList<String[]>();

		CopedID copedIDblast = copedID.getCopedIDBlast();
		if (copedIDblast == null) {
			for (String[] strings : lsResult) {
				String[] result = ArrayOperate.copyArray(strings, 8);
				lsResultBlast.add(result);
			}
			return lsResultBlast;
		}
		HashSet<String> hashPathIDBlast = new HashSet<String>();
		ArrayList<KGentry> lsPathBlast = copedIDblast.getKegEntity(false);
		int k = 0;
		for (int i = 0; i < lsPathBlast.size(); i++) {
			if (hashPathIDBlast.contains(lsPathBlast.get(i).getPathName())) {
				continue;
			}
			if (lsResult == null)
				lsResult = new ArrayList<String[]>();
			String[] tmpResultBlast = new String[8];
			// ��ʼ��
			for (int j = 0; j < tmpResultBlast.length; j++) {
				tmpResultBlast[i] = "";
			}
			if (k < lsResult.size()) {
				for (int j = 0; j < lsResult.get(k).length; j++) {
					tmpResultBlast[j] = lsResult.get(k)[j];
				}
			} else {
				tmpResultBlast[0] = accID;
				tmpResultBlast[1] = copedID.getSymbol();
			}
			tmpResultBlast[4] = copedID.getLsBlastInfos().get(0).getEvalue() + "";
			tmpResultBlast[5] = copedIDblast.getSymbol();
			tmpResultBlast[6] = lsPathBlast.get(i).getPathName();
			tmpResultBlast[7] = lsPathBlast.get(i).getPathTitle();
			lsResultBlast.add(tmpResultBlast);
			k ++;
		}

		return lsResultBlast;
	}
	
	
	
	
	@Override
	public void process(List<ProgressDataPath> data)
	{
		if (isCancelled()) {
			return;
		}
		for (ProgressDataPath ProgressDataPath : data) {
			if (guiBlast.getJProgressBar1().getValue()<ProgressDataPath.rowNum) {
				guiBlast.getJProgressBar1().setValue(ProgressDataPath.rowNum);
			}
			for (String[] strings: ProgressDataPath.lsInfo) 
			{
				guiBlast.getJTabGoandPath().addRow(strings);
			}
		}
	}
	
	public void done() {
		int maxValue = guiBlast.getJProgressBar1().getMaximum();
		guiBlast.getJProgressBar1().setValue(maxValue);
		guiBlast.getJBtnGoPath().setEnabled(true);
		guiBlast.getJBtnSaveGO().setEnabled(true);
		guiBlast.getJLbGOandPath().setText("Complete");
		try {
			guiBlast.setLsGoandPath(this.get());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
class ProgressDataPath
{
	int rowNum=0;
	ArrayList<String[]> lsInfo = null;
}