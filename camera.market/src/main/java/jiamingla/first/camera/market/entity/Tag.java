package jiamingla.first.camera.market.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.ArrayList;

@Entity
@Data
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true) // 確保 tag 名稱的唯一性
    private String name; // Tag 的名稱

    @ManyToMany(mappedBy = "tags") // 被 Listing 實體中的 tags 欄位所映射
    private List<Listing> listings = new ArrayList<>(); // 擁有這個tag的listing

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
