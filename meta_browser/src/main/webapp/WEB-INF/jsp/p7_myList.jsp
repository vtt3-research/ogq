<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="metavideo.vo.ContentVO" %>
<%@ page import="java.util.List" %>
<%@ page import="metavideo.vo.AccountVO" %>
<%@ page import="metavideo.common.ETCLib" %><%--
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
    Map<String, Object> params=null;
    AccountVO myAccountVO =null;
    List<ContentVO> contentVOList = null;
    int myListTotalCount=0;

    params=(HashMap<String, Object>)request.getAttribute("params");
    myAccountVO=(AccountVO)request.getAttribute("myAccountVO");
    contentVOList=(List<ContentVO>)request.getAttribute("contentVOList");
    myListTotalCount=(int)request.getAttribute("myListTotalCount");

    int pageNo=Integer.parseInt(params.get("pageNo").toString());

    String updated=request.getParameter("updated");
    System.out.println("updated: "+updated);

    String myAccountUpdated="";
    if(updated!=null && updated.length()>0){
        if(updated.equals("y")) {
            myAccountUpdated = "showGeneralModal('알림','정보를 수정하였습니다.');";
        }else if(updated.equals("oldPassword")){
            myAccountUpdated = "showGeneralModal('알림','현재 비밀번호를 확인해 주세요.');";
        }
    }else{myAccountUpdated="";}

%>
<%@ include file="/assets/header.jsp" %>

<main>

    <script>

        $(document).ready(function (){
            <%=myAccountUpdated%>
            <%
            String deleted=request.getParameter("deleted");
            if(deleted!=null && deleted.equals("y")){%>
            showGeneralModal("삭제","삭제하였습니다.");
            <%}%>

        });




        function deleteAContent(contentID){
            var send_array = Array();
            send_array[0] = contentID;

            $("#delIDs").val(send_array);
            $('#delCount').text("1");
            showModal('deleteModal');
        }

    </script>



    <!-- 내 정보 변경 팝업 -->
    <div id="myInfoUpdateModal" class="modal">
        <!-- Modal content -->
        <div class="wide-modal-content">
            <div class="modal-body">
                <p class="p-h3">정보 변경</p>
                <br>
<form id="updateMyAccountForm" action="${pageContext.request.contextPath}/account/updateMyAccountOK">
                <dl class="item-dl">
                    <dt><label class="lab_info">소속</label></dt>
                    <dd>
                        <div class="wrap_inp">
                            <input class="input-general" type="text" name="organ"
                                   value="<%=myAccountVO.getOrgan().length()==0?"-":myAccountVO.getOrgan()%>"
                                   placeholder="소속 기관 또는 회사명(선택입력)"
                                   onkeyup='valChecker($(this),64, $("#byteString1"));'>
                            <p ID="caution1" class="txt_message3">* 미 입력시 이메일 주소의 일부가 이름으로 사용됩니다.</p>
                            <br/><span id="byteString1" class="small-bytes-font"><%=myAccountVO.getOrgan().length()%> / 64</span>
                        </div>
                    </dd>
                </dl>
                <dl class="item-dl">
                    <dt><label class="lab_info">이름</label></dt>
                    <dd>
                        <div class="wrap_inp">
                            <input class="input-general" type="text" id="accountName" name="accountName" value="<%=myAccountVO.getAccountName()%>" placeholder="홍길동"
                                   onkeyup='valChecker($(this),32, $("#byteString2"));'>
                            <br/><span id="byteString2" class="small-bytes-font"><%=myAccountVO.getAccountName().length()%> / 32</span>
                        </div>
                    </dd>
                </dl>
                <dl class="item-dl">
                    <dt><label class="lab_info">이메일</label></dt>
                    <dd>
                        <div class="wrap_inp">
                            <p class="p-left"><%=myAccountVO.getEmail()%></p>
                        </div>
                    </dd>
                </dl>
                <div style="margin: 10px;">
                    <!--
                    <button class="large-button" onclick="goUpdateMyAccount()">변경하기</button>
                    -->
                    <input type="button" class="large-button" onclick="goUpdateMyAccount()" value="변경하기">
                </div>
</form>
                <script>
                    function goUpdateMyAccount() {
                        if($('#accountName')==null || $('#accountName').val()==null  || $('#accountName').val().length==0 ){
                            $('#accountName').val("<%=myAccountVO.getEmail().split("@")[0]%>");
                        }else{
                            $('#updateMyAccountForm').submit();
                        }
                    }
                </script>
            </div>
        </div>
    </div>


    <!-- 비밀번호 변경 팝업 -->
    <div id="passwordUpdateModal" class="modal">
        <!-- Modal content -->
        <div class="wide-modal-content">
            <div class="modal-body">
                <p class="p-h3">비밀번호 변경</p>
                <br>


<form id="updatePWForm" action="${pageContext.request.contextPath}/account/updatePWOK">
    <!--
    //todo: /account/updatePWOK
    //TODO: 비번 변경

    -->
                <dl class="item_info">
                    <dt><label class="lab_info">현재 비밀번호</label></dt>
                    <dd>
                        <div class="wrap_inp">
                            <input class="input-general" type="text" id="oldPassword" name="oldPassword" placeholder="현재 비밀번호를 입력해주세요.">
                        </div>
                    </dd>
                </dl>
                <dl class="item_info info_password">
                    <dt><label for="password1" class="lab_info">새 비밀번호</label></dt>
                    <dd>
                        <div class="wrap_inp">
                            <input class="input-general" type="password" id="password1" name="password1" placeholder="변경할 비밀번호를 입력해주세요.">
                        </div>
                    </dd>
                </dl>
                <dl class="item_info info_password" style="margin-top: 0">
                    <dt><label for="password2" class="lab_info">새 비밀번호 확인</label></dt>
                    <dd>
                        <div class="wrap_inp">
                            <input class="input-general" type="password" id="password2" name="password2" placeholder="확인을 위해 동일한 비밀번호를 다시 한번 입력해주세요.">
                        </div>
                    </dd>
                </dl>
                <p id="caution2" class="txt_message"></p>
</form>
                <div style="margin: 10px;">
                    <input type="button" class="large-button" onclick="goUpdatePassword()" value="변경하기" />
                    <input type="button" class="large-button" onclick="hideModal()" value="취소하기" />
<!-- 서브밋액션이 자동으로 발생해서 인풋버튼으로.
<button class="large-button">등록하기</button>
-->
                </div>
                <script>
                    function goUpdatePassword() {
                        if(checkPWForm()) $('#updatePWForm').submit();
                    }



                    function checkPWForm() {
                        str0="";

                        str4="* 비밀번호를 입력해주세요.";
                        str5="* 비밀번호가 일치하지 않습니다.";
                        str6="* 최소 8자리 이상 입력해주세요.";

                        var reg=/^[-A-Za-z0-9_]+[-A-Za-z0-9_.]*[@]{1}[-A-Za-z0-9_]+[-A-Za-z0-9_.]*[.]{1}[A-Za-z]{2,5}$/;



                        var oldPassword=$("#oldPassword").val();
                        var password1=$("#password1").val();
                        var password2=$("#password2").val();

                        if(oldPassword==null || oldPassword.length==0) str0=str4;//
                        else if(password1==null || password1.length<8 || password2==null || password2.length<8) str0=str6;//
                        else if(password1!=password2) str0=str5;//
                        else return true;

                        $("#caution2").text(str0);
                        return false;

                    }
                </script>
            </div>
        </div>
    </div>


    <!-- 삭제알림팝업 -->
    <div id="deleteModal" class="modal">
        <div class="modal-content">
            <form id="deleteForm" name="deleteForm" action="${pageContext.request.contextPath}/content/deleteContent">
                <input type="hidden" id="delIDs" name="delIDs"/>
            </form>
            <div class="modal-body download-complete">
                <p class="p-h3">삭제</p>
                <p>&nbsp;</p>
                <p class="">선택한 <span id="delCount">1</span>개의 파일을 삭제하시겠습니까?</p>
            </div>
            <div class="close-div modal-footer50" style="float:left;" onclick="$('#deleteForm').submit()">
                <p>확인</p>
            </div>
            <div class="close-div modal-footer50" style="float:right;" onclick="hideModal()">
                <p>취소</p>
            </div>
        </div>
    </div>




    <div class="div-left">
        <div>
            <p>
                <span class="p-h2">내정보</span>&nbsp;&nbsp;
                <button class="white-button" onclick="showModal('myInfoUpdateModal');">정보 변경</button>&nbsp;&nbsp;
                <button class="white-button" onclick="showModal('passwordUpdateModal');">비밀번호 변경</button>&nbsp;&nbsp;
            </p>

            ,
        </div>
        <div>
            <div>
                <p>
                    <b>소속</b> : <%=myAccountVO.getOrgan().length()==0?"-": myAccountVO.getOrgan()%> &nbsp; &nbsp; &nbsp; &nbsp;
                    <b>이름</b> : <%=myAccountVO.getAccountName()%>&nbsp; &nbsp; &nbsp; &nbsp;
                    <b>이메일</b> : <%=myAccountVO.getEmail()%>&nbsp; &nbsp; &nbsp; &nbsp;
                </p>
            </div>
        </div>
    </div>

</main>
<br><main>

    <div class="div-left">
        <div>
            <p>
                <span class="p-h2">내 게시물</span>
            </p>
        </div>
    </div>

    <div class="div-center">
        <div style="float:left;">
            <button class="colored-button" onclick="location.href='${pageContext.request.contextPath}/content/writeContent'">업로드</button>
        </div>
        <div style="float:right;text-align: right;">
            <button class="white-button" onclick="deleteSelected2();">선택 삭제</button>
        </div>
        <script>

            function deleteSelected2(){
                if($('input.cbItem:checked').length==0){
                    showToast("삭제할 항목을 선택해 주세요.");
                    return;
                }

                var send_array = Array();
                var send_cnt = 0;
                var chkbox = $(".cbItem");

                for(i=0;i<chkbox.length;i++) {
                    if (chkbox[i].checked == true){
                        send_array[send_cnt] = chkbox[i].value;
                        send_cnt++;
                    }
                }

                $("#delIDs").val(send_array);
                $('#delCount').text(send_array.length);
                showModal('deleteModal');
            }

            function deleteSelected() {
                if($('input.cbItem:checked').length==0){
                    showToast("삭제할 항목을 선택해 주세요.");
                    return;
                }


                $('#delIDs').val("");
                var count=0;
                $('input:checkbox').each(function() {
                    if(this.checked){
                        $('#delIDs').val($('#delIDs').val()+this.value+", ");//문자열 합치기
                        count++;//팝업에 띄울 갯수 카운트
                    }
                });
                $('#delIDs').val($('#delIDs').val("")+"-1");//초기화
                $('#delCount').text(count);

                showModal('deleteModal');

                //TODO 선택 삭제
            }
        </script>

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
            <tbody>
            <%
                for(int i=0; contentVOList!=null && i<contentVOList.size(); i++){
            %>
            <tr>
                <td><input name="contentId[]" value="<%=contentVOList.get(i).getContentId()%>" type="checkbox" class="cbItem"></td>
                <td><%=""+(((pageNo-1)*10)+(i+1))%></td>
                <td><%=contentVOList.get(i).getAccountName()%></td>
                <td><a href="${pageContext.request.contextPath}/content/myDetail?contentID=<%=contentVOList.get(i).getContentId()%>">
                    <div class="video-preview-frame">
                        <video preload="metadata" width="80" height="45">
                            <source src="https://s3.ap-northeast-2.amazonaws.com/vtt-ogq/movie_attach/<%=contentVOList.get(i).getContentId()%>_<%=contentVOList.get(i).getVideoFileUrl()%>#t=0.1" type="video/mp4" />
                        </video>
                    </div>
                </a></td>
                <td><a href="${pageContext.request.contextPath}/content/myDetail?contentID=<%=contentVOList.get(i).getContentId()%>"><%=contentVOList.get(i).getContentTitle()%></a></td>
                <td><%=contentVOList.get(i).getVideoRunningTime()%></td>
                <td><%=ETCLib.obtainFileSizeString(contentVOList.get(i).getVideoFileSize())%></td>
                <td><%=ETCLib.obtainDateString(contentVOList.get(i).getRegisteredDt())%></td>
                <td>
                    <button class="white-button" onclick="deleteAContent('<%=contentVOList.get(i).getContentId()%>')">삭제</button>
                </td>
                <!--
                "삭제알림팝업" 노출
                -->
            </tr>
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

        String thisURL=request.getContextPath()+"/content/myList";
        String qsKeyword="";
        String qsPage="";
        int totalCount = myListTotalCount;
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
                    location.href="${pageContext.request.contextPath}/content/myList?keyword="+keyword;
                }

            }
        </script>
    </div>


    <div class="dummy-div"></div>
</main>
</body>
</html>
