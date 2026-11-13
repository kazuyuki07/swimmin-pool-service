package su.kazuyuki07.swimminpool.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientDTO {
    private String firstName;
    private String lastName;
    private String surname;
    private String phoneNumber;
    private String email;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthDate;
    private SubscriptionDTO subscriptionDTO;
}
