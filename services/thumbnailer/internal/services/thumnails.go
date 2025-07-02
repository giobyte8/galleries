package services

import (
	"context"
	"fmt"
	"log/slog"
	"os"
	"path/filepath"
	"strings"

	"github.com/giobyte8/galleries/thumbnailer/internal/models"
)

type ThumbnailsConfig struct {
	DirOriginalsRoot  string
	DirThumbnailsRoot string
	ThumbnailWidths   []int
}

type ThumbnailsService interface {
	ProcessEvent(ctx context.Context, evt models.FileDiscoveryEvent) error
}

type LilliputThumbsSvc struct {
	config ThumbnailsConfig
}

func NewLilliputThumbsSvc(config ThumbnailsConfig) *LilliputThumbsSvc {
	return &LilliputThumbsSvc{
		config: config,
	}
}

func (s *LilliputThumbsSvc) ProcessEvent(
	ctx context.Context,
	evt models.FileDiscoveryEvent,
) error {
	slog.Info(
		"Processing file discovery event",
		"eventType",
		evt.EventType,
		"filePath",
		evt.FilePath,
	)

	err := s.cleanupExisting(ctx, evt.FilePath)
	if err != nil {
		slog.Error(
			"ThumbsSvc: Failed to cleanup existing thumbnails",
			"error",
			err,
			"filePath",
			evt.FilePath,
		)
	}

	return nil
}

func (s *LilliputThumbsSvc) cleanupExisting(
	ctx context.Context,
	origFilePath string,
) error {
	// TODO Make it configurable?
	thumbnailsExt := ".jpg"

	// Determine sub directory for thumbnails
	origDir := filepath.Dir(origFilePath)
	thumbsDir := filepath.Join(s.config.DirThumbnailsRoot, origDir)
	if _, err := os.Stat(thumbsDir); os.IsNotExist(err) {
		return nil
	}

	// Prepare wildcard pattern to match existing thumbnails
	baseName := filepath.Base(origFilePath)
	ext := filepath.Ext(baseName)
	fileNameNoExt := strings.TrimSuffix(baseName, ext)
	pattern := filepath.Join(
		thumbsDir,
		fmt.Sprintf("%s_*px%s", fileNameNoExt, thumbnailsExt),
	)

	// Find files matching the pattern
	matches, err := filepath.Glob(pattern)
	if err != nil {
		return fmt.Errorf(
			"failed to glob for existing thumbnails with pattern %s: %w",
			pattern,
			err,
		)
	}

	// Remove each file mathing pattern
	for _, matchPath := range matches {
		select {
		case <-ctx.Done():
			slog.Warn(
				"ThumbsSvc: Context cancelled during thumbnail cleanup.",
				"path",
				matchPath,
			)
			return ctx.Err()
		default:
			// Continue with deletion
		}

		slog.Debug("ThumbsSvc: Removing existing thumbnail", "path", matchPath)
		if err := os.Remove(matchPath); err != nil {
			return fmt.Errorf(
				"failed to remove existing thumbnail %s: %w",
				matchPath,
				err,
			)
		}

		// TODO Remove direcotory if empty after removing thumbnails
	}

	return nil
}
