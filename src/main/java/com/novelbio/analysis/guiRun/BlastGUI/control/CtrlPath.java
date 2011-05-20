package com.novelbio.analysis.guiRun.BlastGUI.control;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.novelbio.analysis.annotation.genAnno.GOQuery;
import com.novelbio.analysis.annotation.genAnno.KegPathQuery;
import com.novelbio.analysis.guiRun.BlastGUI.GUI.GUIBlast;


public class CtrlPath extends SwingWorker<ArrayList<String[]>, ProgressDataPath> {


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
	GUIBlast guiBlast;
	
	/**
	 * 
	 * @param blast
	 * @param taxID
	 * @param StaxID
	 * @param evalue
	 * @param guiBlast
	 */
	public CtrlPath(boolean blast, int taxID, int StaxID, double evalue,GUIBlast guiBlast) {
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
				ArrayList<String[]> lstmpAnno = KegPathQuery.getGenPath(geneID, taxID, blast, StaxID, evalue);
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