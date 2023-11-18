package org.acme.resource;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.acme.FruitRepository;
import org.acme.entity.Fruit;

import java.net.URI;


@Path("fruits")
public class FruitResource {

    private final PgPool pgClient;

    public FruitResource(PgPool pgClient) {
        this.pgClient = pgClient;
    }

    @GET
    public Multi<Fruit> getAllFruits() {
        return FruitRepository.findAll(pgClient);
    }

    @POST
    public Uni<Response> create(Fruit fruit) {
        return FruitRepository.save(pgClient, fruit.getName())
                .onItem().transform(id -> URI.create("/fruits/" + id))
                .onItem().transform(uri -> Response.created(uri).build());
    }

    @GET
    @Path("{id}")
    public Uni<Response> getOneFruit(Long id) {
        return FruitRepository.findById(pgClient, id)
                .onItem().transform(fruit -> fruit != null
                        ? Response.ok(fruit) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(Long id, Fruit fruit) {
        return FruitRepository.updateById(pgClient, fruit.getName(), id)
                .onItem().transform(updated -> updated ? Response.Status.OK : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return FruitRepository.deleteById(pgClient, id)
                .onItem().transform(deleted -> deleted ? Response.Status.OK : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

}
