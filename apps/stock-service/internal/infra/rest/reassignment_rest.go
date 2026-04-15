package rest

import (
	"net/http"

	"github.com/gorilla/mux"
	"github.com/openlab-software/erp/apps/stock-service/internal/domain/reassignment"
	"github.com/openlab-software/erp/apps/stock-service/internal/domain/stock"
	"github.com/openlab-software/erp/libs/go-common/audit"
)

// createReassignmentItemDTO representa um item a ser remanejado.
// @name CreateReassignmentItemDTO
type createReassignmentItemDTO struct {
	// ID público do produto.
	ProductID string `json:"product_id" validate:"required" example:"prod_xyz789uvw012"`
	// Quantidade a remanejar.
	Quantity int `json:"quantity" validate:"required,min=1" example:"10"`
}

// createReassignmentDTO representa o payload para criar um remanejamento.
// @name CreateReassignmentDTO
type createReassignmentDTO struct {
	// ID público do estoque de origem.
	FromStockID string `json:"from_stock_id" validate:"required" example:"stock_abc123def456"`
	// ID público do estoque de destino.
	ToStockID string `json:"to_stock_id" validate:"required" example:"stock_def456ghi789"`
	// Itens a serem remanejados.
	Items []createReassignmentItemDTO `json:"items" validate:"required,min=1"`
}

// reassignmentItemDTO representa um item retornado no remanejamento.
// @name ReassignmentItemDTO
type reassignmentItemDTO struct {
	audit.Audit
	// ID público do produto.
	ProductID string `json:"product_id" example:"prod_xyz789uvw012"`
	// Quantidade remanejada.
	Quantity int `json:"quantity" example:"10"`
}

// reassignmentDTO representa a estrutura de um remanejamento retornado pela API.
// @name ReassignmentDTO
type reassignmentDTO struct {
	audit.Audit
	// ID público do remanejamento.
	ReassignmentID string `json:"reassignment_id" example:"stock_reassignment_abc123"`
	// ID público do estoque de origem.
	FromStockID string `json:"from_stock_id" example:"stock_abc123def456"`
	// ID público do estoque de destino.
	ToStockID string `json:"to_stock_id" example:"stock_def456ghi789"`
	// Itens remanejados.
	Items []reassignmentItemDTO `json:"items"`
}

func toReassignmentDTO(r *reassignment.Reassignment) reassignmentDTO {
	items := make([]reassignmentItemDTO, len(r.Items))
	for i, item := range r.Items {
		items[i] = reassignmentItemDTO{
			Audit:     item.Audit,
			ProductID: item.ProductID,
			Quantity:  item.Quantity,
		}
	}
	return reassignmentDTO{
		Audit:          r.Audit,
		ReassignmentID: r.ReassignmentID.ToPublic(),
		FromStockID:    r.FromStockID.ToPublic(),
		ToStockID:      r.ToStockID.ToPublic(),
		Items:          items,
	}
}

type ReassignmentRest struct {
	reassignmentSvc reassignment.ReassignmentService
}

func NewReassignmentRest(r *mux.Router, reassignmentService reassignment.ReassignmentService) {
	reassignmentRest := &ReassignmentRest{
		reassignmentSvc: reassignmentService,
	}

	r.PathPrefix("/reassignments").Subrouter().
		HandleFunc("", reassignmentRest.createReassignment).Methods("POST")
}

// createReassignment godoc
// @Summary Cria um remanejamento de estoque
// @Description Remeja produtos de um estoque de origem para um estoque de destino.
// @Tags reassignments
// @Accept json
// @Produce json
// @Param reassignment body createReassignmentDTO true "Payload para criação do remanejamento"
// @Success 201 {object} reassignmentDTO "Remanejamento criado com sucesso"
// @Failure 400 {object} map[string]string "Requisição inválida"
// @Failure 422 {object} map[string]string "Erro de validação"
// @Router /reassignments [post]
func (rr *ReassignmentRest) createReassignment(w http.ResponseWriter, r *http.Request) {
	var dto createReassignmentDTO
	if !readJSON(w, r, &dto) {
		return
	}

	fromStockID, err := stock.ParseStockID(dto.FromStockID)
	if err != nil {
		badRequest(w, err)
		return
	}

	toStockID, err := stock.ParseStockID(dto.ToStockID)
	if err != nil {
		badRequest(w, err)
		return
	}

	items := make([]reassignment.CreateReassignmentItemPayload, len(dto.Items))
	for i, item := range dto.Items {
		items[i] = reassignment.CreateReassignmentItemPayload{
			ProductID: item.ProductID,
			Quantity:  item.Quantity,
		}
	}

	created, err := rr.reassignmentSvc.CreateReassignment(r.Context(), reassignment.CreateReassignmentPayload{
		FromStockID: fromStockID,
		ToStockID:   toStockID,
		Items:       items,
	})
	if err != nil {
		unprocessableEntity(w, err)
		return
	}

	writeJSON(w, http.StatusCreated, toReassignmentDTO(created))
}
