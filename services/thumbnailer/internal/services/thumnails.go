package services

import (
	"context"
	"log/slog"

	"github.com/giobyte8/galleries/thumbnailer/internal/models"
)

type ThumbnailsService interface {
	ProcessEvent(ctx context.Context, evt models.FileDiscoveryEvent) error
}

type LilliputThumbnailsSvc struct{}

func NewThumbnailsService() *LilliputThumbnailsSvc {
	return &LilliputThumbnailsSvc{}
}

func (s *LilliputThumbnailsSvc) ProcessEvent(
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

	return nil
}
