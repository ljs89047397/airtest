<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>EMP 목록 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root {
  --icas-primary: #0F2C72;
  --icas-primary-hover: #0A2058;
  --icas-accent: #0D6EFD;
  --icas-success: #198754;
  --icas-warning: #FFC107;
  --icas-danger: #DC3545;
  --icas-text: #495057;
  --icas-text-muted: #6C757D;
  --icas-border: #DEE2E6;
  --icas-bg: #F8F9FA;
}
body { background: #f0f2f5; font-family: 'Pretendard', '맑은 고딕', sans-serif; }
.page-header-bar { background: white; border-bottom: 1px solid var(--icas-border); }
.table-icas thead th {
  background: var(--icas-primary);
  color: white;
  font-size: 0.82rem;
  font-weight: 500;
  border: none;
  white-space: nowrap;
}
.table-icas tbody tr:hover { background: #f8f9ff; cursor: pointer; }
.table-icas tbody td { font-size: 0.84rem; vertical-align: middle; }
.status-badge { font-size: 0.72rem; padding: 3px 8px; border-radius: 4px; font-weight: 600; }
.search-card { border: none; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
.list-card { border: none; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
.btn-icas-primary { background: var(--icas-primary); color: white; border: none; }
.btn-icas-primary:hover { background: var(--icas-primary-hover); color: white; }
.grid-toolbar { padding: 0.6rem 1rem; border-bottom: 1px solid var(--icas-border); background: #fafbfc; display: flex; align-items: center; }
.total-count { font-size: 0.84rem; color: var(--icas-text-muted); }
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
        <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">
          <i class="bi bi-file-earmark-text me-2"></i>EMP 목록
        </h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/emp/plan" class="text-decoration-none">EMP</a></li>
            <li class="breadcrumb-item active">계획서 목록</li>
          </ol>
        </nav>
      </div>
      <div>
        <button type="button" id="btnNewEmp" class="btn btn-icas-primary btn-sm">
          <i class="bi bi-plus-lg me-1"></i>신규 작성
        </button>
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">

    <!-- 검색 필터 -->
    <div class="card search-card mb-3">
      <div class="card-body py-3">
        <form id="searchForm" autocomplete="off">
          <div class="row g-2 align-items-end">
            <div class="col-auto">
              <label for="filterRprtYr" class="form-label small fw-semibold mb-1">보고연도</label>
              <select id="filterRprtYr" name="rprtYr" class="form-select form-select-sm" style="width:110px;">
                <option value="">전체</option>
                <option value="2026" selected>2026</option>
                <option value="2025">2025</option>
                <option value="2024">2024</option>
                <option value="2023">2023</option>
              </select>
            </div>
            <div class="col-auto">
              <label for="filterOprtr" class="form-label small fw-semibold mb-1">운영사</label>
              <input type="text" id="filterOprtr" name="oprtrNm" class="form-control form-control-sm"
                     placeholder="운영사명 또는 ICAO" style="width:200px;" maxlength="100">
            </div>
            <div class="col-auto">
              <label for="filterStatus" class="form-label small fw-semibold mb-1">상태</label>
              <select id="filterStatus" name="empStCd" class="form-select form-select-sm" style="width:130px;">
                <option value="">전체</option>
                <option value="DRAFT">작성중</option>
                <option value="SBMTD">제출됨</option>
                <option value="RVWNG">검토중</option>
                <option value="RCMDD">권고</option>
                <option value="APRVD">승인</option>
                <option value="RJCTD">반려</option>
                <option value="CNCLD">취소</option>
              </select>
            </div>
            <div class="col-auto">
              <button type="submit" id="btnSearch" class="btn btn-sm btn-icas-primary">
                <i class="bi bi-search me-1"></i>조회
              </button>
              <button type="button" id="btnReset" class="btn btn-sm btn-outline-secondary ms-1">
                <i class="bi bi-arrow-counterclockwise me-1"></i>초기화
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>

    <!-- 목록 그리드 -->
    <div class="card list-card">
      <div class="grid-toolbar">
        <span class="total-count">총 <strong id="empPlanTotal">0</strong>건</span>
        <div class="ms-auto">
          <button type="button" id="btnExcel" class="btn btn-sm btn-outline-success">
            <i class="bi bi-file-earmark-excel me-1"></i>엑셀 다운로드
          </button>
        </div>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-sm mb-0 table-icas" id="empPlanGrid" aria-label="EMP 목록">
            <thead>
              <tr>
                <th class="ps-3" style="width:50px;">No</th>
                <th style="width:20%;">운영사명</th>
                <th style="width:90px;">ICAO</th>
                <th style="width:90px;">보고연도</th>
                <th style="width:70px; text-align:center;">버전</th>
                <th style="width:100px;">상태</th>
                <th style="width:130px;">제출일시</th>
                <th style="width:130px;">승인일시</th>
                <th style="width:120px;">등록자</th>
                <th style="width:80px; text-align:center;">액션</th>
              </tr>
            </thead>
            <tbody id="empListBody">
              <tr>
                <td colspan="10" class="text-center py-5 text-muted">
                  <div class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></div>
                  <span>데이터를 불러오는 중입니다...</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <!-- 페이징 -->
      <div class="card-footer bg-white border-top-0 py-2">
        <nav aria-label="EMP 목록 페이징">
          <ul class="pagination pagination-sm mb-0 justify-content-center" id="empPagination"></ul>
        </nav>
      </div>
    </div>

  </div><!-- /container-fluid -->
</div><!-- /margin-left -->

<!-- 신규 작성 모달 -->
<div class="modal fade" id="modalNewEmp" tabindex="-1" aria-labelledby="modalNewEmpLabel" aria-hidden="true">
  <div class="modal-dialog modal-md">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h5 class="modal-title" id="modalNewEmpLabel">
          <i class="bi bi-file-earmark-plus me-2"></i>EMP 신규 작성
        </h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label for="newEmpRprtYr" class="form-label fw-semibold">보고연도 <span class="text-danger" aria-hidden="true">*</span></label>
          <select id="newEmpRprtYr" name="rprtYr" class="form-select" aria-required="true">
            <option value="">선택하세요</option>
            <option value="2027">2027</option>
            <option value="2026" selected>2026</option>
            <option value="2025">2025</option>
            <option value="2024">2024</option>
          </select>
          <div class="invalid-feedback" id="newEmpRprtYr-error" role="alert"></div>
        </div>
        <div class="mb-3">
          <label for="newEmpOprtrId" class="form-label fw-semibold">운영사 <span class="text-danger" aria-hidden="true">*</span></label>
          <select id="newEmpOprtrId" name="oprtrId" class="form-select" aria-required="true">
            <option value="">선택하세요</option>
            <!-- 운영사 목록은 AJAX 로드 -->
          </select>
          <div class="invalid-feedback" id="newEmpOprtrId-error" role="alert"></div>
        </div>
        <div class="alert alert-info small mb-0">
          <i class="bi bi-info-circle me-1"></i>
          EMP 가 이미 존재하는 운영사·보고연도인 경우, 기존 DRAFT 또는 신버전 생성이 제한될 수 있습니다.
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnCreateEmp" class="btn btn-icas-primary">
          <i class="bi bi-check-lg me-1"></i>작성 시작
        </button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
(function () {
  'use strict';

  // ─────────────────────────────────────────────
  // 상수
  // ─────────────────────────────────────────────
  var PAGE_SIZE = 20;
  var currentPage = 1;
  var totalCount = 0;

  var STATUS_MAP = {
    'DRAFT':  { cls: 'bg-secondary',           label: '작성중' },
    'SBMTD':  { cls: 'bg-primary',             label: '제출됨' },
    'RVWNG':  { cls: 'bg-warning text-dark',   label: '검토중' },
    'RCMDD':  { cls: 'bg-info text-dark',      label: '권고'   },
    'APRVD':  { cls: 'bg-success',             label: '승인'   },
    'RJCTD':  { cls: 'bg-danger',              label: '반려'   },
    'CNCLD':  { cls: 'bg-dark',                label: '취소'   }
  };

  // ─────────────────────────────────────────────
  // 유틸
  // ─────────────────────────────────────────────
  function escHtml(str) {
    if (str == null) return '-';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function renderStatusBadge(cd) {
    if (!cd) return '<span class="badge status-badge bg-light text-muted border">-</span>';
    var map = STATUS_MAP[cd];
    if (!map) return '<span class="badge status-badge bg-light text-muted border">' + escHtml(cd) + '</span>';
    return '<span class="badge status-badge ' + map.cls + '">' + map.label + '</span>';
  }

  function formatDateTime(val) {
    if (!val) return '-';
    return String(val).replace('T', ' ').substring(0, 16);
  }

  // ─────────────────────────────────────────────
  // 그리드 렌더
  // ─────────────────────────────────────────────
  function renderTable(list, total, page) {
    totalCount = total || 0;
    $('#empPlanTotal').text(totalCount);
    if (!list || !list.length) {
      $('#empListBody').html(
        '<tr><td colspan="10" class="text-center py-5 text-muted small">' +
        '<i class="bi bi-inbox fs-3 d-block mb-2"></i>' +
        '조건에 해당하는 EMP 계획서가 없습니다.</td></tr>'
      );
      $('#empPagination').empty();
      return;
    }
    var offset = (page - 1) * PAGE_SIZE;
    var html = '';
    $.each(list, function (idx, row) {
      html += '<tr data-id="' + escHtml(row.empPlanId) + '" style="cursor:pointer;">'
        + '<td class="ps-3 text-muted small">' + (offset + idx + 1) + '</td>'
        + '<td class="fw-semibold small">' + escHtml(row.oprtrNm) + '</td>'
        + '<td class="small text-muted">' + escHtml(row.icaoDesig) + '</td>'
        + '<td class="small">' + escHtml(row.rprtYr) + '</td>'
        + '<td class="small text-center">v' + escHtml(row.empVer) + '</td>'
        + '<td>' + renderStatusBadge(row.empStCd) + '</td>'
        + '<td class="small text-muted">' + formatDateTime(row.sbmtDt) + '</td>'
        + '<td class="small text-muted">' + formatDateTime(row.aprvDt) + '</td>'
        + '<td class="small text-muted">' + escHtml(row.frstRegUserId) + '</td>'
        + '<td class="text-center">'
          + '<a href="/emp/plan/' + escHtml(row.empPlanId) + '" '
          + 'class="btn btn-outline-primary btn-xs" '
          + 'style="font-size:0.72rem;padding:2px 8px;" '
          + 'onclick="event.stopPropagation();">상세</a>'
        + '</td>'
        + '</tr>';
    });
    $('#empListBody').html(html);
    renderPagination(totalCount, page);
  }

  function renderPagination(total, page) {
    var totalPages = Math.ceil(total / PAGE_SIZE) || 1;
    var html = '';
    var start = Math.max(1, page - 2);
    var end = Math.min(totalPages, page + 2);
    if (page > 1) {
      html += '<li class="page-item"><a class="page-link" href="#" data-p="' + (page - 1) + '">&laquo;</a></li>';
    }
    for (var p = start; p <= end; p++) {
      html += '<li class="page-item' + (p === page ? ' active' : '') + '">'
        + '<a class="page-link" href="#" data-p="' + p + '">' + p + '</a></li>';
    }
    if (page < totalPages) {
      html += '<li class="page-item"><a class="page-link" href="#" data-p="' + (page + 1) + '">&raquo;</a></li>';
    }
    $('#empPagination').html(html);
  }

  // ─────────────────────────────────────────────
  // 데이터 로드
  // ─────────────────────────────────────────────
  function buildParams(page) {
    return {
      rprtYr:   $('#filterRprtYr').val(),
      oprtrNm:  $('#filterOprtr').val(),
      empStCd:  $('#filterStatus').val(),
      page:     (page || 1) - 1,
      size:     PAGE_SIZE
    };
  }

  function loadData(page) {
    currentPage = page || 1;
    var params = buildParams(currentPage);
    $('#empListBody').html(
      '<tr><td colspan="10" class="text-center py-5 text-muted">' +
      '<div class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></div>' +
      '데이터를 불러오는 중입니다...</td></tr>'
    );
    $.get('/api/emp/plan', params)
      .done(function (res) {
        var data = res.data || res;
        var list  = data.rows || data.content || data.list || (Array.isArray(data) ? data : []);
        var total = data.total || data.totalElements || data.totalCount || list.length;
        renderTable(list, total, currentPage);
      })
      .fail(function (xhr) {
        var msg = (xhr.responseJSON && xhr.responseJSON.message) || '목록을 불러오지 못했습니다.';
        $('#empListBody').html(
          '<tr><td colspan="10" class="text-center py-5 text-danger small">' +
          '<i class="bi bi-exclamation-triangle me-1"></i>' + escHtml(msg) + '</td></tr>'
        );
      });
  }

  // ─────────────────────────────────────────────
  // 운영사 목록 로드 (신규 작성 모달용)
  // ─────────────────────────────────────────────
  function loadOprtrOptions() {
    $.get('/api/com/oprtr?size=200')
      .done(function (res) {
        var list = (res.data && res.data.content) || res.data || res || [];
        var opts = '<option value="">선택하세요</option>';
        $.each(list, function (i, o) {
          opts += '<option value="' + escHtml(o.oprtrId) + '">' + escHtml(o.oprtrNm) + '</option>';
        });
        $('#newEmpOprtrId').html(opts);
      })
      .fail(function () { /* 운영사 목록 로드 실패 시 무시 */ });
  }

  // ─────────────────────────────────────────────
  // 이벤트
  // ─────────────────────────────────────────────
  $(function () {
    loadData(1);

    // 검색 폼 submit
    $('#searchForm').on('submit', function (e) {
      e.preventDefault();
      loadData(1);
    });

    // 초기화
    $('#btnReset').on('click', function () {
      $('#searchForm')[0].reset();
      loadData(1);
    });

    // 행 클릭 → 상세
    $('#empListBody').on('click', 'tr[data-id]', function () {
      var id = $(this).data('id');
      if (id) location.href = '/emp/plan/' + id;
    });

    // 페이징
    $('#empPagination').on('click', 'a[data-p]', function (e) {
      e.preventDefault();
      loadData(parseInt($(this).data('p'), 10));
    });

    // 신규 작성 버튼
    $('#btnNewEmp').on('click', function () {
      loadOprtrOptions();
      $('#newEmpRprtYr').removeClass('is-invalid');
      $('#newEmpOprtrId').removeClass('is-invalid');
      new bootstrap.Modal(document.getElementById('modalNewEmp')).show();
    });

    // 신규 작성 확인
    $('#btnCreateEmp').on('click', function () {
      var rprtYr  = $('#newEmpRprtYr').val();
      var oprtrId = $('#newEmpOprtrId').val();
      var valid = true;
      if (!rprtYr) {
        $('#newEmpRprtYr').addClass('is-invalid');
        $('#newEmpRprtYr-error').text('보고연도를 선택해주세요.');
        valid = false;
      } else {
        $('#newEmpRprtYr').removeClass('is-invalid');
      }
      if (!oprtrId) {
        $('#newEmpOprtrId').addClass('is-invalid');
        $('#newEmpOprtrId-error').text('운영사를 선택해주세요.');
        valid = false;
      } else {
        $('#newEmpOprtrId').removeClass('is-invalid');
      }
      if (!valid) return;

      $(this).prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>처리 중...');
      $.ajax({
        url: '/api/emp/plan',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ rprtYr: rprtYr, oprtrId: oprtrId })
      })
        .done(function (res) {
          var id = res.data && res.data.empPlanId;
          bootstrap.Modal.getInstance(document.getElementById('modalNewEmp')).hide();
          if (id) {
            location.href = '/emp/plan/' + id;
          } else {
            loadData(1);
          }
        })
        .fail(function (xhr) {
          var msg = (xhr.responseJSON && xhr.responseJSON.message) || '신규 작성에 실패했습니다.';
          IcasAlert.info(msg); // TODO: IcasAlert.error(msg) 로 교체
        })
        .always(function () {
          $('#btnCreateEmp').prop('disabled', false).html('<i class="bi bi-check-lg me-1"></i>작성 시작');
        });
    });

    // 엑셀 다운로드 (임시)
    $('#btnExcel').on('click', function () {
      var params = buildParams(1);
      params.size = 10000;
      var qs = $.param(params);
      location.href = '/api/emp/plan/excel?' + qs;
    });
  });

})();
</script>
</body>
</html>
