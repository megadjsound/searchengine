package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "search_index")
@Getter
@Setter
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", referencedColumnName = "id")
    private PageEntity pageEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", referencedColumnName = "id")
    private LemmaEntity lemmaEntity;

    @Column(nullable = false, name = "lemma_rank")
    private float rank;
}
