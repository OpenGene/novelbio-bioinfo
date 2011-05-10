package com.novelbio.base.detailWebFetch;

import java.io.BufferedReader;
import java.io.IOException;

import com.novelbio.base.dataOperate.WebFetch;


/**
 * ������ַ�����Tigr����
 * @author Administrator
 *
 */
public class TigrSeqGet 
{
	/**
	 * ��ȡTigr����
	 */
	WebFetch TIGRSeq;
	
	
	
	BufferedReader SeqReader;
	
	
	public TigrSeqGet()
	{
	
	}

	
	/**
	 * ������ַ���������
	 * @param seqUrl
	 * @return
	 * @throws IOException 
	 */
	public String getSeq(String seqUrl) throws IOException
	{
		TIGRSeq=new WebFetch();
		TIGRSeq.GetUrl(seqUrl);
		SeqReader = TIGRSeq.GetFetch();
	    String seq="";
	    String content="";
	  
        while ((content=SeqReader.readLine())!= null)//��ȡ����ҳ
        {
            if (content.contains("Protein<br>"))
            {
                for (int i = 0; i < 2; i++)
                {
                	SeqReader.readLine();
                }
                seq= "";
                while ((content=SeqReader.readLine())!= null)
                {
                	content.trim();
                    if (!content.contains("</pre>"))
                    {
                    	seq= seq +  content;
                    }
                    else
                    {
                       return seq;
                    }
                }
            }
        }

		return null;
		
	}
	
	public void close()
	{
		TIGRSeq.closeall();
	}
	
	
	
}
