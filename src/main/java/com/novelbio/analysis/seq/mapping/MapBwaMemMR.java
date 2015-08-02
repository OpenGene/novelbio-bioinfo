package com.novelbio.analysis.seq.mapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.seq.fastq.ExceptionFastq;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamToBam;
import com.novelbio.analysis.seq.sam.SamToBam.SamToBamOutMR;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.StreamIn;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

@Component
@Scope("prototype")
public class MapBwaMemMR extends MapBwaMem {
	boolean isPairend = false;
	InputStream ins;
	OutputStream outs;
	
	public static void main(String[] args) throws IOException {
		MapBwaMemMR mapBwaMemMR = new MapBwaMemMR();
		mapBwaMemMR.setChrIndex("/media/nbfs/nbCloud/public/nbcplatform/testhadoop/chrAll.fa");
		mapBwaMemMR.setStaggeredPairingFQ(false);
		
		mapBwaMemMR.setIns(FileOperate.getInputStream("/media/winE/test/96_filtered_2.fq_sub_test.fastq"));
		mapBwaMemMR.setOuts(System.out);
		
		mapBwaMemMR.mapReads();
	}
	
	public void setIns(InputStream ins) {
		this.ins = ins;
	}
	public void setOuts(OutputStream outs) {
		this.outs = outs;
	}
	protected List<String> getLsCmd() {
		if (!StringOperate.isRealNull(getStaggeredPairingFQParam())) {
			isPairend = true;
		}
		
		List<String> lsCmd = new ArrayList<String>();
		lsCmd.add(exePath + "bwa");
		lsCmd.add("mem");
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		addStringParam(lsCmd, getIsOutputSingleReads());
		ArrayOperate.addArrayToList(lsCmd, getMinSeedLenParam());
		ArrayOperate.addArrayToList(lsCmd, getBandWidthParam());
		ArrayOperate.addArrayToList(lsCmd, getzDropoffParam());
		ArrayOperate.addArrayToList(lsCmd, getSeedSplitRatioParam());
		ArrayOperate.addArrayToList(lsCmd, getMaxOccParam());
		ArrayOperate.addArrayToList(lsCmd, getMatchScoreParam());
		ArrayOperate.addArrayToList(lsCmd, getMmPenaltyParam());
		ArrayOperate.addArrayToList(lsCmd, getGapOpenPenParam());
		ArrayOperate.addArrayToList(lsCmd, getGapExtPenParam());
		ArrayOperate.addArrayToList(lsCmd, getClipPenParam());
		ArrayOperate.addArrayToList(lsCmd, getUnpairPenParam());
		ArrayOperate.addArrayToList(lsCmd, getRGlineParam());
		ArrayOperate.addArrayToList(lsCmd, getMinMapQuality());
		addStringParam(lsCmd, getMarkShorterSplitAsSecondary());
		addStringParam(lsCmd, getHardClippingParam());
		addStringParam(lsCmd, getStaggeredPairingFQParam());
		addStringParam(lsCmd, getSwDataParam());
		lsCmd.add(chrFile);
		lsCmd.add("-");
		return lsCmd;
	}
	
	@Override
	protected SamFile mapping() {
		List<String> lsCmd = getLsCmd();
		
		BamStreamIn bamStreamIn = new BamStreamIn();
		bamStreamIn.setInStream(ins);
		bamStreamIn.setPairend(isPairend);
		
		cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setGetCmdInStdStream(true);
		cmdOperate.setInputStream(bamStreamIn);
		Thread thread = new Thread(cmdOperate);
		thread.start();
		InputStream inputStream = cmdOperate.getStreamStd();
		
		SamToBam sam2SysOutMR = new SamToBam();		
		sam2SysOutMR.setInStream(inputStream);
		
		SamToBamOutMR samWrite2SysOutMR = new SamToBamOutMR();
		samWrite2SysOutMR.setOutputStream(outs);
		sam2SysOutMR.setSamWriteTo(samWrite2SysOutMR);
		
		sam2SysOutMR.setIsPairend(isPairend);
		sam2SysOutMR.readInputStream();
		sam2SysOutMR.writeToOs();
		return null;
	}
	
	public void run() {
		List<String> lsListCmd = new ArrayList<String>();
		lsListCmd.addAll(getLsCmd());
		CmdOperate cmdOperate = new CmdOperate(lsListCmd);
		cmdOperate.run();
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
					i++;
				} else {
					fqRight = fQRecord;
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

