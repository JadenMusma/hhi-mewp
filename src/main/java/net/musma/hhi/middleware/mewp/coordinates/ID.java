package net.musma.hhi.middleware.mewp.coordinates;

public class ID {
    //Public Element정의

    //좌표변환관련
    static final public double BESSEL_MAJOR = 6377397.155;
    static final public double WGS84_MAJOR = 6378137;
    static final public double BESSEL_MINOR = 6356078.96325;
    static final public double WGS84_MINOR = 6356752.3142;

    static final public double TM_SOUTH_FACTOR = 1;
    static final public double TM_CENTER_FACTOR = 1;
    static final public double TM_EASTH_FACTOR = 1;
    static final public double KATEC_FACTOR = 0.9999;
    static final public double UTM_Z52_FACTOR = 0.9996;
    static final public double UTM_Z51_FACTOR = 0.9996;

    static final public double TM_SOUTH_LONG_CEN = 2.18171200985643;
    static final public double TM_CENTER_LONG_CEN = 2.21661859489632;
    static final public double TM_EASTH_LONG_CEN = 2.2515251799362;
    static final public double KATEC_LONG_CEN = 2.23402144255274;
    static final public double UTM_Z52_LONG_CEN = 2.25147473507269;
    static final public double UTM_Z51_LONG_CEN = 2.14675497995303;

    static final public double TM_SOUTH_LAT_CEN = 0.663225115757845;
    static final public double TM_CENTER_LAT_CEN = 0.663225115757845;
    static final public double TM_EASTH_LAT_CEN = 0.663225115757845;
    static final public double KATEC_LAT_CEN = 0.663225115757845;
    static final public double UTM_Z52_LAT_CEN = 0;

    static final public double TM_SOUTH_FALSE_N = 500000;
    static final public double TM_CENTER_FALSE_N = 500000;
    static final public double TM_EASTH_FALSE_N = 500000;
    static final public double KATEC_FALSE_N = 600000;
    static final public double UTM_Z52_FALSE_N = 0;
    static final public double UTM_Z51_FALSE_N = 0;

    static final public double TM_SOUTH_FALSE_E = 200000;
    static final public double TM_CENTER_FALSE_E = 200000;
    static final public double TM_EASTH_FALSE_E = 200000;
    static final public double KATEC_FALSE_E = 400000;
    static final public double UTM_Z52_FALSE_E = 500000;
    static final public double UTM_Z51_FALSE_E = 500000;

    static final public double EPSLN = 0.0000000001;

    static final public int TM_SOUTH = 1;	//서부원점
    static final public int TM_CENTER = 2;  //중부원점
    static final public int TM_EAST = 3;	//동부원점
}
