package com.novelbio.bioinfo.tools.kegarray;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.GridLayout;
import java.awt.ScrollPane;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.commons.lang3.StringUtils;

/**
 * 下载kegg图片
 * 
 * @author renyaoxiang
 * @date 2017年2月17日
 * 
 */
public class DownLoadKeggPngFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private Map<String, String> downloadingURL = Collections.synchronizedMap(new HashMap<>());
	private Map<String, String> downloadedURL = Collections.synchronizedMap(new HashMap<>());
	private TextField sourcePath;
	private TextField storePath;
	private Button downloadButton;
	private JPanel downLoadKeggPngPanel;
	private JTextArea message;
	private ScrollPane messageScrollPane;
	ExecutorService runner = Executors.newFixedThreadPool(10);

	private void closeJFrame() {
		runner.shutdown();
	}

	private DownLoadKeggPngFrame() {

		this.setSize(600, 400);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeJFrame();
			}
		});
		this.setLocationRelativeTo(null);
		this.setLayout(new BorderLayout());

		downLoadKeggPngPanel = new JPanel();
		this.add(downLoadKeggPngPanel, BorderLayout.CENTER);
		sourcePath = new TextField();
		storePath = new TextField();
		downloadButton = new Button("下载");
		message = new JTextArea("");
		messageScrollPane = new ScrollPane();
		messageScrollPane.add(message);
		downloadButton.addActionListener((e) -> {
			String sourcePathString = sourcePath.getText();
			String storePathString = storePath.getText();
			this.download(sourcePathString, storePathString);
		});
		downLoadKeggPngPanel.setLayout(new GridLayout(4, 1));

		downLoadKeggPngPanel.add(sourcePath);
		downLoadKeggPngPanel.add(storePath);
		downLoadKeggPngPanel.add(downloadButton);
		downLoadKeggPngPanel.add(messageScrollPane);
		message.setEditable(false);
		this.setVisible(true);
	}

	private void download(String sourcePathString, String storePathString) {
		if (downloadedURL.containsKey(sourcePathString)) {
			appendMessage("已经下载完成.");
			return;
		}
		if (downloadingURL.containsValue(storePathString) || downloadedURL.containsValue(storePathString)) {
			appendMessage("存储路径已经被使用.");
			return;
		}
		if (downloadingURL.containsKey(sourcePathString)) {
			appendMessage("路径正在下载中.");
			return;
		}
		Path path = Paths.get(storePathString);
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
				appendMessage("路径不存在,正在创建.");
			} catch (IOException e) {
				appendMessage("创建文件失败");
				return;
			}
		}
		if (!Files.isDirectory(path)) {
			appendMessage(storePathString + "是一个文件，请重新输入保存路径");
			return;
		}
		if (StringUtils.isBlank(sourcePathString) || StringUtils.isBlank(storePathString)) {
			appendMessage("请确认数据格式是否正确");
			return;
		}
		setEnable(false);
		runner.execute(() -> {
			try {
				downloadingURL.put(sourcePathString, storePathString);
				DownKeggPngUnit instance = new DownKeggPngUnit(sourcePathString, storePathString);
				appendMessage("开始下载:" + sourcePathString);
				instance.startDownload();
				downloadingURL.remove(sourcePathString);
				appendMessage("下载完成:" + sourcePathString);
				downloadedURL.put(sourcePathString, storePathString);
			} catch (Throwable t) {
				downloadingURL.remove(sourcePathString);
				appendMessage("下载失败:" + sourcePathString);
			}
			setEnable(true);
		});
	}

	private synchronized void appendMessage(String newMessage) {
		String dateString = new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date());
		message.setText(dateString + ":    " + newMessage + "\n" + message.getText());
	}

	private void setEnable(boolean flag) {
		if (downloadingURL.size() == 0) {
			downloadButton.setEnabled(flag);
			sourcePath.setEditable(flag);
			storePath.setEditable(flag);
		}
	}

	public static void main(String[] args) {
		new DownLoadKeggPngFrame();
	}

}
