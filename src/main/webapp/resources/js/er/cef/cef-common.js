/**
 * CEF 도메인 공통 유틸리티
 * - esc()             : HTML 이스케이프
 * - renderCefBadge()  : CEF 상태 배지 HTML 반환
 */

// CEF 상태 코드 맵 (DRAFT→SBMTD→APRVD→CNCLD)
var CEF_STATUS_MAP = {
  'DRAFT': ['bg-secondary',          '작성중'],
  'SBMTD': ['bg-primary',            '제출'],
  'APRVD': ['bg-success',            '승인'],
  'CNCLD': ['bg-danger',             '취소']
};

/**
 * HTML 특수문자 이스케이프
 * @param {string} str
 * @returns {string}
 */
function esc(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;');
}

/**
 * CEF 상태 코드에 대응하는 Bootstrap 배지 HTML 반환
 * @param {string} cd 상태코드
 * @returns {string} badge HTML
 */
function renderCefBadge(cd) {
  if (!cd) return '<span class="badge status-badge bg-light text-muted border">-</span>';
  var pair = CEF_STATUS_MAP[cd];
  if (!pair) return '<span class="badge status-badge bg-secondary">' + esc(cd) + '</span>';
  return '<span class="badge status-badge ' + pair[0] + '">' + pair[1] + '</span>';
}
