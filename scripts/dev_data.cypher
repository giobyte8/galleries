
// Create directories

MERGE (cities:Directory {
    path: 'cities_towns',
    recursive: true,
    status: 'SCAN_PENDING'
})
MERGE (couples:Directory {
    path: 'couples',
    recursive: true,
    status: 'SCAN_PENDING'
})
MERGE (portraits:Directory {
    path: 'portraits',
    recursive: true,
    status: 'SCAN_PENDING'
})

// Enable when video scanning is implemented
//MERGE (d:Directory { path: 'galleries/Cris', recursive: true, status: 'SCAN_PENDING' })


// Create images and add them to directories
//   NOTE: Extra attributes on Image nodes have been
//         ommited in favor of readability

MERGE (i1:Image { path: 'cities_towns/-ZktwJWN_24.jpg', status: 'AVAILABLE' })
MERGE (i2:Image { path: 'cities_towns/0OZFOEjq288.jpg', status: 'AVAILABLE' })
MERGE (i3:Image { path: 'cities_towns/3irmK1KBNX0.jpg', status: 'AVAILABLE' })
MERGE (i4:Image { path: 'cities_towns/FzDQrLFcRs4.jpg', status: 'AVAILABLE' })
MERGE (i5:Image { path: 'cities_towns/Im2nhrQojrk.jpg', status: 'AVAILABLE' })
MERGE (i6:Image { path: 'cities_towns/KMveZDB2r58.jpg', status: 'AVAILABLE' })
MERGE (i7:Image { path: 'cities_towns/bgXekwXoN5o.jpg', status: 'AVAILABLE' })
MERGE (i8:Image { path: 'cities_towns/sbyz9wX9Rrg.jpg', status: 'AVAILABLE' })

MERGE (cities)-[:CONTAINS]->(i1)
MERGE (cities)-[:CONTAINS]->(i2)
MERGE (cities)-[:CONTAINS]->(i3)
MERGE (cities)-[:CONTAINS]->(i4)
MERGE (cities)-[:CONTAINS]->(i5)
MERGE (cities)-[:CONTAINS]->(i6)
MERGE (cities)-[:CONTAINS]->(i7)
MERGE (cities)-[:CONTAINS]->(i8);


// Couples dir
MATCH (couples:Directory { path: 'couples' })
MERGE (c1:Image { path: 'pinterest_484488872426076537.jpg', status: 'AVAILABLE' })
MERGE (couples)-[:CONTAINS]->(c1)

MERGE (c2:Image { path: 'pinterest_484488872426076554.jpg', status: 'AVAILABLE' })
MERGE (couples)-[:CONTAINS]->(c2)

MERGE (c3:Image { path: 'pinterest_484488872426139975.jpg', status: 'AVAILABLE' })
MERGE (couples)-[:CONTAINS]->(c3)

MERGE (c4:Image { path: 'pinterest_484488872426867210.jpg', status: 'AVAILABLE' })
MERGE (couples)-[:CONTAINS]->(c4)

MERGE (c5:Image { path: 'pinterest_484488872428413665.jpg', status: 'AVAILABLE' })
MERGE (couples)-[:CONTAINS]->(c5)

MERGE (c6:Image { path: 'pinterest_484488872426076553.jpg', status: 'AVAILABLE' })
MERGE (couples)-[:CONTAINS]->(c6)

MERGE (c7:Image { path: 'pinterest_484488872426090861.jpg', status: 'AVAILABLE' })
MERGE (couples)-[:CONTAINS]->(c7)

MERGE (c8:Image { path: 'pinterest_484488872426716454.jpg', status: 'AVAILABLE' })
MERGE (couples)-[:CONTAINS]->(c8)

MERGE (c9:Image { path: 'pinterest_484488872427166629.png', status: 'AVAILABLE' })
MERGE (couples)-[:CONTAINS]->(c9)


//
// Graph with multi level relationships between dirs
//
//                              ┌------root------┐
//            ┌---------------dir1---┐          ┌-dir2-┐
//        ┌-dir3-------┐           img4      img5    dir4------┐
//    ┌-dir5--┐     ┌-dir6-┐                                ┌-dir7--┐
//  img6     img7  img8   img9                            img10

CREATE (root:Directory { path: 'root/' })
CREATE (dir1:Directory { path: 'dir1/' })
CREATE (dir2:Directory { path: 'dir2/' })
CREATE (dir3:Directory { path: 'dir3/' })
CREATE (dir4:Directory { path: 'dir4/' })
CREATE (dir5:Directory { path: 'dir5/' })
CREATE (dir6:Directory { path: 'dir6/' })
CREATE (dir7:Directory { path: 'dir7/' })

CREATE (img4:Image { path: 'img4/' })
CREATE (img5:Image { path: 'img5/' })
CREATE (img6:Image { path: 'img6/' })
CREATE (img7:Image { path: 'img7/' })
CREATE (img8:Image { path: 'img8/' })
CREATE (img9:Image { path: 'img9/' })
CREATE (img10:Image { path: 'img10/' })

MERGE (root)-[:CONTAINS]->(dir1)
MERGE (root)-[:CONTAINS]->(dir2)

MERGE (dir1)-[:CONTAINS]->(dir3)
MERGE (dir1)-[:CONTAINS]->(img4)

MERGE (dir2)-[:CONTAINS]->(img5)
MERGE (dir2)-[:CONTAINS]->(dir4)

MERGE (dir3)-[:CONTAINS]->(dir5)
MERGE (dir3)-[:CONTAINS]->(dir6)

MERGE (dir4)-[:CONTAINS]->(dir7)

MERGE (dir5)-[:CONTAINS]->(img6)
MERGE (dir5)-[:CONTAINS]->(img7)

MERGE (dir6)-[:CONTAINS]->(img8)
MERGE (dir6)-[:CONTAINS]->(img9)

MERGE (dir7)-[:CONTAINS]->(img10);
