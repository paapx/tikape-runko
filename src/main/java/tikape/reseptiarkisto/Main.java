package tikape.reseptiarkisto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import spark.ModelAndView;
import spark.Spark;
import static spark.Spark.*;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import tikape.reseptiarkisto.database.Database;
import tikape.reseptiarkisto.database.AnnosDao;
import tikape.reseptiarkisto.database.RaakaAineDao;

public class Main {

    public static void main(String[] args) throws Exception {
        
        // asetetaan portti jos heroku antaa PORT-ympäristömuuttujan
        if (System.getenv("PORT") != null) {
            Spark.port(Integer.valueOf(System.getenv("PORT")));
        }
        
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        
        Database database = new Database(dbUrl);

        AnnosDao annosDao = new AnnosDao(database);

        get("/", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("viesti", ":");

            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());

        get("/annokset", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("annokset", annosDao.findAll());

            return new ModelAndView(map, "annokset");
        }, new ThymeleafTemplateEngine());

        get("/annokset/:id", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("annos", annosDao.findOne(Integer.parseInt(req.params("id"))));

            return new ModelAndView(map, "annos");
        }, new ThymeleafTemplateEngine());
        
        
        
        RaakaAineDao raakaAineDao = new RaakaAineDao(database);

        get("/", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("viesti", ":");

            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());

        get("/raaka-aineet", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("raakaAineet", raakaAineDao.findAll());

            return new ModelAndView(map, "raakaAineet");
        }, new ThymeleafTemplateEngine());

        get("/raaka-aineet/:id", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("raakaAine", annosDao.findOne(Integer.parseInt(req.params("id"))));

            return new ModelAndView(map, "raakaAine");
        }, new ThymeleafTemplateEngine());
    }
}
