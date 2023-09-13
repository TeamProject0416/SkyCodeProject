package teamproject.skycode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import teamproject.skycode.event.EventEntity;
import teamproject.skycode.event.EventRepository;
import teamproject.skycode.event.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import teamproject.skycode.login.MemberEntity;
import teamproject.skycode.login.MemberRepository;

import java.security.Principal;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class MainController {
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;

    @GetMapping(value = "/")
    public String skyCode(Model model, Principal principal) {

        // 이벤트 캐러셀
        List<EventEntity> event = eventRepository.findByONGOING();
        model.addAttribute("events",event);

        // 유저 로그인
        String user = "";
        if (principal != null) {
            user = principal.getName();
            MemberEntity userInfo = memberRepository.findByEmail(user);
            model.addAttribute("userInfo",userInfo);
        }
        model.addAttribute("user", user);
        return "main";
    }
}
