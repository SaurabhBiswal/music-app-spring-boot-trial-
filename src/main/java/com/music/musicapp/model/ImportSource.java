package com.music.musicapp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_sources")
@Data
public class ImportSource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "service_id", nullable = false)
    private String serviceId;
    
    @Column(name = "service_name", nullable = false)
    private String serviceName;
    
    @Column(name = "credentials", length = 2000)
    private String credentials; // JSON string of credentials
    
    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;
    
    @Column(name = "disconnected_at")
    private LocalDateTime disconnectedAt;
    
    @Column(name = "last_sync")
    private LocalDateTime lastSync;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @Column(name = "import_count")
    private Integer importCount = 0;
    
    @Column(name = "last_import")
    private LocalDateTime lastImport;
    
    // Constructors
    public ImportSource() {}
    
    public ImportSource(String serviceId, String serviceName, boolean active, Integer importCount) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.active = active;
        this.importCount = importCount;
        this.connectedAt = LocalDateTime.now();
    }
    
    // Getters and Setters (Lombok @Data generates these automatically)
    // If you need custom logic, you can add them below
}