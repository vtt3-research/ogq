<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.io.*" %><%--
  Created by IntelliJ IDEA.
  User: changgyu78
  Date: 2017-12-24
  Time: 오후 2:18
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="okhttp3.*" %>
<%@ page import="metavideo.vo.ContentVO" %>
<%@ page import="java.util.List" %>
<%@ page import="metavideo.common.ETCLib" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>


<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
/*

* */
%>
<%
    Map<String, Object> params=null;
    List<ContentVO> contentVOList = null;
    int allListTotalCount=0;

    params=(HashMap<String, Object>)request.getAttribute("params");
    contentVOList=(List<ContentVO>)request.getAttribute("contentVOList");
    allListTotalCount=(int)request.getAttribute("allListTotalCount");

    int pageNo=Integer.parseInt(params.get("pageNo").toString());

%>
<%@ include file="/assets/header.jsp" %>
<main>
    <!-- The Modal -->
    <div id="uploadModal" class="modal">
        <!-- Modal content -->
        <div class="modal-content">
            <div class="modal-body">
                <button onclick="move()" style="display: none">Click Me to test.</button>
                <p class="p-h3">동영상을 업로드 중입니다.</p>
                <div id="myProgress">
                    <div id="myBar"></div>
                </div>
                <p class="p-grey">147 / 930 MB</p>
            </div>
            <div class="close-div modal-footer ">
                <p>확인</p>
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



    <div class="" style="height: 32px;">
        <div style="float:left;">
            <!--
            <button class="colored-button" onclick="showModal('uploadModal');">업로드</button>
            -->
            <a href="${pageContext.request.contextPath}/content/writeContent"><button class="colored-button" >업로드</button></a>
        </div>
        <div style="float:right;text-align: right;">
            <button class="white-button" onclick="downloadSelected()">선택 다운로드</button>
<script>
    function downloadSelected() {
        if($('input.cbItem:checked').length==0){
            showToast("다운로드할 항목을 선택해 주세요.");
        }else{
            //TODO 다운로드
            suffix=0;
            downloadAll();
        }
    }

    var suffix=0;

    function downloadAll(){
        var checkBoxObject = $('.cbItem')[suffix++];
        if (checkBoxObject){//11번째 체크박스이면 undefined 이고, false
            if (checkBoxObject.checked){
                //이동은 브라우저가 이동해버린다. location.href = "https://s3.ap-northeast-2.amazonaws.com/vtt-ogq/" + oChk.value;
                $("#btn_"+$(checkBoxObject).attr('contentID')).click();
                setTimeout(function(){downloadAll()}, 1000);
            }else{
                downloadAll();
            }
        }
    }
</script>

        </div>
    </div>

    <div class="div-center">
        <table>
            <thead>
            <tr>
                <th width="10"><input type="checkbox" onclick="$('input.cbItem').prop('checked',$(this).prop('checked'))"></th>
                <th width="30">번호</th>
                <th>등록자</th>
                <th width="10">미리보기</th>
                <th>제목</th>
                <th width="70">재생 시간</th>
                <th width="70">파일 크기</th>
                <th width="80">등록일</th>
                <th width="90">비고</th>
            </tr>
            </thead>
            <script>
                function extractRuntime(rid) {
                    //console.log("rid : "+rid);
                    var rt = $('#v_'+rid)[0].duration;//시간 변수
                    myDate = new Date();
                    myDate.setHours(0, 0, 0, 0);
                    myDate.setSeconds(rt);
                    var rtString= myDate.getHours() + ":" + myDate.getMinutes() + ":" + myDate.getSeconds();//포맷변환
                    if(rtString.indexOf('NaN')>=0) return;//비디오파일이 없다.
                    $('#r_'+rid).text(rtString);//세팅
                    $.get("${pageContext.request.contextPath}/ajaxVideoRuntime?content_id="+rid+"&video_running_time="+rtString);//저장
                }
            </script>


            <tbody>
<%
    for(int i=0; contentVOList!=null && i<contentVOList.size(); i++){
%>
            <tr>
                <td><input contentID="<%=contentVOList.get(i).getContentId()%>" id="cb_<%=contentVOList.get(i).getContentId()%>" name="contentId[]" value="<%=contentVOList.get(i).getContentId()%>_<%=contentVOList.get(i).getVideoFileUrl()%>" type="checkbox" class="cbItem"></td>
                <td><%=""+(((pageNo-1)*10)+(i+1))%></td>
                <td><%=contentVOList.get(i).getAccountName()%></td>
                <td><a href="${pageContext.request.contextPath}/content/allDetail?contentID=<%=contentVOList.get(i).getContentId()%>">
                    <div class="video-preview-frame">
                        <video id="v_<%=contentVOList.get(i).getContentId()%>" preload="metadata" width="80" height="45">
                            <source src="https://s3.ap-northeast-2.amazonaws.com/vtt-ogq/movie_attach/<%=contentVOList.get(i).getContentId()%>_<%=contentVOList.get(i).getVideoFileUrl()%>#t=0.1" type="video/mp4" />
                        </video>
                    </div>
                </a></td>
                <td><a href="${pageContext.request.contextPath}/content/allDetail?contentID=<%=contentVOList.get(i).getContentId()%>"><%=contentVOList.get(i).getContentTitle()%></a></td>
                <td id="r_<%=contentVOList.get(i).getContentId()%>"><%=contentVOList.get(i).getVideoRunningTime()%></td>
                <td><%=ETCLib.obtainFileSizeString(contentVOList.get(i).getVideoFileSize())%></td>
                <td><%=ETCLib.obtainDateString(contentVOList.get(i).getRegisteredDt())%></td>
                <td><a id="download_<%=contentVOList.get(i).getContentId()%>" href="https://s3.ap-northeast-2.amazonaws.com/vtt-ogq/movie_attach/<%=contentVOList.get(i).getContentId()%>_<%=contentVOList.get(i).getVideoFileUrl()%>" target="_blank" download="<%=contentVOList.get(i).getVideoFileUrl()%>"><button id="btn_<%=contentVOList.get(i).getContentId()%>" class="white-button">다운로드</button></a></td>
            </tr>
<%if(contentVOList.get(i).getVideoRunningTime()==null || contentVOList.get(i).getVideoRunningTime().length()<4){%>
<script>
    var c_<%=contentVOList.get(i).getContentId()%>=10;
    var ih_<%=contentVOList.get(i).getContentId()%> = setInterval(function () {
        console.log("id: ih_<%=contentVOList.get(i).getContentId()%>");
        if(!((""+$('#v_<%=contentVOList.get(i).getContentId()%>')[0].duration).indexOf(".")>=0 || isNaN($('#v_<%=contentVOList.get(i).getContentId()%>')[0].duration) || c_<%=contentVOList.get(i).getContentId()%>--<0)) return;
        extractRuntime('<%=contentVOList.get(i).getContentId()%>');
        clearInterval(ih_<%=contentVOList.get(i).getContentId()%>);
    }, 1000);
</script>
<%}%>
<%}
    if(
            (contentVOList==null || contentVOList.size()==0)
                    && (request.getParameter("keyword")!=null && request.getParameter("keyword").length()!=0)
            ) {%>
            <tr>
                <td colspan="9"><B>검색된 결과가 없습니다.</B></td><script>showSnackbar("검색된 결과가 없습니다.");</script>
            </tr>
<%}else if(contentVOList==null || contentVOList.size()==0){%>
            <tr>
                <td colspan="9"><B>등록된 비디오가 없습니다.</B></td>
            </tr>
<%}%>
            </tbody>
        </table>
    </div>
    <br/>
    <!-- paginate -->
    <%

        String thisURL=request.getContextPath()+"/content/allList";
        String qsKeyword="";
        String qsPage="";
        int totalCount = allListTotalCount;
        int printingPage =1;

        //총 페이지 수
        int totalPageCount=((int)((totalCount-1)/10))+1;
        int startPage=((int)(pageNo/10))*10+1;


        qsKeyword=request.getParameter("keyword");
        if(qsKeyword!=null && qsKeyword.length()>0){
            qsKeyword = "keyword="+qsKeyword;
        }else{
            qsKeyword="";
        }

        qsPage=request.getParameter("pageNo");
        if(qsPage!=null && qsPage.length()>0){
            qsPage="&pageNo="+qsPage;
        }else{
            qsPage="&pageNo=1";
        }

    %>
    <div class="div-center">
        <%if(startPage>10){%>
        <a href="<%=thisURL%>?<%=qsKeyword%>&<%="pageNo="+(pageNo-10)%>"><button class="navi-button"> &lt; </button></a>
        <%}%>
        <%
            for(int i=0; i<10 && i<totalPageCount; i++){
                printingPage =startPage+i;
                if(printingPage > totalPageCount) break;
                if(printingPage == pageNo){
        %>
        <button class="navi-button_me"><%=printingPage%></button>
        <%
        }else{
        %>
        <a href="<%=thisURL%>?<%=qsKeyword%>&pageNo=<%=printingPage%>"><button class="navi-button"><%=printingPage%></button></a>
        <%
                }
            }
        %>
        <%if(pageNo+10 <= totalPageCount){%>
        <a href="<%=thisURL%>?pageNo=<%=(pageNo+10)%>" class="arrow"><button class="navi-button"> &gt; </button></a>
        <%}%>

    </div>
    <!-- //paginate -->
<div class="div-center">
    <form action="javascript:goSearch($('#keyword').val())">
        <input id="keyword" name="keyword" class="search-input" placeholder="검색어">
        <button class="search-button" onclick="goSearch($('#keyword').val())">
            <img src="${pageContext.request.contextPath}/assets/images/ic-search.png"
                 class="ic_search">
        </button>
    </form>
    <script>
        function goSearch(keyword) {
            if(keyword.length==0){
                showSnackbar("검색어를 입력해주세요.");
            }else{
                location.href="${pageContext.request.contextPath}/content/allList?keyword="+keyword;
            }

        }
    </script>
</div>


    <div class="dummy-div"></div>
</main>

</body>
</html>
