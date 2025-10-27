package rest

import (
	"net/http"

	"github.com/gorilla/mux"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
)

type createCategoryDTO struct {
	Description string `json:"description" validate:"required"`
}

type categoryDTO struct {
	audit.Audit
	CategoryID  string `json:"category_id"`
	Description string `json:"description"`
}

func toCategoryDTO(c *category.Category) categoryDTO {
	return categoryDTO{
		CategoryID:  c.CategoryID.ToPublic(),
		Description: c.Description,
		Audit:       c.Audit,
	}
}

func toCategoriesDTO(categories []category.Category) *[]categoryDTO {
	dtos := make([]categoryDTO, len(categories))
	for i, c := range categories {
		dtos[i] = toCategoryDTO(&c)
	}
	return &dtos
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
	categoryRouter.HandleFunc("", categoryRest.getCategories).Methods("GET")
	categoryRouter.HandleFunc("/{id}", categoryRest.deleteCategory).Methods("DELETE")
}

func (cr *CategoryRest) createCategory(w http.ResponseWriter, r *http.Request) {
	var dto createCategoryDTO
	if !readJSON(w, r, &dto) {
		return
	}

	categoryCreated, err := cr.categorySvc.Create(&category.CreateCategoryPayload{Description: dto.Description})
	if err != nil {
		unprocessableEntity(w, err)
		return
	}

	writeJSON(w, http.StatusCreated, toCategoryDTO(categoryCreated))
}

func (cr *CategoryRest) getCategories(w http.ResponseWriter, r *http.Request) {
	categories := cr.categorySvc.GetCategories()

	writeJSON(w, http.StatusOK, toCategoriesDTO(categories))
}

func (cr *CategoryRest) deleteCategory(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)

	categoryID, err := category.ParseCategoryID(vars["id"])
	if err != nil {
		badRequest(w, err)
		return
	}

	if err := cr.categorySvc.Delete(categoryID); err != nil {
		notFound(w)
	}
}
