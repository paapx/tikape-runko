package tikape.reseptiarkisto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import spark.ModelAndView;
import spark.Spark;
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

        // Näyttää kaikki reseptit
        Spark.get("/", (req, res) -> {
            
            List<Annos> reseptit = annosDao.findAll();
            HashMap map = new HashMap<>();
            map.put("annokset", reseptit);
            
            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());

        // Näyttää reseptilisäyksen
        Spark.get("/reseptit", (req, res) -> {
            
            List<Annos> reseptit = annosDao.findAll();
            List<RaakaAine> raakaAineet = raakaAineDao.findAll();
            HashMap map = new HashMap<>();
            map.put("annokset", reseptit);
            map.put("raakaAineet",raakaAineet);

            return new ModelAndView(map, "reseptit");
        }, new ThymeleafTemplateEngine());

        // Lisää uuden reseptin reseptilistaukseen
        Spark.post("/reseptit/lisaa-resepti", (req, res) -> {
            annosDao.save(new Annos(-1, req.queryParams("annos")));
            
            res.redirect("/reseptit");
            
            return "";
        });
        
      
        // Näyttää reseptin
        Spark.get("/resepti/:annosId", (req, res) -> {
            
            Integer annosId = Integer.parseInt(req.params(":annosId"));
            Annos annos = annosDao.findOne(annosId);
            
            List<AnnosRaakaAine> annosRaakaAineet = annosRaakaAineDao.findAllAnnosRaakaAineForAnnos(annosId);
            HashMap map = new HashMap<>();
            
            map.put("annos", annos);
            map.put("annosRaakaAineet", annosRaakaAineet);

            return new ModelAndView(map, "resepti");
        }, new ThymeleafTemplateEngine());
        
        
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
            
            Main.sortByArvo(esiintymiskerrat);
            
            List<RaakaAine> sortedRaakaAineet = new ArrayList<>();
            
            for(RaakaAine rAine : Main.sortByArvo(esiintymiskerrat).keySet()) {
                sortedRaakaAineet.add(rAine);
            }
            
            HashMap map = new HashMap<>();
            map.put("raakaAineet", sortedRaakaAineet);
            map.put("esiintymiskerrat", Main.sortByArvo(esiintymiskerrat));

            return new ModelAndView(map, "raaka-aineet");
        }, new ThymeleafTemplateEngine());
        
        
        Spark.post("/reseptit/lisaa-raaka-aine", (req, res) -> {
            Integer raakaAineId = Integer.parseInt(req.queryParams("raakaAineId"));
            Integer annosId = Integer.parseInt(req.queryParams("annosId"));
            Integer jarjestysnumero = Integer.parseInt(req.queryParams("jarjestysnumero"));
            String maara = req.queryParams("maara");
            String ohje = req.queryParams("ohje");
            
            annosRaakaAineDao.save(new AnnosRaakaAine(raakaAineId, annosId, jarjestysnumero,
                    maara, ohje, "esimerkkiNimi"));
            
            res.redirect("/reseptit");
            
            return "";
        });
        
        
        // Lisää raaka-aineen
        Spark.post("/raaka-aineet", (req, res) -> {
            
            raakaAineDao.save(new RaakaAine(-1,req.queryParams("raakaAine")));

            res.redirect("/raaka-aineet");
            return "";
        });
        
        // Poistaa raaka-aineen
        Spark.post("/raaka-aineet/:raakaAineId/delete", (req, res) -> {
            
            Integer raakaAineId = Integer.parseInt(req.params(":raakaAineId"));
            
            annosRaakaAineDao.deleteRaakaAine(raakaAineId);
            raakaAineDao.delete(raakaAineId);
            
            res.redirect("/raaka-aineet");
            return "";
        });    
    }
    
    
    // Järjestää hajautustaulun arvojen mukaan suurimmasta arvosta pienimpään
    public static <Avain, Arvo extends Comparable<? super Arvo>> Map<Avain, Arvo> sortByArvo(Map<Avain, Arvo> hajautustaulu) {
        
        List<Entry<Avain, Arvo>> lista = new ArrayList<>(hajautustaulu.entrySet());
        lista.sort(Entry.comparingByValue());
        Collections.reverse(lista);
        Map<Avain, Arvo> jarjestettyHajautustaulu = new LinkedHashMap<>();
        
        for (Entry<Avain, Arvo> entry : lista) {
            jarjestettyHajautustaulu.put(entry.getKey(), entry.getValue());
        }

        return jarjestettyHajautustaulu;
    }
}
