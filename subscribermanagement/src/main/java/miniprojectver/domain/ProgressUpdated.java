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
public class ProgressUpdated extends AbstractEvent {

    private Long readingActivityId;
    private String userId;
    private String bookId;
    private Integer currentPage; // 업데이트된 현재 페이지
    private Date lastUpdatedAt;

    public ProgressUpdated(ReadingManagement aggregate) {
        super();
        this.readingActivityId = aggregate.getReadingActivityId();
        this.userId = aggregate.getUserId();
        this.bookId = aggregate.getBookId();
        this.currentPage = aggregate.getCurrentPage(); // 업데이트된 currentPage
        this.lastUpdatedAt = aggregate.getLastUpdatedAt();
    }
}
//>>> DDD / Domain Event
