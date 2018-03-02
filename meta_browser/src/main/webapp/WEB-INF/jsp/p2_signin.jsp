<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.io.*" %><%--
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
    String myMessage="일단 OK.";

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
        function goJoin(){
            checkJoininForm();
            if($("#caution1").text().length==0 && $("#caution2").text().length==0){
                //window.location.href="/login";
                // TODO: 개발용 링크 제거, 메일발송
                //$('form').submit();
                xhrSignin.submit();
            }
        }

        var xhrSignin;
        $(function() {
            xhrSignin = $('form').ajaxForm({
                fail: function(){
                    console.log('fail!!!!!!!!');
                },
                complete: function(xhr) {
                    console.log('complete!!!!!!!!');
                    console.log(xhr.responseText);
                    if(xhr.responseText=="OK"){
                        showGeneralModal("가입인증 메일 발송","입력하신 "+$("#emailAddress").val()+"으로 가입인증 메일을 발송했습니다.");
                    }else if(xhr.responseText=="Duplicate"){
                        console.log('Duplicate!!!!!!!!');
                        $("#caution1").text(str3);
                    }else "Fail";
                }
            });
        });

//  span.onclick = function() {
//if(event.id=="generalModal"){
//location.href="${pageContext.request.contextPath}/login";
//}
//  }

        function hiderCallback(event){
            if(event.id=="generalModal"){
                location.href="${pageContext.request.contextPath}/login";
            }
        }

        function checkJoininForm() {
            str0="";
            str1="* 이메일을 입력해주세요.";
            str2="* 잘못된 이메일 주소 입니다.";
            str3="* 이미 등록된 이메일 주소 입니다.";

            str4="* 비밀번호를 입력해주세요.";
            str5="* 비밀번호가 일치하지 않습니다.";
            str6="* 최소 8자리 이상 입력해주세요.";

            var reg=/^[-A-Za-z0-9_]+[-A-Za-z0-9_.]*[@]{1}[-A-Za-z0-9_]+[-A-Za-z0-9_.]*[.]{1}[A-Za-z]{2,5}$/;



            var emailAddress=$("#emailAddress").val();
            var password1=$("#password1").val();
            var password2=$("#password2").val();

            if(!reg.test(emailAddress))str0=str2;

            if(emailAddress==null || emailAddress.length==0) str0=str1;//
            //else if(accountPW==null || accountPW.length==0) str0=str3;//TODO 넘기고나서 처리.
            else ;
            $("#caution1").text(str0);

            if(password1==null || password1.length==0) str0=str4;//
            else if(password2==null || password1!=password2) str0=str5;//
            else if(password1==null || password1.length<8) str0=str6;//
            else ;

            $("#caution2").text(str0);



        }
    </script>

</head>
<body>
<main>


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





    <div class="div-center600">
        <h2>회원가입</h2>
        <div class="dummy-div"></div>
        <div class="box_info">
<form action="${pageContext.request.contextPath}/account/signinOK">

            <dl class="item_info">
                <dt><label class="lab_info">소속</label></dt>
                <dd>
                    <div class="wrap_inp">
                        <input class="input-general" type="text" id="organ" name="organ" placeholder="소속 기관 또는 회사명(선택입력)"
                               onkeyup='valChecker($(this),64, $("#byteLength1"));'>
                        <br/><span id="byteLength1" class="small-bytes-font">0 / 64</span>
                    </div>
                </dd>
            </dl>
            <dl class="item_info">
                <dt><label class="lab_info">이름</label></dt>
                <dd>
                    <div class="wrap_inp">
                        <input class="input-general" type="text" id="userName" name="userName" placeholder="서비스내에서 표시될 이름(선택입력)"
                               onkeyup='valChecker($(this),32, $("#byteLength2"));'>
                        <br/><span id="byteLength2" class="small-bytes-font">0 / 32</span>
                        <p style="margin-left: 8px;" class="txt_message">* 미 입력시 이메일 주소의 일부가 이름으로 사용됩니다.</p>
                    </div>
                </dd>
            </dl>
            <dl class="item_info">
                <dt><label class="lab_info">이메일</label></dt>
                <dd>
                    <div class="wrap_inp">
                        <input class="input-general" type="text" id="emailAddress" name="emailAddress" placeholder="이메일 주소는 가입인증 및 아이디로 사용됩니다."
                               onkeyup='valChecker($(this),128, $("#byteLength3"));'>
                        <br/><span id="byteLength3" class="small-bytes-font">0 / 128</span>
                        <p id="caution1" class="txt_message"></p>
                    </div>
                </dd>
            </dl>
            <dl class="item_info info_password">
                <dt><label for="password1" class="lab_info">비밀번호</label></dt>
                <dd>
                    <div class="wrap_inp">
                        <input class="input-general" type="password" id="password1" name="password1" placeholder="최소 8자 이상">
                    </div>
                </dd>
            </dl>
            <dl class="item_info info_password" style="margin-top: 0">
                <dt><label for="password2" class="lab_info">비밀번호 재확인</label></dt>
                <dd>
                    <div class="wrap_inp">
                        <input class="input-general" type="password" id="password2" name="password2" placeholder="동일한 비밀번호를 입력해주세요.">
                    </div>
                    <p id="caution2" class="txt_message"></p>
                </dd>
            </dl>

            <div class="hr-div"></div>

            <input class="large-button" onclick="goJoin()" type="button" value="회원 가입"/>
</form>
        </div>


    </div>
    <div class="dummy-div"></div>
</main>


</body>
</html>
