package class_ut

import (
	"strconv"

	"github.com/alarmfox/game-repository/model"
)

type ClassUT struct {
	id    int64	 `json:"id"`
	name        string      `json:"name"`
	date  string       `json:"date"`
	difficulty string  `json:"difficulty"`
	codeUri string  `json:"codeUri"`
	description  string      `json:"description"`
	category  []string      `json:"category"`
	
}

type CreateRequest struct {
	name        string     `json:"name"`
	date     string   `json:"date"`
	difficulty string     `json:"difficulty"`
	description  string     `json:"description"`
	codeUri    string `json:"codeUri"`
	category  []string      `json:"category"`
}

func (CreateRequest) Validate() error {
	return nil
}

type KeyType int64

func (c KeyType) Parse(s string) (KeyType, error) {
	a, err := strconv.ParseInt(s, 10, 64)
	return KeyType(a), err
}

func (k KeyType) AsInt64() int64 {
	return int64(k)
}

type UpdateRequest struct {
	name         string     `json:"name"`
	description  string     `json:"description"`
	category  []string      `json:"category"`
}

func (UpdateRequest) Validate() error {
	return nil
}

func fromModel(classUT *model.ClassUT) ClassUT {
	return ClassUT{
		name:        classUT.Name,
		date:  classUT.Date,
		difficulty:    classUT.Difficulty,
		codeUri: classUT.CodeUri,
		description: classUT.Description,
		category:  classUT.Category,
	}
}
