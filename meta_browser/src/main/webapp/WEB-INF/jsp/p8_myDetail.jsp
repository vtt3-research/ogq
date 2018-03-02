<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.Map" %>
<%@ page import="metavideo.vo.ContentVO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="metavideo.common.ETCLib" %>
<%@ page import="metavideo.vo.ShotVO" %>
<%@ page import="java.util.List" %><%--
  Created by IntelliJ IDEA.
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
    Map<String, String> params=null;
    ContentVO contentVO = null;
    List<ShotVO> shotVOList=null;

    params = (Map<String, String>)request.getAttribute("params");
    contentVO = (ContentVO)request.getAttribute("contentVO");
    shotVOList = (List<ShotVO>) request.getAttribute("shotVOList");
    //System.out.println("shotVOList: "+shotVOList);

%>
<%@ include file="/assets/header.jsp" %>

<main>
    <script type="text/javascript">
        var shotMap={0:""
            <%for(ShotVO shotVO: shotVOList){%>
            ,"<%=shotVO.getShotTime()%>":"<%=shotVO.getSceneTags()%>"
            <%}%>
        };

        var keysSorted = Object.keys(shotMap).sort(function(a,b){return a-b;});


        //비디오 타임 갱신 리스너
        function timeUpdateListener3(theTime) {
            var leftTime=0;//최근 샷 시각

            for(var idx in keysSorted){
                var candidateTime=keysSorted[idx];//샷 시간들 중에서
                if(parseInt(candidateTime) < parseInt(theTime)){
                    leftTime=candidateTime;//이전시간들은 계속 저장하다가
                }else{
                    break;//큰게 발견되면 종료하면 최근 샷 시간만 남는다.
                }
            }
            console.log('shotMap['+leftTime+']'+shotMap[leftTime]);
            //태그업데이트
            $("#sceneTagFrame").html(getTagsHTML(shotMap[leftTime]));
        }

        var gv0;
        var gv1;
        var gv2;
        var gv3;
        //샷태그에 대한 HTML 문장을 받는다.
        function getTagsHTML(tagsString){
            gv0=tagsString;

            if( tagsString===undefined || tagsString==undefined || tagsString==null || tagsString=="") return "";
            gv1=tagsString;
            tagsString = tagsString.split("|").join("</button><button class=\"tag-button\">#");
            gv2=tagsString;
            tagsString = tagsString.substr(9, tagsString.length-37);//<37>태그<9>
            gv3=tagsString;
            return tagsString;
        }
    </script>

    <!-- 비디오 정보 변경 팝업 -->
    <div id="editModal" class="modal">
        <!-- Modal content -->
        <div class="wide-modal-content">
            <div class="modal-body">
                <p class="p-h3">게시물 정보 수정</p>
                <p class="txt_message4">제목, 설명을 제외한 다른 부분은 수정이 불가합니다.</p>
                <br>
<form id="updateContentForm" action="${pageContext.request.contextPath}/content/updateContentOK">
    <input name="contentId" type="hidden" value="<%=contentVO.getContentId()%>"/>
<!--
//ToDo: 갱신 제출처리
-->
                <dl class="item-dl">
                    <dt><label class="lab_info">제목</label></dt>
                    <dd>
                        <div class="wrap_inp">
                            <input class="" type="text" id="contentTitle" name="contentTitle" value="<%=contentVO.getContentTitle()%>" placeholder=""
                                   onkeyup='valChecker($(this),256, $("#titleLength"));'>
                            <p class="txt_message3">* 필수입력</p>
                            <br/><span id="titleLength" class="small-bytes-font"><%=contentVO.getContentTitle().length()%>0 / 256</span>
                        </div>
                    </dd>
                </dl>
                <dl class="item-dl">
                    <dt><label class="lab_info">설명</label></dt>
                    <dd>
                        <div class="wrap_inp">
                <textarea id="explanation" name="explanation" class="textarea-h244"
                          onkeyup='valChecker($(this),1024, $("#explanationLength"));'><%=contentVO.getExplanation()%></textarea>
                            <br/><span id="explanationLength" class="small-bytes-font"><%=contentVO.getExplanation().length()%>0 / 1024</span>
                        </div>
                    </dd>
                </dl>
</form>
                <div>
                    <p id="noti1" class="txt_message3" style="padding-left: 70px;"></p>
                </div>
                <br>
                <div style="margin: 10px;">
                    <button class="large-button" onclick="goUpdate()">변경하기</button>
                    <button class="large-button" onclick="hideModal()">취소하기</button>
                </div>

                <script>
                    function goUpdate() {
                        if($('#contentTitle')==null ||$('#contentTitle').length==0 ){
                            $('#noti1').text("* 이름을 입력해주세요.");
                        }else{
                            $('#updateContentForm').submit();
                        }
                    }
                </script>
            </div>
        </div>
    </div>

    <!-- The Modal -->
    <div id="completeModal" class="modal">
        <div class="modal-content">
            <div class="modal-body download-complete">
                <p class="p-h3">업로드 완료</p>
                <p>&nbsp;</p>
                <p class="">업로드된 동영상은 수분 후 목록에 보여집니다.</p>
            </div>
            <div class="close-div modal-footer ">
                <p>확인</p>
            </div>
        </div>
    </div>



    <div style="height: 400px">
        <div class="video-frame">
            <video preload="metadata" width="700" height="394" id="video1" controls ontimeupdate="console.log('ontimeupdate');timeUpdateListener3($(this)[0].currentTime);">
                //  Replace these with your own video files.
                <source src="https://s3.ap-northeast-2.amazonaws.com/vtt-ogq/movie_attach/<%=contentVO.getContentId()%>_<%=contentVO.getVideoFileUrl()%>#t=0.1" type="video/mp4" />
                HTML5 Video is required for this example.
                <a href="https://s3.ap-northeast-2.amazonaws.com/vtt-ogq/movie_attach/<%=contentVO.getContentId()%>_<%=contentVO.getVideoFileUrl()%>" download="<%=contentVO.getVideoFileUrl()%>">Download the video</a> file.
            </video>

        </div>
        <div class="scene-tags-frame">
            <p class="p-h3">Scene Tags</p>
            <hr>
            <p class="p-tag-button">
                <div id="sceneTagFrame"></div>
            </p>
        </div>
    </div>
    <br/>

    <div>
        <p class="p-h3">All Tags</p>
        <hr style="visibility: hidden;">
        <p class="p-tag-button">
            <script>document.write(getTagsHTML('<%=contentVO.getEntireTags()%>'));</script>
        </p>
    </div>
    <br/>
    <hr>
    <br/>
    <div>
        <div style="float: right;">
            <button class="white36-button" onclick="showModal('editModal');">정보수정</button>
            <a href="https://s3.ap-northeast-2.amazonaws.com/vtt-ogq/movie_attach/<%=contentVO.getContentId()%>_<%=contentVO.getVideoFileUrl()%>" target="_blank" download="<%=contentVO.getVideoFileUrl()%>">
                <button class="colored36-button"><i class="ic_download"></i>다운로드</button>
            </a>
        </div>
        <p class="p-h2"><%=contentVO.getContentTitle()%></p>
        <p class="p-grey">등록일: <%=ETCLib.obtainDatetimeString(contentVO.getRegisteredDt())%> / 수정일: <%=ETCLib.obtainDatetimeString(contentVO.getLastUpdateDt())%></p>
    </div>

    <div class="explanation-content-div">
        <%=contentVO.getExplanation()%>
    </div>
    <br/>
    <hr>
    <br/>

    <div>
        <p>
            <b>등록자</b> : <%=contentVO.getAccountName()%> (<%=contentVO.getOrgan()%>)&nbsp; &nbsp; | &nbsp; &nbsp;
            <b>파일명</b> : <%=contentVO.getVideoFileUrl()%>&nbsp; &nbsp; | &nbsp; &nbsp;
            <b>파일크기</b> : <%=ETCLib.obtainFileSizeString(contentVO.getVideoFileSize())%>&nbsp; &nbsp; | &nbsp; &nbsp;
            <b>재생시간</b> : <span id="runtime"><%=contentVO.getVideoRunningTime()%></span>
        </p>
    </div>

</main>

</body>
</html>
