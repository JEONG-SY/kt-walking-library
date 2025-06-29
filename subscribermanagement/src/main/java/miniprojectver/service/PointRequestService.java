package miniprojectver.service;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import miniprojectver.domain.PointRequestManagement;
import miniprojectver.domain.PointRequestManagementRepository;
// import miniprojectver.external.PaymentService; // 외부 결제 서비스가 있다면 여기에 주입

@Service
@Transactional
public class PointRequestService {

    @Autowired
    private PointRequestManagementRepository pointRequestManagementRepository;

    // @Autowired // TODO: 외부 PaymentService가 있다면 여기에 주입
    // private PaymentService paymentService;

    // --- [Command: "포인트 구매 요청" 처리 메서드] ---
    public PointRequestManagement requestPointPurchase(
        String userId,
        Long requestedPointAmount,
        String paymentMethodId,
        BigDecimal actualPaymentAmount
    ) {
        // [1] 외부 결제 시스템 연동 (이 서비스의 책임)
        // 이 부분에서 실제 결제 시스템(예: PG사)과의 연동 로직이 들어갈 수 있습니다.
        // 결제 성공 여부에 따라 예외를 던져 트랜잭션을 롤백할 수 있습니다.
        try {
            // TODO: 실제 결제 서비스 호출 (예: paymentService.processPayment(actualPaymentAmount, paymentMethodId, userId);)
            System.out.println("PointRequestService: Attempting to process payment for user " + userId + " with amount " + actualPaymentAmount);
            // 가상 결제 성공/실패 로직 (실제로는 PaymentService 호출)
            // if (actualPaymentAmount.compareTo(BigDecimal.valueOf(100)) < 0) {
            //    throw new RuntimeException("Payment amount too low for processing.");
            // }

        } catch (Exception e) {
            // 결제 실패 시 예외 처리
            throw new RuntimeException("Payment processing failed for point request: " + e.getMessage(), e);
        }

        // [2] 애그리게이트 루트 생성 및 초기화
        PointRequestManagement newRequest = PointRequestManagement.requestPointPurchase(
            userId,
            requestedPointAmount,
            paymentMethodId,
            actualPaymentAmount
        );

        // [3] 애그리게이트 루트를 영속화
        pointRequestManagementRepository.save(newRequest);

        // 중요: `PointRequestService`는 `PointPurchaseRequested` 이벤트를 발행하는 것으로 자신의 역할을 마칩니다.
        // 이 이벤트는 Kafka를 통해 다른 바운디드 컨텍스트(예: PointManagement BC)로 전달되어
        // 실제 포인트 충전 및 구매 요청의 최종 승인/거절 로직을 수행합니다.

        return newRequest;
    }
}