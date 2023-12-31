package teamproject.skycode.review;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import teamproject.skycode.constant.ActionType;
import teamproject.skycode.constant.Role;
import teamproject.skycode.coupon.Member_CouponEntity;
import teamproject.skycode.coupon.Member_CouponRepository;
import teamproject.skycode.event.EventFormDto;
import teamproject.skycode.login.MemberEntity;
import teamproject.skycode.login.MemberRepository;
import teamproject.skycode.news.inquiry.Inquiry;
import teamproject.skycode.news.inquiry.InquiryRepository;
import teamproject.skycode.point.PointHistoryEntity;
import teamproject.skycode.point.PointHistoryRepository;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("review")
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final CommentService commentService;
    private final MemberRepository memberRepository;
    private final InquiryRepository inquiryRepository;
    private final Member_CouponRepository memberCouponRepository;
    private final PointHistoryRepository pointHistoryRepository;


    @GetMapping(value = "/newReview")
    public String newReviewForm(Model model, Principal principal) {

        // 유저 로그인 모달 함수
        populateAdminModel(model, principal);

        model.addAttribute("reviewFormDto", new ReviewDto());
        return "review/newReview";
    }

    //  리뷰 생성 (Post)
    @PostMapping(value = "/create")
    public String createReview(@Valid ReviewDto reviewDto, BindingResult bindingResult,
                               Model model, Principal principal,
                               @RequestParam("reviewImgFile1") MultipartFile reviewImgFile1,
                               @RequestParam("reviewImgFile2") MultipartFile reviewImgFile2) {
        if (bindingResult.hasErrors()) {
            return "review/newReview";
        }
        try {
            // 유저 로그인
            if (principal != null) {
                String user = principal.getName();
                MemberEntity memberEntity = memberRepository.findByEmail(user);
                reviewDto.setEmail(user);
                reviewDto.setMemberId(memberEntity.getId());
                reviewDto.setNickName(memberEntity.getNickName());
            }

            reviewService.saveReview(reviewDto, reviewImgFile1, reviewImgFile2);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "이벤트 등록 중 에러가 발생하였습니다");
            return "review/newReview";
        }
        return "redirect:/review/reviewSub";
    }

    @GetMapping(value = "/reviewSub")
    public String reviewSub(@RequestParam(value = "page", defaultValue = "0") int page,
                            Principal principal, Model model) {

        // 유저 로그인 모달 함수
        populateAdminModel(model, principal);

        Page<ReviewEntity> paging = this.reviewService.getList(page);
        List<ReviewEntity> bestReviews = reviewService.getTop3BestReviews();

        model.addAttribute("bestReviews", bestReviews);
        model.addAttribute("paging", paging);
        model.addAttribute("maxPage", 5);

        return "review/reviewSub";
    }


    //    리뷰 디테일 보여주기
    @GetMapping(value = "/{reviewId}")
    public String reviewDetail(@PathVariable("reviewId") Long reviewId,
                               Principal principal, Model model) {
        // 유저 로그인
        if (principal != null) {
            String user = principal.getName();
            MemberEntity userInfo = memberRepository.findByEmail(user);
            String nickname = userInfo.getNickName();
            model.addAttribute("nickname", nickname);
        }
        // 유저 로그인 모달 함수
        populateAdminModel(model, principal);

        /*
            해당 게시글의 조회수를 하나 올리고
            게시글 데이터를 가져와서 reviewShow.html에 출력
        */
        reviewService.updateHits(reviewId);

        ReviewDto reviewDto = reviewService.getReviewDtl(reviewId);
        List<CommentDTO> commentDTOList = commentService.findAll(reviewId);
        model.addAttribute("commentList", commentDTOList);
        model.addAttribute("reviewDto", reviewDto);

        return "review/reviewShow";
    }


    //    리뷰 수정하기
    @GetMapping(value = "/{reviewId}/edit")
    public String reviewEdit(@PathVariable("reviewId") Long reviewId,
                             Model model, Principal principal) {
        // 유저 로그인 모달 함수
        populateAdminModel(model, principal);

        ReviewDto reviewDto = reviewService.getReviewDtl(reviewId);
        model.addAttribute("reviewFormDto", reviewDto);
        return "review/newReview";
    }


    //    리뷰 업데이트
    @PostMapping(value = "/update")
    public String reviewUpdate(@Valid ReviewDto reviewDto, BindingResult bindingResult,
                               @RequestParam("reviewImgFile1") MultipartFile reviewImgFile1,
                               @RequestParam("reviewImgFile2") MultipartFile reviewImgFile2,
                               Model model, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "review/newReview";
        }
        try {

            // 유저 로그인
            if (principal != null) {
                String user = principal.getName();
                MemberEntity memberEntity = memberRepository.findByEmail(user);
                reviewDto.setEmail(user);
                reviewDto.setMemberId(memberEntity.getId());
                reviewDto.setNickName(memberEntity.getNickName());
            }

            reviewService.updateReview(reviewDto, reviewImgFile1, reviewImgFile2);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "이벤트 등록 중 에러가 발생하였습니다");
            return "review/newReview";
        }
        return "redirect:/review/reviewSub";
    }

    //    리뷰 삭제
    @GetMapping("/{reviewId}/delete")
    public String deleteEvent(@PathVariable("reviewId") Long reviewId) {
        // 리뷰 삭제 로직을 구현
        reviewService.deleteReview(reviewId);

        // 삭제 후 리다이렉션할 URL을 반환
        return "redirect:/review/reviewSub";
    }


    @PostMapping("/search")
    public String searchReviews(
            @RequestParam("search-type") String searchType,
            @RequestParam("search-value") String searchValue,
            Model model
    ) {
        List<ReviewEntity> searchResults;

        // 검색 유형이 "reviewTitle" 또는 "contents"일 때만 검색을 수행하도록 변경
        if ("reviewTitle".equals(searchType) || "contents".equals(searchType)) {
            try {
                if ("reviewTitle".equals(searchType)) {
                    // 검색 유형이 "reviewTitle"인 경우 제목으로 검색
                    searchResults = reviewService.searchReviews(searchType, searchValue);
                    System.out.println("service=" + searchResults);
                } else {
                    // 검색 유형이 "contents"인 경우 내용으로 검색
                    searchResults = reviewService.searchReviews(searchType, searchValue);
                }
                model.addAttribute("searchResults", searchResults);
                return "review/reviewSearch";
            } catch (IllegalArgumentException ex) {
                // 예외 발생 시 리다이렉트 및 예외 메시지 전달
                return "redirect:/reviewSub?error=" + ex.getMessage();
            }
        } else {
            // 검색 유형이 잘못된 경우 리다이렉트 및 오류 메시지 전달
            return "redirect:/review/reviewSub?error=잘못된 검색 유형입니다. '제목' 또는 '내용'을 선택해주세요";
        }
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
            model.addAttribute("reviewNum", reviewNum);

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

