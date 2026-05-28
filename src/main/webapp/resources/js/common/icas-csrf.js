/**
 * ICAS-CEMS CSRF 글로벌 설정
 *
 * - Spring Security CookieCsrfTokenRepository(httpOnly=false) 로 발급된
 *   XSRF-TOKEN 쿠키를 읽어 모든 jQuery AJAX 요청의 헤더에 자동 첨부한다.
 * - 쿠키 방식이므로 <meta> 태그 없이도 동작하지만,
 *   <meta name="_csrf"> 태그가 있으면 해당 값을 우선 사용한다.
 */
(function ($) {
    'use strict';

    /** 이름으로 쿠키 값 읽기 */
    function getCookie(name) {
        var match = document.cookie.match(new RegExp('(?:^|;)\\s*' + name + '=([^;]*)'));
        return match ? decodeURIComponent(match[1]) : null;
    }

    $.ajaxSetup({
        beforeSend: function (xhr, settings) {
            /* GET · HEAD · OPTIONS · TRACE 는 CSRF 토큰 불필요 */
            var method = (settings.type || 'GET').toUpperCase();
            if (/^(GET|HEAD|OPTIONS|TRACE)$/.test(method)) {
                return;
            }

            /* 1순위: <meta name="_csrf"> (서버 렌더링 토큰) */
            var token  = $('meta[name="_csrf"]').attr('content');
            var header = $('meta[name="_csrf_header"]').attr('content') || 'X-XSRF-TOKEN';

            /* 2순위: XSRF-TOKEN 쿠키 (CookieCsrfTokenRepository) */
            if (!token) {
                token  = getCookie('XSRF-TOKEN');
                header = 'X-XSRF-TOKEN';
            }

            if (token && header) {
                xhr.setRequestHeader(header, token);
            }
        }
    });

}(jQuery));
