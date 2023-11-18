package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.acme.entity.Fruit;

public class FruitRepository {

    public static Multi<Fruit> findAll(PgPool pgClient) {
        return pgClient.query("SELECT id, name FROM fruits ORDER BY name ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(FruitRepository::from);
    }

    public static Uni<Fruit> findById(PgPool pgClient, Long id) {
        return pgClient.preparedQuery("SELECT id, name FROM fruits WHERE ID = $1")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()): null);
    }

    public static Uni<Long> save(PgPool pgClient, Object name) {
        return pgClient.preparedQuery("INSERT INTO fruits (name) VALUES ($1) RETURNING id")
                .execute(Tuple.of(name))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public static Uni<Boolean> updateById(PgPool pgClient, Object name, Long id) {
        return pgClient.preparedQuery("UPDATE fruits SET name = $1 WHERE id = $2")
                .execute(Tuple.of(name, id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> deleteById(PgPool pgClient, Long id) {
        return pgClient.preparedQuery("DELETE FROM fruits WHERE id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Fruit from(Row row) {
        return new Fruit(row.getString("name"), row.getLong("id"));
    }

}
