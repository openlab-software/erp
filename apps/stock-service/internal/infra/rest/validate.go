package rest

import (
	"fmt"
	"net/http"
	"strings"

	"github.com/go-playground/validator/v10"
)

func validateStruct(strct interface{}, w http.ResponseWriter) bool {
	validate := validator.New(validator.WithRequiredStructEnabled())
	err := validate.Struct(strct)

	if err == nil {
		return true
	}

	validationErrors, ok := err.(validator.ValidationErrors)
	if !ok {
		http.Error(w, "Erro interno ao validar", http.StatusInternalServerError)
		return false
	}

	errosFormatados := formatValidationErrors(validationErrors)

	writeJSON(w, http.StatusBadRequest, map[string]interface{}{
		"mensagem": "Um ou mais campos são inválidos",
		"erros":    errosFormatados,
	})
	return false

}

func formatValidationErrors(errs validator.ValidationErrors) map[string]string {
	erros := make(map[string]string)

	for _, err := range errs {
		jsonFieldName := strings.ToLower(err.Field()[:1]) + err.Field()[1:]

		tag := err.Tag()
		switch tag {
		case "required":
			erros[jsonFieldName] = "Este campo é obrigatório."
		case "email":
			erros[jsonFieldName] = "Este campo deve ser um e-mail válido."
		case "min":
			erros[jsonFieldName] = fmt.Sprintf("Este campo deve ter no mínimo %s caracteres.", err.Param())
		case "gte":
			erros[jsonFieldName] = fmt.Sprintf("O valor deve ser maior ou igual a %s.", err.Param())
		case "lte":
			erros[jsonFieldName] = fmt.Sprintf("O valor deve ser menor ou igual a %s.", err.Param())
		case "oneof":
			erros[jsonFieldName] = fmt.Sprintf("O valor deve ser um de: %s.", err.Param())
		default:
			erros[jsonFieldName] = fmt.Sprintf("Falha na validação da tag: '%s'.", tag)
		}
	}

	return erros
}
