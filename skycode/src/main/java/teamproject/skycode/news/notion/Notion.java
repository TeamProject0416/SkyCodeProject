package teamproject.skycode.news.notion;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@Table(name = "notion")
public class Notion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 공지글 id

    @Column(nullable = false)
    private String type;    // 공지글 종류

//    @Column
//    @NotBlank(message = "Notion title is required")
//    private String notionTitle;     // 공지글 제목
//
////    @NotNull(message = "Notion content is required")
////    private String notionContent;   // 공지글 내용
//    @NotBlank(message = "Notion content is required")
//    private String notionContent;
    @Column(nullable = false)
    @NotBlank(message = "Notion title is required")
    private String notionTitle;

//    @NotBlank(message = "Notion content is required")
    @Size(min = 0)
    private String notionContent;

    @CreationTimestamp
    @Column(name = "reg_time")
    private LocalDateTime regTime; // 공지사항 올린 시간

    public LocalDateTime getRegistrationTime() {
        return regTime;
    }

    public void setRegistrationTime(LocalDateTime registrationTime) {
        this.regTime = registrationTime;
    }

    @Column(name = "view_count", nullable = false, columnDefinition = "int default 0")
    private int countView;



    // 생성 시간을 현재 시간으로 설정
    @PrePersist
    protected void onCreate() {
        regTime = LocalDateTime.now();
    }



}
