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


// --- ---
// Create development user

MERGE (u:AppUser {
    id: '6e8c16e3-5b43-4f35-8c5b-737b23b422a1',
    username: 'dev_admin',
    password: '$2a$10$REPLACE_WITH_OUTPUT_OF_encrypt_password_sh',
    roles: ['ROLE_ADMIN'],
    enabled: true,
    version: 0
});

