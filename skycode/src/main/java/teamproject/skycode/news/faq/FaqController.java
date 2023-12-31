package teamproject.skycode.news.faq;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import teamproject.skycode.login.MemberEntity;
import teamproject.skycode.login.MemberRepository;
import teamproject.skycode.login.MemberUtils;
import teamproject.skycode.news.notion.Notion;
import teamproject.skycode.review.ReviewEntity;
import teamproject.skycode.review.ReviewRepository;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class FaqController {

    private final FaqRepository faqRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final FaqService faqService;


    @GetMapping(value = "/admin/news/faqUp")
    public String newsFaqUp(Model model, Principal principal){

        // 유저 로그인
        if (principal != null) {
            String user = principal.getName();
            MemberEntity userInfo = memberRepository.findByEmail(user);
            model.addAttribute("userInfo", userInfo);
            List<ReviewEntity> review = reviewRepository.findByMemberEntityId(userInfo.getId());
            int reviewNum = review.size();
            model.addAttribute("reviewNum",reviewNum);
        }

        model.addAttribute("faqForm", new FaqForm());
        System.out.println("1234");
        return "news/faq/faqUp";
    }

    @PostMapping(value = "/admin/news/faq/faq")
    public String createFaq(@ModelAttribute FaqForm faqForm, Model model){
        if (faqForm.getFaqQuestion().isEmpty() || faqForm.getFaqAnswer().isEmpty()) {
            // 필수 필드가 비어있는 경우
            String errorMsg = "질문과 답변을 모두 입력해주세요.";
            model.addAttribute("errorMsg", errorMsg);
            return "news/faq/faqUp"; // 에러 메시지와 함께 원래의 입력 페이지로 돌아갑니다.
        }
        Faq faq = faqForm.toEntity();
        faqRepository.save(faq);
        System.out.println("faq보내기");
        return "redirect:/news/faq/faq";
    }
    private static final int PAGE_SIZE = 10; // You can change this to your desired page size


    @GetMapping("/news/faq/faq")
    public String faqPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model, Principal principal
    ) {

        // 유저 로그인
        if (principal != null) {
            String user = principal.getName();
            MemberEntity userInfo = memberRepository.findByEmail(user);
            model.addAttribute("userInfo", userInfo);
            List<ReviewEntity> review = reviewRepository.findByMemberEntityId(userInfo.getId());
            int reviewNum = review.size();
            model.addAttribute("reviewNum",reviewNum);
        }

        Page<Faq> faqPage = faqRepository.findAll(PageRequest.of(page - 1, PAGE_SIZE));
        int totalPages = faqPage.getTotalPages();

        // Check if the user is an admin
        boolean isAdmin = checkIfUserIsAdmin();

        model.addAttribute("isAdmin", isAdmin);

        // Create a list of page numbers for the pager
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNumbers.add(i);
        }

        model.addAttribute("faqs", faqPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageNumbers", pageNumbers);

        return "news/faq/faq";
    }

    private boolean checkIfUserIsAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

}
