
// Drop all existing media
MATCH (n:ScanStats) DETACH DELETE n;
MATCH (n:Image) DETACH DELETE n;
MATCH (n:Video) DETACH DELETE n;
MATCH (n:Directory) DETACH DELETE n;

MERGE (iphone:Directory {
    id: '74156b41-ea23-45e0-ba28-ee7673d39980',
    path: 'cameras/iPhone-RO',
    recursive: true,
    version: 0,
    status: 'SCAN_PENDING'
});

MERGE (galleries:Directory {
    id: 'd037703f-109b-44d1-ae5e-a8e2d6bc1f65',
    path: 'galleries.r',
    recursive: true,
    version: 0,
    status: 'SCAN_PENDING'
});


// Create a Directory node in the graph
MERGE (galleries:Directory {
    id: 'd037703f-109b-44d1-ae5e-a8e2d6bc1f65',
    path: 'galleries.r',
    recursive: true,
    version: 0,
    status: 'SCAN_PENDING'
});

// Update an existing Directory node's status
MATCH (d:Directory {path: 'Wallpapers/horizontal'})
SET d.status = 'SCAN_PENDING'
RETURN d;


// --- --- ---
// Queries

// Images without a captureDateTime
MATCH (n:Image) WHERE n.captureDateTime IS NULL RETURN n;

// No captureDateTime in given path prefix
MATCH (n:Image)
WHERE n.path STARTS WITH 'cameras/iPhone-RO/'
  AND n.captureDateTime IS NULL
RETURN n;
