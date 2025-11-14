package uruhingore.ua.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uruhingore.ua.model.Module;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {
    
    List<Module> findByActiveOrderByIndexOrder(boolean active);
    
    List<Module> findByNameAndActive(String name, boolean active);
}
