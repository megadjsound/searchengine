package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {
    List<LemmaEntity> findBySiteEntity(SiteEntity siteEntity);

    List<LemmaEntity> findByLemma(String lemma);

    @Query(value = "SELECT l.* FROM Lemma l WHERE l.lemma = :lemma AND l.site_id = :site", nativeQuery = true)
    LemmaEntity findLemmaByLemmaAndSite(@Param("lemma") String lemma, @Param("site") SiteEntity siteEntity);

    @Query(value = "SELECT l.* FROM Lemma l WHERE l.lemma IN :lemmas AND l.site_id IN :site", nativeQuery = true)
    List<LemmaEntity> findLemmasBySite(@Param("lemmas") List<String> lemmas, @Param("site") List<SiteEntity> site);

    @Modifying
    @Transactional
    @Query(value = "UPDATE lemma SET frequency = frequency - 1 WHERE id IN (SELECT lemma_id FROM `search_index` WHERE page_id = ?)", nativeQuery = true)
    void deleteByPage(int siteId);

    int countBySiteEntity(SiteEntity site);

}
