package kr.go.molit.icas.common;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * JSP 화면 라우팅 Controller.
 * 정적 화면 이동 — 데이터는 AJAX로 별도 조회.
 */
@Controller
public class PageController {

    @GetMapping({"/", "/main"})
    public String main(Model model) {
        model.addAttribute("currentMenu", "dashboard");
        return "main";
    }

    @GetMapping("/login")
    public String login() {
        return "com/login";
    }

    /** 시스템 매뉴얼 — 전체 흐름·박스별 업무·권한 매트릭스 (헤더 우상단 진입) */
    @GetMapping("/manual")
    public String manual() {
        return "manual";
    }

    // ─── EMP (배출량 모니터링 계획) ─────────────────────────────
    @GetMapping("/emp/plan")
    public String empList(Model model) {
        model.addAttribute("currentMenu", "emp");
        return "emp/list";
    }

    @GetMapping("/emp/plan/{empPlanId}")
    public String empDetail(@PathVariable String empPlanId, Model model) {
        model.addAttribute("currentMenu", "emp");
        model.addAttribute("empPlanId", empPlanId);
        return "emp/detail";
    }

    // ─── ER (배출량보고서) ──────────────────────────────────────
    @GetMapping("/er/list")
    public String erList(Model model) {
        model.addAttribute("currentMenu", "er");
        return "er/list";
    }

    @GetMapping("/er/{erId}")
    public String erDetail(@PathVariable String erId, Model model) {
        model.addAttribute("currentMenu", "er");
        model.addAttribute("erId", erId);
        return "er/detail";
    }

    // ─── CEF (적격연료) ─────────────────────────────────────────
    @GetMapping({"/er/cef", "/er/cef/list"})
    public String cefList(Model model) {
        model.addAttribute("currentMenu", "cef");
        return "er/cef/list";
    }

    @GetMapping("/er/cef/{cefId}")
    public String cefDetail(@PathVariable String cefId, Model model) {
        model.addAttribute("currentMenu", "cef");
        model.addAttribute("cefId", cefId);
        return "er/cef/detail";
    }

    // ─── EUCR (배출권 취소) ────────────────────────────────────
    @GetMapping({"/er/eucr", "/er/eucr/list"})
    public String eucrList(Model model) {
        model.addAttribute("currentMenu", "eucr");
        return "er/eucr/list";
    }

    @GetMapping("/er/eucr/{eucrId}")
    public String eucrDetail(@PathVariable String eucrId, Model model) {
        model.addAttribute("currentMenu", "eucr");
        model.addAttribute("eucrId", eucrId);
        return "er/eucr/detail";
    }

    // ─── OoM (적정성 검토) ─────────────────────────────────────
    @GetMapping({"/er/oom", "/er/oom/list"})
    public String oomList(Model model) {
        model.addAttribute("currentMenu", "oom");
        return "er/oom/list";
    }

    @GetMapping("/er/oom/{oomId}")
    public String oomDetail(@PathVariable String oomId, Model model) {
        model.addAttribute("currentMenu", "oom");
        model.addAttribute("oomId", oomId);
        return "er/oom/detail";
    }

    // ─── CORSIA 세부항목 검증 (RFP 박스 ⑦) ─────────────────────
    @GetMapping({"/er/oom/qchk", "/er/oom/qchk/list"})
    public String qchkList(Model model) {
        model.addAttribute("currentMenu", "qchk");
        return "er/oom/qchk/list";
    }

    @GetMapping("/er/oom/qchk/{checkId}")
    public String qchkDetail(@PathVariable String checkId, Model model) {
        // qchk 상세는 OoM 상세와 동일 데이터 — 메뉴 활성만 다름
        model.addAttribute("currentMenu", "qchk");
        model.addAttribute("oomId", checkId);
        return "er/oom/detail";
    }

    // ─── 공통 AI 서비스 (RFP 박스 ⑩ - 2차년도 자리) ────────────
    @GetMapping("/ai/console")
    public String aiConsole(Model model) {
        model.addAttribute("currentMenu", "aiConsole");
        return "ai/console";
    }

    // ─── SAF 공항별 급유·구매 자식 경로 (편의 단축) ────────────
    @GetMapping("/saf/airprt/fuel")
    public String safAirprtFuel(Model model) {
        model.addAttribute("currentMenu", "safAirprt");
        model.addAttribute("airprtTab", "fuel");
        return "saf/airprt/list";
    }

    @GetMapping("/saf/airprt/purch")
    public String safAirprtPurch(Model model) {
        model.addAttribute("currentMenu", "safAirprt");
        model.addAttribute("airprtTab", "purch");
        return "saf/airprt/list";
    }

    // ─── VR (검증보고서) ───────────────────────────────────────
    @GetMapping("/vr/list")
    public String vrList(Model model) {
        model.addAttribute("currentMenu", "vr");
        return "vr/list";
    }

    @GetMapping("/vr/{vrId}")
    public String vrDetail(@PathVariable String vrId, Model model) {
        model.addAttribute("currentMenu", "vr");
        model.addAttribute("vrId", vrId);
        return "vr/detail";
    }

    // ─── SAF (지속가능항공유) ──────────────────────────────────
    @GetMapping("/saf/dashboard")
    public String safDashboard(Model model) {
        model.addAttribute("currentMenu", "saf");
        return "saf/dashboard";
    }

    @GetMapping("/saf/cert")
    public String safCertList(Model model) {
        model.addAttribute("currentMenu", "safCert");
        return "saf/cert/list";
    }

    @GetMapping("/saf/cert/{certId}")
    public String safCertDetail(@PathVariable String certId, Model model) {
        model.addAttribute("currentMenu", "safCert");
        model.addAttribute("certId", certId);
        return "saf/cert/detail";
    }

    @GetMapping("/saf/batch")
    public String safBatchList(Model model) {
        model.addAttribute("currentMenu", "safBatch");
        return "saf/batch/list";
    }

    @GetMapping("/saf/batch/{batchId}")
    public String safBatchDetail(@PathVariable String batchId, Model model) {
        model.addAttribute("currentMenu", "safBatch");
        model.addAttribute("batchId", batchId);
        return "saf/batch/detail";
    }

    @GetMapping("/saf/airprt")
    public String safAirprt(Model model) {
        model.addAttribute("currentMenu", "safAirprt");
        return "saf/airprt/list";
    }

    @GetMapping("/saf/mntr")
    public String safMntr(Model model) {
        model.addAttribute("currentMenu", "safMntr");
        return "saf/mntr/list";
    }

    // ─── PTL (통합포털) ────────────────────────────────────────
    @GetMapping("/ptl/workflow")
    public String ptlWorkflow(Model model) {
        model.addAttribute("currentMenu", "workflow");
        return "ptl/workflow";
    }

    @GetMapping("/ptl/stat")
    public String ptlStat(Model model) {
        model.addAttribute("currentMenu", "stat");
        return "ptl/stat";
    }

    @GetMapping("/ptl/ccr")
    public String ptlCcr(Model model) {
        model.addAttribute("currentMenu", "ccr");
        return "ptl/ccr";
    }

    @GetMapping("/ptl/sim")
    public String ptlSim(Model model) {
        model.addAttribute("currentMenu", "sim");
        return "ptl/sim";
    }

    @GetMapping("/ptl/actn")
    public String ptlActn(Model model) {
        model.addAttribute("currentMenu", "actn");
        return "ptl/actn";
    }

    /** CORSIA 운영 일정 캘린더 — 시행계획 p.13 (검증 5~6월, ICAO 제출 7월, 상쇄의무 통보 11월) */
    @GetMapping("/ptl/calendar")
    public String ptlCalendar(Model model) {
        model.addAttribute("currentMenu", "calendar");
        return "ptl/calendar";
    }

    /** 비밀번호 변경 — 본인 계정 */
    @GetMapping("/com/user/me/password")
    public String userPasswordChange(Model model) {
        model.addAttribute("currentMenu", "passwordChange");
        return "com/user/password";
    }

    /** 시스템 헬스 대시보드 (운영자) */
    @GetMapping("/admin/health")
    public String adminHealth(Model model) {
        model.addAttribute("currentMenu", "adminHealth");
        return "admin/health";
    }

    /** 친환경 항공기 도입 트래커 — 시행계획 p.8 (5사 40대) */
    @GetMapping("/com/eco-fleet")
    public String ecoFleet(Model model) {
        model.addAttribute("currentMenu", "ecoFleet");
        return "com/eco-fleet";
    }

    /** ICAO 송신 콘솔 (Mock) — 시행계획 p.13 (7월 ICAO 제출) */
    @GetMapping("/admin/icao-submit")
    public String icaoSubmit(Model model) {
        model.addAttribute("currentMenu", "icaoSubmit");
        return "admin/icao-submit";
    }

    // ─── COM (공통/관리) ───────────────────────────────────────
    @GetMapping("/com/user")
    public String comUser(Model model) {
        model.addAttribute("currentMenu", "comUser");
        return "com/user/list";
    }

    @GetMapping("/com/ognz")
    public String comOgnz(Model model) {
        model.addAttribute("currentMenu", "comOgnz");
        return "com/ognz/list";
    }

    @GetMapping("/com/oprtr")
    public String comOprtr(Model model) {
        model.addAttribute("currentMenu", "comOprtr");
        return "com/oprtr/list";
    }

    @GetMapping("/com/vrfcn")
    public String comVrfcn(Model model) {
        model.addAttribute("currentMenu", "comVrfcn");
        return "com/vrfcn/list";
    }

    @GetMapping("/com/authrt")
    public String comAuthrt(Model model) {
        model.addAttribute("currentMenu", "comAuthrt");
        return "com/authrt/list";
    }

    @GetMapping("/com/cd")
    public String comCd(Model model) {
        model.addAttribute("currentMenu", "comCd");
        return "com/cd/list";
    }

    @GetMapping("/com/atrz")
    public String comAtrz(Model model) {
        model.addAttribute("currentMenu", "comAtrz");
        return "com/atrz/list";
    }

    @GetMapping("/com/rglt")
    public String comRglt(Model model) {
        model.addAttribute("currentMenu", "comRglt");
        return "com/rglt/list";
    }

    @GetMapping("/com/role")
    public String comRole(Model model) {
        model.addAttribute("currentMenu", "comRole");
        return "com/role/list";
    }

    @GetMapping("/com/menu")
    public String comMenu(Model model) {
        model.addAttribute("currentMenu", "comMenu");
        return "com/menu/list";
    }

    @GetMapping("/com/prgrm")
    public String comPrgrm(Model model) {
        model.addAttribute("currentMenu", "comPrgrm");
        return "com/prgrm/list";
    }
}
