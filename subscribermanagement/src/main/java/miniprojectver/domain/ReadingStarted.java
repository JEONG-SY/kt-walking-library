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
public class ReadingStarted extends AbstractEvent {

    private Long readingActivityId;
    private String userId;
    private String bookId;
    private Integer initialPage; // 독서 시작 페이지 (보통 1)
    private Date startedAt;

    public ReadingStarted(ReadingManagement aggregate) {
        super();
        this.readingActivityId = aggregate.getReadingActivityId();
        this.userId = aggregate.getUserId();
        this.bookId = aggregate.getBookId();
        this.initialPage = aggregate.getCurrentPage(); // 시작 시점의 currentPage가 initialPage
        this.startedAt = aggregate.getStartedAt();
    }
}
//>>> DDD / Domain Event
