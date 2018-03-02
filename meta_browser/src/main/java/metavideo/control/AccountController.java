package metavideo.control;

import metavideo.common.ETCLib;
import metavideo.common.EncryptionManager;
import metavideo.common.POJOMail;
import metavideo.dto.AccountDTO;
import metavideo.vo.AccountVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AccountController {

    @Autowired
    private AccountDTO accountDTO;

    //복붙용 템플릿
    @RequestMapping(value="/ddd")
    public ModelAndView tempController(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("/tempView");
        return returnModelAndView;
    }



    //p1-1. 로그인 리디렉션 진입점
    @RequestMapping(value="/")
    public ModelAndView login_2(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("p1_login");

        //no action needed
        return returnModelAndView;
    }


    //p1-1. 로그인 리디렉션 진입점
    @RequestMapping(value="/login")
    public ModelAndView login_1(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("p1_login");

        //no action needed
        return returnModelAndView;
    }




    //p1. 로그인 화면
    @RequestMapping(value="/account/login")
    public ModelAndView login(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("p1_login");

        //no action needed
        return returnModelAndView;
    }

    //p2. 회원가입 화면
    @RequestMapping(value="/account/signin")
    public ModelAndView signin(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("p2_signin");

        //no action needed
        return returnModelAndView;
    }




    //로그인 제출
    @RequestMapping(value = "/account/loginOK", method = RequestMethod.GET)
    public String loginCheck(HttpServletRequest request) {
        String id = request.getParameter("accountID");
        String pw = request.getParameter("accountPW");

        Map<String, String> params = new HashMap<String, String>();
        params.put("account_id", id);
        params.put("account_pw", EncryptionManager.encrypt(pw));

        //DB조회(사용자)
        AccountVO accountVO;
        accountVO = accountDTO.loginCheck("selectOneAccount",params);
        System.out.println("myAccountVO: "+accountVO);

        //DB조회(로그인)
        AccountVO myAccountVO;
        myAccountVO = accountDTO.loginCheck("checkLoginIDPW",params);
        System.out.println("myAccountVO: "+myAccountVO);

        //로그인시도 실패
        if(accountVO==null){
            return "redirect:/login?msg=noID";
        }
        if(myAccountVO==null){
            return "redirect:/login?msg=wrongPW";
        }
        if(accountVO.getCertify()==null){
            return "redirect:/login?msg=nocert";
        }
        
        String certification = request.getParameter("certify");
        if(!accountVO.getCertify().equals("CERTIFIED") && (certification==null || certification.length()<5)){
            //최초로그인시 이메일인증을 타고 오지 않은 경우이다.
            return "redirect:/login?msg=nocert";
        }else{
            accountDTO.updateAccountCertification("updateAccountCertification",params);
        }

        //세션에 넣고
        request.getSession().setAttribute("myAccountVO", myAccountVO);

        return "redirect:/content/allList";//후화면

    }

    //로그아웃
    @RequestMapping(value = "/account/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();  //세션삭제

        return "redirect:/account/login";

    }

    //p7> 내정보 수정 제출 (확인팝업->리디렉션->알림팝업 순으로 진행. ajax로 하면 페이지 데이터 갱신이 발생한다.)
    @RequestMapping(value="/account/updateMyAccountOK")
    public ModelAndView www(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("redirect:/content/myList?updated=y");
        Map<String, Object> params = new HashMap<String, Object>();
        AccountVO myAccountVO = null;
        int updateCount=0;

        myAccountVO = (AccountVO)request.getSession().getAttribute("myAccountVO");
        params.put("account_id", myAccountVO.getAccountId());
        params.put("organ", request.getParameter("organ"));
        params.put("account_name", request.getParameter("accountName"));

        updateCount = accountDTO.updateAccount("updateAccount", params);

        //로그인세션정보 업데이트
        if(updateCount>0){
            myAccountVO.setOrgan(params.get("organ").toString());
            myAccountVO.setAccountName(params.get("account_name").toString());
            request.getSession().setAttribute("myAccountVO", myAccountVO);
        }

        return returnModelAndView;
    }

    //p7> 비밀번호변경 제출 (확인팝업->리디렉션->알림팝업 순으로 진행. ajax로 하면 페이지 데이터 갱신이 발생한다.)
    @RequestMapping(value="/account/updatePWOK")
    public ModelAndView updatePWOK(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("redirect:/content/myList?updated=y");
        Map<String, Object> params = new HashMap<String, Object>();
        AccountVO myAccountVO = null;
        int updateCount=0;

        myAccountVO = (AccountVO)request.getSession().getAttribute("myAccountVO");
        params.put("account_id", myAccountVO.getAccountId());
        String password1 = request.getParameter("password1");
        String password2 = request.getParameter("password2");
        String oldPassword = request.getParameter("oldPassword");

        //기존의 비밀번호가 일치하지 않는경우는?

        if(!myAccountVO.getAccountPw().equals(EncryptionManager.encrypt(oldPassword))){
            return new ModelAndView("redirect:/content/myList?updated=oldPassword");
        }


        params.put("account_pw", EncryptionManager.encrypt(password1));

        updateCount = accountDTO.changePW("changePW", params);
        return returnModelAndView;
    }





    //AJAX 회원가입 제출
    @RequestMapping(value="/account/signinOK", headers="accept=application/json")
    @ResponseBody
    public String ajaxSigninOK(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) {
        String returnString="";
        Map<String, Object> params = new HashMap<String, Object>();
        int insertCount=0;
        String certification="";

        //userName organ
        params.put("email", request.getParameter("emailAddress"));
        params.put("organ", request.getParameter("organ"));
        params.put("account_name", request.getParameter("userName"));
        params.put("password1", request.getParameter("password1"));
        params.put("password2", request.getParameter("password2"));

        params.put("account_id", params.get("email"));
        if(params.get("password1").toString().equals(params.get("password2").toString())){
            params.put("account_pw", EncryptionManager.encrypt(params.get("password1").toString()));
        }

        if(params.get("account_name")==null || params.get("account_name").toString().length()==0){
            params.put("account_name", params.get("email").toString().split("@")[0]);
        }

        //최초로그인을 위한 인증키
        certification = EncryptionManager.getRandomString();
        params.put("certify", certification);

        try {
            insertCount = accountDTO.insertAccountMap("insertAccountMap", params);
        }catch (Exception e){
            System.out.println(e.getMessage());
            returnString=e.getMessage();
        }

        if(insertCount==1){
            //가입환영메일
            POJOMail.sendJoinMail(
                    params.get("email").toString()
                    ,params.get("account_name").toString()
                    ,certification);
            returnString="OK";
        }
        else if(returnString.contains("Duplicate")){returnString="Duplicate";}
        else returnString="Fail";


        return returnString;
    }

    //AJAX 아이디찾기 제출
    @RequestMapping(value="/account/findIDOK", headers="accept=application/json")
    @ResponseBody
    public Map<String , Object> findIDOK(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) {
        Map<String, Object> returnMap = new HashMap<String, Object>();

        System.out.println("1");

        POJOMail.sendIDMail(request.getParameter("pi1"), request.getParameter("pi2"), request.getParameter("pi3"));
        System.out.println("3");

        return returnMap;
    }

    //AJAX 비밀번호찾기 제출
    @RequestMapping(value="/account/resetPassword", headers="accept=application/json")
    @ResponseBody
    public Map<String , Object> resetPassword(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Map<String, Object> params = new HashMap<String, Object>();
        AccountVO accountVO = null;
        String newPassword="";
        int updateCount=0;

        String resultString = "OK";

        newPassword = EncryptionManager.getRandomString();

        params.put("account_id",request.getParameter("accountID"));
        params.put("account_pw", EncryptionManager.encrypt(newPassword));
        accountVO = accountDTO.selectOneAccount("selectOneAccount", params);//조회하고
        updateCount = accountDTO.changePW("changePW", params);//업데이트

        POJOMail.sendPWMail(accountVO.getEmail(), accountVO.getAccountName(), newPassword);//메일발송

        returnMap.put("result", resultString);
        return returnMap;
    }


}
