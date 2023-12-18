package class_ut

import (
	"net/http"

	"github.com/alarmfox/game-repository/api"
)

type Service interface {
	Create(request *CreateRequest) (ClassUT, error)
	FindById(id int64) (ClassUT, error)
	Delete(id int64) error
	Update(id int64, ug *UpdateRequest) (ClassUT, error)
}
type Controller struct {
	service Service
}

func NewController(gs Service) *Controller {
	return &Controller{service: gs}
}

func (gc *Controller) Create(w http.ResponseWriter, req *http.Request) error {

	request, err := api.FromJsonBody[CreateRequest](req.Body)

	if err != nil {
		return err
	}

	classUT, err := gc.service.Create(&request)

	if err != nil {
		return api.MakeHttpError(err)
	}

	return api.WriteJson(w, http.StatusCreated, classUT)

}

func (gc *Controller) FindByID(w http.ResponseWriter, req *http.Request) error {

	id, err := api.FromUrlParams[KeyType](req, "id")
	if err != nil {
		return err
	}

	classUT, err := gc.service.FindById(id.AsInt64())

	if err != nil {
		return api.MakeHttpError(err)
	}

	return api.WriteJson(w, http.StatusOK, classUT)

}

func (gc *Controller) Delete(w http.ResponseWriter, req *http.Request) error {

	id, err := api.FromUrlParams[KeyType](req, "id")
	if err != nil {
		return err
	}

	if err := gc.service.Delete(id.AsInt64()); err != nil {
		return api.MakeHttpError(err)
	}
	w.WriteHeader(http.StatusNoContent)
	return nil
}

func (gc *Controller) Update(w http.ResponseWriter, req *http.Request) error {

	id, err := api.FromUrlParams[KeyType](req, "id")
	if err != nil {
		return err
	}

	request, err := api.FromJsonBody[UpdateRequest](req.Body)
	if err != nil {
		return err
	}

	classUT, err := gc.service.Update(id.AsInt64(), &request)
	if err != nil {
		return api.MakeHttpError(err)
	}

	return api.WriteJson(w, http.StatusOK, classUT)
}
