package com.novelbio.netty.task.thread;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.rutils.Medme;
import com.novelbio.web.model.task.TaskInfo;

public class TaskThreadRScript implements Runnable{
	private static final Logger logger = Logger.getLogger(TaskThreadRScript.class);
	private TaskInfo taskInfo;

	public TaskThreadRScript(TaskInfo taskInfo) {
		this.taskInfo = taskInfo;
	}

	@Override
	public void run() {
		JSONObject jsonObject = JSONObject.fromObject(taskInfo.getTaskData());
		Medme medme = new Medme("MEDMERscript.txt");
		List<String> lsRequiredFiles = new ArrayList<String>();
		for(String file : lsRequiredFiles){
			String files[] = FileOperate.getFileNameSep(file);
			String newFile = FileOperate.addSep(medme.getTempFolder()) + files[0] + files [1];
			lsRequiredFiles.add(newFile);
		}
		//TODO 把hdfs上的两件文件copy到本地临时文件夹下
		medme.setLsRequiredFiles(lsRequiredFiles);
		medme.setSpecies((String)jsonObject.get("species"));
		medme.renderTemp();
		medme.runR();
		logger.info("R脚本任务 " + taskInfo.getTaskId() + " 线程已启动");
	}
}
