package publicid

import (
	"errors"
	"fmt"
	"strings"

	"github.com/google/uuid"
)

type PublicID string

func (id PublicID) ToPublic() string {
	return strings.Split(string(id), "_")[1]
}

func New(prefix string) PublicID {
	return PublicID(fmt.Sprintf("%s_%s", prefix, uuid.New().String()))
}

func Parse(prefix string, s string) (PublicID, error) {
	if !strings.HasPrefix(s, prefix) {
		return "", errors.New("invalid id prefix")
	}
	idWithoutPrefix, ok := strings.CutPrefix(s, fmt.Sprintf("%s_", prefix))
	if !ok {
		return "", errors.New("invalid id")
	}
	if _, err := uuid.Parse(idWithoutPrefix); err != nil {
		return "", errors.New("invalid id")
	}

	return PublicID(s), nil
}

func ParsePublic(prefix string, withoutPrefix string) (PublicID, error) {
	if _, err := uuid.Parse(withoutPrefix); err != nil {
		return "", errors.New("invalid id")
	}
	return PublicID(fmt.Sprintf("%s_%s", prefix, withoutPrefix)), nil
}
