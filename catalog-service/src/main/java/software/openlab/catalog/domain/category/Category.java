package software.openlab.catalog.domain.category;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private CategoryId categoryId;
    private String description;
    private CategoryId parentCategoryId;
    private Instant createdAt;
    private Instant updatedAt;

    public static Category newCategory(String description, CategoryId parentCategoryId) {
        return new Category(CategoryId.generate(), description, parentCategoryId, Instant.now(), null);
    }
}
