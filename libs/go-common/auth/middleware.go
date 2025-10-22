package auth

import (
	"context"
	"encoding/json"
	"log"
	"net/http"

	"github.com/coreos/go-oidc"
)

type contextKey string

const userKey contextKey = "user"

func NewMiddleware(oidcProvider *oidc.Provider) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			authorizationHeader := r.Header.Get("Authorization")

			if authorizationHeader == "" {
				http.Error(w, "Unauthorized", http.StatusUnauthorized)
				return
			}

			token, err := extractToken(authorizationHeader)
			if err != nil {
				writeJSONError(w, http.StatusUnauthorized, "invalid auth header")
				return
			}

			claims, err := checkToken(r.Context(), oidcProvider, token)
			if err != nil {
				log.Println("Erro ao validar token:", err)
				writeJSONError(w, http.StatusUnauthorized, "invalid token")
				return
			}

			ctx := context.WithValue(r.Context(), userKey, claims.Email)
			next.ServeHTTP(w, r.WithContext(ctx))
		})
	}
}

func writeJSONError(w http.ResponseWriter, status int, message string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(map[string]string{"error": message})
}
