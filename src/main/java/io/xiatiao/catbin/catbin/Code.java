package io.xiatiao.catbin.catbin;

import com.sun.istack.NotNull;

import javax.persistence.*;
import java.util.Date;

@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"digest"},
                        name="uk_digest"
                )
        }
)
@Entity
public class Code {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(nullable = false)
    private String digest;

    @NotNull
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP default CURRENT_TIMESTAMP", nullable = false, updatable = false, insertable = false)
    private Date createdAt;

    public Integer getId() {
        return id;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
