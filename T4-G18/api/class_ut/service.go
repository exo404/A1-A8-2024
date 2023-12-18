package class_ut

import (
	"github.com/alarmfox/game-repository/api"
	"github.com/alarmfox/game-repository/model"
	"gorm.io/gorm"
)

type Repository struct {
	db *gorm.DB
}

func NewRepository(db *gorm.DB) *Repository {
	return &Repository{
		db: db,
	}
}

func (gs *Repository) Create(req *CreateRequest) (ClassUT, error) {
	var (
		classUT = model.ClassUT{
			Name:      req.name,
			Date: req.date,
			Difficulty: req.difficulty,
			Description:  req.description,
			CodeUri: req.codeUri,
			Category: req.category,

		}
	)

	err := gs.db.Transaction(func(tx *gorm.DB) error {
		return tx.Create(&classUT).Error
	})

	if err != nil {
		return ClassUT{}, api.MakeServiceError(err)
	}

	return fromModel(&classUT), nil
}

func (tr *Repository) FindById(id int64) (ClassUT, error) {
	var classUT model.ClassUT

	err := tr.db.
		First(&classUT, id).
		Error

	return fromModel(&classUT), api.MakeServiceError(err)
}

func (gs *Repository) Update(id int64, r *UpdateRequest) (ClassUT, error) {
    var existingClassUT model.ClassUT
    if err := gs.db.First(&existingClassUT, id).Error; err != nil {
        return ClassUT{}, api.MakeServiceError(err)
    }

    existingClassUT.Name = r.name
    existingClassUT.Description = r.description
    existingClassUT.Category = r.category

    if err := gs.db.Save(&existingClassUT).Error; err != nil {
        return ClassUT{}, api.MakeServiceError(err)
    }

    return fromModel(&existingClassUT), nil
}


func (gs *Repository) Delete(id int64) error {
	db := gs.db.
		Where(&model.ClassUT{ID: id}).
		Delete(&model.ClassUT{})

	if db.Error != nil {
		return api.MakeServiceError(db.Error)
	} else if db.RowsAffected < 1 {
		return api.ErrNotFound
	}
	return nil
}