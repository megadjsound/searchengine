package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;

import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {
    SiteEntity findByUrl(String url);

    SiteEntity findByUrlLike(String url);

    boolean existsByUrlAndStatus(String url, StatusType status);

    boolean existsByStatus(StatusType status);

    List<SiteEntity> findByStatus(StatusType status);
}
