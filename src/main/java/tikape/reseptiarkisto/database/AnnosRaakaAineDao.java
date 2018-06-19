/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tikape.reseptiarkisto.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import tikape.reseptiarkisto.domain.AnnosRaakaAine;

public class AnnosRaakaAineDao implements Dao<AnnosRaakaAine, Integer> {

    private Database database;

    public AnnosRaakaAineDao(Database database) {
        this.database = database;
    }

    @Override
    public AnnosRaakaAine findOne(Integer key) throws SQLException {
        //Ei toteutettu
        return null;
    }
    
    @Override
    public List<AnnosRaakaAine> findAll() throws SQLException {
        //Ei toteutettu
        return null;
    }
        
    public List<AnnosRaakaAine> findAllAnnosRaakaAineForAnnos(Integer annosId) throws SQLException {

        Connection conn = database.getConnection();
        
        PreparedStatement stmt
                    = conn.prepareStatement("SELECT RaakaAine.nimi AS nimi, "
                            + "AnnosRaakaAine.jarjestysnumero AS jarjestysnumero, "
                            + "AnnosRaakaAine.maara AS maara, "
                            + "AnnosRaakaAine.ohje AS ohje "
                            + "FROM AnnosRaakaAine, RaakaAine "
                            + "WHERE AnnosRaakaAine.annos_id = ? "
                            + "AND AnnosRaakaAine.raakaAine_id = RaakaAine.id "
                            + "ORDER BY AnnosRaakaAine.jarjestysnumero");
        stmt.setInt(1,annosId);
        ResultSet rs = stmt.executeQuery();
            
        List<AnnosRaakaAine> annosRaakaAineet = new ArrayList<>();
            
        while(rs.next()) {
            String nimi = rs.getString("nimi");
            Integer jarjestysnumero = rs.getInt("jarjestysnumero");
            String maara = rs.getString("maara");
            String ohje = rs.getString("ohje");
            annosRaakaAineet.add(new AnnosRaakaAine(-1, -1, jarjestysnumero, maara, ohje, nimi));
}

        rs.close();
        stmt.close();
        conn.close();

        return annosRaakaAineet;
    }
    

    @Override
    public void delete(Integer key) throws SQLException {
        // Ei toteutettu
    }
    
    public void deleteAnnos(Integer annosId) throws SQLException {
        // avaa yhteys tietokantaan
        Connection conn = database.getConnection();
            
        // tee kysely
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM AnnosRaakaAine WHERE annos_id = ?");
        stmt.setInt(1, annosId);
        stmt.executeUpdate();
        
        stmt.close();
        conn.close();
    }
    
    public void deleteRaakaAine(Integer raakaAineId) throws SQLException {
        // avaa yhteys tietokantaan
        Connection conn = database.getConnection();
            
        // tee kysely
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM AnnosRaakaAine WHERE raakaAine_id = ?");
        stmt.setInt(1, raakaAineId);
        stmt.executeUpdate();
        
        stmt.close();
        conn.close();
    }
    
    public int countAllAnnosForRaakaAine(Integer raakaAineId) throws SQLException {
        int annoksia = 0;
        
        Connection conn = database.getConnection();
        
        PreparedStatement stmt 
                = conn.prepareStatement("SELECT COUNT(annos_id) AS annoksia "
                        + "FROM AnnosRaakaAine WHERE raakaAine_id = ? ");
        stmt.setInt(1, raakaAineId);
        ResultSet rs = stmt.executeQuery();
        
        if(rs.next()) {
            annoksia = rs.getInt("annoksia");
        }
        
        rs.close();
        stmt.close();
        conn.close();
        
        return annoksia;
        
    }
    
    @Override
    public void save(AnnosRaakaAine annosaine) throws SQLException {
        if(annosaine.getRaakaAine_id() == null || annosaine.getAnnos_id() == null || annosaine.getJarjestysnumero() == null || annosaine.getMaara() == null) {
            return;
        }
        
        Connection conn = database.getConnection();
        
        PreparedStatement test 
                = conn.prepareStatement("SELECT * FROM AnnosRaakaAine "
                + "WHERE raakaAine_id = ? AND annos_id = ? ");
        test.setInt(1, annosaine.getRaakaAine_id());
        test.setInt(2, annosaine.getAnnos_id());
        ResultSet rs = test.executeQuery();
        
        if(rs.next()) {
            return;
        }
        
        PreparedStatement stmt 
                = conn.prepareStatement("INSERT INTO AnnosRaakaAine "
                + "(raakaAine_id, annos_id, jarjestysnumero, maara, ohje) VALUES "
                + "(?, ?, ?, ?, ?)");
        stmt.setInt(1, annosaine.getRaakaAine_id());
        stmt.setInt(2, annosaine.getAnnos_id());
        stmt.setInt(3, annosaine.getJarjestysnumero());
        stmt.setString(4, annosaine.getMaara());
        stmt.setString(5, annosaine.getOhje());
        
        stmt.executeUpdate();
        
        stmt.close();
        conn.close();
    }
}
