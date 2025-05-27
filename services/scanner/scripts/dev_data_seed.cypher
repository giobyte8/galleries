
// --- --- --- --- --- --- --- --- ---
// Create directories

MERGE (inspiring_dig_art:Directory {
    path: 'inspiring_digital_art',
    recursive: true,
    status: 'SCAN_PENDING'
})
