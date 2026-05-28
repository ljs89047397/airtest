package kr.go.molit.icas.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 단방향 해시 + 고정 salt.
 * <p>
 * 평문 앞에 {@code "icas"}, 뒤에 {@code "cems"} 를 붙여서 해시한다.
 * 호출자는 평문만 전달. salt 직접 prepend/append 금지 (이중 적용 방지).
 */
public final class Sha256 {

    private static final String PREFIX_SALT = "icas";
    private static final String SUFFIX_SALT = "cems";

    private Sha256() { }

    /** 평문을 받아 SHA-256 hex 소문자 64자를 반환. */
    public static String hex(String plain) {
        if (plain == null) throw new IllegalArgumentException("plain must not be null");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((PREFIX_SALT + plain + SUFFIX_SALT).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * 비밀번호 검증 — 평문과 저장된 hex 가 일치하는가?
     * SER-009 시큐어코딩: timing attack 방지를 위해 constant-time 비교(MessageDigest.isEqual) 사용.
     * 저장된 hex 는 대소문자 무시 비교 대상이므로 양쪽을 소문자로 정규화한 뒤 바이트 비교.
     */
    public static boolean matches(String plain, String storedHex) {
        if (plain == null || storedHex == null) return false;
        byte[] computed = hex(plain).getBytes(StandardCharsets.US_ASCII);
        byte[] stored   = storedHex.toLowerCase().getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(computed, stored);
    }
}
