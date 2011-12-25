package com.novelbio.analysis.guiRun.BlastGUI.control;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;


import com.google.common.base.Splitter;
import com.novelbio.analysis.guiRun.BlastGUI.GUI.GUIBlast;
import com.novelbio.analysis.guiRun.BlastGUI.GUI.GuiBlastJpanel;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.CopedID;



public class CtrlBlastAnno extends SwingWorker<ArrayList<String[]>, ProgressData>
{
	/**
	 * 是否需要blast
	 */
	boolean blast = false;
	/**
	 * 查找物种
	 */
	int taxID = 0;
	/**
	 * blast物种
	 */
	int StaxID = 0;
	/**
	 * blast的evalue
	 */
	double evalue = 100;
	
	/**
	 * 界面对象
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
//	public CtrlBlastAnno(boolean blast, int taxID, int StaxID, double evalue,GUIBlast guiBlast) {
	public CtrlBlastAnno(boolean blast, int taxID, int StaxID, double evalue,GuiBlastJpanel guiBlast) {
		this.blast = blast;
		this.taxID = taxID;
		this.StaxID = StaxID;
		this.evalue = evalue;
		this.guiBlast =guiBlast;
		
	}
	
	
	List<String> lsGeneID = null;
	/**
	 * 准备工作，将geneID读入内存同时准备查找，同时返回总共查找的数量，给进度条计数
	 * @return
	 * @throws Exception
	 */
	public int prepare(String splitRegx, int colNum) throws Exception {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		ArrayList<String[]> lsGeneInfo = txtReadandWrite.ExcelRead(splitRegx, 1, 1, txtReadandWrite.ExcelRows(), 1, 1);
		for (String[] strings : lsGeneInfo) {
			lsGeneID.add(strings[0]);
		}
		return lsGeneInfo.size();
	}
	
	/**
	 * 准备工作，将geneID读入内存同时准备查找，同时返回总共查找的数量，给进度条计数
	 * @return
	 * @throws Exception
	 */
	public int prepare(List<String> lsGeneID) {
		this.lsGeneID = lsGeneID;
		return lsGeneID.size();
	}
	
	/**
	 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
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
			length = 6;
		else 
			length = 2; 
		
		for (int i = 0; i<lsGeneID.size(); i++) {
			String geneID = lsGeneID.get(i).trim();
		
			try {
				CopedID copedID = new CopedID(geneID, taxID, false);
//				String[] tmpAnno = ServAnno.getAnno(geneID, taxID, blast, StaxID, evalue);
				String[] tmpAnno = copedID.getAnno(blast);
				if (tmpAnno == null) {
					tmpAnno = new String[length];
					for (int j = 0; j < tmpAnno.length; j++) {
						tmpAnno[j] = "";
					}
				}
				String[] tmpanno2 = new String[tmpAnno.length+1];
				for (int j = 1; j < tmpanno2.length; j++) {
					tmpanno2[j]=tmpAnno[j-1];
				}
				tmpanno2[0]=lsGeneID.get(i);
				
				String[] tmpanno3 = null;
				if(blast)
				{
					tmpanno3 = new String[9];
					int j = 0;
					for (int m = 0; m < tmpanno2.length; m++) {
						if (m == 3) {
							continue;
						}
						tmpanno3[j] = tmpanno2[m]; j++;
					}
				}
				else {
					tmpanno3 =  tmpanno2;
				}

				ProgressData progressData = new ProgressData();
				progressData.rowNum = i;
				progressData.tmpInfo = tmpanno3;
				publish(progressData);
				lsDesp.add(tmpanno3);
//				Thread.sleep(100);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return lsDesp;
	}
	
	@Override
	public void process(List<ProgressData> data)
	{
		if (isCancelled()) {
			return;
		}
		for (ProgressData progressData : data) {
			if (guiBlast.getJProgressBar1().getValue()<progressData.rowNum) {
				guiBlast.getJProgressBar1().setValue(progressData.rowNum);
			}
			guiBlast.getJTabAnnol().addRow(progressData.tmpInfo);
		}
	}
	
	public void done() {
		int maxValue = guiBlast.getJProgressBar1().getMaximum();
		guiBlast.getJProgressBar1().setValue(maxValue);
		guiBlast.getJBtnSaveAno().setEnabled(true);
		guiBlast.getJBtnAnno().setEnabled(true);
		guiBlast.getJLblCond().setText("Complete");
		try {
			guiBlast.setLsAnno(this.get());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}

class ProgressData
{
	public int rowNum;
	public String[] tmpInfo;
}

