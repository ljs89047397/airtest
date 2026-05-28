<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>비밀번호 변경 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>body { background:#f5f7fb; }
.pwd-card { max-width:540px; margin:32px auto; }
.policy-list { background:#f8fafc; border-radius:6px; padding:12px 16px; margin-bottom:16px; font-size:0.82rem; color:#475569; }
.policy-list li { margin-bottom:4px; }
.policy-list li.ok    { color:#10B981; }
.policy-list li.bad   { color:#9ca3af; }
.policy-list li i { margin-right:6px; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="p-4">
    <div class="card pwd-card border-0 shadow-sm">
      <div class="card-header bg-white py-3">
        <h6 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-shield-lock me-2"></i>비밀번호 변경</h6>
      </div>
      <div class="card-body p-4">
        <div class="policy-list">
          <strong style="color:#0F2C72;">비밀번호 정책</strong>
          <ul class="mb-0 ps-3 mt-2" id="policyList">
            <li id="pol-len"     class="bad"><i class="bi bi-circle"></i>8자 이상</li>
            <li id="pol-alpha"   class="bad"><i class="bi bi-circle"></i>영문 포함</li>
            <li id="pol-digit"   class="bad"><i class="bi bi-circle"></i>숫자 포함</li>
            <li id="pol-special" class="bad"><i class="bi bi-circle"></i>특수문자 포함 (!@#$%^&amp;* 등)</li>
            <li id="pol-match"   class="bad"><i class="bi bi-circle"></i>새 비밀번호 일치</li>
          </ul>
        </div>

        <form id="pwdForm" autocomplete="off">
          <div class="mb-3">
            <label class="form-label small fw-semibold">현재 비밀번호 <span class="text-danger">*</span></label>
            <input type="password" id="oldPwd" class="form-control" required>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">새 비밀번호 <span class="text-danger">*</span></label>
            <input type="password" id="newPwd" class="form-control" required>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">새 비밀번호 확인 <span class="text-danger">*</span></label>
            <input type="password" id="newPwd2" class="form-control" required>
          </div>
          <div id="pwdMsg" class="small mb-3"></div>
          <div class="d-flex gap-2">
            <button type="submit" id="btnSubmit" class="btn btn-primary" disabled><i class="bi bi-check-circle me-1"></i>변경</button>
            <a href="/main" class="btn btn-outline-secondary">취소</a>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>
function checkPolicy() {
  var p1 = $('#newPwd').val() || '';
  var p2 = $('#newPwd2').val() || '';
  var rules = {
    'pol-len':     p1.length >= 8,
    'pol-alpha':   /[A-Za-z]/.test(p1),
    'pol-digit':   /\d/.test(p1),
    'pol-special': /[^A-Za-z0-9]/.test(p1),
    'pol-match':   p1.length > 0 && p1 === p2
  };
  var allOk = true;
  for (var id in rules) {
    var ok = rules[id];
    var $li = $('#' + id);
    $li.removeClass('ok bad').addClass(ok ? 'ok' : 'bad');
    $li.find('i').attr('class', ok ? 'bi bi-check-circle-fill' : 'bi bi-circle');
    if (!ok) allOk = false;
  }
  $('#btnSubmit').prop('disabled', !allOk);
  return allOk;
}
$('#newPwd, #newPwd2').on('input', checkPolicy);

$('#pwdForm').on('submit', function(e){
  e.preventDefault();
  if (!checkPolicy()) return;

  // 현재 사용자ID 추출 (헤더 'admin' 텍스트 또는 세션 쿠키에서) — me API 호출
  $.get('/api/com/user/me').done(function(res){
    var uid = (res && res.data && res.data.userId) ? res.data.userId : null;
    if (!uid) { $('#pwdMsg').html('<span class="text-danger">사용자 정보를 가져올 수 없습니다.</span>'); return; }
    $.ajax({
      url: '/api/com/user/' + encodeURIComponent(uid) + '/password',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({ oldPassword: $('#oldPwd').val(), newPassword: $('#newPwd').val() })
    }).done(function(){
      $('#pwdMsg').html('<span class="text-success"><i class="bi bi-check-circle me-1"></i>비밀번호가 변경되었습니다. 5초 후 로그아웃됩니다.</span>');
      setTimeout(function(){
        var csrf = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
        var token = csrf ? decodeURIComponent(csrf[1]) : '';
        fetch('/api/com/auth/logout',{method:'POST',headers:token?{'X-XSRF-TOKEN':token}:{}}).finally(function(){ location.href='/login'; });
      }, 5000);
    }).fail(function(xhr){
      var msg = '변경 실패';
      try { msg = JSON.parse(xhr.responseText).message || msg; } catch(e) {}
      $('#pwdMsg').html('<span class="text-danger"><i class="bi bi-exclamation-triangle me-1"></i>' + msg + '</span>');
    });
  }).fail(function(){ $('#pwdMsg').html('<span class="text-danger">로그인이 만료되었습니다.</span>'); });
});
</script>
</body>
</html>
