package me.giobyte8.galleries.persistence.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@Node("AppUser")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    private String username;
    private String password;
    private List<String> roles;
    private boolean enabled;

    @Version
    private Long version;
}

