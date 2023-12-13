package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lemma")
@Getter
@Setter
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "site_id", referencedColumnName = "id")
    private SiteEntity siteEntity;

    @Column(nullable = false, columnDefinition = "varchar(515)")
    private String lemma;

    @Column(nullable = false)
    private int frequency;

    @OneToMany(mappedBy = "lemmaEntity", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    protected List<IndexEntity> IndexEntityList = new ArrayList<>();
}
