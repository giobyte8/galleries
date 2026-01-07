
// Create a Directory node in the graph
MERGE (walls_horizontal:Directory {
    path: 'Wallpapers/horizontal',
    recursive: true,
    status: 'SCAN_PENDING'
});

// Update an existing Directory node's status
MATCH (d:Directory {path: 'Wallpapers/horizontal'})
SET d.status = 'SCAN_PENDING'
RETURN d;
