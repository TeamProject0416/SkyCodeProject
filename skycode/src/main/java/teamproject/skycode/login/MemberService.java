package teamproject.skycode.login;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import teamproject.skycode.common.FileService;
import teamproject.skycode.constant.ActionType;
import teamproject.skycode.myPage.users.MemberEditFormDto;
import teamproject.skycode.point.*;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;
    private final Member_PointRepository memberPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final FileService fileService;

    @Value("${imgLocation}")
    private String imgLocation;

    public MemberEntity saveMember(MemberEntity member) {
        validateDuplicateMember(member);
        DuplicateNicknameMember(member);

        // 기존에 저장된 "회원가입 보너스 포인트"를 가져오기
        PointEntity point = pointRepository.findByPointName("회원가입을 축하합니다");

        if (point == null) {
            // 기존에 저장된 포인트가 없다면 새로운 포인트 엔티티 생성
            point = new PointEntity();
            point.setPointName("회원가입을 축하합니다");
            point.setPointNum(1000); // 포인트 지급
            point = pointRepository.save(point); // 새로운 포인트를 저장하고 ID를 얻음
        }

        pointRepository.save(point);

        // 사용자와 포인트 간의 관계를 맺어줌
        Member_PointEntity memberPoint = new Member_PointEntity();
        memberPoint.setMemberEntity(member); // 사용자 정보
        memberPoint.setMemberEmail(member.getEmail());
        memberPoint.setPoint(1000);
        memberPoint.setPointEntity(point); // 포인트 정보
        memberPointRepository.save(memberPoint);

        // 포인트 지급 이력 생성
        PointHistoryEntity earnedHistory = new PointHistoryEntity();
        earnedHistory.setMemberPointEntity(memberPoint);
        earnedHistory.setPointsEarned(1000); // 지급한 포인트 양 설정
        earnedHistory.setUsageDate(new Date()); // 지급 일자 설정
        earnedHistory.setPointName(point.getPointName()); // 포인트 이름 저장
        earnedHistory.setActionType(ActionType.EARNED); // 포인트 지급 이력인 경우 설정

        pointHistoryRepository.save(earnedHistory); // 포인트 지급 이력 저장

        return memberRepository.save(member);
    }

    private void validateDuplicateMember(MemberEntity member) {
        MemberEntity findEmail = memberRepository.findByEmail(member.getEmail());
        if (findEmail != null) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    private void DuplicateNicknameMember(MemberEntity member) {
        MemberEntity findNickname = memberRepository.findByNickName(member.getNickName());
        if (findNickname != null) {
            throw new IllegalStateException("이미 사용중인 닉네임입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        MemberEntity member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new UsernameNotFoundException(email);
        }
        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }

    public MemberEditFormDto getMemberDtl(Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(EntityNotFoundException::new);
        // member 정보를 memberEditFormDto 로 변환합니다
        MemberEditFormDto memberEditFormDto = MemberEditFormDto.of(member);
        return memberEditFormDto;
    }

    public Long updateUser(MemberEditFormDto memberEditFormDto, MultipartFile userImgFile) throws Exception {
        // 유저 정보 수정
        MemberEntity member = memberRepository.findById(memberEditFormDto.getId())
                .orElseThrow(EntityNotFoundException::new);
        ;

        // 파일 경로 설정
        String basePath = "/user";

        // 폴더 없을시 폴더 생성
        Path directoryPath = Paths.get("/SkyCodeProject/img/user");

        try {
            // 디렉토리 생성
            Files.createDirectories(directoryPath);
            System.out.println(directoryPath + " 디렉토리가 생성되었습니다.");

        } catch (IOException e) {
            System.out.println(directoryPath + " 디렉토리 생성이 실패하였습니다.");
            e.printStackTrace();
        }

        // 이미지 업로드 처리 및 파일명 생성
        String userImgName = member.getUserImgName();
        String userOriImgName = member.getUserOriImgName();
        String userImgUrl = member.getUserImgUrl();

        if (userImgFile != null && !userImgFile.isEmpty()) {
            // 수정 전 파일 삭제하기
            if (member.getUserImgName() != null) {
                String filePath = imgLocation + basePath + "/" + userImgName;
                File fileToDelete = new File(filePath);
                if (fileToDelete.delete()) {
                    System.out.println("파일이 성공적으로 삭제되었습니다.");
                } else {
                    System.err.println("파일 삭제 중 오류가 발생했습니다.");
                }
            }

            // 파일명 가져오기
            userOriImgName = userImgFile.getOriginalFilename();

            // 파일명 생성
            userImgName = fileService.uploadFile(imgLocation + basePath, userOriImgName,
                    userImgFile.getBytes());

            // 파일 경로 생성
            userImgUrl = "/img/user/" + userImgName;
        }

        // DB 업데이트 시간 저장
        LocalDateTime now = LocalDateTime.now();
        member.setUpdateTime(now);

        memberEditFormDto.setUserImgName(userImgName);
        memberEditFormDto.setUserOriImgName(userOriImgName);
        memberEditFormDto.setUserImgUrl(userImgUrl);

        member.updateUser(memberEditFormDto);

        return member.getId();
    }


    public String getUserRole(String name) {
        if (name.equals("admin")) {
            return "ADMIN";
        } else {
            return "USER";
        }
    }
}













