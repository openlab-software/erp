package audit

import "time"

type Audit struct {
	CreatedAt  time.Time `bson:"created_at" json:"created_at"`
	ModifiedAt time.Time `bson:"modified_at" json:"modified_at"`
}
