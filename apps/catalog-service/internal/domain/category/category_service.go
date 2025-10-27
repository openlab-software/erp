package category

type CategoryService interface {
	Create(c *CreateCategoryPayload) (*Category, error)
	GetById(id CategoryID) *Category
	GetCategories(filter *GetCategoriesFilter) []Category
	Delete(id CategoryID) error
}

type CreateCategoryPayload struct {
	Description string
}

type GetCategoriesFilter struct {
	Q string
}
