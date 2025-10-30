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

// productDTO representa a estrutura de um produto retornado pela API.
type productDTO struct {
	audit.Audit
	// ID público do produto.
	ProductID string `json:"product_id" example:"prod_xyz789uvw012"`
	// Exemplo de como o ID do produto é formatado.
	// swagger:example prod_xyz789uvw012
}

func toProductDTO(p *product.Product) productDTO {
	return productDTO{
		ProductID: publicid.PublicID(p.ProductID).ToPublic(),
		Audit:     p.Audit,
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

	productCreated, err := cr.productSvc.Create(&product.CreateProductPayload{
		Description:      dto.Description,
		ShortDescription: dto.ShortDescription,
		UnitOfMeasure:    dto.UnitOfMeasure,
		CategoryID:       categoryID,
	})

	if err != nil {
		// Função utilitária para 422 (não implementada aqui)
		unprocessableEntity(w, err)
		return
	}

	// Função utilitária para escrever JSON (não implementada aqui)
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

	products := cr.productSvc.GetProducts(&product.GetProductsFilter{
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

	if err := cr.productSvc.Delete(productID); err != nil {
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

	founded := cr.productSvc.GetById(productID)

	if founded == nil {
		notFound(w)
		return
	}

	writeJSON(w, http.StatusOK, toProductDTO(founded))
}
