<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.io.*" %>
<%@ page import="metavideo.vo.AccountVO" %><%--
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

    AccountVO me=(AccountVO)session.getAttribute("myAccountVO");
    System.out.println("me: "+me);

%>
<%@ include file="/assets/header.jsp" %>
<main>

    <!-- 파일업로드 스크립트 -->
    <script>

        //등록하기 클릭시
        function goRegister(){
            console.log('checkForm');
            if($("#contentTitle").val()==null || $("#contentTitle").val().length==0){
                $(".txt_message").text("* 제목을 입력해주세요.");
            }else if($("#fileHidden1").val().length==0){
                $(".txt_message").text("* 업로드할 동영상을 선택해주세요.");
            }else if(!$("#agreeYn").prop("checked")){
                $(".txt_message").text("* CC0 라이선스에 동의해주세요.");
            }else{
                console.log('else');
                $('form').submit();
                //xhrWrite.submit();
                showModal('uploadingModal');
            }
        }


        var xhrWrite;//for xhrWrite.ajaxStop();
        (function() {/*변수는 지역화*/
            xhrWrite = $('form').ajaxForm({
                beforeSend: function() {
                    //console.log('beforeSend!!!!!!!!!!!!');
                    $("#myBar").css("width","0%");
                },
                uploadProgress: function(event, position, total, percentComplete) {
                    //console.log(new Date().getMilliseconds());
                    $("#uploadPosition").text((""+position/1024/1024).split('.')[0]);
                    $("#uploadTotal").text((""+total/1024/1024).split('.')[0]);
                    $("#myBar").css("width",(percentComplete)+"%");
                },
                success: function() {
                    ;//console.log('success!!!!!!!!!!');
                },
                fail: function(){
                    ;//console.log('fail!!!!!!!!');
                },
                complete: function(xhr) {
                    console.log('complete!!!!!!!!!!!');
                    showModal('uploadCompleteModal');
                }

            });
        })();


    </script>
    <!-- The Modal -->
    <div id="uploadingModal" class="modal">
        <!-- Modal content -->
        <div class="modal-content">
            <div class="modal-body">
                <button onclick="move()" style="display: none">Click Me to test.</button>
                <p class="p-h3">동영상을 업로드 중 입니다.</p>
                <div id="myProgress">
                    <div id="myBar"></div>
                </div>
                <p class="p-grey"><span id="uploadPosition">0</span> / <span id="uploadTotal">0</span> MB</p>
            </div>
            <div class="close-div modal-footer ">
                <p onclick="hideModal('#uploadingModal');xhrWrite.ajaxStop();">취소</p>
            </div>
        </div>
    </div>

    <!-- The Modal -->
    <div id="uploadCompleteModal" class="modal">
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
    <script>
        //업로드 컴플릿 모달의 콜백 처리(리스트 화면으로 이동)
        function hiderCallback(event){
            if(event.id=="uploadCompleteModal"){
                location.href="${pageContext.request.contextPath}/content/myList";
            }
        }
    </script>


    <div class="div-center800">
        <h2>비디오 업로드</h2>
        <div class="dummy-div"></div>
        <div class="box_info">
<form action="${pageContext.request.contextPath}/ajaxUploadVideo" method="post" enctype="multipart/form-data">
            <dl class="item_info7">
                <dt><label class="lab_info">제목</label></dt>
                <dd>
                    <div class="">
                        <input class="input7" type="text" id="contentTitle" name="contentTitle" placeholder="제목을 입력해 주세요."
                               onkeyup='valChecker($(this),256, $("#noti1"));'>
                        <br/><span id="noti1" class="small-bytes-font">0 / 256</span>
                    </div>
                </dd>
            </dl>
            <dl class="item_info7">
                <dt><label class="lab_info">설명</label></dt>
                <dd>
                    <div class="">
                        <input class="input7" type="text" id="explanation" name="explanation" placeholder="설명을 입력해 주세요.(선택입력)"
                               onkeyup='valChecker($(this),1024, $("#noti2"));'>
                        <br/><span id="noti2" class="small-bytes-font">0 / 1024</span>
                    </div>
                </dd>
            </dl>
            <dl class="item_info7">
                <dt><label for="fileText1" class="lab_info">동영상</label></dt>
                <dd>
                    <div class="">
                        <input id="fileText1" name="fileText1" class="input6" type="text" placeholder="등록할 비디오 파일을 선택해주세요.">
                        <label for="fileHidden1" class="button-navy">파일찾기</label>
                        <input type="file" accept="video/*" id='fileHidden1' name='fileHidden1' style="display: none;" onchange="$('#fileText1').val($(this)[0].files[0].name);"/>
                    </div>
                </dd>
            </dl>
            <dl class="item_info7">
                <dt><label for="fileText2" class="lab_info">메타파일</label></dt>
                <dd>
                    <div class="">
                        <input id="fileText2" name="fileText2" class="input6" type="text" placeholder="비디오와 같이 등록할 메타데이터 파일을 선택해주세요. (선택입력)">
                        <label for="fileHidden2" class="button-navy">파일찾기</label>
                        <input type="file" accept="text/plain" id='fileHidden2' name='fileHidden2' style="display: none;" onchange="$('#fileText2').val($(this)[0].files[0].name);"/>
                    </div>
                </dd>
            </dl>


            <hr style="margin: 20px 0">

            <div class="cco-link-div">
                <span>해당 비디오는 CC0 라이선스 규칙을 준수합니다.</span> <a href="https://creativecommons.org/publicdomain/zero/1.0/legalcode" target="_blank">자세히 보기</a>
                <div>
                    <input type="checkbox" id="agreeYn" name="agreeYn">동의합니다.
                </div>
            </div>


            <div class="cco-link-div">
                <p class="txt_message"></p>
            </div>

            <div class="dummy-div"></div>
<!-- 서브밋액션이 자동으로 발생해서 인풋버튼으로.
            <button class="large-button">등록하기</button>
-->
            <input onclick="goRegister()" type="button" class="large-button" value="등록하기">

</form>
        </div>


    </div>
    <div class="dummy-div"></div>
</main>

</body>
</html>
