package kr.go.molit.icas.common.service;

import kr.go.molit.icas.com.cd.CdMapper;
import kr.go.molit.icas.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 공통코드 유효성 검증.
 *
 * <p>FK 가 아닌 공통코드 컬럼(예: {@code emp_st_cd}, {@code fuel_type_cd}) 을 저장하기 전,
 * 현재 유효한 그룹·코드 인스턴스가 존재하는지 검증.
 */
@Component
@RequiredArgsConstructor
public class CdValidator {

    private final CdMapper cdMapper;

    public void assertValid(String groupId, String cd) {
        if (cd == null || cd.isBlank()) {
            throw BusinessException.badRequest(String.format("[%s] 코드는 필수입니다.", groupId));
        }
        boolean exists = cdMapper.existsValidCd(groupId, cd);
        if (!exists) {
            throw BusinessException.badRequest(
                String.format("선택한 공통코드(%s.%s)는 더 이상 사용되지 않거나 존재하지 않는 항목입니다.", groupId, cd));
        }
    }
}
