package mohammed.payloadwatch.repositories;

import mohammed.payloadwatch.entities.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    // delete monitor rules
    void deleteByMonitorId(Long monitorId);

    // get monitor rules
    List<Contract> findByMonitorId(Long monitorId);
}
