package jpcompany.smartwire.web.machine.controller;

import jpcompany.smartwire.domain.Member;
import jpcompany.smartwire.web.machine.dto.MachineDto;
import jpcompany.smartwire.web.machine.dto.MachineDtoList;
import jpcompany.smartwire.web.machine.repository.MachineRepositoryJdbcTemplate;
import jpcompany.smartwire.web.machine.service.MachineService;
import jpcompany.smartwire.security.common.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MachineController {

    private final MachineService machineService;
    private final MachineRepositoryJdbcTemplate machineRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/member/machine")
    public String machine(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        Member member = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Member member = principalDetails.getMember();

        model.addAttribute("member", member);

        List<MachineDto> machines = machineRepository.findAll(member.getId());
        machines.sort(Comparator.comparingInt(MachineDto::getSequence));

        model.addAttribute("machineDtoList", new MachineDtoList(machines));
        return "home/machine";
    }


    // TODO - machine id 값 클라이언트 노출
    @Transactional
    @PostMapping("/member/machine")
    public String postMachine(@AuthenticationPrincipal PrincipalDetails principalDetails,
                              @Valid @ModelAttribute MachineDtoList machineDtoList, BindingResult bindingResult,
                              RedirectAttributes redirectAttrs, Model model) {
//        Member member = principalDetails.getMember();
        Member member = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        Set<String> set = new HashSet<>();
        if (machineDtoList.getMachines().stream().anyMatch(n -> !set.add(n.getMachineName()))) {
            bindingResult.reject("MachineNameDuplicated", "기계 이름은 중복될 수 없습니다.");
        }
        
        for (MachineDto machineDto : machineDtoList.getMachines()) {
            // 검증 로직
            if (bindingResult.hasErrors()) {
                log.info("errors = {}", bindingResult);
                model.addAttribute("member", member);
                return "home/machine";
            }

            redirectAttrs.addFlashAttribute("popupMessage", "기계 설정이 완료되었습니다.");
            if (!machineService.saveMachineFormNHaveMachine(member.getId(), member.getHaveMachine(), machineDto)) {
                // 메인 페이지에서 기계 있을 때 화면 보여줘야 하니까
                updateMemberSession(member, true);
            }
        }
        return "redirect:/member/machine";
    }

    @PostMapping("/member/machine/delete")
    public String deleteMachine(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                @RequestParam Integer machineIdSend, @RequestParam String loginPassword,
                                RedirectAttributes redirectAttrs) {
//        Member member = principalDetails.getMember();
        Member member = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        // 비밀번호 확인
        if (!passwordEncoder.matches(loginPassword, member.getLoginPassword())) {
            redirectAttrs.addFlashAttribute("popupMessage", "비밀번호가 일치하지 않습니다.");
            return "redirect:/member/machine";
        }

        // 기계가 하나도 없으면 DB, 세션 업데이트
        // TODO - Member 의 Setter 가 열려 있음
        if (!machineService.deleteMachineNHaveMachine(machineIdSend, member.getId())) {
            updateMemberSession(member, false);
        }

        redirectAttrs.addFlashAttribute("popupMessage", "기계 삭제에 성공하였습니다.");
        redirectAttrs.addFlashAttribute("autoClickButton", true);
        return "redirect:/member/machine";
    }

    // TODO - 새션 값 변경 동시성 문제
    private static void updateMemberSession(Member loginMember, boolean havMachine) {
        loginMember.setHaveMachine(havMachine);
        PrincipalDetails newPrincipalDetails = new PrincipalDetails(loginMember);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                newPrincipalDetails,    // 변경된 정보를 가진 새로운 PrincipalDetails
                newPrincipalDetails.getPassword(),    // 보통 기존 패스워드를 그대로 사용합니다
                newPrincipalDetails.getAuthorities());    // 보통 기존 권한을 그대로 사용합니다
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
