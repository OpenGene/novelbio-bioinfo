package com.novelbio.web.annotation.go;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.newDebugLog;
import org.hamcrest.core.IsEqual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.service.servgeneanno.ServNCBIID;
import com.novelbio.web.model.User;

@Controller
@SessionAttributes("goparam")
@RequestMapping("/goanalysis")
public class GOctrl {
	
	
	
	@RequestMapping(method = RequestMethod.GET)
	// ���ʵ�ַ http://localhost:8080/Novelbio/test.htm
	public ModelAndView getForm() {
		// 1.ָ����ͼ������
		ModelAndView mav = new ModelAndView("GeneOntology");
		// ����������ֱ�Ӳ���һ�� �½�����������Ӧ�ı��оͻ�������ص���Ϣ
		GoParam goParam = new GoParam();
		goParam.setGoType(GOInfoAbs.GO_CC);
//		goParam.setInputFile("fesefsef");
		goParam.setBlast(true);
		goParam.setQueryTaxID(9823);
		mav.addObject("goparam", goParam);
		mav.addObject("hashGoType",CopedID.getHashGOID());
		mav.addObject("summiturl", "goanalysis.htm");//װ��session,ͨ��session���ݣ�Ҳ����ͨ��url����
		ServNCBIID servNCBIID = new ServNCBIID();
		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID("tp53"); ncbiid.setTaxID(9606);
		NCBIID ncbiid2 = servNCBIID.queryNCBIID(ncbiid);
		System.out.println(ncbiid2.getGenUniID());
		System.out.println(CopedID.getHashTaxIDName().get(9606));
		mav.addObject("hashTaxID", CopedID.getHashTaxIDName());
		return mav;
	}
	
//    //redirect: ���Ӵ����������������ٴη��ʣ���ַ�ᷢ���仯������ͨ��session����
//    @RequestMapping( method = RequestMethod.POST)  
//    public String post(GoParam goParam, BindingResult result,Model model) {
//        model.addAttribute("goParam", goParam);//װ��session,ͨ��session���ݣ�Ҳ����ͨ��url����
//        return "redirect:goanalysis/result.htm";
//    }
    //redirect: ���Ӵ����������������ٴη��ʣ���ַ�ᷢ���仯������ͨ��session����
    @RequestMapping(method = RequestMethod.POST)  
    public ModelAndView post2(GoParam goParam, BindingResult result,Model model)  {
        model.addAttribute("message", goParam.getInputFile());//װ��session,ͨ��session���ݣ�Ҳ����ͨ��url����
        model.addAttribute("counter", goParam.getQueryTaxID());//װ��session,ͨ��session���ݣ�Ҳ����ͨ��url����
        ModelAndView modelAndView = new ModelAndView("test");
        modelAndView.addObject("message", goParam.getInputFile());
        modelAndView.addObject("counter", goParam.getQueryTaxID());
        try {
        	 if (!goParam.getInputFile().isEmpty()) {
                 byte[] bytes = goParam.getInputFile().getBytes();
                 FileOutputStream fos = new FileOutputStream("/home/zong0jie/����/testupload"); // �ϴ���д�����ϴ�·��
                 fos.write(bytes);  //д���ļ�
                 System.out.println("/home/zong0jie/����/testupload");
               }
		} catch (Exception e) {
			// TODO: handle exception
		}
        return modelAndView;
    }
  
}
