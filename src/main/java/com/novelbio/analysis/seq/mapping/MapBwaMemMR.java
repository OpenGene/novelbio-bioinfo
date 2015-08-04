package com.novelbio.analysis.seq.mapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.seq.fastq.ExceptionFastq;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.sam.SamToBam;
import com.novelbio.analysis.seq.sam.SamToBam.SamToBamOutMR;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.StreamIn;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

@Component
@Scope("prototype")
public class MapBwaMemMR {
	private static final Logger logger = Logger.getLogger(MapBwaMemMR.class);
	boolean isPairend = false;
	InputStream ins;
	OutputStream outs;
	String[] cmds;
	public static void main(String[] args) throws IOException {
		MapBwaMemMR mapBwaMemMR = new MapBwaMemMR();
		logger.info(ArrayOperate.cmbString(args, " "));
//		String ss = "bwa mem -t 8 -a -p  /media/nbfs/nbCloud/public/nbcplatform/genome/index/bwa/3702/tair10/Chr_Index/chrAll.fa -";
//		args = ss.split(" ");
		mapBwaMemMR.setCmds(args);
		
		mapBwaMemMR.setIns(System.in);
		mapBwaMemMR.setOuts(System.out);
		mapBwaMemMR.mapping();
	}
	
	public void setIns(InputStream ins) {
		this.ins = ins;
	}
	public void setOuts(OutputStream outs) {
		this.outs = outs;
	}
	
	public void setCmds(String[] cmds) {
		this.cmds = cmds;
	}
	
	private List<String> getCmds() {
		List<String> lsCmd = new ArrayList<>();
		for (String param : cmds) {
			if (StringOperate.isRealNull(param)) continue;
			if (param.equals("-p")) {
				isPairend = true;
			}
			lsCmd.add(param);
		}
		return lsCmd;
	}
	
	private void mapping() {
		List<String> lsCmd = getCmds();
		
		BamStreamIn bamStreamIn = new BamStreamIn();
		bamStreamIn.setInStream(ins);
		bamStreamIn.setPairend(isPairend);
		
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setGetCmdInStdStream(true);
		cmdOperate.setInputStream(bamStreamIn);
		Thread thread = new Thread(cmdOperate);
		thread.start();
		InputStream inputStream = cmdOperate.getStreamStd();
		
		SamToBam sam2SysOutMR = new SamToBam();		
		sam2SysOutMR.setInStream(inputStream);
		sam2SysOutMR.setIsAddMultiFlag(true);
		
		SamToBamOutMR samWrite2SysOutMR = new SamToBamOutMR();
		samWrite2SysOutMR.setOutputStream(outs);
		sam2SysOutMR.setSamWriteTo(samWrite2SysOutMR);
		
		sam2SysOutMR.setIsPairend(isPairend);
		sam2SysOutMR.readInputStream();
		sam2SysOutMR.writeToOs();
	}
	
	public static class BamStreamIn extends StreamIn {
		boolean isPairend = false;
		
		/** 默认false */
		public void setPairend(boolean isPairend) {
			this.isPairend = isPairend;
		}
		
		protected void running() {
			try {
				FastQ fastQ = new FastQ(inStream);
				fastQ.setCheckFormat(false);
				if (isPairend) {
					runPE(fastQ);
				} else {
					runSE(fastQ);
				}
			} catch (Exception e) {
				throw e;
			} finally {
				try {
					processInStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		
		private void runPE(FastQ fastQ) {
			Iterator<FastQRecord> itFqPE = fastQ.readlines().iterator();
			//左端序列
			FastQRecord fqLeft = new FastQRecord();fqLeft.setName("");
			//右端序列
			FastQRecord fqRight = null;
			
			int i = 0;
			boolean isPairend = false;
			while (itFqPE.hasNext()) {
				FastQRecord fQRecord = itFqPE.next();
				if (!fqLeft.getName().split(" ")[0].equals(fQRecord.getName().split(" ")[0])) {
					fqLeft = fQRecord;
					isPairend = false;
					i++;
				} else {
					fqRight = fQRecord;
					isPairend = true;
					break;
				}
				if (i > 100) {
					break;
				}
			}
			
			if (!isPairend) throw new ExceptionFastq("input file is not pairend");
			
			writePE(fqLeft, fqRight);

			while (itFqPE.hasNext()) {
				fqLeft = itFqPE.next();
				if (itFqPE.hasNext()) {
					fqRight = itFqPE.next();
					writePE(fqLeft, fqRight);
				}
			}
		}
		
		private void writePE(FastQRecord fqLeft, FastQRecord fqRight) {
			try {
				processInStream.write((fqLeft.toString() + TxtReadandWrite.ENTER_LINUX).getBytes("UTF-8"));
				processInStream.write((fqRight.toString() + TxtReadandWrite.ENTER_LINUX).getBytes("UTF-8"));
			} catch (Exception e) {
				throw new ExceptionFastq(e);
			
			}

		}
		
		
		private void runSE(FastQ fastQ) {
			for (FastQRecord fastQRecord : fastQ.readlines()) {
				try {
					processInStream.write((fastQRecord.toString() + TxtReadandWrite.ENTER_LINUX).getBytes("UTF-8"));
				} catch (Exception e) {
					throw new ExceptionFastq(e);
				}
			}
		}
	}

}

