package com.novelbio.netty.task.thread;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.newDebugLog;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.novelbio.base.SpringFactory;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.fileOperate.ZipOperate;
import com.novelbio.gatk.GATKRealign;
import com.novelbio.species.Species;
import com.novelbio.web.model.task.TaskInfo;

public class TaskThreadGATKRealign implements Runnable {
	private static final Logger logger = Logger.getLogger(TaskThreadGATKRealign.class);
	private TaskInfo taskInfo;
	private Species species;
	public TaskThreadGATKRealign(TaskInfo taskInfo) {
		this.taskInfo = taskInfo;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		logger.info("GATKRealign任务 " + taskInfo.getTaskId() + " 线程已启动");
		MongoTemplate mongoTemplate = (MongoTemplate) SpringFactory.getFactory().getBean("mongoTemplate");
		FileSystem fileSystem = (FileSystem) SpringFactory.getFactory().getBean("hadoopFs");
		//从全局变量里获得临时目录名
		String tempDir = FileOperate.addSep(SpringFactory.getGlobeParameters().get("tempDir")) + "temp" + DateUtil.getDateAndRandom();
		//从taskInfo中读取参数输入文件的路径
		Path inputPath = new Path(taskInfo.getTaskData().get("inputPath"));
		//在临时目录中创建一个输入临时文件
		Path inputTempPath = new Path(FileOperate.addSep(tempDir) + "forRealign.bam");
		//在临时目录下的public文件夹下查找有没有对应的chr文件,没有的话就从hdfs中下载下来
		String fastaFile =  FileOperate.addSep(tempDir) + FileOperate.getFileName(species.getChromFaPath());
		try {
			if(!FileOperate.isFileExist(fastaFile)){
				Path fastaPath = new Path(fastaFile);
				fileSystem.copyToLocalFile(new Path(species.getChromFaPath()), fastaPath);
			}
			//把用户输入的文件复制为本地的输入临时文件
			fileSystem.copyToLocalFile(inputPath,inputTempPath);
		} catch (IOException e) {
			logger.info("GATKRealign任务 " + taskInfo.getTaskId() + "出现异常:" + e.getMessage());
		}
		//开始做realign并把最后结果上传到hdfs上
		GATKRealign gatkRealign = new GATKRealign(inputTempPath.toString(),fastaFile,tempDir);
		try {
			fileSystem.copyFromLocalFile(new Path(gatkRealign.realign()), new Path(FileOperate.addSep(taskInfo.getRootPath()) + "result"));
		} catch (IOException e) {
			logger.info("GATKRealign任务 " + taskInfo.getTaskId() + "出现异常:" + e.getMessage());
		}
		FileOperate.delFolder(tempDir);
	}
}
