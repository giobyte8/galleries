

// --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
// Clean up

// Delete all Directory,Image,ScanStats and Video nodes
MATCH (n:Directory)
DETACH DELETE n;

MATCH (n:Image)
DETACH DELETE n;

MATCH (n:ScanStats)
DETACH DELETE n;

MATCH (n:Video)
DETACH DELETE n;

// --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
// Clean up all images, videos and directories contained in a directory

// Count before delete
MATCH (d:Directory {path: 'cameras/iPhone-RO'})-[:CONTAINS*..5000]->(i:Image)
RETURN COUNT(i);

MATCH (d:Directory {path: 'cameras/iPhone-RO'})-[:CONTAINS*..5000]->(i:Image)
DETACH DELETE i;

MATCH (d:Directory {path: 'cameras/iPhone-RO'})-[:CONTAINS*..5000]->(v:Video)
DETACH DELETE v;

MATCH (d:Directory {path: 'cameras/iPhone-RO'})-[:CONTAINS*..5000]->(child:Directory)
DETACH DELETE child;


// --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
// Seeding

// Create a Directory
MERGE (glr:Directory {
    id: '9287a3bc-6e9d-4e61-9165-3aebae03f6d8',
    path: 'galleries.r',
    recursive: true,
    version: 0,
    status: 'SCAN_PENDING'
});

MERGE (iphone:Directory {
    id: '2cb1d234-900b-4e10-a947-8f5c1c5a3dcb',
    path: 'cameras/iPhone-RO',
    recursive: true,
    version: 0,
    status: 'SCAN_PENDING'
});

MERGE (ss:Directory {
    id: '9eba861c-793d-45a4-9c2b-2f8c02ede732',
    path: 'Screenshots',
    recursive: true,
    version: 0,
    status: 'SCAN_PENDING'
});
