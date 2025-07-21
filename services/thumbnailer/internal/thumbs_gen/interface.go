package thumbsgen

import (
	"context"
)

const ThumbsExtension = ".jpg"
const ThumbsQuality = 85

type ThumbnailMeta struct {
	OrigFileAbsPath string
	ThumbFileAbsDir string
	ThumbWidths     []int
}

type ThumbsGenerator interface {
	Generate(ctx context.Context, meta ThumbnailMeta) error
}
