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
import miniprojectver.domain.PointPurchaseRequested;

@Entity
@Table(name = "point_request_management") // 테이블명은 일반적으로 스네이크 케이스로 작성
@Data // Lombok을 통해 getter, setter, toString, equals, hashCode 자동 생성
//<<< DDD / Aggregate Root
public class PointRequestManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pointRequestId; // 포인트 요청 ID (애그리게이트 루트 식별자)

    private String userId; // 사용자 ID

    private Long requestedPointAmount; // 요청된 포인트 양

    private String paymentMethodId; // 결제 수단 ID

    private BigDecimal actualPaymentAmount; // 실제 결제 금액

    private String status; // 요청 상태 (예: "PENDING", "COMPLETED", "FAILED")
                           // 이 상태는 '포인트 구매 요청' 자체의 최종 상태를 나타낼 수 있습니다.
                           // 즉, 이 요청은 성공적으로 처리(COMPLETED)되었는지, 실패(FAILED)했는지 등

    private Date timestamp; // 요청 생성 시점

    // JPA를 위한 기본 생성자 (protected로 선언하여 외부 직접 생성 제어)
    protected PointRequestManagement() {
    }

    // --- [1] 비즈니스 행위를 나타내는 팩토리 메서드 (Command: "포인트 구매 요청") ---
    // 이 메서드가 "포인트 구매 요청" Command를 처리하는 시작점입니다.
    public static PointRequestManagement requestPointPurchase(
        String userId,
        Long requestedPointAmount,
        String paymentMethodId,
        BigDecimal actualPaymentAmount
    ) {
        // [1-1] Command 유효성 검증
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        if (requestedPointAmount == null || requestedPointAmount <= 0) {
            throw new IllegalArgumentException("Requested point amount must be positive.");
        }
        if (paymentMethodId == null || paymentMethodId.isEmpty()) {
            throw new IllegalArgumentException("Payment method ID cannot be null or empty.");
        }
        if (actualPaymentAmount == null || actualPaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Actual payment amount must be positive.");
        }

        // [1-2] 애그리게이트 객체 생성 및 초기 상태 설정
        PointRequestManagement newRequest = new PointRequestManagement();
        newRequest.setUserId(userId);
        newRequest.setRequestedPointAmount(requestedPointAmount);
        newRequest.setPaymentMethodId(paymentMethodId);
        newRequest.setActualPaymentAmount(actualPaymentAmount);
        newRequest.setStatus("PENDING"); // 초기 상태는 '대기 중'으로 설정
        newRequest.setTimestamp(new Date()); // 요청 생성 시점 기록

        // 이벤트 발행은 @PostPersist 훅에서 처리하여 ID가 할당된 후 이루어지도록 합니다.
        return newRequest;
    }

    // --- [2] JPA 라이프사이클 훅: @PostPersist ---
    // 엔티티가 데이터베이스에 영속화(DB에 저장)된 직후 호출됩니다.
    // 이 시점에는 pointRequestId (ID)가 이미 할당되어 있습니다.
    @PostPersist
    public void onPostPersist() {
        System.out.println("PointRequestManagement: Running @PostPersist for ID: " + this.pointRequestId);
        // ID가 할당된 상태이므로, 이벤트에 모든 필요한 정보를 담아 발행할 수 있습니다.
        PointPurchaseRequested pointPurchaseRequested = new PointPurchaseRequested(this);
        pointPurchaseRequested.publishAfterCommit(); // <-- 이벤트 발행
    }

    // `repository()` 정적 메서드는 제거합니다.
    // 이전의 `approvePurchase()`와 `declinePurchase()` 메서드는 제거되었습니다.
    // 이 애그리게이트는 "포인트 구매 요청"의 생성을 담당하고,
    // 후속 처리(승인/거절, 실제 포인트 충전)는 PointManagement BC의 책임입니다.
}
//>>> DDD / Aggregate Root