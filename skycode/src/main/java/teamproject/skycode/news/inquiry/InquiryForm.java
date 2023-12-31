package teamproject.skycode.news.inquiry;

import lombok.*;
import teamproject.skycode.common.BaseEntity;
import teamproject.skycode.login.MemberEntity;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InquiryForm extends BaseEntity {
    private Long id;

    private String type;

    private String email;

    private boolean isPrivate;
//    private String inquiryTitle
    @NotBlank(message = "1 대 1 문의의 내용은 필수 입력값 입니다")
    private String inquiryContent;

    @NotBlank(message = "1 대 1 문의의 제목은 필수 입력값 입니다")
    private String inquiryTitle;

    private MemberEntity writer;

    private String nickName;


    public Inquiry toEntity() {
        Inquiry inquiry = new Inquiry();
        inquiry.setId(this.id);
        inquiry.setType(this.type);
        inquiry.setEmail(this.email);
        inquiry.setWriter(this.writer);
        inquiry.setNickName(this.nickName);
        inquiry.setIsPrivate(this.isPrivate);
        inquiry.setInquiryTitle(this.inquiryTitle);
        inquiry.setInquiryContent(this.inquiryContent);

        // regTime은 @PrePersist 어노테이션으로 자동 설정될 것이므로 따로 설정하지 않음
        return inquiry;
    }


}


