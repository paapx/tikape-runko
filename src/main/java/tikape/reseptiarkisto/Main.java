package tikape.reseptiarkisto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import spark.ModelAndView;
import spark.Spark;
import static spark.Spark.*;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import tikape.reseptiarkisto.database.Database;
import tikape.reseptiarkisto.database.AnnosDao;
import tikape.reseptiarkisto.database.AnnosRaakaAineDao;
import tikape.reseptiarkisto.database.RaakaAineDao;
import tikape.reseptiarkisto.domain.Annos;
import tikape.reseptiarkisto.domain.AnnosRaakaAine;
import tikape.reseptiarkisto.domain.RaakaAine;

public class Main {

    public static void main(String[] args) throws Exception {
        
        // aseta portti jos heroku antaa PORT-ympäristömuuttujan
        if (System.getenv("PORT") != null) {
            Spark.port(Integer.valueOf(System.getenv("PORT")));
        }
        
        // hae Herokun tietokannan osoite
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        
        // tee Database-olio
        Database database = new Database(dbUrl);

        // tee Dao-oliot
        AnnosDao annosDao = new AnnosDao(database);
        RaakaAineDao raakaAineDao = new RaakaAineDao(database);
        AnnosRaakaAineDao annosRaakaAineDao = new AnnosRaakaAineDao(database);

        // näyttää kaikki reseptit
        Spark.get("/", (req, res) -> {
            
            List<Annos> reseptit = annosDao.findAll();
            HashMap map = new HashMap<>();
            map.put("annokset", reseptit);
            
            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());

        // näyttää reseptilisäyksen
        Spark.get("/reseptit", (req, res) -> {
            
            List<Annos> reseptit = annosDao.findAll();
            List<RaakaAine> raakaAineet = raakaAineDao.findAll();
            HashMap map = new HashMap<>();
            map.put("annokset", reseptit);
            map.put("raakaAineet",raakaAineet);

            return new ModelAndView(map, "reseptit");
        }, new ThymeleafTemplateEngine());

        // lisää uuden reseptin reseptilistaukseen
        Spark.post("/reseptit/lisaa-resepti", (req, res) -> {
            annosDao.save(new Annos(-1, req.queryParams("annos")));
            
            res.redirect("/reseptit");
            
            return "";
        });
        
        /*
        // näyttää reseptin
        Spark.get("/resepti/:annosId", (req, res) -> {
            
            Integer annosId = Integer.parseInt(req.params(":annosId"));
            Annos annos = annosDao.findOne(annosId);
            
            List<RaakaAine> raakaAineet = annosRaakaAineDao.findAllRaakaAineInAnnos(annosId);

            HashMap map = new HashMap<>();
            
            map.put("annos", annos);
            map.put("annosRaakaAineet", raakaAineet);

            return new ModelAndView(map, "resepti");
        }, new ThymeleafTemplateEngine());
        */
            
        // näyttää reseptin
        Spark.get("/resepti/:annosId", (req, res) -> {
            
            Integer annosId = Integer.parseInt(req.params(":annosId"));
            Annos annos = annosDao.findOne(annosId);
            
            List<AnnosRaakaAine> annosRaakaAineet = annosRaakaAineDao.findAllAnnosRaakaAineForAnnos(annosId);
            HashMap map = new HashMap<>();
            
            map.put("annos", annos);
            map.put("annosRaakaAineet", annosRaakaAineet);

            return new ModelAndView(map, "resepti");
        }, new ThymeleafTemplateEngine());
        
        
        
        /*
        Spark.post("/reseptit", (req, res) -> {

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
        */
        
        // Poistaa reseptin
        Spark.post("/reseptit/:annosId/delete", (req, res) -> {
            
            Integer annosId = Integer.parseInt(req.params(":annosId"));
            annosRaakaAineDao.deleteAnnos(annosId);
            annosDao.delete(annosId);
        
            res.redirect("/reseptit");
            return "";
        });
        
        // Poistaa raaka-aineen
        Spark.post("/:raakaAineId/delete", (req, res) -> {
            
            Integer raakaAineId = Integer.parseInt(req.params(":raakaAineId"));
            annosRaakaAineDao.deleteRaakaAine(raakaAineId);
            raakaAineDao.delete(raakaAineId);
        
            res.redirect("/raaka-aineet");
            return "";
        });
  
        // näyttää kaikki raaka-aineet
        Spark.get("/raaka-aineet", (req, res) -> {
            
            List<RaakaAine> raakaAineet = raakaAineDao.findAll();
            HashMap<RaakaAine, Integer> esiintymiskerrat = new HashMap<>();
            
            for(RaakaAine rAine : raakaAineet) {
                Integer annoksia = annosRaakaAineDao.countAllAnnosForRaakaAine(rAine.getId());
                esiintymiskerrat.put(rAine,annoksia);
            }
            
            Main.sortByValue(esiintymiskerrat);
            
            HashMap map = new HashMap<>();
            map.put("raakaAineet", raakaAineet);
            map.put("esiintymiskerrat", esiintymiskerrat);

            return new ModelAndView(map, "raaka-aineet");
        }, new ThymeleafTemplateEngine());

        
        /*
        Spark.get("/raaka-aineet/:id", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("raakaAine", raakaAineDao.findOne(Integer.parseInt(req.params(":id"))));

            return new ModelAndView(map, "raakaAine");
        }, new ThymeleafTemplateEngine());
        */
        
        
        Spark.post("/reseptit/lisaa-raaka-aine", (req, res) -> {
            Integer raakaAineId = Integer.parseInt(req.queryParams("raakaAineId"));
            Integer annosId = Integer.parseInt(req.queryParams("annosId"));
            Integer jarjestysnumero = Integer.parseInt(req.queryParams("jarjestysnumero"));
            String maara = req.queryParams("maara");
            String ohje = req.queryParams("ohje");
            
            annosRaakaAineDao.save(new AnnosRaakaAine(raakaAineId, annosId, "esimerkkiNimi", jarjestysnumero,
                    maara, ohje));
            
            res.redirect("/reseptit");
            
            return "";
        });
        
        // lisää raaka-aineen
        Spark.post("/raaka-aineet", (req, res) -> {
            
            raakaAineDao.save(new RaakaAine(-1,req.queryParams("raakaAine")));

            res.redirect("/raaka-aineet");
            return "";
        });
        
        // poista raaka-aine
        Spark.post("/raaka-aineet/:raakaAineId/delete", (req, res) -> {
            
            Integer raakaAineId = Integer.parseInt(req.params(":raakaAineId"));
            
            annosRaakaAineDao.deleteRaakaAine(raakaAineId);
            raakaAineDao.delete(raakaAineId);
            
            res.redirect("/raaka-aineet");
            return "";
        });
        

        /*
        get("/resepti/:annosId/raaka-aineet/:id", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("raakaAine", annosRaakaAineDao.findOne(Integer.parseInt(req.params(":id"))));

            return new ModelAndView(map, "raakaAine");
        }, new ThymeleafTemplateEngine());
        */
        
        /*
        Spark.post("/resepti/:annosId", (req, res) -> {
            // avaa yhteys tietokantaan
            Connection conn = database.getConnection();
            
            // tee kysely
            PreparedStatement stmt
                    = conn.prepareStatement("INSERT INTO RaakaAine (nimi) VALUES (?)");
            stmt.setString(1, req.queryParams("raakaAine"));

            stmt.executeUpdate();

            // sulje yhteys tietokantaan
            conn.close();

            res.redirect("/resepti/:annosId");
            return "";
        });
        */
        
        /*
        Spark.post("/resepti/:annosId/raaka-aineet/:raakaAineId/delete", (req, res) -> {
            
            raakaAineDao.delete(Integer.parseInt(req.params(":raakaAineId")));
        
            res.redirect("/resepti/:annosId/raaka-aineet");
            return "";
        });
        */
        
    }
    
    // https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
