package teamproject.skycode.news.inquiry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@EnableJpaRepositories
public interface InquiryRepository extends JpaRepository<Inquiry, Long > {

    @Query("SELECT i FROM Inquiry i ORDER BY i.regTime DESC")
    List<Inquiry> findAllOrderByRegistrationTimeDesc();

    List<Inquiry> findAllByOrderByViewCountDesc();

    List<Inquiry> findByInquiryTitleContaining(String searchValue);

    List<Inquiry> findByInquiryContentContaining(String searchValue);

    List<Inquiry> findByIdContaining(String searchValue);

    Page<Inquiry> findAll(Pageable pageable);

    List<Inquiry> findByWriterId(Long memberId);


//    List<Inquiry> findByUserNicknameContaining(String searchValue);

//    List<Inquiry> findByHashtagsContaining(String searchValue);





}
