package tikape.reseptiarkisto.domain;

public class AnnosRaakaAine {

    private Integer annos_id;
    private Integer raakaAine_id;
    private Integer jarjestysnumero;
    private String maara;
    private String ohje;

    public AnnosRaakaAine(Integer annos_id, Integer raakaAine_id, Integer jarjestysnumero, String maara, String ohje) {
        this.annos_id = annos_id;
        this.raakaAine_id = raakaAine_id;
        this.jarjestysnumero = jarjestysnumero;
        this.maara = maara;
        this.ohje = ohje;
    }

    public Integer getAnnos_id() {
        return this.annos_id;
    }
    
    public Integer getRaakaAine_id() {
        return this.raakaAine_id;
    }
    
    public Integer getJarjestysnumero() {
        return this.annos_id;
    }
    
    public String getMaara() {
        return this.maara;
    }
    
    public String getOhje() {
        return this.ohje;
    }

}
