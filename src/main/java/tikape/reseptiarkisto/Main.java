package tikape.reseptiarkisto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import spark.ModelAndView;
import spark.Spark;
import static spark.Spark.*;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import tikape.reseptiarkisto.database.Database;
import tikape.reseptiarkisto.database.AnnosDao;
import tikape.reseptiarkisto.database.AnnosRaakaAineDao;
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

        Spark.get("/", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("annokset", annosDao.findAll());
            
            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());

        Spark.get("/reseptit", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("annokset", annosDao.findAll());

            return new ModelAndView(map, "reseptit");
        }, new ThymeleafTemplateEngine());

        Spark.get("/reseptit/:id", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("annos", annosDao.findOne(Integer.parseInt(req.params(":id"))));

            return new ModelAndView(map, "annos");
        }, new ThymeleafTemplateEngine());
        
        Spark.post("/reseptit", (req, res) -> {
            System.out.println("Hei maailma!");
            System.out.println("Saatiin: "
                    + req.queryParams("annos"));

            // avaa yhteys tietokantaan
            Connection conn = database.getConnection();
            
            // tee kysely
            PreparedStatement stmt
                    = conn.prepareStatement("INSERT INTO Annos (nimi) VALUES (?)");
            stmt.setString(1, req.queryParams("annos"));

            stmt.executeUpdate();

            // sulje yhteys tietokantaan
            conn.close();

            res.redirect("/reseptit");
            return "";
        });
        
        Spark.post("/:annosId/delete", (req, res) -> {
            
            annosDao.delete(Integer.parseInt(req.params(":annosId")));
        
            res.redirect("/reseptit");
            return "";
        });
        
        
        
        //RAAKA-AINEET
        
        RaakaAineDao raakaAineDao = new RaakaAineDao(database);

        get("/raaka-aineet", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("raakaAineet", raakaAineDao.findAll());

            return new ModelAndView(map, "raaka-aineet");
        }, new ThymeleafTemplateEngine());

        get("/raaka-aineet/:id", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("raakaAine", raakaAineDao.findOne(Integer.parseInt(req.params(":id"))));

            return new ModelAndView(map, "raakaAine");
        }, new ThymeleafTemplateEngine());
        
        Spark.post("/raaka-aineet", (req, res) -> {
            System.out.println("Hei maailma!");
            System.out.println("Saatiin: "
                    + req.queryParams("raakaAine"));

            // avaa yhteys tietokantaan
            Connection conn = database.getConnection();
            
            // tee kysely
            PreparedStatement stmt
                    = conn.prepareStatement("INSERT INTO RaakaAine (nimi) VALUES (?)");
            stmt.setString(1, req.queryParams("raakaAine"));

            stmt.executeUpdate();

            // sulje yhteys tietokantaan
            conn.close();

            res.redirect("/raaka-aineet");
            return "";
        });
        
        Spark.post("/:raakaAineId/delete", (req, res) -> {
            
            raakaAineDao.delete(Integer.parseInt(req.params(":raakaAineId")));
        
            res.redirect("/raaka-aineet");
            return "";
        });
        
        
        
        //ANNOSRAAKA-AINEET
        
        AnnosRaakaAineDao annosRaakaAineDao = new AnnosRaakaAineDao(database);

        get("/resepti/:annosId/raaka-aineet", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("annosRaakaAineet", annosRaakaAineDao.etsiAnnoksenRaakaAineet(Integer.parseInt(req.params(":annosId"))));

            return new ModelAndView(map, "resepti");
        }, new ThymeleafTemplateEngine());

        /*
        get("/resepti/:annosId/raaka-aineet/:id", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("raakaAine", annosRaakaAineDao.findOne(Integer.parseInt(req.params(":id"))));

            return new ModelAndView(map, "raakaAine");
        }, new ThymeleafTemplateEngine());
        */
        
        
        Spark.post("/resepti/:annosId/raaka-aineet", (req, res) -> {
            System.out.println("Hei maailma!");
            System.out.println("Saatiin: "
                    + req.queryParams("raakaAine"));

            // avaa yhteys tietokantaan
            Connection conn = database.getConnection();
            
            // tee kysely
            PreparedStatement stmt
                    = conn.prepareStatement("INSERT INTO RaakaAine (nimi) VALUES (?)");
            stmt.setString(1, req.queryParams("raakaAine"));

            stmt.executeUpdate();

            // sulje yhteys tietokantaan
            conn.close();

            res.redirect("/resepti/:annosId/raaka-aineet");
            return "";
        });
        
        /*
        Spark.post("/resepti/:annosId/raaka-aineet/:raakaAineId/delete", (req, res) -> {
            
            raakaAineDao.delete(Integer.parseInt(req.params(":raakaAineId")));
        
            res.redirect("/resepti/:annosId/raaka-aineet");
            return "";
        });
        */
        
    }
}
