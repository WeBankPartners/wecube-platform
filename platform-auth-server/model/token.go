package model

import (
	"errors"
	"time"
)

var (
	ErrInvalidClaim = errors.New("invalid claim")
)

type AuthClaims struct {
	Subject      string   `json:"sub"`
	IssuedAt     int64    `json:"iat"`
	ExpiresAt    int64    `json:"exp"`
	Type         string   `json:"type"`
	ClientType   string   `json:"clientType"`
	Roles        []string `json:"roles"`
	Authority    string   `json:"authority"`
	NeedRegister bool     `json:"needRegister"`
	//Authority   string   `json:"authority"`
	//Auth        []model.AggAuth `json:"auth"`
	//LoginType string `json:"loginType"`
	//AdminType string `json:"adminType"`
	//UserName  string `json:"userName"`
}

func (c AuthClaims) Valid() error {
	now := time.Now().UTC()
	exp := time.Unix(c.ExpiresAt, 0).UTC()
	if now.After(exp) {
		return ErrInvalidClaim
	}

	iat := time.Unix(c.IssuedAt, 0).UTC()
	if now.Before(iat) {
		return ErrInvalidClaim
	}
	return nil
}
