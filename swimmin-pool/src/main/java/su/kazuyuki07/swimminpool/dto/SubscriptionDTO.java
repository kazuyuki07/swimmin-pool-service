package su.kazuyuki07.swimminpool.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import su.kazuyuki07.swimminpool.enums.Status;
import su.kazuyuki07.swimminpool.enums.SubscriptionType;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class SubscriptionDTO {
    private Status status;
    private SubscriptionType subscriptionType;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate expire;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate createdAt;
}
