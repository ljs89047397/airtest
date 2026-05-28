/**
 * IcasAlert — Bootstrap 5 기반 공통 알림/확인 모듈
 * 의존: jQuery, Bootstrap 5 JS
 *
 * API:
 *   IcasAlert.info(msg)                   — 정보 토스트 (primary, 자동닫힘 3.5s)
 *   IcasAlert.success(msg)                — 성공 토스트 (success, 자동닫힘 3.5s)
 *   IcasAlert.warning(msg)                — 경고 토스트 (warning, 수동 닫기)
 *   IcasAlert.error(msg)                  — 에러 토스트 (danger, 수동 닫기)
 *   IcasAlert.confirm(msg, onOk, onCancel) — Bootstrap modal, 콜백 + Promise
 */
(function(global) {
  'use strict';

  /* ── jQuery 가용성 검사 헬퍼 ─────────────────────────── */
  function hasJQuery() {
    if (typeof jQuery === 'undefined') {
      console.warn('[IcasAlert] jQuery 가 로드되지 않았습니다.');
      return false;
    }
    return true;
  }

  /* ── 토스트 컨테이너 확보 ────────────────────────────── */
  var CONTAINER_ID = 'icasToastContainer';

  function getContainer() {
    var $c = jQuery('#' + CONTAINER_ID);
    if (!$c.length) {
      $c = jQuery('<div>')
        .attr('id', CONTAINER_ID)
        .css({
          position: 'fixed',
          bottom: '1rem',
          right: '1rem',
          zIndex: 9999,
          display: 'flex',
          flexDirection: 'column',
          gap: '0.5rem'
        });
      jQuery('body').append($c);
    }
    return $c;
  }

  /* ── 토스트 표시 내부 함수 ───────────────────────────── */
  function showToast(msg, variant, autohide, delay) {
    if (!hasJQuery()) return;

    var autoStr  = autohide ? 'true'  : 'false';
    var delayStr = autohide ? String(delay || 3500) : '0';

    var iconMap = {
      primary : '<i class="bi bi-info-circle-fill me-2"></i>',
      success : '<i class="bi bi-check-circle-fill me-2"></i>',
      warning : '<i class="bi bi-exclamation-triangle-fill me-2"></i>',
      danger  : '<i class="bi bi-x-circle-fill me-2"></i>'
    };
    var icon = iconMap[variant] || '';

    var $toast = jQuery(
      '<div class="toast align-items-center text-bg-' + variant + ' border-0" role="alert" aria-live="assertive" aria-atomic="true"' +
      ' data-bs-autohide="' + autoStr + '" data-bs-delay="' + delayStr + '">' +
      '  <div class="d-flex">' +
      '    <div class="toast-body">' + icon + jQuery('<span>').text(msg).html() + '</div>' +
      '    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="닫기"></button>' +
      '  </div>' +
      '</div>'
    );

    getContainer().append($toast);

    var toastEl = $toast[0];
    var bsToast = new bootstrap.Toast(toastEl);
    toastEl.addEventListener('hidden.bs.toast', function() {
      $toast.remove();
    });
    bsToast.show();
  }

  /* ── confirm 모달 ────────────────────────────────────── */
  var MODAL_ID = 'icasConfirmModal';

  function ensureModal() {
    if (jQuery('#' + MODAL_ID).length) return;

    var html =
      '<div class="modal fade" id="' + MODAL_ID + '" tabindex="-1" aria-hidden="true">' +
      '  <div class="modal-dialog modal-dialog-centered">' +
      '    <div class="modal-content">' +
      '      <div class="modal-header">' +
      '        <h5 class="modal-title"><i class="bi bi-question-circle text-primary me-2"></i>확인</h5>' +
      '        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="닫기"></button>' +
      '      </div>' +
      '      <div class="modal-body" id="icasConfirmBody"></div>' +
      '      <div class="modal-footer">' +
      '        <button type="button" class="btn btn-secondary" id="icasConfirmCancelBtn" data-bs-dismiss="modal">취소</button>' +
      '        <button type="button" class="btn btn-primary" id="icasConfirmOkBtn">확인</button>' +
      '      </div>' +
      '    </div>' +
      '  </div>' +
      '</div>';

    jQuery('body').append(html);
  }

  /* ── 공개 API ────────────────────────────────────────── */
  global.IcasAlert = {

    /** 정보 토스트 (primary, 자동닫힘 3.5s) */
    info: function(msg) {
      showToast(msg, 'primary', true, 3500);
    },

    /** 성공 토스트 (success, 자동닫힘 3.5s) */
    success: function(msg) {
      showToast(msg, 'success', true, 3500);
    },

    /** 경고 토스트 (warning, 수동 닫기) */
    warning: function(msg) {
      showToast(msg, 'warning', false);
    },

    /** 에러 토스트 (danger, 수동 닫기) */
    error: function(msg) {
      showToast(msg, 'danger', false);
    },

    /**
     * Bootstrap 모달 confirm
     * @param {string}   msg       본문 메시지
     * @param {Function} [onOk]    확인 콜백
     * @param {Function} [onCancel] 취소 콜백
     * @returns {Promise<boolean>} 확인 → true, 취소 → false
     */
    confirm: function(msg, onOk, onCancel) {
      if (!hasJQuery()) {
        /* jQuery 없을 때 네이티브 fallback */
        var result = window.confirm(msg);
        if (result && typeof onOk === 'function')     onOk();
        if (!result && typeof onCancel === 'function') onCancel();
        return Promise.resolve(result);
      }

      ensureModal();

      var $modal  = jQuery('#' + MODAL_ID);
      var $body   = jQuery('#icasConfirmBody');
      var $okBtn  = jQuery('#icasConfirmOkBtn');
      var $cnlBtn = jQuery('#icasConfirmCancelBtn');

      /* 메시지 설정 (XSS 방어) */
      $body.text(msg);

      /* 이전 핸들러 제거 후 새로 바인딩 */
      $okBtn.off('click.icasConfirm');
      $cnlBtn.off('click.icasConfirm');
      $modal.off('hidden.bs.modal.icasConfirm');

      var bsModal = bootstrap.Modal.getOrCreateInstance($modal[0]);

      return new Promise(function(resolve) {
        var answered = false;

        function doClose(ok) {
          if (answered) return;
          answered = true;
          bsModal.hide();
          if (ok  && typeof onOk     === 'function') onOk();
          if (!ok && typeof onCancel === 'function') onCancel();
          resolve(ok);
        }

        $okBtn.on('click.icasConfirm', function() { doClose(true); });
        $cnlBtn.on('click.icasConfirm', function() { doClose(false); });

        /* 모달 외부 클릭·ESC 로 닫힐 때도 취소 처리 */
        $modal.on('hidden.bs.modal.icasConfirm', function() { doClose(false); });

        bsModal.show();
      });
    }
  };

})(window);
