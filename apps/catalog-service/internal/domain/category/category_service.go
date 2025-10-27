package category

type CategoryService interface {
	Create(c *Category) error
	GetCategories() []Category
}
