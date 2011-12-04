package com.novelbio.web.example;

import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.web.model.User;
import com.novelbio.web.validator.UserValidator;
@SessionAttributes("currUser")
@Controller
@RequestMapping("/test")
// ���ʵ�ַ http://localhost:8080/Novelbio/test.htm
public final class CntCtrl2 {

	@Autowired
	private UserValidator userValidator;

	public void setValidator(UserValidator userValidator) {
		this.userValidator = userValidator;
	}


	@RequestMapping(method = RequestMethod.GET)
	// ���ʵ�ַ http://localhost:8080/Novelbio/test.htm
	public ModelAndView getFormNoparam() {
		// 1.ָ����ͼ������
		ModelAndView mav = new ModelAndView("form");
		// ����������ֱ�Ӳ���һ�� �½�����������Ӧ�ı��оͻ�������ص���Ϣ
		User user = new User();
		user.setName("fsefs");
		user.setText("zongjie");
		user.setEmail("fwefe@fewef.com");// ��ʱ��������Ӧѡ��ͻ������
		mav.addObject("user", user);
		return mav;
	}
	// <���URL�����а���"method=tnaknsses"�Ĳ������ɱ��������д���
    @RequestMapping(params = "method=thanksses",method = RequestMethod.GET )//ֻ�е�������get�����ύʱ���н��
    public ModelAndView getThanksPost(Integer topicId) {
    	ModelAndView mav = new ModelAndView("test");
		//ModelAndView mav = new ModelAndView();//Ĭ�����test��������ͼ
		System.out.println(mav.getViewName());
		return mav;
    }

    @ModelAttribute("myuser")//��ģ�Ͷ��������һ����Ϊitems������
    public User propUser() {
    	User user = new User();
		user.setName("fsefs");
		user.setText("zongjie");
		user.setEmail("fwefe@fewef.com");// ��ʱ��������Ӧѡ��ͻ������
        return user;
    }
  
    
    
    
    //��propUser()��׼���õ�Ԫ���������
	@RequestMapping("/listAllBoard.htm")
	// ���ʵ�ַ http://localhost:8080/Novelbio/test/listAllBoard.htm
	public ModelAndView getFormUrl(ModelMap model) {
		// ���¼��ֿ��Ե����
		// 1.ָ����ͼ������
		ModelAndView mav = new ModelAndView();
		mav.setViewName("form");
		// ����������ֱ�Ӳ���һ�� �½�����
		// Ҳ����ָ�����������ٲ������
		mav.addObject(model.get("myuser"));// ����һ���ձ�
		return mav;
	}
    
    
    //redirect: ���Ӵ����������������ٴη��ʣ���ַ�ᷢ���仯������ͨ��session����
	//forward���������˴���
//    ����ֻ����һ��
    @RequestMapping( method = RequestMethod.POST)  
    public String post(User user, BindingResult result,Model model) {
 	  // Model model �ǽ�post�������ͼ�����Ĵ����˽��������Զ������һЩ��������������Ҳû��ϵ,�ò�������ʡ
 	   userValidator.validate(user, result);
        if (result.hasErrors()) {
        	return "form";
        }
        model.addAttribute("currUser", user);//װ��session,ͨ��session���ݣ�Ҳ����ͨ��url����
        // Use the redirect-after-post pattern to reduce double-submits. 
        return "redirect:test.htm?method=test2";
//        return "forward:test.htm?method=test2";
    }
//    
//    ����ֻ����һ��
//    @RequestMapping( method = RequestMethod.POST)  
//    public ModelAndView post2(User user, BindingResult result,Model model) {
// 	  // Model model �ǽ�post�������ͼ�����Ĵ����˽��������Զ������һЩ��������������Ҳû��ϵ,�ò�������ʡ
// 	   userValidator.validate(user, result);
//        if (result.hasErrors()) {
//        	ModelAndView mav = new ModelAndView("form");
//        	user.setName("fsefs");
//        	user.setText("zongjie");
//        	user.setEmail("fwefe@fewef.com");// ��ʱ��������Ӧѡ��ͻ������
//        	mav.addObject("user",user);//��������ܣ�����ĵط������
//        	return mav;
//        }
//        model.addAttribute("user", user);//װ��session
//
//        Map<String, String> map = new HashMap<String, String>();
//        map.put("message", "sfefsefefee");
//        ModelAndView mav = new ModelAndView("thanks",map);
////      return mav;
//      return getThanks(2, user.getName()); 
//    }
    
  


    @RequestMapping(params = "method=test")
    // ���ʵ�ַ���޲�����http://localhost:8080/Novelbio/test.htm
    //�в���  http://localhost:8080/Novelbio/test.htm?method=test&topicId=10&message=aaa
    //����������һ������
    public ModelAndView getThanks(Integer topicId,String message) 
    { 
    	ModelAndView mav = new ModelAndView("test");
    	HashMap<String, Object> hashMap = new HashMap<String, Object>();
    	
    	CopedID copedID = new CopedID(message, 9606, false);
    	
    	hashMap.put("message", copedID.getDescription());
    	hashMap.put("counter", topicId);
		mav.addAllObjects(hashMap);
		System.out.println(mav.getViewName());
		return mav;
    }

    @RequestMapping(params = "method=userTest")
       //�в���  http://localhost:8080/Novelbio/test.htm?method=userTest&topicId=10&name=zongjie
    //����  http://localhost:8080/Novelbio/test.htm?method=userTest&topicId=10&Name=zongjie
    public ModelAndView getThanks(int topicId,User user) {
    	ModelAndView mav = new ModelAndView("test");
    	HashMap<String, Object> hashMap = new HashMap<String, Object>();
    	hashMap.put("message", user.getName());
    	hashMap.put("counter", topicId);
		mav.addAllObjects(hashMap);
		System.out.println(mav.getViewName());
		return mav;
	}

	@RequestMapping(params = "method=userTestID")
	// �� id �󶨵� topicId��ͬʱ����session
	// �в���
	// http://localhost:8080/Novelbio/test.htm?method=userTestID&id=10&name=zongjie
	// ����
	// http://localhost:8080/Novelbio/test.htm?method=userTestID&id=10&Name=zongjie
	public ModelAndView getThanks2(@RequestParam("id") int topicId, User user,
			Model model) {
		ModelAndView mav = new ModelAndView("test");
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("message", user.getName());
		hashMap.put("counter", topicId);
		mav.addAllObjects(hashMap);
		System.out.println(mav.getViewName());
		model.addAttribute("currUser", user);
		return mav;
	}
	

	 // <-- ���URL�����а���"method=listAllBoard"�Ĳ������ɱ��������д���
    @RequestMapping(params = "method=test3") //���ʵ�ַ http://localhost:8080/Novelbio/test.htm?method=test2
    public ModelAndView getThanks(Map map) {
    	ModelAndView mav = new ModelAndView("test");
    	mav.addObject("message", map.get("name"));
		
		
		return mav;
    }
	
	
	  //�ȵ��ú���session�ķ���getThanks2 ��ַ http://localhost:8080/Novelbio/test.htm?method=userTestID&id=10&Name=zongjie
	//Ȼ�����  http://localhost:8080/Novelbio/test.htm?method=test2
	 // <-- ���URL�����а���"method=listAllBoard"�Ĳ������ɱ��������д���
    @RequestMapping(params = "method=test2") //���ʵ�ַ http://localhost:8080/Novelbio/test.htm?method=test2
    public ModelAndView getThanks(@ModelAttribute("currUser") User sessionValue) {
    	ModelAndView mav = new ModelAndView("test");
    	if (sessionValue !=null) {
    		mav.addObject("message", sessionValue.getName());
    		System.out.println(sessionValue.getName());
		}
		
		return mav;
    }

	  //�ȵ��ú���session�ķ���getThanks2 ��ַ http://localhost:8080/Novelbio/test.htm?method=userTestID&id=10&Name=zongjie
	//Ȼ�����  http://localhost:8080/Novelbio/test.htm?method=test3
	 // <-- ���URL�����а���"method=listAllBoard"�Ĳ������ɱ��������д���
    @RequestMapping(params = "method=test3") //���ʵ�ַ http://localhost:8080/Novelbio/test.htm?method=test3
    public ModelAndView getThanks(@ModelAttribute("currUser") User sessionValue, SessionStatus status) {
    	ModelAndView mav = new ModelAndView("test");
    	if (sessionValue.getName().equals("test")) {
			status.setComplete(); // ���session �з��� session �����ģ����������
			//����sessionValue�Ѿ�����ֵ��������Ȼ����
		}
     	mav.addObject("message", sessionValue.getName());
		System.out.println(sessionValue.getName());
		return mav;
    }

	
	
}