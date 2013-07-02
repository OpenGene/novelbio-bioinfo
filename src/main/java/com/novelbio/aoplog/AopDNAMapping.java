package com.novelbio.aoplog;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.seq.mapping.MapDNAint;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileStatistics;
import com.novelbio.base.fileOperate.FileOperate;

@Component
@Aspect
public class AopDNAMapping {
	private static final Logger logger = Logger.getLogger(AopDNAMapping.class);
	/** 超过50条染色体就不画这个图了 */
	private static int chrNumMax = 50;
	@Around("execution (* com.novelbio.analysis.seq.mapping.MapDNAint.mapReads(..)) && target(mapDNAint)")
	public void aroundMapping(ProceedingJoinPoint pjp, MapDNAint mapDNAint) {
		SamFile samFile = null;
		try {
			samFile = (SamFile) pjp.proceed();
		} catch (Throwable e) {
			logger.error("aopDNAMapping拦截失败！");
		}
		if (!(samFile != null && FileOperate.isFileExistAndBigThanSize(samFile.getFileName(), 0))) {
			logger.error("samFile不存在，aopDNAMapping拦截失败！");
			return;
		}
		
		MappingBuilder mappingBuilder = new MappingBuilder(samFile, mapDNAint, chrNumMax);
		mappingBuilder.writeInfo();
		
		//TODO 保存执行的命令
		mappingBuilder.saveCmdMapping();
	}
	
	/** 仅用于单元测试 */
	public void test(SamFile samFile, MapDNAint mapDNAint) {
		if (!(samFile != null && FileOperate.isFileExistAndBigThanSize(samFile.getFileName(), 0))) {
			logger.error("samFile不存在，aopDNAMapping拦截失败！");
			return;
		}
		
		MappingBuilder mappingBuilder = new MappingBuilder(samFile, mapDNAint, chrNumMax);
		mappingBuilder.writeInfo();
		
		//TODO 保存执行的命令
		mappingBuilder.saveCmdMapping();
	}
	
}

class MappingBuilder extends SamStatisticsBuilder {
	/** 运行的cmd命令 */
	private String cmdMapping;
	
	public MappingBuilder(SamFile samFile, MapDNAint mapDNAint, int chrNumMax) {
		super(chrNumMax);
		for (AlignmentRecorder alignmentRecorder : mapDNAint.getLsAlignmentRecorders()) {
			if (alignmentRecorder instanceof SamFileStatistics) {
				setSamFileStatistics(samFile, (SamFileStatistics) alignmentRecorder);
				break;
			}
		}
		cmdMapping = mapDNAint.getCmdMapping();
	}

	
	
	public boolean saveCmdMapping() {
		// TODO 把命令持久化起来
		System.out.println(cmdMapping);
		return true;
	}
}


