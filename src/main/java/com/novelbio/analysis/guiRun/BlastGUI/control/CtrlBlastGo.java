package com.novelbio.analysis.guiRun.BlastGUI.control;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.novelbio.analysis.guiRun.GoPathScr2Trg.GUI.GuiBlastJpanel;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modcopeid.CopedID;


public class CtrlBlastGo extends SwingWorker<ArrayList<String[]>, ProgressDataGo>
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
	 * Go的类型
	 */
	String GoClass = Go2Term.GO_BP;
	/**
	 * 界面对象
	 */
	GuiBlastJpanel guiBlast;
	/**
	 * 
	 * @param blast
	 * @param taxID
	 * @param StaxID
	 * @param evalue
	 * @param guiBlast
	 */
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
			length = 13;
		else 
			length = 5; 
		
		for (int i = 0; i<lsGeneID.size(); i++)
		{
			String geneID = lsGeneID.get(i).trim();
			ArrayList<String[]> lsTmpInfo = new ArrayList<String[]>();
			try {
				ArrayList<String[]> lstmpAnno = getLsGeneGo(geneID,taxID,GoClass, blast, evalue, StaxID);
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
				
					//结果中包含了第2列和第9列的geneID信息
					//没有blast为stirng[5]
					//blast为string[13]
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
				e.printStackTrace();
			}
		}
		return lsDesp;
	}
	
	/**
	 * @param accID
	 * @param taxID
	 * @param GoClass * GO_BP<br>
	 * GO_CC<br>
	 * GO_MF<br>
	 * GO_ALL<br>
	 * @param blast
	 * @param evalue
	 * @param StaxID
	 * @return
	 * <b>当blast为false时</b><br>
	 * ArrayList-String[7] <br>
其中：0: queryID<br>
1: uniID<br>
2: symbol<br>
3: description<br>
4: GOID<br>
5: GOTerm<br>
6: Evidence<br>
	 * <b>如果没找到，则返回null，不会返回一个空的list</b><br>
	 * 	 * <b>当blast为true时</b><br>
	 * ArrayList-String[15]
	 * 其中：<br>
	 * 0: queryID<br>
	 * 1: queryGeneID<br>
	 * 2: querySymbol<br>
	 * 3: Description<br>
	 * 4: GOID<br>
	 * 5: GOTerm<br>
	 * 6: GO可信度<br>
	 * 7: blastEvalue<br>
	 * 8: taxID<br>
	 * 9: subjectGeneID<br>
	 * 10: subjectSymbol<br>
	 * 11: Description<br>
	 * 12: GOID<br>
	 * 13: GOTerm<br>
	 * 14: GO可信度<br>
	 * <b>如果没找到GO信息，则返回null，不会返回一个空的list</b>
	 */
	private ArrayList<String[]> getLsGeneGo(String accID, int taxID,String GoClass, boolean blast, double evalue, int StaxID)
	{
		String[] tmpAccIDInfo = new String[]{accID, "", "",""};
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		CopedID copedID = new CopedID(accID, taxID);
		if (copedID.getIDtype().equals(CopedID.IDTYPE_ACCID)) {
			return null;
		}
		tmpAccIDInfo[1] = copedID.getGenUniID();
		tmpAccIDInfo[2] = copedID.getSymbol();tmpAccIDInfo[3] = copedID.getDescription();
		copedID.setBlastInfo(evalue, StaxID);
		//本基因的GO信息
		ArrayList<AGene2Go> lsGOthis = copedID.getGene2GO(GoClass);
		for (AGene2Go aGene2Go : lsGOthis) {
			String[] tmpResult = ArrayOperate.copyArray(tmpAccIDInfo, 7);
			tmpResult[4] = aGene2Go.getGOID(); tmpResult[5] = aGene2Go.getGOTerm(); tmpResult[6] = aGene2Go.getEvidence();
			lsResult.add(tmpResult);
		}
		if (!blast) {
			if (lsResult.size() == 0) {
				return null;
			}
			return lsResult;
		}
		//blast基因的GO信息
		ArrayList<String[]> lsResultBlast = new ArrayList<String[]>();
		
		CopedID copedIDblast = copedID.getCopedIDBlast();
		if (copedIDblast == null) {
			for (String[] strings : lsResult) {
				String[] result = ArrayOperate.copyArray(strings, 15);
				lsResultBlast.add(result);
			}
			return lsResultBlast;
		}
		ArrayList<AGene2Go> lsGOBlast = copedIDblast.getGene2GO(GoClass);
		for (int i = 0; i < lsGOBlast.size(); i++) {
			if (lsResult == null)
				lsResult = new ArrayList<String[]>();
			String[] tmpResultBlast = new String[15];
			//初始化
			for (int j = 0; j < tmpResultBlast.length; j++) {
				tmpResultBlast[j] = "";
			}
			if (i < lsResult.size()) {
				for (int j = 0; j < lsResult.get(i).length; j++) {
					tmpResultBlast [j] = lsResult.get(i)[j];
				}
			}
			else {
				tmpResultBlast[0] = accID; tmpResultBlast[1] = copedID.getGenUniID();
				tmpResultBlast[2] = copedID.getSymbol();tmpResultBlast[3] = copedID.getDescription();
			}
			tmpResultBlast[7] = copedID.getLsBlastInfos().get(0).getEvalue() + "";
			tmpResultBlast[8] = copedIDblast.getTaxID() + "";
			tmpResultBlast[9] = copedIDblast.getGenUniID();
			tmpResultBlast[10] = copedIDblast.getSymbol();
			tmpResultBlast[11] = copedIDblast.getDescription();
			tmpResultBlast[12] = lsGOBlast.get(i).getGOID();
			tmpResultBlast[13] = lsGOBlast.get(i).getGOTerm();
			tmpResultBlast[14] = lsGOBlast.get(i).getEvidence();;
			lsResultBlast.add(tmpResultBlast);
		}
		
		return lsResultBlast;
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