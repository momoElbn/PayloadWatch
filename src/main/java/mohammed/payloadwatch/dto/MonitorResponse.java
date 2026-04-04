package mohammed.payloadwatch.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class MonitorResponse {
    private Long id;              // allows edit/delete
    private String name;
    private String url;
    private String httpMethod;
    private int interval;
    private String status;        // indicates status
    private List<ContractDto> contracts;

    public MonitorResponse(Long id, String name, String url, String httpMethod, int interval, String status, List<ContractDto> contracts) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.httpMethod = httpMethod;
        this.interval = interval;
        this.status = status;
        this.contracts = contracts;
    }
}
