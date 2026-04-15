package category

import (
	"github.com/openlab-software/erp/libs/go-common/audit"
	"github.com/openlab-software/erp/libs/go-common/publicid"
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

func NewCategory(description string) *Category {
	return &Category{
		Audit:       audit.CreatedNow(),
		CategoryID:  publicid.New("category"),
		Description: description,
	}
}
