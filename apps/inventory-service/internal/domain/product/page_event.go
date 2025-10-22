package product

const (
	ProductCreatedEvent = "product.created"
)

type ProductCreatedPayload struct {
	ID    Product `json:"product_id"`
	Title string  `json:"title"`
}
