package com.novelbio.analysis.guiRun.BlastGUI.control;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.annotation.genAnno.GOQuery;
import com.novelbio.analysis.guiRun.BlastGUI.GUI.GUIBlast;
import com.novelbio.analysis.guiRun.BlastGUI.GUI.GuiBlastJpanel;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.service.ServGo;


public class CtrlBlastGo extends SwingWorker<ArrayList<String[]>, ProgressDataGo>
{

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
//	public CtrlBlastGo(boolean blast, int taxID, int StaxID, double evalue,GUIBlast guiBlast,String GOclass) {
	public CtrlBlastGo(boolean blast, int taxID, int StaxID, double evalue,GuiBlastJpanel guiBlast,String GOclass) {
		this.blast = blast;
		this.taxID = taxID;
		this.StaxID = StaxID;
		this.evalue = evalue;
		this.guiBlast =guiBlast;
		this.GoClass = GOclass;
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
		
		for (int i = 0; i<lsGeneID.size(); i++)
		{
			String geneID = lsGeneID.get(i).trim();
			ArrayList<String[]> lsTmpInfo = new ArrayList<String[]>();
			try {
				ArrayList<String[]> lstmpAnno = GOQuery.getLsGeneGo(geneID,taxID,GoClass, blast, evalue, 9606);
				if (lstmpAnno == null || lstmpAnno.size()<1) {
					String[] tmp = new String[length];
					for (int j = 0; j < tmp.length; j++) {
						tmp[j] = "";
					}
					tmp[0] = geneID;
					lstmpAnno = new ArrayList<String[]>();
					lstmpAnno.add(tmp);
				}
	
				ProgressDataGo progressData  = new ProgressDataGo();
				progressData.rowNum = i;
				for (int j = 0; j < lstmpAnno.size(); j++) {
					
				// (String[] strings : lstmpAnno) 
				
					//����а����˵�2�к͵�9�е�geneID��Ϣ
					//û��blastΪstirng[5]
					//blastΪstring[13]
					String[] strings2 =null;
					
					if(blast)
					{
						strings2 = new String[9];
						int m = 0;
						for (int k = 0; k < lstmpAnno.get(0).length; k++) {
							if (k == 1 || k==3 || k == 6 || k== 8 || k==9 || k == 11 || k == 14) {
								continue;
							}
							strings2[m] =  lstmpAnno.get(j)[k]; m++;
						}
					}
					else 
					{
						strings2 = new String[4];
						int m = 0;
						for (int k = 0; k <  lstmpAnno.get(0).length; k++) {
							if (k == 1 || k == 3 || k == 6) {
								continue;
							}
							strings2[m] =  lstmpAnno.get(j)[k]; m++;
						}
					}
					lsTmpInfo.add(strings2);
					lsDesp.add(strings2);
				}
				progressData.lsInfo = lsTmpInfo;
				publish(progressData);
//				lsDesp.addAll(lstmpAnno);
			} catch (Exception e) {
			}
		}
		
		return lsDesp;
	}
	
	@Override
	public void process(List<ProgressDataGo> data)
	{
		if (isCancelled()) {
			return;
		}
		for (ProgressDataGo progressDataGo : data) {
			if (guiBlast.getJProgressBar1().getValue()<progressDataGo.rowNum) {
				guiBlast.getJProgressBar1().setValue(progressDataGo.rowNum);
			}
			for (String[] strings: progressDataGo.lsInfo) 
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

class ProgressDataGo
{
	int rowNum=0;
	ArrayList<String[]> lsInfo = null;
}