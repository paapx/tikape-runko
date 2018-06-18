package tikape.reseptiarkisto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import spark.ModelAndView;
import static spark.Spark.*;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import tikape.reseptiarkisto.database.Database;
import tikape.reseptiarkisto.database.AnnosDao;

public class Main {

    public static void main(String[] args) throws Exception {
        
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        
        Database database = new Database(dbUrl);

        AnnosDao annosDao = new AnnosDao(database);

        get("/", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("viesti", "tervehdys");

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
    }
}
