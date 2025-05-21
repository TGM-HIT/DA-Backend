package at.ac.tgm.diplomarbeit.diplomdb.repository;

import at.ac.tgm.diplomarbeit.diplomdb.entity.Diplomarbeit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiplomarbeitRepository extends JpaRepository<Diplomarbeit, Long> {

    @Query("SELECT COUNT(d) FROM Diplomarbeit d JOIN d.mitarbeiterSamAccountNames m WHERE m = :samAccountName")
    long countByMitarbeiterSamAccountName(@Param("samAccountName") String samAccountName);
}
