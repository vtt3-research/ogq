package metavideo.dto;

import metavideo.vo.ContentVO;
import metavideo.vo.ShotVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ContentDTO {
    @Autowired
    private SqlSessionTemplate sqlSession;

    public int insertContent(String qid, Map<String, Object> params) {
        return sqlSession.insert(qid, params);
    }

    public String maxContentId(String qid, Map<String, Object> params) {
        return sqlSession.selectOne(qid, params);
    }

    public int updateContent(String qid, Map<String, Object> params) {
        return sqlSession.update(qid, params);
    }

    public Object deleteContent(String qid, Map<String, Object> params) {
        return sqlSession.delete(qid, params);
    }

    public List<ShotVO> shotVOList(String qid, Map<String, Object> params) {
        return sqlSession.selectList(qid, params);
    }

    public Object myDetail(String qid, Map<String, Object> params) {
        return sqlSession.selectOne(qid, params);
    }

    public int myListTotalCount(String qid, Map<String, Object> params) {
        return sqlSession.selectOne(qid, params);
    }

    public List<ContentVO> myList(String qid, Map<String, Object> params) {
        return sqlSession.selectList(qid, params);
    }

    public ContentVO allDetail(String qid, Map<String, Object> params) {
        return sqlSession.selectOne(qid, params);
    }

    public int allListTotalCount(String qid, Map<String, Object> params) {
        return sqlSession.selectOne(qid, params);
    }

    public List<ContentVO> allList(String qid, Map<String, Object> params) {
        return sqlSession.selectList(qid, params);
    }
}
