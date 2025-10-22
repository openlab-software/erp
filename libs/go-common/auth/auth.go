package auth

import (
	"context"
	"os"

	"github.com/coreos/go-oidc"
)

var (
	issuer   = os.Getenv("OIDC_ISSUER_URL")
	cliendID = os.Getenv("OIDC_CLIENT_ID")
)

func NewOIDCProvider() (*oidc.Provider, error) {
	ctx := context.Background()

	provider, err := oidc.NewProvider(ctx, issuer)
	if err != nil {
		return nil, err
	}

	return provider, nil
}
