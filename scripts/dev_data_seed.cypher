
// --- --- --- --- --- --- --- --- ---
// Create directories for main galleries

MERGE (walls_horizontal:Directory {
    path: 'Wallpapers/horizontal',
    recursive: true,
    status: 'SCAN_PENDING'
});

MERGE (walls_vertical:Directory {
    path: 'Wallpapers/vertical',
    recursive: true,
    status: 'SCAN_PENDING'
});
