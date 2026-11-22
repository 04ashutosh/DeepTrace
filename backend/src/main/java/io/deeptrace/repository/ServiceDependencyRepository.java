package io.deeptrace.repository;

import io.deeptrace.entity.ServiceDependencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceDependencyRepository extends JpaRepository<ServiceDependencyEntity, ServiceDependencyEntity.ServiceDependencyId> {
    List<ServiceDependencyEntity> findByCallerService(String callerService);
    List<ServiceDependencyEntity> findByCalleeService(String calleeService);
}