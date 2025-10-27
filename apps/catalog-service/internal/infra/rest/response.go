package rest

import (
	"encoding/json"
	"net/http"
)

func writeJSON(w http.ResponseWriter, status int, data any) error {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	return json.NewEncoder(w).Encode(data)
}

func writeErr(w http.ResponseWriter, err error) error {
	return writeJSON(w, http.StatusInternalServerError, map[string]any{"err": err.Error()})
}

func badRequest(w http.ResponseWriter, err error) error {
	return writeJSON(w, http.StatusBadRequest, map[string]any{"message": err.Error()})
}

func notFound(w http.ResponseWriter) {
	w.WriteHeader(http.StatusNotFound)
}

func unprocessableEntity(w http.ResponseWriter, err error) error {
	return writeJSON(w, http.StatusBadRequest, map[string]any{"message": err.Error()})
}

func readJSON(w http.ResponseWriter, r *http.Request, v interface{}) bool {
	if err := json.NewDecoder(r.Body).Decode(v); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]any{"err": err})
		return false
	}
	defer r.Body.Close()

	return validateStruct(v, w)
}
