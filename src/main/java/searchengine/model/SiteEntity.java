package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "site")
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "enum('INDEXING', 'INDEXED', 'FAILED')", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusType status;

    @Column(name = "status_time", nullable = false)
    private LocalDateTime statusTime;

    @Column(nullable = false, columnDefinition = "varchar(255)")
    private String url;

    @Column(nullable = false, columnDefinition = "varchar(255)")
    private String name;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "siteEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    protected List<PageEntity> pageList = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "siteEntity", cascade = CascadeType.ALL)
    protected List<LemmaEntity> lemmaEntityList = new ArrayList<>();
}
