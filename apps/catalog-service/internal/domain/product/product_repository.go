package product

type ProductRepository interface {
	Insert(Product *Product) error
	Update(Product *Product) error
	FindByTitle(title string) ([]*Product, error)
	FindById(id ProductID) (*Product, error)
}
