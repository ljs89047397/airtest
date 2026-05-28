<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<sec:csrfMetaTags/>
<title>로그인 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: linear-gradient(135deg, #0F2C72 0%, #1a4a9a 100%); min-height: 100vh; display:flex; align-items:center; justify-content:center; }
.login-card { width: 440px; border-radius: 16px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); }
.btn-icas { background: #0F2C72; color: white; }
.btn-icas:hover { background: #0a1f54; color: white; }
</style>
</head>
<body>
<div class="card login-card">
  <div class="card-body p-5">
    <div class="text-center mb-4">
      <div style="font-size:2.5rem;">&#9992;</div>
      <h5 class="fw-bold mt-2" style="color:#0F2C72;">국제항공 탄소배출량 관리시스템</h5>
      <small class="text-muted">ICAS-CEMS</small>
    </div>

    <div id="errMsg" class="alert alert-danger py-2 small d-none" role="alert">
      <i class="bi bi-exclamation-triangle me-1"></i><span id="errTxt">로그인에 실패했습니다.</span>
    </div>
    <% if (request.getParameter("logout") != null) { %>
    <div class="alert alert-info py-2 small" role="alert">
      <i class="bi bi-check-circle me-1"></i>정상적으로 로그아웃되었습니다.
    </div>
    <% } %>

    <form id="loginForm">
      <div class="mb-3">
        <label class="form-label fw-semibold">아이디</label>
        <div class="input-group">
          <span class="input-group-text"><i class="bi bi-person"></i></span>
          <input type="text" id="userId" name="userId" class="form-control" value="admin" autocomplete="username">
        </div>
      </div>
      <div class="mb-4">
        <label class="form-label fw-semibold">비밀번호</label>
        <div class="input-group">
          <span class="input-group-text"><i class="bi bi-lock"></i></span>
          <input type="password" id="password" name="password" class="form-control" value="gnsoft12345!" autocomplete="current-password">
        </div>
      </div>
      <button type="submit" id="loginBtn" class="btn btn-icas w-100 fw-bold py-2">
        <i class="bi bi-box-arrow-in-right me-2"></i>로그인
      </button>
    </form>

    <hr class="my-4">
    <div class="text-center text-muted small">
      <span class="me-3">&#127963; 국토교통부</span>
      <span>&#128663; 한국교통안전공단</span>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="/resources/js/common/icas-csrf.js"></script>
<script>
$(function(){
  $('#loginForm').on('submit', function(e){
    e.preventDefault();
    $('#loginBtn').prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-2"></span>로그인 중...');
    $('#errMsg').addClass('d-none');

    var csrfToken  = $('meta[name="_csrf"]').attr('content');
    var csrfHeader = $('meta[name="_csrf_header"]').attr('content') || 'X-XSRF-TOKEN';
    var headers = {};
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

    $.ajax({
      url: '/api/com/auth/login',
      type: 'POST',
      headers: headers,
      data: { userId: $('#userId').val(), password: $('#password').val() },
      success: function(res){
        // JSON 응답이면 성공 — /main 으로 이동
        window.location.href = '/main';
      },
      error: function(xhr){
        var msg;
        if (xhr.status === 403) {
          msg = '보안 토큰이 만료되었습니다. 페이지를 새로고침 후 다시 시도해 주세요.';
        } else if (xhr.status === 0) {
          msg = '서버에 연결할 수 없습니다.';
        } else {
          msg = '아이디 또는 비밀번호가 올바르지 않습니다.';
        }
        try {
          var body = JSON.parse(xhr.responseText);
          if (body && body.message) msg = body.message;
        } catch(ex){}
        $('#errTxt').text(msg + ' (HTTP ' + xhr.status + ')');
        $('#errMsg').removeClass('d-none');
        $('#loginBtn').prop('disabled', false).html('<i class="bi bi-box-arrow-in-right me-2"></i>로그인');
      }
    });
  });
});
</script>
</body>
</html>
