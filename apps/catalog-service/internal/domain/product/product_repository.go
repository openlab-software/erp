package product

type ProductRepository interface {
	Insert(p *Product) error
	Update(p *Product) error
	Find(description string) []*Product
	FindById(id ProductID) *Product
	DeleteById(id ProductID) error
}
