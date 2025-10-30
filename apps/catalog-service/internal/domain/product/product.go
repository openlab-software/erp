package product

import (
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/publicid"
)

const (
	DRAFT     = "DRAFT"
	PUBLISHED = "PUBLISHED"
	INACTIVE  = "INACTIVE"
)

type ProductID = publicid.PublicID

type ProductStatus string

const (
	ActiveStatus   ProductStatus = "ACTIVE"
	InactiveStatus ProductStatus = "INACTIVE"
)

type Product struct {
	audit.Audit
	ProductID        ProductID
	Description      string
	ShortDescription string
	UnitOfMeasure    string
	Status           ProductStatus
	Category         category.Category
}

func ParseProductID(s string) (ProductID, error) {
	publicID, err := publicid.ParsePublic("product", s)

	return ProductID(publicID), err
}

func NewProduct(description string, shorDescription string, unitOfMeasure string, categoryID category.CategoryID) *Product {
	return &Product{
		ProductID:        publicid.New("product"),
		Description:      description,
		ShortDescription: shorDescription,
		UnitOfMeasure:    unitOfMeasure,
		Status:           InactiveStatus,
		Audit:            audit.CreatedNow(),
		Category: category.Category{
			CategoryID: categoryID,
		},
	}

}
