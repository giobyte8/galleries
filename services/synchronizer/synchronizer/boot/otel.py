from opentelemetry import trace
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
from opentelemetry.sdk.resources import Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from synchronizer import config as cfg


def setup_tracer_provider():
    # All config is done by 'opentelemetry-instrument' wrapper command
    # through env variables and arguments
    pass

    # Init service as a resource
    # resource  = Resource.create({ 'service.name': cfg.otel_svc_name() })
    # processor = BatchSpanProcessor(OTLPSpanExporter(
    #     endpoint=cfg.otel_collector_endpoint()
    # ))

    # provider = TracerProvider(resource=resource)
    # provider.add_span_processor(processor)
    # trace.set_tracer_provider(provider)


def setup_meter_provider():
    pass
