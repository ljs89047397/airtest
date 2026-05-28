<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>SAF 인증서 상세 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.detail-label { font-size: 0.80rem; color: #6c757d; font-weight: 500; }
.detail-value { font-size: 0.92rem; font-weight: 600; color: #212529; }
.audit-timeline { border-left: 2px solid #dee2e6; padding-left: 1rem; }
.audit-item { position: relative; margin-bottom: 1rem; }
.audit-item::before { content: ''; position: absolute; left: -1.4rem; top: 4px; width: 10px; height: 10px; border-radius: 50%; background: #0F2C72; }
.badge-srnd { background: #c62828; }
.badge-ok   { background: #2e7d32; }
.banner-srnd { background: #fff3f3; border: 1px solid #f5c6c6; border-radius: 8px; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <!-- 페이지 헤더 -->
  <div class="page-header-bar px-4 py-3">
    <div class="d-flex align-items-center justify-content-between">
      <div>
        <button class="btn btn-sm btn-outline-secondary me-2" onclick="history.back()">
          <i class="bi bi-arrow-left"></i> 목록으로
        </button>
        <h5 class="fw-bold mb-0 d-inline" style="color:#0F2C72;">SAF 인증서 상세</h5>
        <nav aria-label="breadcrumb" class="d-inline-block ms-2">
          <ol class="breadcrumb mb-0 small d-inline-flex">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/saf/dashboard" class="text-decoration-none">SAF</a></li>
            <li class="breadcrumb-item"><a href="/saf/cert" class="text-decoration-none">인증서 목록</a></li>
            <li class="breadcrumb-item active">상세</li>
          </ol>
        </nav>
      </div>
      <div id="actionArea"></div>
    </div>
  </div>

  <div class="container-fluid p-4" id="mainContent">
    <div class="text-center py-5 text-muted">
      <div class="spinner-border text-primary" role="status"></div>
      <div class="mt-2 small">데이터 로딩 중...</div>
    </div>
  </div>
</div>

<!-- 회수 확인 모달 (강화) -->
<div class="modal fade" id="surrenderModal" tabindex="-1" aria-labelledby="surrenderModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#c62828;">
        <h6 class="modal-title fw-bold text-white" id="surrenderModalLabel">
          <i class="bi bi-exclamation-triangle-fill me-1"></i> 인증서 회수(Surrender) 처리
        </h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="alert alert-danger border border-danger">
          <strong><i class="bi bi-shield-exclamation me-1"></i>비가역 경고</strong><br>
          이 작업은 <u>절대로 되돌릴 수 없습니다.</u><br>
          회수된 인증서는 외부 인증 시스템에서 동일 일련번호 재등록이 <strong>영구 차단</strong>되며,
          연결된 배치의 SAF 혼합비율 의무이행 산출에서 제외됩니다.
        </div>
        <div class="mb-3">
          <div class="small text-muted mb-1">회수 대상 인증서 번호</div>
          <div class="fw-bold text-danger fs-6" id="surrenderCertNo"></div>
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">회수 사유 <span class="text-danger">*</span></label>
          <textarea id="surrenderReason" class="form-control form-control-sm" rows="3"
                    placeholder="회수 사유를 반드시 입력하세요 (예: 인증기관 취소, 원료 부적합 판정 등)" maxlength="500"></textarea>
          <div class="form-text text-danger small" id="surrenderReasonErr" style="display:none;">
            회수 사유를 입력하세요.
          </div>
        </div>
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="surrenderConfirmChk">
          <label class="form-check-label small text-danger fw-semibold" for="surrenderConfirmChk">
            위 내용을 확인했으며, 비가역적 회수 처리에 동의합니다.
          </label>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-danger btn-sm" id="confirmSurrenderBtn">
          <i class="bi bi-x-circle-fill me-1"></i> 회수 처리 확정
        </button>
      </div>
    </div>
  </div>
</div>

<!-- 인증서 정보 수정 모달 -->
<div class="modal fade" id="certEditModal" tabindex="-1" aria-labelledby="certEditModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;">
        <h6 class="modal-title fw-bold text-white" id="certEditModalLabel">
          <i class="bi bi-pencil-square me-1"></i> 인증서 정보 수정
        </h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <form id="certEditForm" novalidate>
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label small fw-semibold">인증서 유형 <span class="text-danger">*</span></label>
              <select name="certTypeCd" id="edit_certTypeCd" class="form-select form-select-sm" required>
                <option value="PoS">PoS (Proof of Sustainability)</option>
                <option value="PoC">PoC (Proof of Care)</option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">인증 체계 <span class="text-danger">*</span></label>
              <select name="certSchmCd" id="edit_certSchmCd" class="form-select form-select-sm" required>
                <option value="ISCC">ISCC</option>
                <option value="RSB">RSB</option>
                <option value="ISCC_PLUS">ISCC+</option>
                <option value="CORSIA">CORSIA</option>
                <option value="RSPO">RSPO</option>
              </select>
            </div>
            <div class="col-md-8">
              <label class="form-label small fw-semibold">인증서 번호 (외부 발급) <span class="text-danger">*</span></label>
              <input type="text" id="edit_certNo" class="form-control form-control-sm" required maxlength="100">
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">연결 배치 ID</label>
              <input type="text" id="edit_batchId" class="form-control form-control-sm" maxlength="100">
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">적용 시작일</label>
              <input type="date" id="edit_useBgngDt" class="form-control form-control-sm">
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">적용 종료일</label>
              <input type="date" id="edit_useEndDt" class="form-control form-control-sm">
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary btn-sm" id="certEditSubmitBtn">
          <i class="bi bi-check-lg me-1"></i> 저장
        </button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>
var CERT_ID = location.pathname.split('/').pop();
var certData = {};

function escHtml(s) {
  return $('<div>').text(s == null ? '-' : String(s)).html();
}

function srndBadge(yn) {
  return yn === 'Y'
    ? '<span class="badge badge-srnd fs-6"><i class="bi bi-x-circle-fill me-1"></i>회수됨</span>'
    : '<span class="badge badge-ok fs-6"><i class="bi bi-check-circle-fill me-1"></i>유효</span>';
}

function renderDetail(d) {
  certData = d;
  var srndBanner = d.srndYn === 'Y'
    ? '<div class="banner-srnd p-3 mb-4 d-flex align-items-center gap-2"><i class="bi bi-x-octagon-fill text-danger fs-5"></i><div><strong class="text-danger">이 인증서는 회수(Surrender) 처리되었습니다.</strong><br><span class="small text-muted">회수일: ' + escHtml(d.srndDt) + '</span></div></div>'
    : '';

  var html = srndBanner
    + '<div class="row g-3">'
    + '<div class="col-lg-8">'
    +   '<div class="card border-0 shadow-sm mb-3">'
    +     '<div class="card-header bg-white border-bottom py-2"><h6 class="fw-bold mb-0" style="color:#0F2C72;">기본 정보</h6></div>'
    +     '<div class="card-body">'
    +       '<div class="row g-3">'
    +         '<div class="col-md-6"><div class="detail-label">인증서 번호 (SC No)</div><div class="detail-value">' + escHtml(d.certNo) + '</div></div>'
    +         '<div class="col-md-3"><div class="detail-label">유형</div><div class="detail-value">' + escHtml(d.certTypeCd) + '</div></div>'
    +         '<div class="col-md-3"><div class="detail-label">인증 체계</div><div class="detail-value">' + escHtml(d.certSchmCd) + '</div></div>'
    +         '<div class="col-md-6"><div class="detail-label">배치 ID</div><div class="detail-value"><a href="/saf/batch/' + encodeURIComponent(d.batchId || '') + '">' + escHtml(d.batchId) + '</a></div></div>'
    +         '<div class="col-md-3"><div class="detail-label">회수 여부</div><div class="detail-value">' + srndBadge(d.srndYn) + '</div></div>'
    +         '<div class="col-md-3"><div class="detail-label">등록일시</div><div class="detail-value small">' + escHtml(d.frstRegDt ? d.frstRegDt.substring(0,16) : null) + '</div></div>'
    +         '<div class="col-md-6"><div class="detail-label">등록자</div><div class="detail-value small">' + escHtml(d.frstRegUserId) + '</div></div>'
    +       '</div>'
    +     '</div>'
    +   '</div>'
    + '</div>'
    + '<div class="col-lg-4">'
    +   '<div class="card border-0 shadow-sm">'
    +     '<div class="card-header bg-white border-bottom py-2"><h6 class="fw-bold mb-0" style="color:#0F2C72;">감사 이력</h6></div>'
    +     '<div class="card-body" id="auditArea"><div class="text-center text-muted small py-3">불러오는 중...</div></div>'
    +   '</div>'
    + '</div>'
    + '</div>';

  $('#mainContent').html(html);

  // 액션 버튼 (미회수 상태에서만 활성)
  if (d.srndYn !== 'Y') {
    $('#actionArea').html(
      '<button type="button" class="btn btn-outline-primary btn-sm me-2" id="editBtn">'
      + '<i class="bi bi-pencil-square me-1"></i> 정보 수정'
      + '</button>'
      + '<button type="button" class="btn btn-danger btn-sm" id="surrenderBtn">'
      + '<i class="bi bi-x-circle me-1"></i> 회수(Surrender) 처리'
      + '</button>'
    );

    /* 회수 모달 */
    $('#surrenderBtn').on('click', function() {
      $('#surrenderCertNo').text(d.certNo);
      $('#surrenderReason').val('');
      $('#surrenderConfirmChk').prop('checked', false);
      $('#surrenderReasonErr').hide();
      new bootstrap.Modal('#surrenderModal').show();
    });

    /* 수정 모달 */
    $('#editBtn').on('click', function() {
      $('#edit_certTypeCd').val(d.certTypeCd || '');
      $('#edit_certSchmCd').val(d.certSchmCd || '');
      $('#edit_certNo').val(d.certNo || '');
      $('#edit_batchId').val(d.batchId || '');
      $('#edit_useBgngDt').val(d.useBgngDt ? String(d.useBgngDt).substring(0,10) : '');
      $('#edit_useEndDt').val(d.useEndDt ? String(d.useEndDt).substring(0,10) : '');
      $('#certEditForm').removeClass('was-validated');
      new bootstrap.Modal('#certEditModal').show();
    });
  }

  loadAudit();
}

function loadAudit() {
  $.get('/api/saf/cert/' + encodeURIComponent(CERT_ID) + '/audit')
    .done(function(res) {
      var list = res.data || res;
      if (!list || list.length === 0) {
        $('#auditArea').html('<div class="text-muted small text-center py-2">감사 이력 없음</div>');
        return;
      }
      var html = '<div class="audit-timeline">';
      $.each(list, function(i, a) {
        var actnLabel = { UPLD: '등록', EXTR: '추출', SRND: '회수' }[a.actnCd] || escHtml(a.actnCd);
        var actnColor = a.actnCd === 'SRND' ? 'text-danger' : (a.actnCd === 'UPLD' ? 'text-success' : 'text-primary');
        html += '<div class="audit-item">'
          + '<div class="small ' + actnColor + ' fw-semibold">' + actnLabel + '</div>'
          + '<div class="small text-muted">' + escHtml(a.userId) + '</div>'
          + '<div class="small text-muted">' + escHtml(a.dt ? a.dt.substring(0,16) : null) + '</div>'
          + '</div>';
      });
      html += '</div>';
      $('#auditArea').html(html);
    })
    .fail(function(xhr) {
      $('#auditArea').html('<div class="text-danger small">감사이력 로드 오류 (HTTP ' + xhr.status + ')</div>');
    });
}

$(function() {
  // 인증서 데이터 로드
  $.get('/api/saf/cert/' + encodeURIComponent(CERT_ID))
    .done(function(res) { renderDetail(res.data || res); })
    .fail(function(xhr) {
      $('#mainContent').html('<div class="alert alert-danger m-4">인증서 정보를 불러오지 못했습니다. (HTTP ' + xhr.status + ')</div>');
    });

  /* 회수 처리 확인 (강화: 사유 + 체크박스 검증) */
  $(document).on('click', '#confirmSurrenderBtn', function() {
    var reason = $('#surrenderReason').val().trim();
    if (!reason) {
      $('#surrenderReasonErr').show();
      $('#surrenderReason').focus();
      return;
    }
    $('#surrenderReasonErr').hide();
    if (!$('#surrenderConfirmChk').is(':checked')) {
      IcasAlert.error('비가역 처리 동의 체크박스를 선택하세요.');
      return;
    }
    var $btn = $(this);
    $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span>처리 중...');
    $.post('/api/saf/cert/' + encodeURIComponent(CERT_ID) + '/surrender')
      .done(function(res) {
        bootstrap.Modal.getInstance('#surrenderModal').hide();
        IcasAlert.success(res.message || '인증서가 회수(Surrender) 처리되었습니다.');
        location.reload();
      })
      .fail(function(xhr) {
        var msg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : 'HTTP ' + xhr.status;
        IcasAlert.error('회수 처리 중 오류가 발생했습니다: ' + msg);
        $btn.prop('disabled', false).html('<i class="bi bi-x-circle-fill me-1"></i> 회수 처리 확정');
      });
  });

  /* 수정 저장 */
  $(document).on('click', '#certEditSubmitBtn', function() {
    var $form = $('#certEditForm');
    $form.addClass('was-validated');
    if (!$form[0].checkValidity()) return;

    var payload = {
      certTypeCd : $('#edit_certTypeCd').val(),
      certSchmCd : $('#edit_certSchmCd').val(),
      certNo     : $('#edit_certNo').val().trim(),
      batchId    : $('#edit_batchId').val().trim() || null,
      useBgngDt  : $('#edit_useBgngDt').val() || null,
      useEndDt   : $('#edit_useEndDt').val() || null
    };

    var $btn = $(this);
    $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span>저장 중...');

    $.ajax({
      url         : '/api/saf/cert/' + encodeURIComponent(CERT_ID),
      type        : 'PUT',
      contentType : 'application/json',
      data        : JSON.stringify(payload)
    })
    .done(function(res) {
      bootstrap.Modal.getInstance('#certEditModal').hide();
      IcasAlert.success('인증서 정보가 수정되었습니다.');
      location.reload();
    })
    .fail(function(xhr) {
      var msg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : 'HTTP ' + xhr.status;
      IcasAlert.error('수정 실패: ' + msg);
    })
    .always(function() {
      $btn.prop('disabled', false).html('<i class="bi bi-check-lg me-1"></i> 저장');
    });
  });
});
</script>
</body>
</html>
