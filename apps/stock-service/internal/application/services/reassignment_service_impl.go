package services

import (
	"context"

	"github.com/openlab-software/erp/apps/stock-service/internal/domain/reassignment"
)

type ReassignmentServiceImpl struct {
	reassignment.ReassignmentService
	repo reassignment.ReassignmentRepository
}

func NewReassignmentService(repo reassignment.ReassignmentRepository) reassignment.ReassignmentService {
	return &ReassignmentServiceImpl{
		repo: repo,
	}
}

func (svc *ReassignmentServiceImpl) CreateReassignment(ctx context.Context, payload reassignment.CreateReassignmentPayload) (*reassignment.Reassignment, error) {
	newReassignment := reassignment.NewReassignment(payload.FromStockID, payload.ToStockID)
	for _, item := range payload.Items {
		newReassignment.Items = append(newReassignment.Items, reassignment.NewReassignmentItem(item.ProductID, item.Quantity))
	}
	if err := svc.repo.Save(ctx, newReassignment); err != nil {
		return nil, err
	}
	return newReassignment, nil
}
