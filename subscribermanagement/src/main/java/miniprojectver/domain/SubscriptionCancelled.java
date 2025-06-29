package miniprojectver.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import miniprojectver.domain.*;
import miniprojectver.infra.AbstractEvent;

//<<< DDD / Domain Event
@Getter // 외부에서 이벤트 필드를 읽을 수 있도록 하지만, 쓰기(수정)는 불가능하게 함
@ToString // 로깅/디버깅에 유용한 문자열 표현 제공
@NoArgsConstructor // 객체 인스턴스화를 위해 JPA/Jackson과 같은 프레임워크에서 요구하는 기본 생성자
public class SubscriptionCancelled extends AbstractEvent {

    private Long subscriptionId; // 취소된 구독의 ID
    private String userId;       // 구독과 관련된 사용자 ID
    private String status;       // 구독의 현재 상태 (예: "CANCELLED")
    private Date cancelledAt;    // 구독이 취소된 특정 날짜 및 시간
    // private Date timestamp; // 만약 AbstractEvent에서 이미 이벤트 발생 시각을 처리하고 있다면 중복될 수 있습니다.
                               // 일반적으로 이벤트 자체의 발생 시각(AbstractEvent에서 오는)만으로 충분합니다.

    /**
     * 애그리게이트로부터 이벤트를 생성하기 위한 생성자입니다.
     * 취소 시점의 애그리게이트 관련 상태를 스냅샷으로 캡처합니다.
     * @param aggregate 이벤트가 발생한 SubscribeManagement 애그리게이트.
     */
    public SubscriptionCancelled(SubscribeManagement aggregate) {
        // 공통 이벤트 속성(예: 이벤트 발생 시각, 이벤트 타입 등)을 초기화하기 위해
        // 상위 클래스(AbstractEvent)의 생성자를 호출합니다.
        // 만약 AbstractEvent가 애그리게이트를 매개변수로 받는 생성자를 가지고 있다면,
        // super(aggregate); 를 사용해야 합니다.
        // 여기서는 AbstractEvent가 기본 생성자를 가지고 있거나 자체적으로 초기화한다고 가정하고 super()를 사용합니다.
        super();

        this.subscriptionId = aggregate.getSubscriptionId();
        this.userId = aggregate.getUserId();
        this.status = aggregate.getStatus(); // 애그리게이트의 상태는 취소를 반영해야 합니다.
        this.cancelledAt = new Date();       // 취소된 현재 시각을 캡처합니다.
    }

    // 매개변수 없는 생성자는 @NoArgsConstructor에 의해 자동으로 생성됩니다.
    // public SubscriptionCancelled() {
    //     super();
    // }
}
//>>> DDD / Domain Event
