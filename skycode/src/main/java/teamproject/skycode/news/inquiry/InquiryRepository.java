package teamproject.skycode.news.inquiry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

}
