package su.kazuyuki07.swimminpool.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;

import java.util.stream.Stream;

@Getter
public enum ClientSort {
    FIRST_NAME_ASC("first_name", "firstName"),
    FIST_NAME_DESC("-first_name", "firstName"),
    LAST_NAME_ASC("last_name", "lastName"),
    LAST_NAME_DESC("-last_name", "lastName"),
    SURNAME_ASC("surname", "surname"),
    SURNAME_DESC("-surname", "surname"),
    PHONE_NUMBER_ASC("phone_number", "phoneNumber"),
    PHONE_NUMBER_DESC("-phone_number", "phoneNumber"),
    EMAIL_ASC("email", "email"),
    EMAIL_DESC("-email", "email"),
    BIRTH_DATE_ASC("birth_date", "birthDate"),
    BIRTH_DATE_DESC("-birth_date", "birthDate"),
    SUBSCRIPTION_ID_ASC("subscription_id", "subscriptionId"),
    SUBSCRIPTION_ID_DESC("-subscription_id", "subscriptionId");

    private final String requestValue;
    private final String entityValue;
    private final Sort.Direction direction;

    ClientSort(String requestValue, String entityValue) {
        this.requestValue = requestValue;
        this.entityValue = entityValue;
        this.direction = requestValue.startsWith("-") ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    public static ClientSort of(String requestValue) {
        return Stream.of(ClientSort.values())
                .filter(e -> e.requestValue.equals(requestValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Вид сортировки не найден"));
    }
}
