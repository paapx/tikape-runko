package tikape.reseptiarkisto.domain;

public class AnnosRaakaAine {

    private Integer annos_id;
    private Integer raakaAine_id;
    private Integer jarjestysnumero;
    private String maara;
    private String ohje;
    private String raakaAineenNimi;

    public AnnosRaakaAine(Integer raakaAine_id, Integer annos_id, Integer jarjestysnumero, String maara, String ohje, String raakaAineenNimi) {
        this.annos_id = annos_id;
        this.raakaAine_id = raakaAine_id;
        this.jarjestysnumero = jarjestysnumero;
        this.maara = maara;
        this.ohje = ohje;
        this.raakaAineenNimi = raakaAineenNimi;
        
    }

    public Integer getAnnos_id() {
        return this.annos_id;
    }
    
    public Integer getRaakaAine_id() {
        return this.raakaAine_id;
    }
    
    public Integer getJarjestysnumero() {
        return this.jarjestysnumero;
    }
    
    public String getMaara() {
        return this.maara;
    }
    
    public String getOhje() {
        return this.ohje;
    }
    
    public String getNimi() {
        return this.raakaAineenNimi;
    }

}
