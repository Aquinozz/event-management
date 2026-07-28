package event_management.dto;

import event_management.enums.EventStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlterarStatusRequestDto {

    @NotNull
    private EventStatus status;
}
