package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {
    @Query(value = "SELECT i.* FROM search_index i WHERE i.lemma_id IN :lemmas AND i.page_id IN :pages",
            nativeQuery = true)
    List<IndexEntity> findByLemmasAndPages(@Param("lemmas") List<LemmaEntity> lemmas,
                                           @Param("pages") List<PageEntity> pages);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM search_index WHERE page_id = ?", nativeQuery = true)
    void deleteByPage(int pageId);
}
