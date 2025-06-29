package miniprojectver.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import miniprojectver.SubscribermanagementApplication;
import miniprojectver.domain.ProgressUpdated;
import miniprojectver.domain.ReadingCompleted;
import miniprojectver.domain.ReadingStarted;

@Entity
@Table(name = "reading_management") // 테이블명은 일반적으로 스네이크 케이스로 작성
@Getter // 모든 필드에 대한 getter만 제공 (외부에서 직접 필드 변경 불가)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자. 외부에서 직접 인스턴스 생성 방지.
// 애그리게이트의 식별자(ID)만을 기반으로 equals와 hashCode를 생성하여 JPA 프록시 문제 방지 및 DDD 원칙 준수
@EqualsAndHashCode(of = "readingActivityId", callSuper = false)
//<<< DDD / Aggregate Root
public class ReadingManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long readingActivityId; // 독서 활동 ID (애그리게이트 루트 식별자)

    private String userId; // 사용자 ID

    private String bookId; // 책 ID (어떤 책을 읽고 있는지)

    private Integer currentPage; // 현재 읽고 있는 페이지 (진행률)

    private String readingStatus; // 독서 상태 (예: "STARTED", "IN_PROGRESS", "COMPLETED", "PAUSED")

    private Date startedAt; // 독서 시작 시점

    private Date completedAt; // 독서 완료 시점

    private Date lastUpdatedAt; // 마지막 진행률 업데이트 시점

    // --- [1] 비즈니스 행위: 독서 시작 (Command) - 팩토리 메서드 ---
    // 새로운 독서 활동을 시작하고 초기 상태를 설정하는 유일한 방법
    public static ReadingManagement startReading(String userId, String bookId, Integer initialPage) {
        // [1-1] Command 유효성 검증
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        if (bookId == null || bookId.isEmpty()) {
            throw new IllegalArgumentException("Book ID cannot be null or empty.");
        }
        if (initialPage == null || initialPage < 1) { // 독서는 1페이지부터 시작한다고 가정
            throw new IllegalArgumentException("Initial page must be 1 or greater.");
        }

        // [1-2] 애그리게이트 객체 생성 및 초기 상태 설정
        ReadingManagement newReading = new ReadingManagement();
        newReading.userId = userId;
        newReading.bookId = bookId;
        newReading.currentPage = initialPage;
        newReading.readingStatus = "STARTED"; // 초기 독서 상태는 '시작됨'
        newReading.startedAt = new Date(); // 현재 시간으로 시작 시점 기록
        newReading.lastUpdatedAt = newReading.startedAt; // 시작 시점이 마지막 업데이트 시점

        // Note: ReadingStarted 이벤트는 @PostPersist 훅에서 발행됩니다.
        // 이는 readingActivityId가 생성된 후에 이벤트에 포함될 수 있도록 합니다.
        return newReading;
    }

    // --- [2] 비즈니스 행위: 독서 진행률 업데이트 (Command) ---
    public void updateProgress(Integer newPage) {
        // [2-1] 불변 조건 검사
        if ("COMPLETED".equals(this.readingStatus)) {
            throw new IllegalStateException("Cannot update progress for a completed reading activity.");
        }
        if (newPage == null || newPage <= this.currentPage) {
            throw new IllegalArgumentException("New page must be greater than current page.");
        }

        // [2-2] 상태 변경 (setter는 사용하지 않고 직접 필드 변경)
        this.currentPage = newPage;
        this.readingStatus = "IN_PROGRESS"; // 상태를 '진행 중'으로 변경
        this.lastUpdatedAt = new Date(); // 마지막 업데이트 시점 기록

        // [2-3] 도메인 이벤트 발행
        new ProgressUpdated(this).publishAfterCommit();
    }

    // --- [3] 비즈니스 행위: 독서 완료 (Command) ---
    public void completeReading(Integer finalPage) { // 일반적으로 finalPage는 책의 총 페이지 수와 같음
        // [3-1] 불변 조건 검사
        if ("COMPLETED".equals(this.readingStatus)) {
            throw new IllegalStateException("Reading activity is already completed.");
        }
        if (finalPage == null || finalPage < this.currentPage) {
            throw new IllegalArgumentException("Final page must be greater than or equal to current page.");
        }
        // TODO: 실제 책의 총 페이지 수와 finalPage를 비교하는 로직은 외부(예: BookManagement BC)에서
        // 이벤트(BookInfoQueried 또는 유사)를 통해 정보를 가져와 검증하거나,
        // 이 BC의 책임 범위를 넘어선다고 가정할 수 있습니다. 여기서는 단순히 페이지 수 증가만 검증.

        // [3-2] 상태 변경
        this.currentPage = finalPage; // 최종 페이지로 설정
        this.readingStatus = "COMPLETED"; // 상태를 '완료됨'으로 변경
        this.completedAt = new Date(); // 완료 시점 기록
        this.lastUpdatedAt = this.completedAt; // 완료 시점이 마지막 업데이트 시점

        // [3-3] 도메인 이벤트 발행
        new ReadingCompleted(this).publishAfterCommit();
    }

    // --- [JPA 라이프사이클 훅] ---
    @PostPersist
    public void onPostPersist() {
        System.out.println("ReadingManagement: Running @PostPersist for ID: " + this.readingActivityId);
        // 애그리게이트가 처음 생성되어 DB에 저장될 때만 'ReadingStarted' 이벤트를 발행합니다.
        ReadingStarted readingStarted = new ReadingStarted(this);
        readingStarted.publishAfterCommit();
        // `ProgressUpdated`와 `ReadingCompleted`는 여기에서 발행되지 않습니다.
        // 이는 각각 '진행률 업데이트'와 '독서 완료'라는 별도의 비즈니스 행위입니다.
    }
}
//>>> DDD / Aggregate Root
