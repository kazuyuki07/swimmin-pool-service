package su.kazuyuki07.swimminpool.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class VisitDTO {
    private ClientDTO clientDTO;
    private SubscriptionDTO subscriptionDTO;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private LocalDateTime createdAt;
}
