package category

import (
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/publicid"
)

type CategoryID = publicid.PublicID

type Category struct {
	audit.Audit
	CategoryID  CategoryID
	Description string
}

func ParseCategoryID(s string) (CategoryID, error) {
	publicID, err := publicid.ParsePublic("category", s)

	return CategoryID(publicID), err
}

func NewCategoryID() CategoryID {
	return publicid.New("category")
}

func NewCategory(description string) *Category {
	return &Category{
		Audit:       *audit.CreatedNow(),
		CategoryID:  NewCategoryID(),
		Description: description,
	}
}
