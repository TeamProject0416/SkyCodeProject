package teamproject.skycode.login;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;
import teamproject.skycode.common.BaseEntity;
import teamproject.skycode.constant.Role;
import teamproject.skycode.review.CommentEntity;
import teamproject.skycode.review.ReviewEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "member")
public class MemberEntity extends BaseEntity {

    @Id
    @Column(name = "Member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "이름을 입력해 주세요.")
    private String name;

    @Column(unique = true)  // unique 제약 조건 추가
    private String nickName;

    @Column(unique = true)  // unique 제약 조건 추가
    private String email;

    @Column
    private String password;

//    @Column
//    private String passwordCheck;

    @Column
    private String address;

    @Column
    private String phoneNum;

    @Column
    private String birthday;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "memberEntity", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReviewEntity> reviewEntityList = new ArrayList<>();

    public static MemberEntity createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder) {
        MemberEntity member = new MemberEntity();
        member.setName(memberFormDto.getName());
        member.setNickName(memberFormDto.getNickName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());
        member.setPhoneNum(memberFormDto.getPhoneNum());
        member.setBirthday(memberFormDto.getBirthday());
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        member.setRole(Role.ADMIN);
        return member;
    }
}

