package rest

import (
	"net/http"

	"github.com/gorilla/mux"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/publicid"
)

type createCategoryDTO struct {
	Description string `json:"description"`
}

type categoryDTO struct {
	audit.Audit
	CategoryID string `json:"category_id"`
}

func toCategoryDTO(c *category.Category) categoryDTO {
	return categoryDTO{
		CategoryID: publicid.PublicID(c.CategoryID).ToPublic(),
		Audit:      c.Audit,
	}
}

type CategoryRest struct {
	categorySvc category.CategoryService
}

func NewCategoryRest(r *mux.Router, categoryService category.CategoryService) {
	categoryRest := &CategoryRest{
		categorySvc: categoryService,
	}

	categoryRouter := r.PathPrefix("/categories").Subrouter()

	categoryRouter.HandleFunc("", categoryRest.createCategory).Methods("POST")
}

func (cr *CategoryRest) createCategory(w http.ResponseWriter, r *http.Request) {
	var dto createCategoryDTO
	if err := readJSON(w, r, &dto); err != nil {
		return
	}

	c := category.Category{
		CategoryID:  category.NewCategoryID(),
		Description: dto.Description,
	}

	cr.categorySvc.Create(&c)

	writeJSON(w, http.StatusCreated, toCategoryDTO(&c))
}
