package utils

import (
	"math"
	"strings"
)

func Contains(s []string, e string) bool {
	for _, a := range s {
		if a == e {
			return true
		}
	}
	return false
}

func ContainSlice(s1 []string, s2 []string) bool {
	if len(s1) < len(s2) {
		return false
	}
	for _, s := range s2 {
		if !Contains(s1, s) {
			return false
		}
	}
	return true
}

func ContainAny(s1 []string, s2 []string) bool {
	for _, s := range s2 {
		if Contains(s1, s) {
			return true
		}
	}
	return false
}

func SafeGetString(m map[string]any, key string) string {
	valueObj, has := m[key]
	if !has {
		return ""
	}
	valueStr, ok := valueObj.(string)
	if !ok {
		return ""
	}
	return valueStr
}

func SafeGetMap(m map[string]any, key string) map[string]any {
	valueObj, has := m[key]
	if !has {
		return nil
	}
	valueMap, ok := valueObj.(map[string]any)
	if !ok {
		return nil
	}
	return valueMap
}

func RoundFloat(val float64, precision int) float64 {
	ratio := math.Pow(10, float64(precision))
	return math.Round(val*ratio) / ratio
}

func CheckDecimalPlaces(val float64, decimal int) bool {
	roundedVal := RoundFloat(val, decimal)
	diff := val - roundedVal
	return diff == 0.0
}

func IsBlank(s string) bool {
	return strings.TrimSpace(s) == ""
}
