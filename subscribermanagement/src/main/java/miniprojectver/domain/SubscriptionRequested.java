package miniprojectver.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import miniprojectver.domain.*;
import miniprojectver.infra.AbstractEvent;

//<<< DDD / Domain Event
@Getter // 이벤트 필드를 외부에서 읽을 수 있도록
@ToString // toString() 메서드 자동 생성
@NoArgsConstructor // 역직렬화 (JSON to Object)를 위한 기본 생성자
public class SubscriptionRequested extends AbstractEvent {

    private Long subscriptionId; // 구독 ID
    private String userId;       // 사용자 ID
    private Date startedAt;      // 구독 시작일
    private Date endsAt;         // 구독 종료일
    // private Date timestamp; // AbstractEvent에서 이미 이벤트 발생 시각을 관리하고 있다면 중복 제거

    // 애그리게이트 객체(SubscribeManagement)를 인자로 받아 필요한 스냅샷 정보를 추출하여 이벤트 필드를 초기화합니다.
    // 이 생성자는 애그리게이트의 현재 상태를 이벤트로 캡처하는 역할을 합니다.
    public SubscriptionRequested(SubscribeManagement aggregate) {
        // AbstractEvent의 생성자를 호출하여 이벤트의 기본적인 속성(예: 이벤트 발생 시각)을 초기화합니다.
        // `super(aggregate);`는 일부 프레임워크에서 Aggregate Root 자체를 이벤트에 등록하는 데 사용될 수 있지만,
        // 일반적으로는 이벤트가 Aggregate의 스냅샷을 포함하도록 명시적으로 필드를 복사하는 것이 더 명확합니다.
        // 여기서는 `AbstractEvent`가 `super()`로만 호출될 수 있다고 가정하고,
        // 이벤트 필드는 명시적으로 Aggregate에서 가져옵니다.
        super();

        this.subscriptionId = aggregate.getSubscriptionId();
        this.userId = aggregate.getUserId();
        this.startedAt = aggregate.getStartedAt();
        this.endsAt = aggregate.getEndsAt();
        // this.timestamp = aggregate.getTimestamp(); // Aggregate에 timestamp 필드가 있다면 가져올 수 있습니다.
                                                     // 하지만 이벤트 자체의 발생 시각은 AbstractEvent에서 관리하는 것이 일반적입니다.
    }

    // 기본 생성자는 @NoArgsConstructor로 Lombok이 자동 생성해 줍니다.
    // public SubscriptionRequested() {
    //     super();
    // }
}
//>>> DDD / Domain Event
