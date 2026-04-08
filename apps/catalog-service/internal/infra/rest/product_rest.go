package rest

import (
	"net/http"

	"github.com/gorilla/mux"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/product"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/publicid"
)

// createProductDTO representa o payload de entrada para criação de um produto.
// @name CreateProductDTO
type createProductDTO struct {
	// Descrição completa do produto.
	Description string `json:"description" example:"Smartphone X Pro 256GB"`
	// Descrição curta do produto.
	ShortDescription string `json:"short_description" example:"Smartphone X Pro"`
	// Unidade de medida (ex: UN, KG, MT).
	UnitOfMeasure string `json:"unit_of_measure" example:"UN"`
	// ID público da Categoria à qual o produto pertence.
	CategoryID string `json:"category_id" example:"cat_abc123def456"`
}

// categoryInProductDTO representa a categoria associada a um produto.
// @name CategoryInProductDTO
type categoryInProductDTO struct {
	CategoryID  string `json:"category_id" example:"cat_2a1b3c4d5e"`
	Description string `json:"description" example:"Eletrônicos"`
}

// productDTO representa a estrutura de um produto retornado pela API.
// @name ProductDTO
type productDTO struct {
	audit.Audit
	ProductID        string               `json:"product_id" example:"prod_xyz789uvw012"`
	Description      string               `json:"description" example:"Smartphone X Pro 256GB"`
	ShortDescription string               `json:"short_description" example:"Smartphone X Pro"`
	UnitOfMeasure    string               `json:"unit_of_measure" example:"UN"`
	Status           string               `json:"status" example:"INACTIVE"`
	Category         categoryInProductDTO `json:"category"`
}

func toProductDTO(p *product.Product) productDTO {
	return productDTO{
		ProductID:        publicid.PublicID(p.ProductID).ToPublic(),
		Audit:            p.Audit,
		Description:      p.Description,
		ShortDescription: p.ShortDescription,
		UnitOfMeasure:    p.UnitOfMeasure,
		Status:           string(p.Status),
		Category: categoryInProductDTO{
			CategoryID:  p.Category.CategoryID.ToPublic(),
			Description: p.Category.Description,
		},
	}
}

func toProductsDTO(products []*product.Product) *[]productDTO {
	dtos := make([]productDTO, len(products))
	for i, p := range products {
		dtos[i] = toProductDTO(p)
	}
	return &dtos
}

// ProductRest é o handler REST para operações de Produto.
type ProductRest struct {
	productSvc product.ProductService
}

func NewProductRest(r *mux.Router, productService product.ProductService) {
	productRest := &ProductRest{
		productSvc: productService,
	}

	productRouter := r.PathPrefix("/products").Subrouter()

	productRouter.HandleFunc("", productRest.createProduct).Methods("POST")
	productRouter.HandleFunc("", productRest.getProducts).Methods("GET")
	productRouter.HandleFunc("/{id}", productRest.deleteProduct).Methods("DELETE")
	productRouter.HandleFunc("/{id}", productRest.getProduct).Methods("GET")
}

// createProduct godoc
// @Summary Cria um novo produto
// @Description Cria um novo produto no catálogo com base no payload fornecido.
// @Tags products
// @Accept json
// @Produce json
// @Param product body createProductDTO true "Payload para criação do produto"
// @Success 201 {object} productDTO "Produto criado com sucesso"
// @Router /products [post]
func (cr *ProductRest) createProduct(w http.ResponseWriter, r *http.Request) {
	var dto createProductDTO
	// Função utilitária para ler JSON (não implementada aqui)
	if !readJSON(w, r, &dto) {
		return
	}

	categoryID, err := category.ParseCategoryID(dto.CategoryID)
	if err != nil {
		unprocessableEntity(w, err)
		return
	}

	productCreated, err := cr.productSvc.Create(r.Context(), &product.CreateProductPayload{
		Description:      dto.Description,
		ShortDescription: dto.ShortDescription,
		UnitOfMeasure:    dto.UnitOfMeasure,
		CategoryID:       categoryID,
	})

	if err != nil {
		unprocessableEntity(w, err)
		return
	}

	writeJSON(w, http.StatusCreated, toProductDTO(productCreated))
}

// getProducts godoc
// @Summary Lista todos os produtos
// @Description Retorna uma lista de produtos, opcionalmente filtrada por uma query de pesquisa 'q'.
// @Tags products
// @Accept json
// @Produce json
// @Param q query string false "Query de pesquisa (ex: descrição, id curto)"
// @Success 200 {array} productDTO "Lista de produtos"
// @Router /products [get]
func (cr *ProductRest) getProducts(w http.ResponseWriter, r *http.Request) {
	query := r.URL.Query()

	products := cr.productSvc.GetProducts(r.Context(), &product.GetProductsFilter{
		Q: query.Get("q"),
	})

	writeJSON(w, http.StatusOK, toProductsDTO(products))
}

// deleteProduct godoc
// @Summary Deleta um produto
// @Description Deleta um produto permanentemente usando seu ID público.
// @Tags products
// @Accept json
// @Produce json
// @Param id path string true "ID público do produto (formato prod_...)"
// @Success 204 "No Content"
// @Router /products/{id} [delete]
func (cr *ProductRest) deleteProduct(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)

	productID, err := product.ParseProductID(vars["id"])
	if err != nil {
		badRequest(w, err)
		return
	}

	if err := cr.productSvc.Delete(r.Context(), productID); err != nil {
		notFound(w)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

// getProduct godoc
// @Summary Obtém um produto pelo ID
// @Description Retorna um único produto dado seu ID público.
// @Tags products
// @Accept json
// @Produce json
// @Param id path string true "ID público do produto (formato prod_...)"
// @Success 200 {object} productDTO "Produto encontrado"
// @Router /products/{id} [get]
func (cr *ProductRest) getProduct(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)

	productID, err := product.ParseProductID(vars["id"])
	if err != nil {
		badRequest(w, err)
		return
	}

	founded := cr.productSvc.GetById(r.Context(), productID)

	if founded == nil {
		notFound(w)
		return
	}

	writeJSON(w, http.StatusOK, toProductDTO(founded))
}
