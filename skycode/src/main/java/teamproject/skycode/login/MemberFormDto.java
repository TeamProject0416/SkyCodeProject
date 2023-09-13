package teamproject.skycode.login;


import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.modelmapper.ModelMapper;
import teamproject.skycode.event.EventFormDto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;


@Getter
@Setter
public class MemberFormDto {
    private Long id;

    @NotBlank(message = "이름을 입력해 주세요.")
    private String name;

    @NotBlank(message = "닉네임을 입력해 주세요.")
    private String nickName;

    @NotEmpty(message = "이메일은 필수 입력 값입니다")
    @Email(message = "이메일 형식으로 입력해주세요")
    private String email;

    @NotEmpty(message = "비밀번호는 필수 입력 값입니다")
    @Length(min = 8,max = 16,message = "비밀번호는 8자 이상, 16자 이하로 입력해주세요")
    private String password;

    @NotBlank(message = "주소를 입력해 주세요.")
    private String address;

    @NotBlank(message ="휴대전화를 입력해 주세요.")
    private String phoneNum;

    @NotBlank(message ="생일을 입력해 주세요.")
    private String birthday;

    private static ModelMapper modelMapper = new ModelMapper();

    private String userImgName; // 미니 이미지 파일명
    private String userOriImgName; // 미니 원본 이미지 파일명
    private String userImgUrl; // 미니 이미지 조회 경로

    public static MemberFormDto of(MemberEntity member) {
        return modelMapper.map(member, MemberFormDto.class);
    }
}
