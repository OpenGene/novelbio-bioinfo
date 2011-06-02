   package com.novelbio.web;  
     
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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.novelbio.web.model.User;
     
   @Component
   @RequestMapping(value = "/")
   @SessionAttributes("user")
   public final class ContactController {  
         
       @Autowired  
         private UserValidator userValidator;
       public void setValidator(UserValidator userValidator) {  
           this.userValidator = userValidator;  
       }
         
       @RequestMapping(value = "/form",method = RequestMethod.GET)  //���ʵ�ַΪ   ����/servlet/class��value/��value + ��׺
//       @RequestMapping(method = RequestMethod.GET)  
       public ModelAndView get() {
    	   //���¼��ֿ��Ե����
    	   //1.ָ����ͼ������
           ModelAndView mav = new ModelAndView("form");
           //mav.setViewName("form");
           //����������ֱ�Ӳ���һ�� �½�����
      	   User user = new User();
      	   user.setName("fsefs");
      	   user.setText("zongjie");
    	   user.setEmail("fwefe@fewef.com");//��ʱ��������Ӧѡ��ͻ������
           mav.addObject("user",user);
           //Ҳ����ָ�����������ٲ������
           //mav.addObject( new User());
           
           //2. ����һ��ModelMap��ModelMap�ƺ�����ָ����ͼ��
           //��ô���ǰ���@RequestMapping ���ú�������spring��Ĭ�Ͻ���ͼ��Ӧ��form��ȥ������ֱ����ModelMap�в���һ������ͺá�
           //ע���ʱ��return������ModelMap
           //return new ModelMap("user", new User());  
           return mav;
       }  
      
//     @RequestMapping(value = "form22",method = RequestMethod.GET)  //���ʵ�ַΪ   ����/servlet/class��value/��value + ��׺
       @RequestMapping(method = RequestMethod.GET)  
       public ModelMap get(ModelMap model) {
//    	   ��ʱmodelֱ�Ӿ���form��ͼ,�������������user����,�������趨һЩֵ
//    	   ��ȻҲ����ModelAndView mav = new ModelAndView("form");,�����������user
    	   User user = new User();
    	   user.setEmail("fwefe@fewef");//��ʱ��������Ӧѡ��ͻ������
           model.addAttribute(user);
           return model;
       }
       
       
       
       
//     @RequestMapping(value = "form",method = RequestMethod.GET)  //���ʵ�ַΪ   ����/servlet/class��value/��value + ��׺
       @RequestMapping(value ="/formhuih",method =RequestMethod.GET )  
       public ModelAndView get(@RequestBody String body) {
    	   System.out.println(body);
    	   //���¼��ֿ��Ե����
    	   //1.ָ����ͼ������
           ModelAndView mav = new ModelAndView("form");
           //mav.setViewName("form");
           //����������ֱ�Ӳ���һ�� �½�����
           mav.addObject("user", new User());
           //Ҳ����ָ�����������ٲ������
           //mav.addObject( new User());
           
           //2. ����һ��ModelMap��ModelMap�ƺ�����ָ����ͼ��
           //��ô���ǰ���@RequestMapping ���ú�������spring��Ĭ�Ͻ���ͼ��Ӧ��form��ȥ������ֱ����ModelMap�в���һ������ͺá�
           //ע���ʱ��return������ModelMap
           //return new ModelMap("userMessage", new UserMessage());  
           return mav;
       }
       
       
       @RequestMapping(value = "/something", method = RequestMethod.PUT)
       public String helloWorld()  {
         return "Hello World";
       }
       
       @RequestMapping( method = RequestMethod.POST)  
       public String post(@ModelAttribute("user") User user, BindingResult result,Model model) {
    	  // Model model �ǽ�post�������ͼ�����Ĵ����˽��������Զ������һЩ��������������Ҳû��ϵ,�ò�������ʡ
    	   userValidator.validate(user, result);  
           if (result.hasErrors()) { return "form"; }  
             
           // Use the redirect-after-post pattern to reduce double-submits. 
           model.addAttribute("user", user);//װ��session
           return "redirect:thanks.htm";  
       }
        static int aa = 1;
       @RequestMapping("thanks")  
       public String thanks() {
    	   if (aa<5) {
    		   aa++;
    		   System.out.println(aa);
    		   return "thanks";
		}
    	  else {
			return "redirect:thanks2.htm";
		}
//    	   return "redirect:thanks2.htm";
    	   
       }
       //��session�е�user��ȡ����
       @RequestMapping(value ="thanks2", method = RequestMethod.GET)  
       public ModelAndView thanks2(@ModelAttribute("user") User user)
       {
    		ModelAndView mav = new ModelAndView("test");
    		//ModelAndView mav = new ModelAndView();//Ĭ�����test��������ͼ
    		mav.addObject("message", user.getEmail());
    		mav.addObject("counter", user.getName());
    		System.out.println(mav.getViewName());
    		return mav;
       }
   }  