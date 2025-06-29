package miniprojectver.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor; // JPA를 위한 기본 생성자 (protected 접근 제어자)
import lombok.Getter;       // 필드에 대한 getter만 자동 생성
import lombok.AccessLevel; // NoArgsConstructor의 접근 제어자 설정을 위함
import lombok.EqualsAndHashCode; // equals/hashCode 명시적 제어 (subscriptionId만 사용)
import miniprojectver.SubscribermanagementApplication;
import miniprojectver.domain.SubscriptionCancelled;
import miniprojectver.domain.SubscriptionRequested;

@Entity
@Table(name = "subscribe_management")
@Getter // 모든 필드에 대한 getter만 제공 (외부에서 직접 필드 변경 불가)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자. 외부에서 직접 인스턴스 생성 방지.
// 애그리게이트의 식별자(ID)만을 기반으로 equals와 hashCode를 생성하여 JPA 프록시 문제 방지 및 DDD 원칙 준수
@EqualsAndHashCode(of = "subscriptionId", callSuper = false)
//<<< DDD / Aggregate Root
public class SubscribeManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long subscriptionId; // 구독 ID (애그리게이트 루트 식별자)

    private String userId; // 사용자 ID

    private String status; // 구독 상태 (예: "PENDING_REQUEST", "ACTIVE", "CANCELLED")

    private Date startedAt; // 구독 시작일

    private Date endsAt; // 구독 종료일

    // private Date lastRenewalAt; // 요청하신 범위에 없으므로 제거

    // --- [1] 비즈니스 행위: 구독 요청 (Command) - 팩토리 메서드 ---
    // 새로운 구독을 생성하고 초기 상태를 설정하는 유일한 방법
    public static SubscribeManagement requestSubscription(String userId, Date startedAt, Date endsAt) {
        // [1-1] 불변 조건/Command 유효성 검증
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        if (startedAt == null) {
            throw new IllegalArgumentException("Started At date cannot be null.");
        }
        if (endsAt == null || endsAt.before(startedAt)) {
            throw new IllegalArgumentException("Ends At date must be after Started At date.");
        }

        // [1-2] 애그리게이트 객체 생성 및 초기 상태 설정
        SubscribeManagement newSubscription = new SubscribeManagement();
        // ID는 DB에 의해 생성되므로, 여기서 설정하지 않습니다.
        newSubscription.userId = userId;
        newSubscription.startedAt = startedAt;
        newSubscription.endsAt = endsAt;
        newSubscription.status = "PENDING_REQUEST"; // 초기 상태는 '요청 대기 중'

        // Note: SubscriptionRequested 이벤트는 @PostPersist 훅에서 발행됩니다.
        // 이는 subscriptionId가 생성된 후에 이벤트에 포함될 수 있도록 합니다.
        return newSubscription;
    }

    // --- [2] 비즈니스 행위: 구독 취소 (Command) ---
    public void cancelSubscription() {
        // [2-1] 불변 조건 검사
        if (!"ACTIVE".equals(this.status) && !"PENDING_REQUEST".equals(this.status)) {
            throw new IllegalStateException("Subscription can only be cancelled from ACTIVE or PENDING_REQUEST status. Current: " + this.status);
        }
        if ("CANCELLED".equals(this.status)) { // 이미 취소된 경우
            throw new IllegalStateException("Subscription is already cancelled.");
        }

        // [2-2] 상태 변경 (setter는 사용하지 않고 직접 필드 변경)
        this.status = "CANCELLED";
        // [2-3] 도메인 이벤트 발행
        new SubscriptionCancelled(this).publishAfterCommit();
    }

    // --- [JPA 라이프사이클 훅] ---
    @PostPersist
    public void onPostPersist() {
        System.out.println("SubscribeManagement: Running @PostPersist for ID: " + this.subscriptionId);
        // 애그리게이트가 처음 생성되어 DB에 저장될 때만 'SubscriptionRequested' 이벤트를 발행합니다.
        // 이 이벤트는 구독 요청이 접수되었음을 알립니다.
        SubscriptionRequested subscriptionRequested = new SubscriptionRequested(this);
        subscriptionRequested.publishAfterCommit();
        // `SubscriptionCancelled`는 여기에서 발행되지 않습니다. 이는 '취소'라는 별도의 비즈니스 행위입니다.
    }
}
//>>> DDD / Aggregate Root
