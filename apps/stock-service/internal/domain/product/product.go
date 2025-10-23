package product

import (
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/publicid"
)

const (
	DRAFT     = "DRAFT"
	PUBLISHED = "PUBLISHED"
	INACTIVE  = "INACTIVE"
)

type ProductID publicid.PublicID

type Product struct {
	audit.Audit
	ProductID ProductID `bson:"product_id" json:"product_id"`
}

func ParseProductID(s string) (ProductID, error) {
	publicID, err := publicid.ParsePublic("page", s)

	return ProductID(publicID), err
}
