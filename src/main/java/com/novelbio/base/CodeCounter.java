package com.novelbio.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class CodeCounter {
	public static void main(String[] args) {
		CodeCounter codeCounter = new CodeCounter("/media/winD/fedora/gitnbc/src/main/java/com/novelbio", "/media/winD/fedora/codestatistics/allCodeLines.txt");
		codeCounter = new CodeCounter("/media/winD/fedora/gitnbc/src/main/java/com/novelbio/analysis/seq/mirna", "/media/winD/fedora/codestatistics/mirnaCodeLines.txt");
		codeCounter = new CodeCounter("/media/winD/fedora/gitnbc/src/main/java/com/novelbio/project", "/media/winD/fedora/codestatistics/projectMinusCodeLines.txt");

	}
	TxtReadandWrite txtOut;
	/**��ͨ����*/
	private long normalLines=0;
	/**ע������*/
	private long commentLines=0;
	/**�հ�����*/
	private long spaceLines=0;
	/**������*/
	private long totalLines=0;
	/**��ͨ����*/
	private long normalLinesAll=0;
	/**ע������*/
	private long commentLinesAll=0;
	/**�հ�����*/
	private long spaceLinesAll=0;
	/**������*/
	private long totalLinesAll=0;
	
	/***
	 * ͨ��java�ļ�·������ö���
	 * @param filePath java�ļ�·��
	 */
	public CodeCounter(String filePath, String txtOutFile){
		txtOut = new TxtReadandWrite(txtOutFile, true);
		tree(filePath);
		conclution(filePath);
	}
	/**
	 * �����ļ��ķ���
	 * @param filePath �ļ�·��
	 */
	private void tree(String filePath){
		File file=new File(filePath);
		File[] childs=file.listFiles();
		if(childs==null){
			parse(file);
		}else{
		for(int i=0;i<childs.length;i++){
			if(childs[i].isDirectory()){
				//��ͳ�������ļ���
				if (childs[i].getName().startsWith(".")) {
					continue;
				}
//				System.out.println("path:"+childs[i].getPath());
				txtOut.writefileln("path:"+childs[i].getPath());
				tree(childs[i].getPath());
			}else{
				if (!childs[i].getName().matches(".*\\.java$")) {
					continue;
				}
//				System.out.println("��ǰ"+childs[i].getName()+"��������:");
				txtOut.writefileln("��ǰ"+childs[i].getName()+"��������:");
				parse(childs[i]);
				getCodeCounter();
			}
		}
		}
	}
	private void conclution(String filePath) {
		getCodeCounterAll(filePath);
		txtOut.close();
	}
	/**
	 * �����ļ�
	 * @param file �ļ�����
	 */
	private void parse(File file){
		BufferedReader br=null;
		boolean comment=false;
		try {
			br=new BufferedReader(new FileReader(file));
			String line="";
			while((line=br.readLine())!=null){
				line=line.trim();//ȥ���ո�
				if(line.matches("^[\\s&&[^\\n]]*$")
						|| line.equals("{") || line.equals("}") 
						|| line.equals("});") 
						|| line.startsWith("import") || line.startsWith("package")) {
					   spaceLines ++;   spaceLinesAll ++;
				}else if((line.startsWith("/*"))&& !line.endsWith("*/")) {
					   commentLines ++;   commentLinesAll ++;
					   comment = true;   
	            }else if(true == comment) {
			           commentLines ++;    commentLinesAll ++; 
	            if(line.endsWith("*/")) {
					   comment = false;
					   }
				}else if(line.startsWith("//") || (line.startsWith("/*")) && line.endsWith("*/")) {
					    commentLines ++; commentLinesAll ++;
				}else {
					   normalLines ++; normalLinesAll ++;
			          }
			  }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * �õ�Java�ļ��Ĵ�������
	 */
	private void getCodeCounter(){
		totalLines=normalLines+spaceLines+commentLines;
//		System.out.println("��ͨ��������:"+normalLines);
//		System.out.println("�հ״�������:"+spaceLines);
//		System.out.println("ע�ʹ�������:"+commentLines);
//		System.out.println("����������:"+totalLines);
		txtOut.writefileln("��ͨ��������:"+normalLines);
		txtOut.writefileln("�հ״�������:"+spaceLines);
		txtOut.writefileln("ע�ʹ�������:"+commentLines);
		txtOut.writefileln("���ļ�����������:"+totalLines);

		normalLines=0;
		spaceLines=0;
		commentLines=0;
		totalLines=0;
	}
	
	/**
	 * �õ�Java�ļ��Ĵ�������
	 */
	private void getCodeCounterAll(String filePath){
		System.out.println("");
		System.out.println(filePath + "�ܴ���ͳ��");
		totalLinesAll=normalLinesAll+spaceLinesAll+commentLinesAll;
		System.out.println("��ͨ�ܴ�������:"+normalLinesAll);
		System.out.println("�հ��ܴ�������:"+spaceLinesAll);
		System.out.println("ע���ܴ�������:"+commentLinesAll);
		System.out.println("����������:"+totalLinesAll);
		
		txtOut.writefileln();
		txtOut.writefileln(filePath + "�ܴ���ͳ��");
		txtOut.writefileln("��ͨ�ܴ�������:"+normalLinesAll);
		txtOut.writefileln("�հ��ܴ�������:"+spaceLinesAll);
		txtOut.writefileln("ע���ܴ�������:"+commentLinesAll);
		txtOut.writefileln("����������:"+totalLinesAll);
	}
}
