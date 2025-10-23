package category

type CategoryRepository interface {
	Insert(c *Category) error
}
