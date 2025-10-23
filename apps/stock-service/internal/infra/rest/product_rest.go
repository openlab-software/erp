package rest

import (
	"net/http"

	"github.com/gorilla/mux"
	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/domain/product"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/publicid"
)

type createProductDTO struct {
	Title string `json:"title"`
}

type productDTO struct {
	audit.Audit
	ProductID string `json:"product_id"`
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
}

func (pr *ProductRest) createProduct(w http.ResponseWriter, r *http.Request) {
	var dto createProductDTO
	if err := readJSON(w, r, &dto); err != nil {
		return
	}

}

func (pr *ProductRest) getProducts(w http.ResponseWriter, r *http.Request) {
	// query := r.URL.Query()
	// filter := product.GetProducts{
	// 	Title: query.Get("title"),
	// }
}
