package su.kazuyuki07.swimminpool.repositories.specifications;


import org.springframework.data.jpa.domain.Specification;
import su.kazuyuki07.swimminpool.entities.Visit;

import java.time.LocalDateTime;

public class VisitSpecification {
    public static Specification<Visit> createdAtFilter(LocalDateTime createdAtFrom, LocalDateTime createdAtTo) {
        return (root, _, builder) -> {
            if (createdAtFrom == null && createdAtTo == null) {
                return null;
            }

            if (createdAtFrom == null) {
                return builder.lessThanOrEqualTo(root.get("createdAt"), createdAtTo);
            }

            if (createdAtTo == null) {
                return builder.greaterThanOrEqualTo(root.get("createdAt"), createdAtFrom);
            }

            return builder.between(root.get("createdAt"), createdAtFrom, createdAtTo);
        };
   }
}
