package teamproject.skycode.review;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//@RequestMapping("/review")
@Controller
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    @GetMapping(value = "/reviewSub")
    public String reviewSub(Model model) {
        List<Review> reviewEntityList = reviewRepository.findAll();
        model.addAttribute("reviews", reviewEntityList);
        return "review/reviewSub";
    }


    @PostMapping(value = "/review/create")
    public String createReview(ReviewFormDto form) {
//        Review review = new Review();
//        review.setReviewTitle(form.getReviewTitle());
//        review.setNickName(form.getNickName());
//        review.setBody(form.getBody());
        Review review = form.toEntity();
        Review saved = reviewRepository.save(review);
        return "redirect:/review/" + saved.getId();
    }

    @GetMapping(value = "/review/{id}")
    public String userReview(@PathVariable Long id, Model model) {
        Review reviewEntity = reviewRepository.findById(id).orElse(null);
        model.addAttribute("reviews", reviewEntity);
        reviewEntity.setRegTime(LocalDateTime.now());
        return "review/reviewSub";
    }





    //    임시
    @GetMapping(value = "/newReview")
    public String newReviewForm() {
        return "review/newReview";
    }

    @GetMapping(value = "/reviewShow")
    public String reviewShow() {
        return "review/reviewShow";
    }
}

