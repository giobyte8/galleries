from synchronizer.boot import logs, otel


logs.init_logging()

otel.setup_tracer_provider()
otel.setup_meter_provider()
