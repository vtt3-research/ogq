package metavideo.dto;

import metavideo.common.EncryptionManager;
import metavideo.common.POJOMail;
import metavideo.vo.AccountVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Repository
public class AccountDTO {
    @Autowired
    private SqlSessionTemplate sqlSession;

    //로그인 제출
    public AccountVO loginCheck(String qid, Map params) {
        return sqlSession.selectOne(qid, params);
    }

    public int updateAccountCertification(String qid, Map<String, String> params) {
        return sqlSession.update(qid, params);
    }

    public int updateAccount(String qid, Map<String, Object> params) {
        return sqlSession.update(qid,params);
    }

    public int changePW(String qid, Map<String, Object> params) {
        return sqlSession.update(qid, params);
    }

    public int insertAccountMap(String qid, Map<String, Object> params) {
        return sqlSession.insert(qid, params);
    }

    public AccountVO selectOneAccount(String qid, Map<String, Object> params) {
        return sqlSession.selectOne(qid, params);
    }
}
