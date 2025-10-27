package category

type CategoryRepository interface {
	Insert(c *Category) error
	Find(description string) []Category
	FindById(id CategoryID) *Category
	FindByDescription(description string) *Category
	DeleteById(id CategoryID) error
}
