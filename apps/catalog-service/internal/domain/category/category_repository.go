package category

type CategoryRepository interface {
	Insert(c *Category) error
	Find() []Category
	FindByDescription(description string) *Category
	DeleteById(id CategoryID) error
}
