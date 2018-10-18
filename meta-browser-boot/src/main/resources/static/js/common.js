/* 모달 박스 초기화*/
//현재 활성화 중인 모달 DIV 객체.
var currentModal;

$(function () {
// Get the modal
    currentModal = document.getElementById('noneModal');

// Get the button that opens the modal
//  var btn = document.getElementById("myBtn");

// Get the <span> element that closes the modal
    var span = document.getElementsByClassName("close-div")[0];

// When the user clicks the button, open the modal
//  btn.onclick = function() {
//    currentModal.style.display = "block";
//  }

// When the user clicks on <span> (x), close the modal
//  span.onclick = function() {
//    currentModal.style.display = "none";
//  }
//  span.onclick = function() {
//    currentModal.style.display = "none";
//  }

// When the user clicks anywhere outside of the modal, close it
    window.onclick = function (event) {
        if (event.target == currentModal) {
            hideModal();
        }
    }

    $(".close-div").click(function () {
        hideModal();
    });


});


function showGeneralModal(modalTitle, modalContent) {
    var modalID = "generalModal";
    hideModal();
    currentModal = document.getElementById(modalID);
    if (currentModal == null || currentModal.style == null) return;
    currentModal.style.display = "block";
    $("#generalModal .p-h3").text(modalTitle);
    $("#generalModal .p-content").text(modalContent);
    //모션 스타일은 CSS에서.
}


function showModal(modalID) {

    hideModal();

    console.log(modalID);


    currentModal = document.getElementById(modalID);
    console.log(document.getElementById(modalID));
    if (currentModal == null || currentModal.style == null) return;
    currentModal.style.display = "block";

    //모션 스타일은 CSS에서.
}

function hideModal() {
    if (currentModal == null || currentModal.style == null) return;

    currentModal.style.display = "none";
    //모션 스타일은 CSS에서.

    if (window.hiderCallback != null) {
        hiderCallback(currentModal);
    }
}


/* 프로그래스 바 */
function move() {
    var elem = document.getElementById("myBar");
    var width = 1;
    var id = setInterval(frame, 10);

    function frame() {
        if (width >= 100) {
            clearInterval(id);
        } else {
            width++;
            elem.style.width = width + '%';
        }
    }
}


/* 문자열 제한 넘길 경우 체크 스크립트 */
function cutStringByLength(inputString, limitLength) {
    var len = 0;
    var newStr = '';

    for (var i = 0; i < inputString.length; i++) {
        var n = inputString.charCodeAt(i); // charCodeAt : String개체에서 지정한 인덱스에 있는 문자의 unicode값을 나타내는 수를 리턴한다.
        // 값의 범위는 0과 65535사이이여 첫 128 unicode값은 ascii문자set과 일치한다.지정한 인덱스에 문자가 없다면 NaN을 리턴한다.
        var nv = inputString.charAt(i); // charAt : string 개체로부터 지정한 위치에 있는 문자를 꺼낸다.
        if ((n >= 0) && (n < 256)) len++; // ASCII 문자코드 set.
        else len += 2; // 한글이면 2byte로 계산한다.

        if (len > limitLength) break; // 제한 문자수를 넘길경우.
        else newStr = newStr + nv;
    }
    return newStr;
}

/* 문자열 제한 넘길 경우 처리 스크립트 */
function valChecker(valObject, lengthLimit, notifier) {
    //valObject.val(cutStringByLength(valObject.val(), lengthLimit));//넘으면 잘라서 보여준다.
    notifier.text(valObject.val().length + " / " + lengthLimit);//길이를 표시하는 부분에 업데이트.
}

/* 문자열 제한 넘길 경우 처리 스크립트 */
function textChecker(valObject, lengthLimit, notifier) {
    console.log('111' + valObject.text() + "---" + lengthLimit + "===" + notifier.text());
    //valObject.text(cutStringByLength(valObject.text(), lengthLimit));//넘으면 잘라서 보여준다.
    notifier.text(("" + valObject.text()).length + " / " + lengthLimit);//길이를 표시하는 부분에 업데이트.
}



function updateVideoDuration($videoTableBodyTr){

    var $videoView = $('.videoView');
    for(var i = 0; i < $videoTableBodyTr.length; i++){

        if($($videoTableBodyTr[i]).find('.runningTime').prop('text') == null){
            (function(selector){

                $(selector).on('durationchange',function(){
                    var durationFormat = new Date();
                    durationFormat.setHours(0, 0, 0, 0);
                    durationFormat.setSeconds(this.duration);


                    var duration = durationFormat.getHours() + ":" + durationFormat.getMinutes() + ":" + durationFormat.getSeconds();//포맷변환
                    var $tr = $(this).closest('tr');
                    $tr.find('.runningTime').text(duration);

                    putVideoDuration( $tr.data('id'), duration)

                });
            })($videoView.get(i));
        }
    }
}


function putVideoDuration(id,duration){
    $.ajax({
        url:'/content/videos/'+id+'/duration/'+duration,
        type:'PUT',

        beforeSend: function (x) {
        },
        success: function (data) {

        },
        error: function(resp){
            console.log(resp);
        }
    })
}









