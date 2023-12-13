package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.Collection;
import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {

    List<PageEntity> findBySiteEntity(SiteEntity siteEntity);

    @Query("SELECT p FROM PageEntity p WHERE p.siteEntity.id = :site_id and p.path = :path")
    PageEntity findPageBySiteIdAndPath(
            @Param("site_id") int siteId,
            @Param("path") String path);

    @Query(value = "SELECT p.* FROM page p JOIN search_index i ON p.id = i.page_id WHERE i.lemma_id IN :lemmas",
            nativeQuery = true)
    List<PageEntity> findByLemmas(@Param("lemmas") Collection<LemmaEntity> lemmas);

    int countBySiteEntity(SiteEntity siteId);

    @Query(value = "SELECT COUNT(1) FROM page p WHERE p.site_id IN :site", nativeQuery = true)
    int getPageCountBySiteEntity(@Param("site") List<SiteEntity> site);

    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO page (site_id, path, code, content) VALUES (?, ?, ?, ?)", nativeQuery = true)
    void savePage(int site_id, String path, int code, String content);
}
