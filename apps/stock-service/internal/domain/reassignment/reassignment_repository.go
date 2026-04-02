package reassignment

import "context"

type ReassignmentRepository interface {
	Save(ctx context.Context, reassignment *Reassignment) error
}
