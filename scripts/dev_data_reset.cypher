// Detach existing data if any
MATCH (n)
DETACH DELETE n;


// --- --- --- --- --- --- --- --- ---
// Create directories for dev galleries
//
//MERGE (walls:Directory {
//    id: 'b461fc53-6645-4c71-9b56-842c6d0e6238',
//    path: 'Wallpapers',
//    recursive: true,
//    version: 0,
//    status: 'SCAN_PENDING'
//});

// dev_data_reset.sh script will replace below marker with
// directories defined in .env file
// __DIR_MERGES__


// --- ---
// Create development user
//   Use `./encrypt_password.sh` to generate a valid password hash.
//   For dev purposes we use '123' here. Use something different in prod

MERGE (u:AppUser {
    id: '6e8c16e3-5b43-4f35-8c5b-737b23b422a1',
    username: 'dev',
    password: '$2b$12$6FyCmnc2pfpz2VG7NkNrweerGhUD1JIY.OcdobKApijS9Iu/MtTri',
    roles: ['ROLE_ADMIN'],
    enabled: true,
    version: 0
});

