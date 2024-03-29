package com.zord.recipe.api;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.zord.recipe.api.config.Config;
import com.zord.recipe.api.controllers.RecipeController;
import com.zord.recipe.api.controllers.RecipeControllerImpl;
import com.zord.recipe.api.exceptions.DefaultError;
import com.zord.recipe.api.exceptions.ExistsUserIdException;
import com.zord.recipe.api.exceptions.IdInvalidException;
import com.zord.recipe.api.exceptions.ObjectNotFoundException;
import com.zord.recipe.api.repositories.CommentRepositoryImpl;
import com.zord.recipe.api.repositories.RecipeRepositoryImpl;
import com.zord.recipe.api.services.CommentService;
import com.zord.recipe.api.services.CommentServiceImpl;
import com.zord.recipe.api.services.RecipeService;
import com.zord.recipe.api.services.RecipeServiceImpl;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

import java.io.*;
import java.util.Properties;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RecipeApplication {

    public static void main(String[] args) {
        String pwd = new File("").getAbsolutePath();

        Config config = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(pwd + "/zord-recipe.properties");
            Properties properties = new Properties();
            properties.load(fileInputStream);
            config = new Config(properties);
        } catch (IOException e) {
            System.out.println("Ocorreu um erro ao ler o arquivo de configuração!");
            e.printStackTrace();
            return;
        }

        MongoClient mongoClient = config.getMongoAtlasClient();
        MongoDatabase mongoDatabase = mongoClient.getDatabase(config.getMongoDatabase());

        CommentService commentService = new CommentServiceImpl(new CommentRepositoryImpl(mongoDatabase));
        RecipeService recipeService = new RecipeServiceImpl(new RecipeRepositoryImpl(mongoDatabase));
        RecipeController recipeController = new RecipeControllerImpl(recipeService, commentService);

        Javalin app = Javalin.create().start(config.getApplicationPort());

        app.routes(() -> {
            path("recipe", () -> {
                get(recipeController::get);
                post(recipeController::post);
                path("ingredient", () -> {
                    get(recipeController::getByIngredient);
                });
                path("search", () -> {
                    get(recipeController::getBySearch);
                });
                path("{id}", () -> {
                    get(recipeController::getById);
                    put(recipeController::put);
                    delete(recipeController::delete);
                    path("like", () -> {
                        path("{userId}", () -> {
                            post(recipeController::postLike);
                            delete(recipeController::deleteLike);
                        });
                    });
                    path("comment", () -> {
                        post(recipeController::postComment);
                        path("{commentId}", () -> {
                            put(recipeController::putComment);
                            delete(recipeController::deleteComment);
                        });
                    });
                });
            });
        });

        app.exception(UnrecognizedPropertyException.class, (e, ctx) -> {
           HttpStatus status = HttpStatus.BAD_REQUEST;
           String errorMessage = "Request body inválido. Envie um JSON conforme específicado na documentação!";
           DefaultError error = new DefaultError(String.valueOf(System.currentTimeMillis()), status.toString(), errorMessage, ctx.path());
           ctx.json(error).status(status);
        });

        app.exception(ObjectNotFoundException.class, (e, ctx) -> {
            HttpStatus status = HttpStatus.NOT_FOUND;
            DefaultError error = new DefaultError(String.valueOf(System.currentTimeMillis()), status.toString(), e.getMessage(), ctx.path());
            ctx.json(error).status(status);
        });

        app.exception(ExistsUserIdException.class, (e, ctx) -> {
            HttpStatus status = HttpStatus.CONFLICT;
            DefaultError error = new DefaultError(String.valueOf(System.currentTimeMillis()), status.toString(), e.getMessage(), ctx.path());
            ctx.json(error).status(status);
        });

        app.exception(IdInvalidException.class, (e, ctx) -> {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            DefaultError error = new DefaultError(String.valueOf(System.currentTimeMillis()), status.toString(), e.getMessage(), ctx.path());
            ctx.json(error).status(status);
        });
    }
}
