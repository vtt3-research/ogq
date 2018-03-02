package metavideo.control;

import com.google.gson.*;
import metavideo.common.*;
import metavideo.dto.ContentDTO;
import metavideo.dto.MainDTO;
import metavideo.vo.ContentVO;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {
    @Autowired
    private MainDTO mainDTO;

    @Autowired
    private ContentDTO contentDTO;

    //복붙용 템플릿
    @RequestMapping(value="/tempController")
    public ModelAndView tempController(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("tempView");
        String ddd = "";
        ddd=mainDTO.ddd("ddd");


        try{
            String sss= "";
            System.out.println(""+sss);


        }catch(Exception e){e.printStackTrace();}



        return returnModelAndView;
    }

    //AJAX 복붙용 템플릿
    @RequestMapping(value="/ajaxTempController", headers="accept=application/json")
    @ResponseBody
    public Map<String , Object> ajaxTempController(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Map<String, Object> params = new HashMap<String, Object>();



        Object rows = null;
        //rows = selectList("mapper.selectAll", params);
        returnMap.put("rows", rows);
        return returnMap;
    }


    //송신은 ContentController. /ajaxUploadVideo 에서.

    //가짜 코난의 테스트 리시버(코난으로 보내는 호출url을 이쪽으로 넘기면 테스트 할 수 있음)
    @RequestMapping(value="/mockKONANreceiver", method = RequestMethod.PUT)
    public void mockKONANreceiver(HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        String requestBody="";
        String responseBody="";
        try {
            requestBody=ETCLib.watchDog(request.getInputStream());
            System.out.println("[테스트리시버가 수신한 request Body] "+requestBody);
        }catch (IOException ioe){
            responseBody = "IOException으로 받을 수 없음.";
        }

        responseBody = "받은 내용: "+requestBody;
        ETCLib.printFileLog(responseBody);
    }

    //가짜 코난의 테스트 센딩 페이지(코난이 처리하듯이 손으로 태크를 만들어서 보내는 작업용 페이지)
    @RequestMapping(value="/mockKONANSender")
    public ModelAndView mockKONANSender(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        ModelAndView returnModelAndView = new ModelAndView("mockKONANSender");
        Map<String, Object> params=new HashMap<String, Object>();
        List<ContentVO> contentVOList = null;

        params.put("pageNo", request.getParameter("pageNo"));
        if(params.get("pageNo")==null || params.get("pageNo").toString().equals("")) params.put("pageNo","1");
        params.put("offset", Integer.parseInt(params.get("pageNo").toString())*10-10);

        contentVOList = (List<ContentVO>)(List)contentDTO.allList("allList",params);

        returnModelAndView.addObject("params",params);
        returnModelAndView.addObject("contentVOList",contentVOList);

        return returnModelAndView;
    }


    //콜백 리시버 (태깅API콜백을 수신하는 정식 서비스)
    @RequestMapping(value="/callbackReceiver", method = RequestMethod.PUT)
    public void callbacker(HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) throws Exception{
        Map<String, Object> params = new HashMap<String, Object>();
        BufferedReader br = null;
        Gson gson=null;
        int updateCount=0;
        int insertCount=0;


        //수신
        try {
            br = request.getReader();
        }catch (IOException ioe){}
        gson = new com.google.gson.Gson();

        //전체태그 처리
        HashMap<String,Object> root = gson.fromJson(br, HashMap.class);
        String entire_tags = "|";
        ArrayList<Object> entire_tagsList = null;
        entire_tagsList=gson.fromJson(root.get("tags").toString(), ArrayList.class);
        for (Object obj: entire_tagsList) {
            entire_tags+=obj.toString()+"|";
        }


        ArrayList<Object> shots = gson.fromJson(root.get("shots").toString(), ArrayList.class);

        //DB 처리 1
        params.put("content_id", request.getParameter("content_id"));
        params.put("entire_tags", entire_tags);
        //update contents table
        updateCount = mainDTO.updateEntireTags("updateEntireTags", params);


        //씬태그 처리
        HashMap<String,Object> shot = null;
        String shot_time="";
        String seek_pos="";
        //String scene_tags="";
        ArrayList<Object> scene_tagsList = null;//scene_tags.stream().collect(Collectors.joining("|"))
        String scene_tags="|";

        System.out.println("3333"+ shots.get(0).toString());
        for(int i = 0; i< shots.size(); i++){
            shot = gson.fromJson(shots.get(i).toString(), HashMap.class);
            //shot = gson.fromJson(shot, HashMap.class);
            shot_time= shot.get("time").toString();
            seek_pos= shot.get("seek_pos").toString();
            scene_tagsList=gson.fromJson(shot.get("tags").toString(), ArrayList.class);
            for (Object obj: scene_tagsList) {
                scene_tags+=obj.toString()+"|";
            }

            //DB 처리 2
            params.put("shot_time", shot_time);
            params.put("seek_pos", seek_pos);
            params.put("scene_tags", scene_tags);
            System.out.println("insert st: "+shot_time+", sp: "+seek_pos+", tags: "+scene_tags+".");

            //insert shots table
            insertCount += mainDTO.insertShot("insertShot", params);
            scene_tags="|";

        }



        // 응답이 필요하게 되면 리디렉션 후, JSP GET 포워딩 하면된다.
    }








    //AJAX 동영상 실행시간 업데이트
    @RequestMapping(value="/ajaxVideoRuntime", headers="accept=application/json")
    @ResponseBody
    public Map<String , Object> ajaxVideoRuntime(HttpServletRequest request , HttpSession session, RedirectAttributes redirectAttributes) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Map<String, Object> params = new HashMap<String, Object>();
        int updateCount=0;
        //System.out.println("111");


        params.put("content_id",request.getParameter("content_id"));
        params.put("video_running_time",request.getParameter("video_running_time"));

        updateCount=mainDTO.updateRunningTime("updateRunningTime",params);

        //System.out.println("999");
        return returnMap;
    }


}
