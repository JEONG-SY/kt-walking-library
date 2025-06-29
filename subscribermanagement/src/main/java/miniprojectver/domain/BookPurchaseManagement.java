package miniprojectver.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.Data;
import miniprojectver.SubscribermanagementApplication;
import miniprojectver.domain.BookPurchaseRequested;

@Entity
@Table(name = "book_purchase_management") // 테이블명은 일반적으로 스네이크 케이스로 작성
@Data // Lombok을 통해 getter, setter 등 자동 생성
//<<< DDD / Aggregate Root
public class BookPurchaseManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long purchaseRequestId; // 구매 요청 ID (애그리게이트 루트 식별자)

    private String userId; // 사용자 ID

    private String bookId; // 책 ID

    private BigDecimal price; // 가격

    private BigDecimal point; // 포인트 (구매 요청 시 사용될 포인트)

    // JPA를 위한 기본 생성자 (private 또는 protected로 선언하여 직접적인 외부 인스턴스 생성을 막을 수 있음)
    protected BookPurchaseManagement() {
    }

    // --- [1] 비즈니스 행위를 나타내는 팩토리 메서드 (Command: "포인트 구매 요청") ---
    // 이 메서드가 "포인트 구매 요청"이라는 Command를 처리하는 시작점입니다.
    // Order 예시처럼 외부에서 Order 객체를 생성하듯이, 여기서는 BookPurchaseManagement 객체를 생성합니다.
    public static BookPurchaseManagement requestBookPurchase(
        String userId,
        String bookId,
        BigDecimal price,
        BigDecimal point
    ) {
        // [1-1] Command 유효성 검증 (애그리게이트 생성 전 검증)
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive.");
        }
        if (point == null || point.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Point must be non-negative.");
        }
        // TODO: 추가적인 Command 유효성 검사 (예: bookId 형식, userId 형식 등)

        // [1-2] 애그리게이트 객체 생성 및 초기 상태 설정
        BookPurchaseManagement newPurchase = new BookPurchaseManagement();
        // ID는 DB 저장 시 자동 생성되므로, 여기서는 설정하지 않습니다.
        newPurchase.setUserId(userId);
        newPurchase.setBookId(bookId);
        newPurchase.setPrice(price);
        newPurchase.setPoint(point);

        // 이벤트 발행은 @PostPersist 훅에서 처리하여 ID가 할당된 후 이루어지도록 합니다.
        // 또는, Spring Data JPA의 DomainEventsPublisher를 사용하여 비즈니스 메서드 내에서 이벤트를 등록할 수도 있습니다.

        return newPurchase; // 생성된 애그리게이트 인스턴스 반환
    }

    // --- [2] JPA 라이프사이클 훅: @PostPersist (Order 예시의 onPostPersist와 동일) ---
    // 엔티티가 데이터베이스에 영속화(DB에 저장)된 직후 호출됩니다.
    // 이 시점에는 purchaseRequestId (ID)가 이미 할당되어 있습니다.
    @PostPersist
    public void onPostPersist() {
        System.out.println("BookPurchaseManagement: Running @PostPersist for ID: " + this.purchaseRequestId);
        // ID가 할당된 상태이므로, 이벤트에 모든 필요한 정보를 담아 발행할 수 있습니다.
        BookPurchaseRequested bookPurchaseRequested = new BookPurchaseRequested(this); // aggregate의 스냅샷을 이벤트에 담음
        bookPurchaseRequested.publishAfterCommit(); // <-- 이벤트 발행
    }

    // `repository()` 정적 메서드는 제거합니다.
    // 리포지토리는 애플리케이션 서비스/커맨드 핸들러에서 주입받아 사용해야 합니다.
    // 모노리스가 아닌 마이크로서비스에서는 컨텍스트 간의 직접적인 Bean 접근은 지양됩니다.
}
//>>> DDD / Aggregate Root