package mohammed.payloadwatch.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// contract response dto
public class ContractDto {
    private String expectedKey;
    private String expectedType;

    public ContractDto(String expectedKey, String expectedType) {
        this.expectedKey = expectedKey;
        this.expectedType = expectedType;
    }
}
