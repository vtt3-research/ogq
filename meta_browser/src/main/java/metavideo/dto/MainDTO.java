package metavideo.dto;

import metavideo.vo.AccountVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class MainDTO {
    @Autowired
    private SqlSessionTemplate sqlSession;


    public int updateRunningTime(String qid, Map<String, Object> params) {
        return sqlSession.update(qid, params);
    }

    public int insertShot(String qid, Map<String, Object> params) {
        return sqlSession.update(qid, params);
    }

    public int updateEntireTags(String qid, Map<String, Object> params) {
        return sqlSession.update(qid, params);
    }

    public String ddd(String qid) {
        return sqlSession.selectOne(qid);
    }
}
