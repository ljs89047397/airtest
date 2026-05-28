package kr.go.molit.icas.common.util;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 자동 채번 헬퍼.
 * <ul>
 *   <li>관리테이블 PK: {@code <PFX>####} (예: OP0001, EP0001, ER0001)</li>
 *   <li>시스템구분 종속: {@code <SYS_SE_CD>_NNNN} (예: COM_0001, EMP_0001)</li>
 * </ul>
 *
 * <p>실제 채번은 도메인 Mapper 에서 {@code SELECT COALESCE(MAX(...),0) + 1} 으로 구현.
 * 본 클래스는 포맷 헬퍼.
 */
@Component
public class IdGenerator {

    /** 예: ("OP", 5) → "OP0005" */
    public String managementPk(String prefix, int seq) {
        return prefix + String.format("%04d", seq);
    }

    /** 예: ("EMP", 12) → "EMP_0012" */
    public String sysScopedId(String sysSeCd, int seq) {
        return sysSeCd + "_" + String.format("%04d", seq);
    }
}
