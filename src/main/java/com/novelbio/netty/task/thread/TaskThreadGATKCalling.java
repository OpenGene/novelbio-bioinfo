package com.novelbio.netty.task.thread;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.novelbio.base.SpringFactory;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.gatk.GATKCalling;
import com.novelbio.web.model.task.TaskInfo;

public class TaskThreadGATKCalling implements Runnable {
	private static final Logger logger = Logger.getLogger(TaskThreadGATKCalling.class);
	private TaskInfo taskInfo;

	public TaskThreadGATKCalling(TaskInfo taskInfo) {
		this.taskInfo = taskInfo;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		logger.info("GatkDuplicate任务 " + taskInfo.getTaskId() + " 线程已启动");
		MongoTemplate mongoTemplate = (MongoTemplate) SpringFactory.getFactory().getBean("mongoTemplate");
		FileSystem fileSystem = (FileSystem) SpringFactory.getFactory().getBean("hadoopFs");
		String tempDir = FileOperate.addSep(SpringFactory.getGlobeParameters().get("tempDir")) + "temp" + DateUtil.getDateAndRandom();
		Path inputPath = new Path(taskInfo.getTaskData());
		Path ouputPath = new Path(FileOperate.addSep(tempDir) + "abc.bam");
		Path fastaPath = new Path(FileOperate.addSep(tempDir) + "chrAll.fa");
		try {
			fileSystem.copyToLocalFile(inputPath,ouputPath);
			fileSystem.copyToLocalFile(new Path("/chrAll.fa"), fastaPath);
			fileSystem.copyToLocalFile(new Path("/chrAll.dict"), new Path(FileOperate.addSep(tempDir) + "/chrAll.dict"));
		} catch (IOException e) {
			logger.info("GatkDuplicate任务 " + taskInfo.getTaskId() + "出现异常:" + e.getMessage());
		}
		GATKCalling gatkCalling = new GATKCalling(ouputPath.toString(),fastaPath.toString(),tempDir);
		try {
			fileSystem.copyFromLocalFile(new Path(gatkCalling.callingByGATK()), new Path("/"));
		} catch (IOException e) {
			logger.info("GatkDuplicate任务 " + taskInfo.getTaskId() + "出现异常:" + e.getMessage());
		}
		FileOperate.delFolder(tempDir);
	}
}
