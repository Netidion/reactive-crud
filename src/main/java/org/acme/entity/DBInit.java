package org.acme.entity;

import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DBInit {

    private final PgPool pgClient;

    private final boolean schemaCreate;

    public DBInit(PgPool pgClient,
                  @ConfigProperty(name = "myapp.schema.create", defaultValue = "true") boolean schemaCreate) {
        this.pgClient = pgClient;
        this.schemaCreate = schemaCreate;
    }

    void onStart(@Observes StartupEvent startupEvent) {
        if (schemaCreate) {
            initdb();
        }
    }

    private void initdb() {
        pgClient.query("DROP TABLE IF EXISTS fruits").execute()
                .flatMap(r -> pgClient.query("CREATE TABLE fruits (id SERIAL PRIMARY KEY, name TEXT NOT NULL)").execute())
                .flatMap(r -> pgClient.query("INSERT INTO fruits (name) VALUES ('Cherry')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO fruits (name) VALUES ('Apple')").execute())
                .flatMap(r -> pgClient.query("INSERT INTO fruits (name) VALUES ('Banana')").execute())
                .await().indefinitely();
    }
}