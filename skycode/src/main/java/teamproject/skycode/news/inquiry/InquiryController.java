package teamproject.skycode.news.inquiry;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import teamproject.skycode.constant.ActionType;
import teamproject.skycode.constant.Role;

import teamproject.skycode.coupon.Member_CouponEntity;
import teamproject.skycode.coupon.Member_CouponRepository;
import teamproject.skycode.login.MemberEntity;
import teamproject.skycode.login.MemberRepository;
import teamproject.skycode.login.MemberService;
import teamproject.skycode.point.PointHistoryEntity;
import teamproject.skycode.point.PointHistoryRepository;
import teamproject.skycode.review.ReviewEntity;
import teamproject.skycode.review.ReviewRepository;

import javax.validation.Valid;
import java.lang.reflect.Member;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/news")
@RequiredArgsConstructor
public class InquiryController {


    @Autowired
    private InquiryRepository inquiryRepository;
    private final InquiryViewCountRepository inquiryViewCountRepository;
    private final InquiryService inquiryService;
    private final InquiryViewCountService inquiryViewCountService;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final MemberService userService;
    private final Member_CouponRepository memberCouponRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Autowired
    public void InquiryController(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    // 1 대 1 문의폼 화면 출력
    @GetMapping("/inquiry")
    public String showInquiryForm(Model model, Principal principal) {

        if (principal == null) {
            // 사용자가 로그인하지 않은 경우 에러 페이지로 리다이렉트
            return "redirect:/error";
        }

        // 유저 로그인 모달 함수
        populateAdminModel(model, principal);

        model.addAttribute("inquiryForm", new InquiryForm());
        return "/news/inquiry/inquiry";
    }

    // 1 대 1 문의 등록시 전송하는 것
    @PostMapping("/inquiry/inquiry")
    public ModelAndView submitInquiry(@Valid InquiryForm inquiryForm, BindingResult bindingResult,
                                      Model model, Principal principal) {
        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("news/inquiry/inquiry");
        } else {
            // 유저 로그인
            if (principal != null) {
                String user = principal.getName();
                MemberEntity memberEntity = memberRepository.findByEmail(user);
                inquiryForm.setEmail(user);
                inquiryForm.setWriter(memberEntity);
                inquiryForm.setNickName(memberEntity.getNickName());
            }

            Inquiry inquiryEntity = inquiryForm.toEntity();

            inquiryService.saveInquiry(inquiryEntity);

            modelAndView.setViewName("redirect:/news/inquiry/inquiryList"); // Adjust the URL accordingly
            modelAndView.addObject("successMessage", "문의가 등록되었습니다.");
        }

        return modelAndView;
    }

    // 1 대 1 문의 리스트 화면 출력
    @GetMapping("/inquiry/inquiryList")
    public String getInquiries(
            Model model, Principal principal,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false) String sortBy
    ) {

        // 유저 로그인 모달 함수
        populateAdminModel(model, principal);

        int pageSize = 10;
        Pageable pageable;

        if ("popularity".equals(sortBy)) {
            pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "viewCount"));
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "regTime"));
        }

        Page<Inquiry> inquiryPage = inquiryRepository.findAll(pageable);
        List<Inquiry> inquiries = inquiryPage.getContent();

        long totalInquiryCount = inquiryService.getTotalInquiryCount();

        // Calculate the total number of pages
        int totalPages = (int) Math.ceil((double) totalInquiryCount / pageSize);

        model.addAttribute("totalInquiryCount", totalInquiryCount);
        model.addAttribute("totalPages", totalPages); // Add totalPages
        model.addAttribute("inquiries", inquiries);
        model.addAttribute("inquiryPage", inquiryPage);

        return "news/inquiry/inquiryList"; // Return the Thymeleaf template name
    }

    // 1 대 1 문의 서브페이지 화면으로 보내기
    @PostMapping("inquiry/inquiryShow")
    public String submitInquiry(@ModelAttribute InquiryForm inquiryForm, Model model, Principal principal) {
        Inquiry savedInquiry = inquiryService.saveInquiry(inquiryForm); // Save or update inquiry
        if (savedInquiry != null) {
            Long id = savedInquiry.getId(); // Get the id of the saved/updated inquiry
            InquiryViewCount viewCount = inquiryViewCountService.incrementViewCount(id);

            // Check if the logged-in user's email matches the writer's email
            if (principal != null) {
                String loggedInUserEmail = principal.getName();
                String writerEmail = savedInquiry.getWriter().getEmail();

                if (!loggedInUserEmail.equals(writerEmail)) {
                    // The logged-in user is not the writer, meaning it's a viewer
                    return "error"; // Or return a view for unauthorized access
                }
            }

            // Now, based on the id, determine the URL to redirect to
            String redirectUrl = "redirect:/news/inquiry/show/" + id; // Adjust the URL pattern according to your mapping

            model.addAttribute("viewCount", viewCount.getCount());

            return redirectUrl;
        }

        // Handle error case if savedInquiry is null
        // You can return an error view or redirect to an error page
        return "error"; // Change to the appropriate view name
    }

    // 1 대 1 문의 서브페이지 화면 출력
    @GetMapping("/inquiry/show/{id}")
    public String showInquiryById(@PathVariable Long id, Model model, Principal principal) {

        // 유저 로그인 모달 함수
        populateAdminModel(model, principal);

        Inquiry inquiry = inquiryService.getInquiryById(id);

        if (inquiry != null) {
            // Increment view count and save
            inquiry.setViewCount(inquiry.getViewCount() + 1); // Increment the view count
            inquiryRepository.save(inquiry); // Save the updated inquiry

            model.addAttribute("inquiry", inquiry);
            model.addAttribute("viewCount", inquiry.getViewCount());

            return "news/inquiry/inquiryShow";
        } else {
            // Handle inquiry not found case
            // Return appropriate error view or handle differently
            return "error";
        }
    }

    // 검색하기
    @GetMapping("/inquiry/search")
    public String searchInquiries(
            @RequestParam("search-type") String searchType,
            @RequestParam("search-value") String searchValue,
            Model model, Principal principal
    ) {

        // 유저 로그인 모달 함수
        populateAdminModel(model, principal);

        List<Inquiry> searchResults;
        System.out.println("검색");
        if ("title".equals(searchType)) {
            searchResults = inquiryService.findByInquiryTitleContaining(searchValue);
        } else if ("content".equals(searchType)) {
            searchResults = inquiryService.findByInquiryContentContaining(searchValue);
        } else if ("id".equals(searchType)) {
            searchResults = inquiryService.findByIdContaining(searchValue);
        } else {
            // Handle invalid search type
            searchResults = Collections.emptyList();
        }
        System.out.println("이건 성공 해야해");
        model.addAttribute("searchResults", searchResults);

        return "news/inquiry/searchResults";
    }

    // 문의글 삭제
    @PostMapping("/inquiry/delete")
    public String deleteInquiry(@RequestParam Long inquiryId) {
        inquiryService.deleteInquiry(inquiryId);
        System.out.println("삭제");
        return "redirect:/news/inquiry/inquiryList"; // 삭제 후 이동할 페이지를 지정합니다.
    }

    // 1 대 1 문의글 수정 페이지 출력
    @GetMapping("/inquiry/edit/{inquiryId}")
    public String showEditForm(@PathVariable Long inquiryId, Model model, Principal principal) {

        // 유저 로그인 모달 함수
        populateAdminModel(model, principal);

        // inquiryId를 사용하여 해당 문의 내용을 불러오는 로직을 추가
        Inquiry inquiry = inquiryService.findById(inquiryId);

        // 조회한 문의 내용을 폼 객체에 매핑
        InquiryForm inquiryForm = new InquiryForm();
        inquiryForm.setId(inquiry.getId());
        inquiryForm.setType(inquiry.getType());
        inquiryForm.setPrivate(inquiry.isPrivate());
        inquiryForm.setInquiryTitle(inquiry.getInquiryTitle());
        inquiryForm.setInquiryContent(inquiry.getInquiryContent());

        model.addAttribute("inquiryForm", inquiryForm);
        System.out.println("수정중");

        return "news/inquiry/edit";
    }

    // 1 대 1 문의글 수정 보내기
    @PostMapping("/inquiry/edit")
    public String editInquiry(@ModelAttribute("inquiryForm") InquiryForm inquiryForm) {
        // 폼 데이터를 기반으로 수정된 내용을 저장하는 로직을 구현합니다.
        // InquiryForm을 Inquiry 엔티티로 변환하고 엔티티를 저장하는 방식으로 구현할 수 있습니다.
        Inquiry inquiry = new Inquiry();
        inquiry.setId(inquiryForm.getId());
        inquiry.setType(inquiryForm.getType());
        inquiry.setPrivate(inquiryForm.isPrivate());
        inquiry.setInquiryTitle(inquiryForm.getInquiryTitle());
        inquiry.setInquiryContent(inquiryForm.getInquiryContent());

        inquiry.setRegTime(LocalDateTime.now()); // Current timestamp

        inquiryService.editInquiry(inquiry);

        // 수정된 상세 페이지로 리다이렉트
        return "redirect:/news/inquiry/show/" + inquiry.getId();
    }

    // 문의 답변 작성
    @PostMapping("/inquiry/respond")
    public String respondToInquiry(@RequestParam Long inquiryId, @RequestParam String responseContent) {
        // inquiryId를 사용하여 해당 문의를 찾아서 답변을 등록하고, responseContent를 저장합니다.
        Inquiry inquiry = inquiryService.findById(inquiryId);

        if (inquiry != null) {
            inquiry.setResponseContent(responseContent);
            inquiryService.save(inquiry);

            // 성공적으로 등록되었다면 답변 내용을 반환합니다.
            return "redirect:/news/inquiry/show/" + inquiry.getId();
        }

        // 실패했을 경우 null을 반환하거나 에러 메시지를 반환할 수 있습니다.
        return "답변 등록에 실패했습니다.";
    }

    private void populateAdminModel(Model model, Principal principal) {
        // 유저 로그인
        if (principal != null) {
            String user = principal.getName();
            MemberEntity userInfo = memberRepository.findByEmail(user);
            model.addAttribute("userInfo", userInfo);

            // 리뷰수
            List<ReviewEntity> review = reviewRepository.findByMemberEntityId(userInfo.getId());
            int reviewNum = review.size();
            model.addAttribute("reviewNum",reviewNum);

            // 문의수
            List<Inquiry> inquiryList = inquiryRepository.findByWriterId(userInfo.getId());
            int inquiryNum = inquiryList.size();
            model.addAttribute("inquiryNum", inquiryNum);

            // 쿠폰수
            List<Member_CouponEntity> couponList = memberCouponRepository.findByMemberEmail(user);
            int couponNum = couponList.size();
            model.addAttribute("couponNum", couponNum);

            // ADMIN 권한 확인
            Role admin = userInfo.getRole();
            if (admin.equals(Role.ADMIN)) {
                model.addAttribute("admin", admin);
            }

            // 포인트 히스토리
            List<PointHistoryEntity> historys = pointHistoryRepository.findByMemberPointEntity_MemberEntityId(userInfo.getId());
            model.addAttribute("historys", historys);

            // 총 포인트 합산
            int totalPoints = 0;
            int totalPointsUsed = 0;

            for (PointHistoryEntity history : historys) {
                if (history.getActionType() == ActionType.EARNED) {
                    totalPoints += history.getPointsEarned();
                } else if (history.getActionType() == ActionType.USED) {
                    totalPointsUsed += history.getPointsUsed();
                }
            }
            int total = totalPoints - totalPointsUsed;
            model.addAttribute("total", total);
        }
    }


}








