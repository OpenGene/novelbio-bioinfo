package com.novelbio.base.dataStructure;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ����������ʽ�������ҵ��ı����ҵ���������ʽ���ֵ�λ��
 */
public class PatternOperate 
{
	/**
	 * <b>�߼�������ʱ��</b>
	 * ����stringinput��������ʽ,�Լ��Ƿ����Ӵ�Сд��True����
	 * @param inputstr ������Ҫ���ҵ�string
	 * @param regex ����Ҫƥ���������ʽ
	 * @param CASE �Ƿ����Ӵ�Сд��False:���Ӵ�Сд��True:����Сд
	 * @return ����List<String[3]>
	 * list(i):input���ҵ��ĵ�i��ƥ���ַ�--��������Ϊ����װ��string[2]���顣<br/>
	 * String[0]:������ʽ��ĳ���ض����ַ���<br/>
	 * String[1]:���ַ�����λ�ã�Ϊ���ַ�����һ���ַ�������ַ�������λ�ã�acksd��aΪ1,kΪ3<br/>
	 * String[2]:���ַ�����λ�ã�Ϊ���ַ������һ���ַ�������ַ����յ��λ�ã�acksd��aΪ5,kΪ3
	 * ���û�ҵ���������null�����Ƿ���һ��sizeΪ0��list
	 */
    public static ArrayList<String[]> getPatLoc(String inputstr, String regex, boolean CASE)
    {
    	//hashtable����װ��������ʽ�Ĳ�ͬ�����ַ����������ж�ĳ���ض��ַ������ֵĴ���
    	 Hashtable<String, Integer> pathash=new Hashtable<String, Integer>();
    	 
    	 ArrayList<String[]> listResult=new ArrayList<String[]>();
    	 
    	 
    	 Pattern patInput;
    	 Matcher matInput;
    	 if(!CASE)//�Ƿ����Ӵ�Сд
    	 {
    	     patInput=Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
    	 }
    	 else 
    	 {
    		 patInput=Pattern.compile(regex);
		}
    	 matInput=patInput.matcher(inputstr);
    	 Integer index;//ĳ���ַ��ĳ��ִ���
       while(matInput.find())
       {   
    	
    	   String[] patinfo=new String[3];//װ������ҵ����ַ����ľ�����Ϣ
    	   patinfo[0]=matInput.group();//����ҵ����ַ���
    	   if((index=pathash.get(patinfo[0]))==null)
    	   {
    		   pathash.put(patinfo[0], 1);//��һ�η��ָ��ַ��������趨Ϊ1
    	   }
    	   else {
    		   pathash.put(patinfo[0], index+1);//��ǰ���ֹ�����+1
		   }
    	   int locationstart=0;//���øñ��ʽ��������Ϊ0
    	   int locationend=0;//�ñ��ʽ���յ����Ϊ0
    	   int num=pathash.get(patinfo[0]);//�ܹ�������num��
    	   for(int i=0; i<num;i++)
    	   {
    		   locationstart=inputstr.indexOf(patinfo[0], locationstart)+1;
    	   }
    	   locationend=inputstr.length()-locationstart-patinfo[0].length()+2;
    	   
    	   patinfo[1]=locationstart+"";
    	   patinfo[2]=locationend+"";
    	  listResult.add(patinfo);
       }
       return listResult;
     }
    /**
     * ����һ�У������������е����֣����û�У��򷵻ؿյ�int[]
     * @param inputstr
     * @return
     */
    public static int[] getNumAll(String inputstr)
    {
		ArrayList<String[]> lsResult = getPatLoc(inputstr, "\\d+", false);
		if (lsResult.size() == 0) {
			return new int[]{};
		}
		int[] colDetail = new int[lsResult.size()];
		for (int i = 0; i < colDetail.length; i++) {
			colDetail[i] = Integer.parseInt(lsResult.get(i)[0]);
		}
		return colDetail;
    }
    
    
    /**
     * <b>�򵥴�����Կ���</b>
     * ���������ָ��������������ʽ��ֵ
     * @param inputstr
     * @param regex
     * @param CASE
     * @return û��ץ���Ļ�������null
     */
    public ArrayList<String> getPat(String inputstr)
    {
    	ArrayList<String> lsresult = new ArrayList<String>();
    	 matInput=patInput.matcher(inputstr);
    	 while (matInput.find()) {
			lsresult.add(matInput.group());
		}
    	 if (lsresult.size() == 0) {
			return null;
		}
    	return lsresult;
    }
    
    String regex = "";
    boolean thiscase = false;
	 Pattern patInput;
	 Matcher matInput;
    public PatternOperate(String regex,boolean thiscase) {
		this.regex = regex;
		this.thiscase = thiscase;

    	 if(!thiscase)//�Ƿ����Ӵ�Сд
    	 {
    	     patInput=Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
    	 }
    	 else 
    	 {
    		 patInput=Pattern.compile(regex);
		}
	}

    
}





