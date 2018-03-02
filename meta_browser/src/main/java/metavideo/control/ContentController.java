package metavideo.control;

import metavideo.common.AWSService;
import metavideo.common.ETCLib;
import metavideo.common.ThumbnailManager;
import metavideo.dto.ContentDTO;
import metavideo.vo.AccountVO;
import metavideo.vo.ContentVO;
import metavideo.vo.ShotVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ContentController {
    @Autowired
    private ContentDTO contentDTO;

    //복붙용 템플릿
    @RequestMapping(value="/cccc")
    public ModelAndView tempController(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("/tempView");
        return returnModelAndView;
    }





    //p3. 전체 리스트
    @RequestMapping(value="/content/allList")
    public ModelAndView allList(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("p3_allList");

        Map<String, Object> params=new HashMap<String, Object>();
        List<ContentVO> contentVOList = null;
        int allListTotalCount=0;

        params.put("pageNo", request.getParameter("pageNo"));
        if(params.get("pageNo")==null || params.get("pageNo").toString().equals("")) params.put("pageNo","1");
        params.put("offset", Integer.parseInt(params.get("pageNo").toString())*10-10);
        //keyword
        params.put("keyword", request.getParameter("keyword"));


        contentVOList = (List<ContentVO>)(List)contentDTO.allList("allList",params);
        allListTotalCount = (Integer) contentDTO.allListTotalCount("allListTotalCount", params);

        returnModelAndView.addObject("params",params);
        returnModelAndView.addObject("contentVOList",contentVOList);
        returnModelAndView.addObject("allListTotalCount",allListTotalCount);

        return returnModelAndView;
    }

    //p4. 전체 상세
    @RequestMapping(value="/content/allDetail")
    public ModelAndView detail(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("p4_detail");
        Map<String, Object> params=new HashMap<String, Object>();
        ContentVO contentVO = null;
        List<ShotVO> shotVOList = null;

        params.put("content_id", request.getParameter("contentID"));
        contentVO = (ContentVO) contentDTO.allDetail("allDetail", params);
        shotVOList = contentDTO.shotVOList("shotVOList", params);

        returnModelAndView.addObject("params", params);
        returnModelAndView.addObject("contentVO", contentVO);
        returnModelAndView.addObject("shotVOList", shotVOList);
        return returnModelAndView;
    }

    //p5. 작성화면
    @RequestMapping(value="/content/writeContent")
    public ModelAndView writeContent(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("p5_writeContent");
        //no action needed

        return returnModelAndView;
    }

    //p6. cc0
    @RequestMapping(value="/content/cc0")
    public ModelAndView cc0(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("p6_cc0");

        //no action needed
        return returnModelAndView;
    }

    //p7. 나의 목록
    @RequestMapping(value="/content/myList")
    public ModelAndView myList(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("p7_myList");

        Map<String, Object> params=new HashMap<String, Object>();
        AccountVO myAccountVO =null;
        List<ContentVO> contentVOList = null;
        int myListTotalCount=0;

        myAccountVO = (AccountVO)request.getSession().getAttribute("myAccountVO");
        params.put("account_id", myAccountVO.getAccountId());
        params.put("pageNo", request.getParameter("pageNo"));
        if(params.get("pageNo")==null || params.get("pageNo").toString().equals("")) params.put("pageNo","1");
        params.put("offset", Integer.parseInt(params.get("pageNo").toString())*10-10);
        //keyword
        params.put("keyword", request.getParameter("keyword"));

        contentVOList = (List<ContentVO>)(List)contentDTO.myList("myList",params);
        myListTotalCount = (Integer) contentDTO.myListTotalCount("myListTotalCount", params);

        returnModelAndView.addObject("params",params);
        returnModelAndView.addObject("myAccountVO",myAccountVO);
        returnModelAndView.addObject("contentVOList",contentVOList);
        returnModelAndView.addObject("myListTotalCount",myListTotalCount);
        return returnModelAndView;
    }

    //p8. 나의 상세
    @RequestMapping(value="/content/myDetail")
    public ModelAndView myDetail(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("p8_myDetail");
        Map<String, Object> params=new HashMap<String, Object>();
        ContentVO contentVO = null;
        List<ShotVO> shotVOList = null;
        AccountVO myAccountVO = null;


        myAccountVO = (AccountVO)request.getSession().getAttribute("myAccountVO");
        params.put("account_id", myAccountVO.getAccountId());
        params.put("content_id", request.getParameter("contentID"));
        contentVO = (ContentVO) contentDTO.myDetail("myDetail", params);
        shotVOList = contentDTO.shotVOList("shotVOList", params);

        returnModelAndView.addObject("params", params);
        returnModelAndView.addObject("contentVO", contentVO);
        returnModelAndView.addObject("shotVOList", shotVOList);
        return returnModelAndView;
    }





    //p7> 컨텐츠 삭제 제출 (확인팝업->리디렉션->알림팝업 순으로 진행. ajax로 하면 페이지 데이터 갱신이 발생한다.)
    @RequestMapping(value="/content/deleteContent")
    public ModelAndView deleteContent(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("redirect:/content/myList?deleted=y");
        Map<String, Object> params=new HashMap<String, Object>();
        int deletedCount=0;
        String delIDs="";

        params.put("delIDs",request.getParameter("delIDs"));
        deletedCount = (Integer) contentDTO.deleteContent("deleteContent", params);

        returnModelAndView.addObject("deletedCount",deletedCount);
        return returnModelAndView;
    }


    //p8> 컨텐츠 수정 (확인팝업->리디렉션->알림팝업 순으로 진행. ajax로 하면 페이지 데이터 갱신이 발생한다.)
    @RequestMapping(value="/content/updateContentOK")
    public ModelAndView updateMyAccountOK(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("redirect:/content/myList");
        Map<String, Object> params = new HashMap<String, Object>();
        AccountVO myAccountVO = null;
        int updateCount=0;

        myAccountVO = (AccountVO)request.getSession().getAttribute("myAccountVO");
        params.put("account_id", myAccountVO.getAccountId());
        params.put("content_id", request.getParameter("contentId"));
        params.put("content_title", request.getParameter("contentTitle"));
        params.put("explanation", request.getParameter("explanation"));

        updateCount = contentDTO.updateContent("updateContent", params);

        //return new ModelAndView("p8_myDetail?contentID="+params.get("content_id"));
        return returnModelAndView;
    }





    /* 처리순서:
    1. DB처리(ffmpeg 재생시간).
    2. S3전송.
    3. 태깅API 호출
    */
    //AJAX 비디오 업로드
    @RequestMapping(value="/ajaxUploadVideo", method = RequestMethod.POST)
    @ResponseBody
    public String ajaxUploadVideo(
            MultipartHttpServletRequest mr,
            HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes
    ) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Map<String, Object> params = new HashMap<String, Object>();
        AccountVO myAccountVO = (AccountVO)session.getAttribute("myAccountVO");
        String originFileName="";
        String saveFileName="";
        String savedFileFullPath="";
        String nextContentId="";
        int insertCount=0;
        String thumbnail_home="D:\\workspace\\metavideo\\web\\assets\\thumbnails";

        nextContentId=contentDTO.maxContentId("maxContentId",null);

        params.put("content_id",""+nextContentId);
        params.put("account_id",  myAccountVO.getAccountId());
        params.put("content_title",request.getParameter("contentTitle"));
        params.put("explanation",request.getParameter("explanation"));

        params.put("preview_image_path", params.get("content_id").toString()+".jpg");
        params.put("agree_yn",request.getParameter("agreeYn"));
        if(params.get("agree_yn").toString().equals("on")) params.put("agree_yn", "Y");
        //entire_tags

        //비디오처리(썸네일, 재생시간)
        String duration = ThumbnailManager.createThumbnail(
                savedFileFullPath
                ,thumbnail_home + "\\"+params.get("preview_image_path").toString()
        );
        params.put("video_running_time",duration);

        params.put("video_file_size",mr.getFile("fileHidden1").getSize());
        params.put("video_file_url",request.getParameter("fileText1"));//원본파일명이다. url도 아니고 저장파일명도 아니다.
        params.put("meta_file_url",request.getParameter("fileText2"));

        //DB 저장
        insertCount=contentDTO.insertContent("insertContent",params);


        //메타파일 EC2 저장
        MultipartFile mpFile=mr.getFile("fileHidden2");
        if(mpFile!=null) {
            originFileName = params.get("meta_file_url").toString();
            saveFileName = nextContentId + "_" + originFileName;//저장할 파일명
            savedFileFullPath = ETCLib.multPart2File(mr.getFile("fileHidden2"), saveFileName);//브라우저->톰캣
            //EC2->S3 전송
            AWSService.transferMetaFile(savedFileFullPath, saveFileName);//전송
        }

        //비디오파일 EC2 저장
        //mpFile=mr.getFile("fileHidden1");
        originFileName = params.get("video_file_url").toString();//mr.getFile("fileHidden1").getOriginalFilename();
        saveFileName = nextContentId + "_" + originFileName;//저장할 파일명
        savedFileFullPath = ETCLib.multPart2File(mr.getFile("fileHidden1"), saveFileName);//브라우저->톰캣
        //EC2->S3 전송
        //AWSService.transferVideoFile(savedFileFullPath, saveFileName);
        ETCLib.asyncTransfer(savedFileFullPath, saveFileName);

        //태깅 API 호출
        String callbackURL = (""+request.getRequestURL()).split(request.getContextPath())[0] + "/callbackReceiver";
        String requestID = saveFileName;
        String videoURL = "https://s3.ap-northeast-2.amazonaws.com/vtt-ogq/movie_attach/"+saveFileName;
        ETCLib.callVideoTaggingAPI(callbackURL, requestID, videoURL);

        returnMap.put("result", "OK");
        return "OK";
    }



}
