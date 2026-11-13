package su.kazuyuki07.swimminpool.repositories.specifications;

import org.springframework.data.jpa.domain.Specification;
import su.kazuyuki07.swimminpool.entities.Client;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ClientSpecification {
    public static Specification<Client> firstNameFilter(String firstName) {
        return (root, _, builder) ->
                firstName == null || firstName.isEmpty() ? null : builder.like(
                        builder.lower(root.get("firstName")),
                        "%" + firstName.trim().toLowerCase() + "%"
                );
    }

    public static Specification<Client> firstNamesFilter(List<String> firstNames) {
        return (root, _, _) ->
                firstNames == null || firstNames.isEmpty() ? null : root.get("firstName").in(firstNames);
    }

    public static Specification<Client> lastNameFilter(String lastName) {
        return (root, _, builder) ->
                lastName == null || lastName.isEmpty() ? null : builder.like(
                    builder.lower(root.get("lastName")),
                    "%" + lastName.trim().toLowerCase() + "%"
        );
    }

    public static Specification<Client> lastNamesFilter(List<String> lastNames) {
        return (root, _, _) ->
                lastNames == null || lastNames.isEmpty() ? null : root.get("lastName").in(lastNames);
    }

    public static Specification<Client> surnameFilter(String surname) {
        return (root, _, builder) ->
                surname == null || surname.isEmpty() ? null : builder.like(
                        builder.lower(root.get("surname")),
                        "%" + surname.trim().toLowerCase() + "%"
        );
    }

    public static Specification<Client> surnamesFilter(List<String> surnames) {
        return (root, _, _) ->
                surnames == null || surnames.isEmpty() ? null : root.get("surname").in(surnames);
    }

    public static Specification<Client> phoneNumberFilter(String phoneNumber) {
        return (root, _, builder) ->
                phoneNumber == null || phoneNumber.isEmpty() ? null : builder.like(
                        builder.lower(root.get("phoneNumber")),
                        "%" + phoneNumber.trim().toLowerCase() + "%"
        );
    }

    public static Specification<Client> birthDateFilter(LocalDate birthDate) {
        return (root, _, builder) ->
                birthDate == null ? null : builder.greaterThanOrEqualTo(root.get("birthDate"), birthDate);
    }

    public static Specification<Client> subscriptionIdFilter(UUID subscriptionId) {
        return (root, _, builder) ->
                subscriptionId == null ? null : builder.equal(root.get("subscriptionId"), subscriptionId);
    }

    public static Specification<Client> subscriptionIdsFilter(List<UUID> subscriptionIds) {
        return (root, _, _) ->
                subscriptionIds == null || subscriptionIds.isEmpty() ? null : root.get("subscriptionId").in(subscriptionIds);
    }
}
