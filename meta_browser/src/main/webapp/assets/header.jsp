<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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

        function downloadSelected(){
        }
    </script>

</head>
<body onload="bodyLoadListener()">
<header>
    <p class="Video-Tagging-Servic" onclick="location.href='${pageContext.request.contextPath}/content/allList'">Video Tagging Service</p>
    <div class="header-right-container">
        <div class="round_button1" onclick="location.href='${pageContext.request.contextPath}/content/myList'"><p>마이페이지</p></div>
        <div class="round_button1" onclick="showToast('로그아웃 되었습니다.');setTimeout(function() {location.href='${pageContext.request.contextPath}/account/logout';}, 3000);"><p>로그아웃</p></div>
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
