package teamproject.skycode.ticket;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import teamproject.skycode.constant.TicketCountry;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping(value = {"/list", "/list/{page}"}) // 진행 페이지
    public String skyTicket(@PathVariable(name = "page", required = false) Integer page, Model model) {
        int pageSize = 3; // 페이지당 표시할 이벤트 수
        Pageable pageable = PageRequest.of(page != null ? page : 0, pageSize, Sort.by("id").descending());

        // EventStatus.ONGOING 값을 사용하여 데이터 조회
        Page<TicketEntity> ticketPage = ticketService.getTicketPage(pageable);

        model.addAttribute("tickets", ticketPage); // Page 객체를 그대로 넘김
        model.addAttribute("maxPage", 5); // 페이지당 표시할 최대 페이지 수

        return "/ticket/ticketList";
    }

    @GetMapping(value = "/new") // 티켓 등록
    public String newTicketForm(Model model) {
        model.addAttribute("ticketFormDto", new TicketFormDto());
        return "ticket/ticketForm";
    }

    @PostMapping(value = "/new") // 티켓 등록
    public String createTicket(@Valid TicketFormDto ticketFormDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "ticket/ticketForm";
        }
        try {
            ticketService.ticketSave(ticketFormDto);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "티켓 등록 중 에러가 발생하였습니다");
            return "ticket/ticketForm";
        }
        return "redirect:/ticket/list";
    }

    @GetMapping(value = "/{ticketId}/edit") // 티켓 수정폼
    public String ticketEdit(@PathVariable("ticketId") Long ticketId, Model model) {
        TicketFormDto ticketFormDto = ticketService.getTicketDtl(ticketId);
        model.addAttribute("ticketFormDto", ticketFormDto);
        return "ticket/ticketForm";
    }

    @PostMapping(value = "/update") // 티켓 수정
    public String ticketUpdate(@Valid TicketFormDto ticketFormDto, BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "ticket/ticketForm";
        }
        try {
            ticketService.updateTicket(ticketFormDto);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "이벤트 등록 중 에러가 발생하였습니다");
            return "ticket/ticketForm";
        }
        return "redirect:/ticket/list";
    }

    @GetMapping("/{ticketId}/delete") // 티켓 삭제
    public String deleteTicket(@PathVariable("ticketId") Long ticketId) {
        // 이벤트 삭제 로직을 구현
        ticketService.deleteTicket(ticketId);

        // 삭제 후 리다이렉션할 URL을 반환
        return "redirect:/ticket/list";
    }

    @PostMapping(value = "/ticketSearch")
    public String searchTicket(
            @RequestParam(name = "startValue") TicketCountry startValue,
            @RequestParam(name = "arriveValue") TicketCountry arriveValue,
            @RequestParam(name = "startDate") String startDate,
            @RequestParam(name = "endDate") String endDate,
            @RequestParam(name = "adultNum") int adultNum,
            @RequestParam(name = "teenagerNum") int teenagerNum,
            @RequestParam(name = "childNum") int childNum,
            @RequestParam(name = "seatGrade") String seatGrade,
            TicketFormDto ticketFormDto,
            Model model) {

        // 받아온 데이터를 사용하거나 처리하는 로직을 추가합니다.
        System.err.println("출발지: " + startValue);
        System.err.println("도착지: " + arriveValue);
        System.err.println("출국일: " + startDate);
        System.err.println("귀국일: " + endDate);
        System.err.println("성인: " + adultNum);
        System.err.println("청소년: " + teenagerNum);
        System.err.println("유아: " + childNum);
        System.err.println("좌석 등급: " + seatGrade);
        Integer totalNum = adultNum + teenagerNum + childNum;

        String userGrade = seatGrade;


        // 모델에 데이터를 추가하고, 결과 페이지로 이동합니다.
        List<TicketEntity> resultGoingList = ticketService.ticketGoinhEntityList(startValue, arriveValue, startDate);
        List<TicketEntity> resultComingList = ticketService.ticketComingEntityList(arriveValue, startValue, endDate);

        switch (userGrade) {
            case "economyClass":
                userGrade = "이코노미석";
                break;
            case "businessClass":
                userGrade = "비즈니스석";
                break;
            case "firstClass":
                userGrade = "일등석";
                break;
            default:
                // 기본 동작 (예외 처리 등)을 추가할 수 있습니다.
                break;
        }
        model.addAttribute("userSeatGrade",userGrade);
        model.addAttribute("goingTickets", resultGoingList); // 가는 편 리스트
        model.addAttribute("comingTickets", resultComingList); // 오는 편 리스트
        return "ticket/ticketSearch"; // 결과를 보여줄 뷰 페이지의 경로를 반환
    }


}
