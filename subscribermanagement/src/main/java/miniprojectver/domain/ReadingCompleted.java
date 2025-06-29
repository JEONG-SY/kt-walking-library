package miniprojectver.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import miniprojectver.domain.*;
import miniprojectver.infra.AbstractEvent;

//<<< DDD / Domain Event
@Getter
@ToString
@NoArgsConstructor
public class ReadingCompleted extends AbstractEvent {

    private Long readingActivityId;
    private String userId;
    private String bookId;
    private Integer finalPage; // 독서 완료 시의 최종 페이지
    private Date completedAt;

    public ReadingCompleted(ReadingManagement aggregate) {
        super();
        this.readingActivityId = aggregate.getReadingActivityId();
        this.userId = aggregate.getUserId();
        this.bookId = aggregate.getBookId();
        this.finalPage = aggregate.getCurrentPage(); // 완료 시점의 currentPage가 finalPage
        this.completedAt = aggregate.getCompletedAt();
    }
}
//>>> DDD / Domain Event
