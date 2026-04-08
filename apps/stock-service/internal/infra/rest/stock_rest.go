package rest

import (
	"net/http"

	"github.com/gorilla/mux"
	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/domain/stock"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/publicid"
)

// createStockDTO representa o payload de entrada para criação de um estoque.
// @name CreateStockDTO
type createStockDTO struct {
	// Descrição do estoque.
	Description string `json:"description" validate:"required" example:"Estoque Central"`
}

// stockDTO representa a estrutura de um estoque retornado pela API.
// @name StockDTO
type stockDTO struct {
	audit.Audit
	// ID público do estoque.
	StockID string `json:"stock_id" example:"stock_abc123def456"`
	// Descrição do estoque.
	Description string `json:"description" example:"Estoque Central"`
}

func toStockDTO(s *stock.Stock) stockDTO {
	return stockDTO{
		StockID:     publicid.PublicID(s.StockID).ToPublic(),
		Audit:       s.Audit,
		Description: s.Description,
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

// createStock godoc
// @Summary Cria um novo estoque
// @Description Cria um novo estoque com base no payload fornecido.
// @Tags stocks
// @Accept json
// @Produce json
// @Param stock body createStockDTO true "Payload para criação do estoque"
// @Success 201 {object} stockDTO "Estoque criado com sucesso"
// @Failure 400 {object} map[string]string "Requisição inválida"
// @Failure 422 {object} map[string]string "Erro de validação"
// @Router /stocks [post]
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

// getStocks godoc
// @Summary Lista todos os estoques
// @Description Retorna uma lista de todos os estoques cadastrados.
// @Tags stocks
// @Produce json
// @Success 200 {array} stockDTO "Lista de estoques"
// @Router /stocks [get]
func (pr *StockRest) getStocks(w http.ResponseWriter, r *http.Request) {
	// query := r.URL.Query()
	// filter := stock.GetStocks{
	// 	Title: query.Get("title"),
	// }
}
