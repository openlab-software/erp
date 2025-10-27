package publicid

import (
	"errors"
	"fmt"
	"strings"

	"github.com/google/uuid"
)

type PublicID string

func (id PublicID) ToPublic() string {
	return strings.Split(string(id), ".")[1]
}

func New(prefix string) PublicID {
	return PublicID(fmt.Sprintf("%s.%s", prefix, uuid.New().String()))
}

func ParsePublic(prefix string, withoutPrefix string) (PublicID, error) {
	if _, err := uuid.Parse(withoutPrefix); err != nil {
		return "", errors.New("invalid id")
	}
	return PublicID(fmt.Sprintf("%s.%s", prefix, withoutPrefix)), nil
}
