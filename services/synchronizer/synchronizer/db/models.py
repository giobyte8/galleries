from enum import Enum
from sqlalchemy import (
    Boolean,
    Column,
    ForeignKey,
    Integer,
    String
)
from sqlalchemy.orm import declarative_base


Base = declarative_base()

class SIRemoteStatus(Enum):
    FOUND = 'Found'
    NOT_FOUND = 'Not found'
    UNKNOWN = 'Unknown'

class HttpSource(Base):
    __tablename__ = 'http_source'

    id = Column(Integer, primary_key=True)
    url = Column(String(5000), nullable=False)
    content_path = Column(String(5000), nullable=False)
    sync_remote_deletes = Column(Boolean, default=True)

class HttpSourceItem(Base):
    __tablename__ = 'http_source_item'

    id = Column(Integer, primary_key=True)
    source_id = Column(Integer, ForeignKey('http_source.id'), nullable=False)
    filename = Column(String(1000), nullable=False)
    remote_status = Column(String(255), nullable=False)
