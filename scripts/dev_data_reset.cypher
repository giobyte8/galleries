// Detach existing data if any
MATCH (n)
DETACH DELETE n;


// --- --- --- --- --- --- --- --- ---
// Create directories for dev galleries

MERGE (walls:Directory {
    id: 'b461fc53-6645-4c71-9b56-842c6d0e6238',
    path: 'Wallpapers',
    recursive: true,
    version: 0,
    status: 'SCAN_PENDING'
});
