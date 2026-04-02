package mohammed.payloadwatch.repositories;

import mohammed.payloadwatch.entities.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    // Deletes all validation rules associated with a specific monitor (used when a monitor is deleted)
    void deleteByMonitorId(Long monitorId);

    //Fetches the validation rules for a specific monitor during a ping
    List<Contract> findByMonitorId(Long monitorId);
}
