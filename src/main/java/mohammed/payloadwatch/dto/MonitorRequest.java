package mohammed.payloadwatch.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class MonitorRequest {
    // frontend payload without id or status
    private String name;
    private String url;
    private String httpMethod;
    private int interval;
    private List<ContractDto> contracts;

    public MonitorRequest(String name, String url, String httpMethod, int interval, List<ContractDto> contracts) {
        this.name = name;
        this.url = url;
        this.httpMethod = httpMethod;
        this.interval = interval;
        this.contracts = contracts;
    }
}
