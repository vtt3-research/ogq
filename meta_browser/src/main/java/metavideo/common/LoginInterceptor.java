package metavideo.common;

import metavideo.vo.AccountVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class LoginInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private SqlSessionTemplate sqlSession;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(1==2 && System.getProperty("user.dir").startsWith("C")){
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("account_id","lcg2004@naver.com");

            AccountVO accountVO = sqlSession.selectOne("selectOneAccount", params);
            request.getSession().setAttribute("myAccountVO",accountVO);
            return true;
        }
        try {

            if(request.getSession().getAttribute("myAccountVO") == null ){
                //System.out.println("myAccountVO이라는 세션key를 가진 정보가 널일경우 로그인페이지로 이동");
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("myAccountVO 세션key 존재시 호출한 그 페이지 이동: "+ request.getSession().getAttribute("myAccountVO"));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }

}

