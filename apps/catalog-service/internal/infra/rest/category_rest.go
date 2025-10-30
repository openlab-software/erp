package rest

import (
	"net/http"

	"github.com/gorilla/mux"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
)

// createCategoryDTO define o payload para criar uma nova category.
type createCategoryDTO struct {
	// A descrição da categoria.
	// required: true
	Description string `json:"description" validate:"required" example:"Eletrônicos"`
}

// categoryDTO define a representação de uma categoria na API.
type categoryDTO struct {
	audit.Audit
	// O ID único da categoria.
	CategoryID string `json:"category_id" example:"cat_2a1b3c4d5e"`
	// A descrição da categoria.
	Description string `json:"description" example:"Eletrônicos"`
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
	categoryRouter.HandleFunc("/{id}", categoryRest.getCategory).Methods("GET")
}

// @Summary      Cria uma nova categoria
// @Description  Cria uma nova categoria com base na descrição fornecida.
// @Tags         Categories
// @Accept       json
// @Produce      json
// @Param        body body     createCategoryDTO true "Payload para criação da categoria"
// @Success      201  {object} categoryDTO "Categoria criada com sucesso"
// @Failure      400  {string} string "Requisição inválida (JSON mal formatado)"
// @Failure      422  {string} string "Erro de validação (ex: descrição em falta)"
// @Router       /categories [post]
func (cr *CategoryRest) createCategory(w http.ResponseWriter, r *http.Request) {
	var dto createCategoryDTO
	if !readJSON(w, r, &dto) {
		return
	}

	categoryCreated, err := cr.categorySvc.Create(r.Context(), &category.CreateCategoryPayload{Description: dto.Description})
	if err != nil {
		unprocessableEntity(w, err)
		return
	}

	writeJSON(w, http.StatusCreated, toCategoryDTO(categoryCreated))
}

// @Summary      List categories
// @Description  Retorna uma lista de categorias. Permite filtrar por uma string de busca 'q'.
// @Tags         Categories
// @Produce      json
// @Param        q   query    string     false  "Termo de busca para filtrar categorias pela descrição"
// @Success      200 {array}  categoryDTO "Lista de categorias"
// @Router       /categories [get]
func (cr *CategoryRest) getCategories(w http.ResponseWriter, r *http.Request) {
	query := r.URL.Query()

	categories := cr.categorySvc.GetCategories(r.Context(), &category.GetCategoriesFilter{
		Q: query.Get("q"),
	})

	writeJSON(w, http.StatusOK, toCategoriesDTO(categories))
}

// @Summary      Deleta uma categoria
// @Description  Remove uma categoria específica do sistema pelo ID.
// @Tags         Categories
// @Produce      json
// @Param        id  path     string     true   "ID da Categoria"
// @Success      204 "Categoria deletada com sucesso (sem conteúdo)"
// @Failure      400 {string} string "ID da categoria inválido"
// @Failure      404 {string} string "Categoria não encontrada"
// @Router       /categories/{id} [delete]
func (cr *CategoryRest) deleteCategory(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)

	categoryID, err := category.ParseCategoryID(vars["id"])
	if err != nil {
		badRequest(w, err)
		return
	}

	if err := cr.categorySvc.Delete(r.Context(), categoryID); err != nil {
		notFound(w)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

// @Summary      Busca uma categoria
// @Description  Retorna os detalhes de uma categoria específica.
// @Tags         Categories
// @Produce      json
// @Param        id  path     string     true   "ID da Categoria"
// @Success      200 {object} categoryDTO "Detalhes da categoria"
// @Failure      400 {string} string "ID da categoria inválido"
// @Failure      404 {string} string "Categoria não encontrada"
// @Router       /categories/{id} [get]
func (cr *CategoryRest) getCategory(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)

	categoryID, err := category.ParseCategoryID(vars["id"])
	if err != nil {
		badRequest(w, err)
		return
	}

	founded := cr.categorySvc.GetById(r.Context(), categoryID)

	// Adicionando verificação de "não encontrado"
	if founded == nil {
		notFound(w)
		return
	}

	writeJSON(w, http.StatusOK, toCategoryDTO(founded))
}
