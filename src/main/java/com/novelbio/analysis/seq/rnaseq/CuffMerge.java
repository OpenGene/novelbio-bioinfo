package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;


/** cuffmerge第437行的
 *  shutil.move("tmp_meta_asm.combined.gtf", output_dir + "/merged.gtf")
 *  修改为
    try: 
        shutil.move("tmp_meta_asm.combined.gtf", output_dir + "/merged.gtf")
    except:
        info=sys.exc_info() 
        print info[0],":",info[1] 
 * @author zong0jie
 *
 */
public class CuffMerge {
	/** 重新计算是否使用以前的结果 */
	boolean isUseOldResult = true;
	
	/** ref的gtf */
	String refGtf;
	/** ref的染色体单个fasta文件 */
	String refChrFa;
	
	/** 待合并的gtf文件 */
	List<String> lsGtfTobeMerged = new ArrayList<String>();
	
	String outputPrefix;
	
	int threadNum = 4;
	
	String exePath = "";
	
	String tmpGtfRecord;
	public void setIsUseOldResult(boolean isUseOldResult) {
		this.isUseOldResult = isUseOldResult;
	}
	/**
	 * 设定cuffdiff所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.exePath = "";
		} else {
			this.exePath = FileOperate.addSep(exePath);
		}
	}
	
	public void setRefChrFa(String refChrFa) {
		this.refChrFa = refChrFa;
	}
	public void setRefGtf(String refGtf) {
		this.refGtf = refGtf;
	}
	/**
	 * <b>直接添加GTF文件会出错</b><br>
	 * 添加的务必是从cufflinks或者cuffcompare所得到的GTF文件
	 */
	public void setLsGtfTobeMerged(List<String> lsGtfTobeMerged) {
		this.lsGtfTobeMerged = lsGtfTobeMerged;
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	public void setOutputPrefix(String outputPrefix) {
		this.outputPrefix = outputPrefix;
	}
	
	private String[] getRefGtf() {
		if (FileOperate.isFileExistAndBigThanSize(refGtf, 0)) {
			return new String[]{"-g", refGtf};
		}
		return null;
	}
	private String[] getRefChrFa() {
		if (FileOperate.isFileExistAndBigThanSize(refChrFa, 0)) {
			return new String[]{"-s", refChrFa};
		}
		return null;
	}
	
	private String getGtfsTobeMerged() {
		tmpGtfRecord = outputPrefix + "gtfList" + DateUtil.getDateAndRandom();
		TxtReadandWrite txtWrite = new TxtReadandWrite(tmpGtfRecord , true);
		for (String string : lsGtfTobeMerged) {
			txtWrite.writefileln(string);
		}
		txtWrite.close();
		return tmpGtfRecord;
	}
	
	private String[] getThreadNum() {
		return new String[]{"-p", threadNum+""};
	}
	
	private String[] getOutPrefixCMD() {
		return new String[]{"-o", outputPrefix + ""};
	}
	
	public String runCuffmerge() {
		String outMergedFile = FileOperate.addSep(outputPrefix) + "merged.gtf"; 
		if (isUseOldResult
				&& FileOperate.isFileExistAndBigThanSize(outMergedFile , 0)
				) {
			return outMergedFile;
		}
		
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		cmdOperate.setGetLsErrOut();
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			String errInfo = cmdOperate.getErrOut();
			FileOperate.DeleteFileFolder(tmpGtfRecord);
			throw new ExceptionCmd("cuffmerge error:\n" + cmdOperate.getCmdExeStrReal() + "\n" + errInfo);
		}
		FileOperate.DeleteFileFolder(tmpGtfRecord);
		return outMergedFile;
	}
	
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "cuffmerge");
		ArrayOperate.addArrayToList(lsCmd, getRefChrFa());
		ArrayOperate.addArrayToList(lsCmd, getRefGtf());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		ArrayOperate.addArrayToList(lsCmd, getOutPrefixCMD());
		lsCmd.add(getGtfsTobeMerged());
		return lsCmd;
	}
	
}
