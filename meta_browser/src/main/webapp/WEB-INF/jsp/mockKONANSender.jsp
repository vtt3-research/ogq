<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.io.*" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="metavideo.vo.ContentVO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    /*

    * */
%>
<%

    List<ContentVO> contentVOList=null;
    contentVOList = (List<ContentVO>)request.getAttribute("contentVOList");

%>
<!doctype html>
<html lang="">
<head>
<meta charset="utf-8">
<title>메타데이터 비디오 브라우저</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles/content.css">

<script src="${pageContext.request.contextPath}/assets/scripts/jquery-1.12.4.js"></script>
<script src="${pageContext.request.contextPath}/assets/scripts/jquery.form.js"></script>
<script src="${pageContext.request.contextPath}/assets/scripts/common.js"></script>
<script>
function bodyLoadListener(){
;
}

function showSnackbar(message) {
// Get the snackbar DIV
var x = document.getElementById("snackbar");
$("#snackbar").text(message);

// Add the "show" class to DIV
$("#snackbar").addClass("show");

// After 3 seconds, remove the show class from DIV
setTimeout(function(){$("#snackbar").removeClass("show");}, 3000);
}
//토스트 메시지 보이기
function showToast(message) {
// Get the snackbar DIV
var x = document.getElementById("toast");
$("#toast").text(message);

// Add the "show" class to DIV
$("#toast").addClass("show");

// After 3 seconds, remove the show class from DIV
setTimeout(function(){$("#toast").removeClass("show");}, 3000);
}


</script>

</head>
<body>
<header>
<p class="Video-Tagging-Servic" onclick="location.href='${pageContext.request.contextPath}/content/allList'">Video Tagging Service</p>
<div class="header-right-container">
<div class="round_button1" onclick="location.href='${pageContext.request.contextPath}/content/myList'"><p>마이페이지</p></div>
<div class="round_button1" onclick="location.href='${pageContext.request.contextPath}/account/login'"><p>로그인</p></div>
</div>

<!-- 일반모달 -->
<div id="generalModal" class="modal">
    <div class="modal-content">
        <div class="modal-body download-complete">
            <p class="p-h3">업로드 완료</p>
            <p>&nbsp;</p>
            <p class="p-content">업로드된 동영상은 수분 후 목록에 보여집니다.</p>
        </div>
        <div class="close-div modal-footer ">
            <p>확인</p>
        </div>
    </div>
</div>

<!-- 토스트/스낵바-->
<div id="snackbar" class="snackbar">Some text some message..</div>
<div id="toast" class="toast">Some text some message..</div>

</header>
<main>
    <div>
        콜백 request주소
        <input type="text" id="callbackURLBase" name="" value="${pageContext.request.contextPath}/callbackReceiver" readonly>
        <br>
        request_id(content_id)
        <table style="text-align: left;"><tr><td>
        <%
            for(int i=0; contentVOList!=null && i<contentVOList.size(); i++){
                if(i%3==0){%></td><td><%}
                %><button cid="<%=contentVOList.get(i).getContentId()%>" onclick="changeURL('<%=contentVOList.get(i).getContentId()%>')" class="tag-button" alt="ddddddddddd"><%=contentVOList.get(i).getContentId()%></button>
                    <%=contentVOList.get(i).getContentTitle()%><br><%
            }
        %>
        </td></tr></table>
    </div>
    <br>
    <br>
    <br>
    <br>

<div>
    <form action="${pageContext.request.contextPath}/callbackReceiver">
        콜백 URL
        <input type="text" id="callbackURL" name="" value="${pageContext.request.contextPath}/callbackReceiver?content_id=<%=contentVOList.get(0).getContentId()%>">
        <textarea id="textarea1" name="" style="height:500px">{
    "tags":["recreation", "fireworks", "event", "caribbean", "sea", "bay", "resort"],
    "shots":[
          {
                "time":0,
                "seek_pos":0,
                "tags":["recreation", "fireworks", "event"]
          },
          {
                 "time":12.03,
                 "seek_pos":12233,
                 "tags":["event", "caribbean", "sea"]
          },
          {
                 "time":17.03,
                 "seek_pos":134533,
                 "tags":["bay", "resort", "swimming_pool", "Island","beach"]
          }
    ]
}</textarea>
        <input class="large-button" onclick="goCallback()" type="button" value="콜백던지기">
    </form>
</div>

<script>

    function changeURL(cid){
        $('#callbackURL').val($('#callbackURLBase').val()+"?content_id="+cid);
    }
    function goCallback(){
//        $('form').attr('action',$('#callbackURL').val());
//        $('form').attr('action',$('#callbackURL').val());
        var settings = {
            "url": $('#callbackURL').val(),
            "method": "PUT",
            "data": $('#textarea1').val()
        };

        $.ajax(settings)
            .complete(function (){
                showSnackbar("콜백이 전송되었습니다.");
            });
    }

</script>

</main>
</body>
</html>
