package com.novelbio.analysis.seq.sam;

import com.novelbio.base.multithread.RunProcess;

public class SamToBed extends RunProcess<Long>{
	SamFile samFile;
	boolean isPairEndConvert = false;
	public void setSamFile(SamFile samFile) {
		this.samFile = samFile;
	}
	/**�Ƿ���˫�˵Ľ�����з��� */
	public void setIsPairEndConvert(boolean isPairEndConvert) {
		if (!samFile.isPairend()) {
			this.isPairEndConvert = false;
		}
		else {
			this.isPairEndConvert = isPairEndConvert;
		}
	}
	@Override
	protected void running() {
		// TODO Auto-generated method stub
		
	}
	
	private void toBedSE() {
		
	}
}

