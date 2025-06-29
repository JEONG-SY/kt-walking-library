package miniprojectver.service; // 서비스는 도메인 패키지 밖에 위치하는 것이 일반적

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import miniprojectver.domain.BookPurchaseManagement;
import miniprojectver.domain.BookPurchaseManagementRepository;


@Service
@Transactional // 서비스 메서드 전체를 하나의 트랜잭션으로 묶습니다.
public class BookPurchaseService {

    @Autowired
    private BookPurchaseManagementRepository bookPurchaseManagementRepository;

    // --- [Command: "포인트 구매 요청" 처리 메서드] ---
    // 이 메서드는 외부(REST API 컨트롤러 등)로부터 Command를 받아 비즈니스 로직을 조율합니다.
    public BookPurchaseManagement requestBookPurchase(
        String userId,
        String bookId,
        BigDecimal price,
        BigDecimal point // 요청된 포인트는 BookPurchaseManagement의 속성으로 저장될 뿐
    ) {
        // [1] 애그리게이트 루트 생성 및 초기화
        // BookPurchaseManagement 내의 비즈니스 규칙 및 초기 상태 설정
        // 이벤트는 BookPurchaseManagement 내부에서 생성되고 publishAfterCommit()으로 준비됩니다.
        BookPurchaseManagement newPurchase = BookPurchaseManagement.requestBookPurchase(
            userId,
            bookId,
            price,
            point
        );

        // [2] 애그리게이트 루트를 영속화: Repository를 통해 DB에 저장
        // @Transactional 어노테이션에 의해 트랜잭션 커밋 시 @PostPersist 훅이 동작하고
        // BookPurchaseRequested 이벤트가 발행됩니다.
        bookPurchaseManagementRepository.save(newPurchase);

        // 중요: 이제 여기서 외부 PointService를 직접 호출할 필요가 없습니다.
        // 포인트 차감은 BookPurchaseRequested 이벤트가 Kafka를 통해 PointManagement 서비스로 전달된 후,
        // PointManagement 서비스 내에서 해당 이벤트를 수신하여 처리합니다.

        return newPurchase; // 생성된 애그리게이트 반환
    }

    // TODO: 필요하다면 다른 비즈니스 메서드 (예: 구매 취소, 상태 변경 등) 추가
    // public void cancelBookPurchase(Long purchaseRequestId) { ... }
}