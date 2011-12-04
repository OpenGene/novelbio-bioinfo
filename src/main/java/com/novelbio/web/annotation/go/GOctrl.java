package com.novelbio.web.annotation.go;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.newDebugLog;
import org.hamcrest.core.IsEqual;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.web.model.User;

@Controller
@SessionAttributes("goparam")
@RequestMapping("/goanalysis")
public class GOctrl {
	
	
	
	@RequestMapping(method = RequestMethod.GET)
	// 访问地址 http://localhost:8080/Novelbio/test.htm
	public ModelAndView getForm() {
		// 1.指定视图并返回
		ModelAndView mav = new ModelAndView("GeneOntology");
		// 可以往里面直接插入一个 新建对象，这样对应的表中就会填上相关的信息
		GoParam goParam = new GoParam();
		goParam.setGoType(GOInfoAbs.GO_CC);
		goParam.setInputFile("fesefsef");
		goParam.setBlast(true);
		goParam.setQueryTaxID(9823);
		mav.addObject("goparam", goParam);
		mav.addObject("hashGoType",CopedID.getHashGOID());
		mav.addObject("summiturl", "goanalysis.htm");//装入session,通过session传递，也可以通过url传递
		System.out.println(CopedID.getHashTaxIDName().get(9606));
		mav.addObject("hashTaxID", CopedID.getHashTaxIDName());
		return mav;
	}
	
//    //redirect: 连接传给浏览器，浏览器再次访问，网址会发生变化，参数通过session传递
//    @RequestMapping( method = RequestMethod.POST)  
//    public String post(GoParam goParam, BindingResult result,Model model) {
//        model.addAttribute("goParam", goParam);//装入session,通过session传递，也可以通过url传递
//        return "redirect:goanalysis/result.htm";
//    }
    //redirect: 连接传给浏览器，浏览器再次访问，网址会发生变化，参数通过session传递
    @RequestMapping(method = RequestMethod.POST)  
    public ModelAndView post2(GoParam goParam, BindingResult result,Model model) {
    	
        model.addAttribute("goparam", goParam);//装入session,通过session传递，也可以通过url传递
        ModelAndView modelAndView = new ModelAndView("test");
        modelAndView.addObject("message", goParam.getInputFile());
        modelAndView.addObject("counter", goParam.getQueryTaxID());
        return modelAndView;
        
    }
  
}
