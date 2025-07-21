package thumbsgen

import (
	"context"
	"log/slog"
)

type LilliputThumbsGenerator struct{}

func NewLilliputThumbsGenerator() *LilliputThumbsGenerator {
	return &LilliputThumbsGenerator{}
}

func (g *LilliputThumbsGenerator) Generate(
	ctx context.Context,
	meta ThumbnailMeta,
) error {
	slog.Debug(
		"LilliputThumbsGen: Generating thumbnail",
		"thumbFileAbsPath",
		meta.OrigFileAbsPath,
	)

	// inputBuf, err := os.ReadFile(meta.OrigFileAbsPath)
	// if err != nil {
	// 	return fmt.Errorf(
	// 		"failed to read original file %s: %w",
	// 		meta.OrigFileAbsPath,
	// 		err,
	// 	)
	// }

	// // Create lilliput decoder
	// decoder, err := lilliput.NewDecoder(inputBuf)
	// if err != nil {
	// 	return fmt.Errorf(
	// 		"failed to create lilliput decoder for %s: %w",
	// 		meta.OrigFileAbsPath,
	// 		err,
	// 	)
	// }
	// defer decoder.Close()

	// // Get original image dimensions
	// imgHeader, err := decoder.Header()
	// if err != nil {
	// 	return fmt.Errorf(
	// 		"failed to get image header for %s: %w",
	// 		meta.OrigFileAbsPath,
	// 		err,
	// 	)
	// }
	// origWidth := imgHeader.Width()
	// origHeight := imgHeader.Height()
	// if origWidth == 0 || origHeight == 0 {
	// 	return fmt.Errorf(
	// 		"invalid original image dimensions: width=%d, height=%d",
	// 		origWidth,
	// 		origHeight,
	// 	)
	// }

	// Implementation for generating thumbnails using Lilliput
	return nil
}
