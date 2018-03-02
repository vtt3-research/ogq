<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.io.*" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.stream.Collectors" %>
<!--%Created by IntelliJ IDEA.
  User: changgyu78
  Date: 2017-12-24
  Time: 오후 2:18
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!

    /*

    * */
%>
<%
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
    String entire_tags = root.get("tags").toString();//구분자처리
    ArrayList<Object> shots = gson.fromJson(root.get("shots").toString(), ArrayList.class);

    //DB 처리 1
    params.put("content_id", request.getParameter("content_id"));
    params.put("entire_tags", entire_tags);
    //update contents table
    //updateCount = sqlSession.update("updateEntireTags", params);


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
            scene_tags+=obj.toString();
        }
        scene_tags+="|";

        //DB 처리 2
        params.put("shot_time", shot_time);
        params.put("seek_pos", seek_pos);
        params.put("scene_tags", scene_tags);
        System.out.println("insert st: "+shot_time+", sp: "+seek_pos+", tags: "+scene_tags+".");

        //insert shots table
        //insertCount += sqlSession.insert("insertShot", params);

    }
%>
<%@ include file="/assets/header.jsp" %>
<main>



</main>

</body>
</html>
