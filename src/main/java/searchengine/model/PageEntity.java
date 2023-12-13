package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLInsert;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Все классы с аннотацией @Entity автоматически
 * ассоциируются с таблицей из БД к которой подключено
 * приложение
 */
@Getter
@Setter
@Entity
@Table(name = "Page", uniqueConstraints = {
        @UniqueConstraint(name = "site_path", columnNames = {"path", "site_id"})})
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false) //тот кто владеет столбцом FK
    private SiteEntity siteEntity;

    @Column(length = 1000, columnDefinition = "varchar(515)")
    private String path;

    @Column(nullable = false, columnDefinition = "INT")
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @OneToMany(mappedBy = "pageEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    protected List<IndexEntity> index = new ArrayList<>();
}
