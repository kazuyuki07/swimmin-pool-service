package su.kazuyuki07.swimminpool.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;
    private String surname;

    @Column(name = "phone_number")
    private String phoneNumber;
    private String email;

    @OneToOne(targetEntity = Subscription.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Subscription subscription;

    @OneToMany(
            targetEntity = Visit.class,
            mappedBy = "client",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<Visit> visit;

    @Column(name = "birth_date")
    private LocalDate birthDate;
}
