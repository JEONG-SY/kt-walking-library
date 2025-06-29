package miniprojectver.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import lombok.*;
import miniprojectver.domain.*;
import miniprojectver.infra.AbstractEvent;

//<<< DDD / Domain Event
@Getter // 이벤트 필드를 외부에서 읽을 수 있도록
@ToString
@NoArgsConstructor // 역직렬화를 위한 기본 생성자
public class BookPurchaseRequested extends AbstractEvent {

    private Long purchaseRequestId; // 구매 요청 ID (ID는 @PostPersist 시점에 할당됨)
    private String userId; // 사용자 ID
    private String bookId; // 책 ID
    private BigDecimal price; // 가격
    private BigDecimal point; // 포인트
    // private Instant timestamp; // AbstractEvent에서 이미 관리하고 있다면 중복 제거

    // 애그리게이트 객체를 인자로 받아 필요한 스냅샷 정보를 추출하여 이벤트 필드를 초기화합니다.
    public BookPurchaseRequested(BookPurchaseManagement aggregate) {
        super(); // AbstractEvent의 생성자 호출 (여기서 이벤트 발생 시간 등을 설정)
        this.purchaseRequestId = aggregate.getPurchaseRequestId();
        this.userId = aggregate.getUserId();
        this.bookId = aggregate.getBookId();
        this.price = aggregate.getPrice();
        this.point = aggregate.getPoint();
        // this.timestamp = Instant.now(); // 만약 AbstractEvent에서 관리하지 않는다면 여기에 추가
    }
}
//>>> DDD / Domain Event
