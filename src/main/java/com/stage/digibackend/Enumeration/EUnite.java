package com.stage.digibackend.Enumeration;

public enum EUnite {
    DEGREE_CELSIUS("°C"),
    MILI_AMPER("mA"),
    MILI_VOLT("mV"),
    MILI_GRAM_Par_LITRE("mg/l"),
    MILI_SEC_PAR_CM("mS/cm"),
    P_P_M("ppm"),
    MILI_GRAM_OX_Par_LITRE("mgO2/l"),
    GRAM_PAR_LITRE("g/l" ),
    N_T_U("NTU"),
    B_A_R("bar"),
    METRE_CUBE_PAR_HEURE("m³/h"),
    KILO_GRAMM("kg"),
    Litre_par_seconde("L/s"),
    Gallon_par_minute("gpm"),

    Pied_cube_par_seconde("ft³/s"),
    Mètre_cube_par_jour("m³/day"),
    Gallon_par_jour("gpd"),
    Litre_par_minute("L/min") ,
    Livre_par_pouce_carré("psi"),
    Atmosphère("atm"),
    Millimètre_de_mercure("mmHg"),
    Degré_Fahrenheit ("°F"),
    Kelvin("K"),
    Microsiemens_par_centimètre("µS/cm"),
    Millisiemens_par_centimètre("mS/cm"),
    Siemens_par_mètre("S/m"),
    Mètre("m"),
    Centimètre("cm"),
    Pied ("ft"),
    Pouce ("in"),
    Jackson_Turbidity_Unit ("JTU"),
    Pratique_des_parties_par_mille ("ppt"),

    Pourcentage ("%"),

    Litre ("L"),
    Mètre_cube ("m³"),
    Gallon("gal"),
    Millilitr("mL"),

    Kilogramme_par_mètre_cub ("kg/m³"),
    Gramme_par_centimètre_cube ("g/cm³"),
    Centipoise("cP"),
    Millipascal_seconde ("mPa·s"),

    Unité_de_couleur ("UC"),
    Hazen ("Pt-Co"),
    Lovibond ("°"),
    Partie_par_milliard ("ppb");




    private final String symbol;

    EUnite(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
