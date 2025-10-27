package product

const (
	ProductCreatedEvent = "product.created"
)

type ProductCreatedPayload struct {
	ID          Product `json:"product_id"`
	Description string  `json:"description"`
}
