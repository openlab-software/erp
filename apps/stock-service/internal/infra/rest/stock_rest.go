package rest

import (
	"net/http"

	"github.com/gorilla/mux"
	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/domain/stock"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/publicid"
)

type createStockDTO struct {
	Description string `json:"description"`
}

type stockDTO struct {
	audit.Audit
	StockID string `json:"stock_id"`
}

func toStockDTO(s *stock.Stock) stockDTO {
	return stockDTO{
		StockID: publicid.PublicID(s.StockID).ToPublic(),
		Audit:   s.Audit,
	}
}

func toStocksDTO(stocks []*stock.Stock) *[]stockDTO {
	dtos := make([]stockDTO, len(stocks))
	for i, s := range stocks {
		dtos[i] = toStockDTO(s)
	}
	return &dtos
}

type StockRest struct {
	stockSvc stock.StockService
}

func NewStockRest(r *mux.Router, stockService stock.StockService) {
	stockRest := &StockRest{
		stockSvc: stockService,
	}

	stockRouter := r.PathPrefix("/stocks").Subrouter()

	stockRouter.HandleFunc("", stockRest.createStock).Methods("POST")
	stockRouter.HandleFunc("", stockRest.getStocks).Methods("GET")
}

func (sr *StockRest) createStock(w http.ResponseWriter, r *http.Request) {
	var dto createStockDTO
	if !readJSON(w, r, &dto) {
		return
	}

	stockCreated, err := sr.stockSvc.CreateStock(r.Context(), stock.CreateStockPayload{Description: dto.Description})
	if err != nil {
		unprocessableEntity(w, err)
		return
	}

	writeJSON(w, http.StatusCreated, toStockDTO(stockCreated))
}

func (pr *StockRest) getStocks(w http.ResponseWriter, r *http.Request) {
	// query := r.URL.Query()
	// filter := stock.GetStocks{
	// 	Title: query.Get("title"),
	// }
}
