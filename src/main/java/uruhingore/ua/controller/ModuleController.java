package uruhingore.ua.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uruhingore.ua.model.Module;
import uruhingore.ua.repository.ModuleRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleRepository moduleRepository;

    // CREATE NEW MODULE
    @PostMapping
    public ResponseEntity<Module> createModule(@RequestBody Module module) {
        Module saved = moduleRepository.save(module);
        return ResponseEntity.ok(saved);
    }

    // CREATE MULTIPLE MODULES AT ONCE
    @PostMapping("/bulk")
    public ResponseEntity<List<Module>> createModules(@RequestBody List<Module> modules) {
        List<Module> savedModules = moduleRepository.saveAll(modules);
        return ResponseEntity.ok(savedModules);
    }

    // GET ALL ACTIVE MODULES (ordered by indexOrder)
    @GetMapping
    public List<Module> getAllActiveModules() {
        return moduleRepository.findByActiveOrderByIndexOrder(true);
    }

    // GET ALL MODULES (including inactive)
    @GetMapping("/all")
    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }

    // GET MODULE BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Module> getModuleById(@PathVariable UUID id) {
        return moduleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE MODULE
    @PutMapping("/{id}")
    public ResponseEntity<Module> updateModule(@PathVariable UUID id, @RequestBody Module module) {
        return moduleRepository.findById(id)
                .map(existing -> {
                    existing.setName(module.getName());
                    existing.setCategory(module.getCategory());
                    existing.setActive(module.isActive());
                    existing.setIndexOrder(module.getIndexOrder() != null ? module.getIndexOrder() : 0);
                    return ResponseEntity.ok(moduleRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE MODULE (soft delete by setting active = false)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable UUID id) {
        return moduleRepository.findById(id)
                .map(module -> {
                    module.setActive(false);
                    moduleRepository.save(module);
                    return ResponseEntity.ok("Module deactivated successfully");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // GET MODULES BY NAME
    @GetMapping("/by-name/{name}")
    public List<Module> getModulesByName(@PathVariable String name) {
        return moduleRepository.findByNameAndActive(name, true);
    }
}
