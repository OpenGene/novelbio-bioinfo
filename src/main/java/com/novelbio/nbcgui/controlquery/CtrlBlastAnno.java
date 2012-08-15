package com.novelbio.nbcgui.controlquery;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.nbcgui.GUI.GuiBlastJpanel;



public class CtrlBlastAnno extends SwingWorker<ArrayList<String[]>, ProgressData>
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
	public CtrlBlastAnno(boolean blast, int taxID, int StaxID, double evalue,GuiBlastJpanel guiBlast) {
		this.blast = blast;
		this.taxID = taxID;
		this.StaxID = StaxID;
		this.evalue = evalue;
		this.guiBlast =guiBlast;
		
	}
	
	
	List<String> lsAccID = null;
	
	/**
	 * ׼����������geneID�����ڴ�ͬʱ׼�����ң�ͬʱ�����ܹ����ҵ�������������������
	 * @return
	 * @throws Exception
	 */
	public int prepare(List<String> lsGeneID) {
		this.lsAccID = lsGeneID;
		return lsGeneID.size();
	}
	
	/**
	 * �����ļ������ļ��ָ�����Լ��ڼ��У���ø��еĻ���ID
	 * @param fileName
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<String[]> doInBackground() throws Exception {
		System.out.println("run");
		ArrayList<String[]> lsDesp = new ArrayList<String[]>();
		int length = 0;
		if (blast) 
			length = 6;
		else 
			length = 2;
		int i = 0;
		for (String accID : lsAccID) {
			i ++;
			accID = accID.trim();
			try {
				GeneID copedID = new GeneID(accID, taxID);
				copedID.setBlastInfo(1e-10, StaxID);
				String[] tmpAnno = copedID.getAnno(blast);
				if (tmpAnno == null) {
					tmpAnno = new String[length];
					for (int j = 0; j < tmpAnno.length; j++) {
						tmpAnno[j] = "";
					}
				}
				//��tmpAnno����ǰ�����accID����β������keggID
				/////////ȥ��������һ��/////////////////////////////
				ArrayList<int[]> lsIndelInfo = new ArrayList<int[]>();
				lsIndelInfo.add(new int[]{0,2});
				lsIndelInfo.add(new int[]{length, 1});
				if (blast) {
					lsIndelInfo.add(new int[]{2,-1});
					lsIndelInfo.add(new int[]{3,1});
					lsIndelInfo.add(new int[]{4,1});
				}
				String[] tmpResult = ArrayOperate.indelElement(tmpAnno, lsIndelInfo, "");
				tmpResult[0] = accID;	//��tmpAnno����ǰ�����accID
				tmpResult[1] = copedID.getAccIDDBinfo();//�ڶ��м���Ĭ�����ݿ��ID
				if (!blast) {
					tmpResult[tmpResult.length - 1] = copedID.getKeggInfo().getKegID(); //��β������keggID
				}
				else {
					tmpResult[4] = copedID.getKeggInfo().getKegID(); //��β������keggID
					GeneID copedIDblast = copedID.getGeneIDBlast();
					if (copedIDblast != null ) {
						tmpResult[6] = copedIDblast.getAccIDDBinfo();//����Ĭ�����ݿ��ID
						tmpResult[tmpResult.length - 1] = copedIDblast.getKeggInfo().getKegID(); //��β������keggID
					}
				}
				///////////////////////////////////////////////////
				ProgressData progressData = new ProgressData();
				progressData.rowNum = i;
				progressData.tmpInfo = tmpResult;
				publish(progressData);
				lsDesp.add(tmpResult);
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

