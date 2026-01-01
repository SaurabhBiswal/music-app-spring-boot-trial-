package com.music.musicapp.repository;

import com.music.musicapp.model.ImportSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImportSourceRepository extends JpaRepository<ImportSource, Long> {
    
    // Check if an active connection exists for a service
    boolean existsByServiceIdAndActive(String serviceId, boolean active); // CHANGED: isActive -> active
    
    // Count imports by service ID
    @Query("SELECT COALESCE(SUM(i.importCount), 0) FROM ImportSource i WHERE i.serviceId = :serviceId")
    Long countImportsByServiceId(@Param("serviceId") String serviceId);
    
    // For backward compatibility
    @Query("SELECT COALESCE(SUM(i.importCount), 0) FROM ImportSource i WHERE i.serviceId = :serviceId")
    Long countimportsByServiceId(@Param("serviceId") String serviceId);
    
    // Find all connections by service ID and active status
    List<ImportSource> findByServiceIdAndActive(String serviceId, boolean active); // CHANGED: isActive -> active
    
    // Sum all import counts across all services
    @Query("SELECT COALESCE(SUM(i.importCount), 0) FROM ImportSource i")
    Long sumImportCounts();
    
    // For backward compatibility
    @Query("SELECT COALESCE(SUM(i.importCount), 0) FROM ImportSource i")
    Long sumimportCounts();
    
    // Count active services
    @Query("SELECT COUNT(i) FROM ImportSource i WHERE i.active = true") // CHANGED: i.isActive -> i.active
    Long countActiveServices();
    
    // For backward compatibility
    @Query("SELECT COUNT(i) FROM ImportSource i WHERE i.active = true") // CHANGED: i.isActive -> i.active
    Long countActive();
    
    // Find single active connection by service ID
    Optional<ImportSource> findFirstByServiceIdAndActive(String serviceId, boolean active); // CHANGED: isActive -> active
    
    // Find all connections by service ID (both active and inactive)
    List<ImportSource> findByServiceId(String serviceId);
    
    // Find all active services - CHANGED: findByIsActive -> findByActive
    List<ImportSource> findByActive(boolean active);
    
    // Find recent imports by last import date (descending order)
    List<ImportSource> findTop5ByOrderByLastImportDesc();
    
    // Find recent connections by connection date (descending order)
    List<ImportSource> findTop5ByOrderByConnectedAtDesc();
    
    // Find imports since a specific date
    @Query("SELECT i FROM ImportSource i WHERE i.lastImport >= :sinceDate AND i.active = true") // CHANGED: i.isActive -> i.active
    List<ImportSource> findImportsSince(@Param("sinceDate") LocalDateTime sinceDate);
}