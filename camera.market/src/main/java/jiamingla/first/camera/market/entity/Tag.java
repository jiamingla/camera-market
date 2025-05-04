package jiamingla.first.camera.market.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;

@Entity
@Data
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tag name is required")
    @Column(unique = true) // 確保 tag 名稱的唯一性
    private String name; // Tag 的名稱

    @Enumerated(EnumType.STRING)
    private TagType type; // 新增 tag type 欄位
    // TODO: 商品可能會重複新增和別的商品一樣的tag，處理這個問題，還有在前端設定好不能新增重複的tag
    @ManyToMany(mappedBy = "tags") // 被 Listing 實體中的 tags 欄位所映射
    @JsonIgnore
    private List<Listing> listings = new ArrayList<>(); // 擁有這個tag的listing

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
