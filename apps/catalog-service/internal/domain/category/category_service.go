package category

type CategoryService interface {
	Create(c *CreateCategoryPayload) (*Category, error)
	GetCategories() []Category
	Delete(id CategoryID) error
}

type CreateCategoryPayload struct {
	Description string
}
