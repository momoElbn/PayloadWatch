package mohammed.payloadwatch.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// This DTO represents the expected structure of the response for the frontend
public class ContractDto {
    private String expectedKey;
    private String expectedType;

    public ContractDto(String expectedKey, String expectedType) {
        this.expectedKey = expectedKey;
        this.expectedType = expectedType;
    }
}
