package auth

import (
	"context"
	"errors"
	"strings"

	"github.com/coreos/go-oidc"
)

type Claims struct {
	Email string `json:"email"`
}

func extractToken(authorizationHeader string) (string, error) {
	parts := strings.Split(authorizationHeader, " ")
	if len(parts) != 2 || strings.ToLower(parts[0]) != "bearer" {
		return "", errors.New("invalid token")
	}

	return parts[1], nil
}

func checkToken(ctx context.Context, oidcProvider *oidc.Provider, token string) (*Claims, error) {
	config := &oidc.Config{
		ClientID:          cliendID,
		SkipClientIDCheck: true, // TODO
	}

	verifier := oidcProvider.Verifier(config)
	idToken, err := verifier.Verify(ctx, token)

	if err != nil {
		return nil, err
	}

	claims := &Claims{}
	idToken.Claims(claims)

	return claims, nil
}
