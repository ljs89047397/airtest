package kr.go.molit.icas.com.cd;

import kr.go.molit.icas.com.cd.domain.CdDtlVO;
import kr.go.molit.icas.com.cd.domain.CdGroupVO;
import kr.go.molit.icas.common.dto.ApiResponse;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/com/cd")
@RequiredArgsConstructor
public class CdController {

    private final CdService cdService;

    @GetMapping
    public ApiResponse<List<CdGroupVO>> listGroups() {
        return ApiResponse.ok(cdService.listGroups());
    }

    @GetMapping("/{grpId}")
    public ApiResponse<List<CdDtlVO>> listDtls(@PathVariable String grpId) {
        return ApiResponse.ok(cdService.listDtlsByGroup(grpId));
    }

    @PostMapping
    public ApiResponse<CdGroupVO> createGroup(@RequestBody CdGroupVO vo,
                                              @AuthenticationPrincipal IcasUser user) {
        return ApiResponse.ok(cdService.createGroup(vo, user), "등록되었습니다");
    }

    @PutMapping("/{grpId}")
    public ApiResponse<Void> updateGroup(@PathVariable String grpId,
                                         @RequestBody CdGroupVO vo,
                                         @AuthenticationPrincipal IcasUser user) {
        vo.setGrpId(grpId);
        cdService.updateGroup(vo, user);
        return ApiResponse.ok(null, "수정되었습니다");
    }

    @DeleteMapping("/{grpId}")
    public ApiResponse<Void> deleteGroup(@PathVariable String grpId,
                                         @AuthenticationPrincipal IcasUser user) {
        cdService.softDeleteGroup(grpId, user);
        return ApiResponse.ok(null, "삭제되었습니다");
    }

    @PostMapping("/{grpId}/dtl")
    public ApiResponse<CdDtlVO> createDtl(@PathVariable String grpId,
                                          @RequestBody CdDtlVO vo,
                                          @AuthenticationPrincipal IcasUser user) {
        vo.setGrpId(grpId);
        return ApiResponse.ok(cdService.createDtl(vo, user), "등록되었습니다");
    }

    @PutMapping("/{grpId}/dtl/{cd}")
    public ApiResponse<Void> updateDtl(@PathVariable String grpId,
                                       @PathVariable String cd,
                                       @RequestBody CdDtlVO vo,
                                       @AuthenticationPrincipal IcasUser user) {
        vo.setGrpId(grpId);
        vo.setCd(cd);
        cdService.updateDtl(vo, user);
        return ApiResponse.ok(null, "수정되었습니다");
    }

    @DeleteMapping("/{grpId}/dtl/{cd}")
    public ApiResponse<Void> deleteDtl(@PathVariable String grpId,
                                       @PathVariable String cd,
                                       @AuthenticationPrincipal IcasUser user) {
        cdService.softDeleteDtl(grpId, cd, user);
        return ApiResponse.ok(null, "삭제되었습니다");
    }
}
