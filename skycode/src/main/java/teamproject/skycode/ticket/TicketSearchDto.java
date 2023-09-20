package teamproject.skycode.ticket;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketSearchDto {
//    private String start;
    private String ticketStart;
//    private String arrive;
    private String ticketEnd;
//    private LocalDate date;
    private String ticketStartDate;
    private String ticketEndDate;
    private int numberOfPeople;
    private String seatGrade;
}
