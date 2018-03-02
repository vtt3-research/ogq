<%--
  Created by IntelliJ IDEA.
  User: changgyu78
  Date: 2017-12-24
  Time: 오후 2:18
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
%>
<%
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
        function goLoginOK(){
            checkLoginForm();
            if($(".txt_message").text().length==0){
                $('form').submit();
            }else{
            }
        }

        var str0="";
        var str1= "* 존재하지 않는 아이디 입니다.";
        var str2="* 아이디를 입력해주세요.";
        var str3="* 입력한 비밀번호가 일치하지 않습니다.";
        var str4="* 비밀번호를 입력해주세요.";
        var str5="* 아이디를 입력해주세요.";
        var str6="* 존재하지 않는 아이디 입니다.";

        function checkLoginForm() {

            var reg=/^[-A-Za-z0-9_]+[-A-Za-z0-9_.@]*/;



            var accountID=$("#accountID").val();
            var accountPW=$("#accountPW").val();

            if(!reg.test(accountID))str0=str1;
console.log('accountPW: '+accountPW);
            if(accountID==null || accountID.length==0) str0=str2;//
            //else if(accountPW==null || accountPW.length==0) str0=str3;//넘기고나서 처리.
            else if(accountPW==null || accountPW.length==0) str0=str4;//
            else str0="";

            $(".txt_message").text(str0);



        }
        function checkFindPWForm() {
            str0="";
            str5="* 아이디를 입력해주세요.";
            str6="* 존재하지 않는 아이디 입니다.";

            var reg=/^[-A-Za-z0-9_]+[-A-Za-z0-9_.@]*/;



            var accountID=$("#accountID").val();

            if(!reg.test(accountID))str0=str6;

            if(accountID==null || accountID.length==0) str0=str5;
            else if(accountID==null || accountID.length==0) str0=str6;

            else ;

            $(".txt_message").text(str0);

        }


        function checkFindIDPopupForm() {
            str11="* 소속 또는 이름을 입력해주세요.";
            str12="* 아이디를 확인할 이메일을 입력해주세요.";
            str13="* 잘못된 이메일 주소 입니다.";

            var reg=/^[-A-Za-z0-9_]+[-A-Za-z0-9_.]*[@]{1}[-A-Za-z0-9_]+[-A-Za-z0-9_.]*[.]{1}[A-Za-z]{2,5}$/;

            var userOrgan=$("#pi1").val();
            var userName = $("#pi2").val();
            var userEmail= $("#pi3").val();

            $("#caution1").text("");
            $("#caution2").text("");
            if((userOrgan==null || userOrgan.length==0)
                && (userName==null || userName.length==0)){
                $("#caution1").text(str11);
            }

            if(userEmail==null || userEmail.length==0){
                $("#caution2").text(str12);
            }else if(!reg.test(userEmail)){
                $("#caution2").text(str13);
            }

        }

        function goFindID(){
            checkFindIDPopupForm();
            if($("#caution2").text().length==0){
                $.get("${pageContext.request.contextPath}/account/findIDOK"
                    +"?pi1="+$("#pi1").val()
                    +"&pi2="+$("#pi2").val()
                    +"&pi3="+$("#pi3").val()
                    , function(){});
                showGeneralModal("알림","확인이 끝난 후, 입력해주신 "+$("#pi3").val()+"으로 아이디를 보내드립니다.")
            }
        }

        function goFindPW(){
            checkFindPWForm();
            if($(".txt_message").text().length==0){
                //TODO 비밀번호 찾기 처리
                ///account/resetPassword
                //비밀번호 찾기 완료팝업, 임시비번생성, DB갱신, 이메일
                $.get("${pageContext.request.contextPath}/account/resetPassword?accountID="+$("#accountID").val(), function(){});
                showGeneralModal("임시 비밀번호 메일 발송",""+$("#accountID").val()+"으로 임시 비밀번호를 발급하여 보내드렸습니다.")
            }
        }
        //checkFindPWForm();


    </script>
</head>
<body>
<header>
    <p class="Video-Tagging-Servic" onclick="location.href='${pageContext.request.contextPath}/content/allList'">Video Tagging Service</p>


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


    <!-- 아이디 찾기 팝업 -->
    <div id="findID" class="modal">
        <!-- Modal content -->
        <div class="wide-modal-content">
            <div class="modal-body">
                <p class="p-h3">아이디 찾기</p>
                <br>
                <dl class="item-dl">
                    <dt><label class="lab_info">소속</label></dt>
                    <dd>
                        <div class="wrap_inp">
                            <input class="input-general" type="text" id="pi1" name="pi1" value="" placeholder="가입 시 입력한 소속 기관 또는 회사명"
                                   onkeyup='valChecker($(this),64, $("#byteLength1"));'>
                            <br/><span id="byteLength1" class="small-bytes-font">0 / 64</span>
                        </div>
                    </dd>
                </dl>
                <dl class="item-dl">
                    <dt><label class="lab_info">이름</label></dt>
                    <dd>
                        <div class="wrap_inp">
                            <input class="input-general" type="text" id="pi2" name="pi2" value="" placeholder="가입시 입력한 서비스 내에서 표시될 이름"
                                   onkeyup='valChecker($(this),32, $("#byteLength2"));'>
                            <br/><span id="byteLength2" class="small-bytes-font">0 / 32</span>
                            <p id="caution1" class="txt_message">* 필수입력</p>
                        </div>
                    </dd>
                </dl>
                <dl class="item-dl">
                    <dt><label class="lab_info">이메일</label></dt>
                    <dd>
                        <div class="wrap_inp">
                            <input class="input-general" type="text" id="pi3" name="pi3" value="" placeholder="아이디를 받을 이메일 주소"
                                   onkeyup='valChecker($(this),128, $("#byteLength3"));'>
                            <br/><span id="byteLength3" class="small-bytes-font">0 / 128</span>
                            <p id="caution2" class="txt_message">* 필수입력</p>
                        </div>
                    </dd>
                </dl>
                <div style="margin-top: 30px">
                    <button onclick="goFindID()" class="large-button" style="width: 250px;">확인</button>
                    <button onclick="hideModal()" class="large-button" style="width: 250px;">취소</button>
                </div>

            </div>
        </div>
    </div>

</header>

<main>

    <div class="dummy-div"></div>
    <div class="div-center320">
        <form action="${pageContext.request.contextPath}/account/loginOK">
            <div>
                <p class="p-h2">환영합니다!</p><br/>
                <input id="accountID" name="accountID" class="input-general" type="text" placeholder=" 아이디 (이메일)"/>
                <input id="accountPW" name="accountPW" class="input-general" type="password" placeholder=" 비밀번호"/>
                <input id="certify" name="certify" type="hidden" value="<%=request.getParameter("certification")%>"/>
                <input class="large-button" style="margin-top: 18px" type="button" value="로그인" onclick="goLoginOK();"/>
            </div>
            <div>
                <p class="txt_message"></p>
            </div>
            <p><b>
                <a href="${pageContext.request.contextPath}/account/signin">회원가입</a> | <a onclick="showModal('findID');">아이디 찾기</a> | <a onclick="goFindPW()">비밀번호 찾기</a>
            </b></p>
        </form>
    </div>


</main>
<script>
<%
String request_msg="";
String showMessage="";
request_msg=request.getParameter("msg");

if(request_msg==null || request_msg.length()==0) showMessage="";
else if(request_msg.equals("noID")) showMessage="str1";
else if(request_msg.equals("wrongPW")) showMessage="str3";
else if(request_msg.equals("nocert")) showMessage="'* 메일인증이 필요합니다.'";
else showMessage="";


%>
$('.txt_message').text(<%=showMessage%>);
</script>
</body>
</html>
