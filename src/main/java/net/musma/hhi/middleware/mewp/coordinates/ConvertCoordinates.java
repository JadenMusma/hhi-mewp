package net.musma.hhi.middleware.mewp.coordinates;

public class ConvertCoordinates {
    public static double r_major;
    public static double r_minor;
    public static double scale_factor;
    public static double lon_center;
    public static double lat_origin;
    public static double false_northing;
    public static double false_easting;
    public static double e0;
    public static double e1;
    public static double e2;
    public static double e3;
    public static double e;
    public static double es;
    public static double esp;
    public static double ml0;
    public static double ind;

    public static Point LL2Six(double lati, double longi)
    {
        double TempLat, TempLong, TailLat, TailLong;
        double valx = 0.0;
        double valy = 0.0;

        if (lati <= 0)
            valx = 0.0;
        else
        {
            TempLat = lati / 100.0;
            TempLat = Math.round(TempLat - 0.5);
            TailLat = (lati - (TempLat * 100.0));
            valx = TempLat + (TailLat * (1.0 / 60.0));
        }

        if (longi <= 0)
            valy = 0.0;
        else
        {
            TempLong = longi / 100.0;
            TempLong = Math.round(TempLong - 0.5);
            TailLong = (longi - (TempLong * 100.0));
            valy = TempLong + (TailLong * (1.0 / 60.0));
        }
        return new Point(valx, valy);
    }

    private static Datum DatumTrans(double input_a, double input_b, double input_Phi, double input_Lamda, double input_H, double output_a,
                                   double output_b, long delta_X, long delta_Y, long delta_Z)
    {
        double delta_a, delta_f, delta_Phi, delta_Lamda, delta_H;
        double Rm, Rn;
        double temp, es_temp, f;

        temp = input_b / input_a;
        f = 1 - temp;
        es_temp = 1 - temp * temp;

        delta_a = output_a - input_a;
        delta_f = input_b / input_a - output_b / output_a;

        double pp = 1 - es_temp * Math.sin(input_Phi) * Math.sin(input_Phi);
        Rm = (input_a * (1 - es_temp)) / Math.sqrt(pp * pp * pp);
        Rn = input_a / Math.sqrt((1 - es_temp * Math.sin(input_Phi) * Math.sin(input_Phi)));

        delta_Phi = ((((-delta_X * Math.sin(input_Phi) * Math.cos(input_Lamda) - delta_Y * Math.sin(input_Phi) * Math.sin(input_Lamda)) + delta_Z * Math.cos(input_Phi)) + delta_a * Rn * es_temp * Math.sin(input_Phi) * Math.cos(input_Phi) / input_a) + delta_f * (Rm / temp + Rn * temp) * Math.sin(input_Phi) * Math.cos(input_Phi)) / (Rm + input_H);
        delta_Lamda = (-delta_X * Math.sin(input_Lamda) + delta_Y * Math.cos(input_Lamda)) / ((Rn + input_H) * Math.cos(input_Phi));
        delta_H = delta_X * Math.cos(input_Phi) * Math.cos(input_Lamda) + delta_Y * Math.cos(input_Phi) * Math.sin(input_Lamda) + delta_Z * Math.sin(input_Phi) - delta_a * input_a / Rn + delta_f * temp * Rn * Math.sin(input_Phi) * Math.sin(input_Phi);

        return new Datum( input_Phi + delta_Phi, input_Lamda + delta_Lamda,  input_H + delta_H);
    }

    private static void TMInit(double major, double minor, double scaleFact, double cen_lon, double cen_lat, double f_east, double f_north)
    {
        r_major = major;
        r_minor = minor;
        scale_factor = scaleFact;

        lon_center = cen_lon;
        lat_origin = cen_lat;
        false_northing = f_east;
        false_easting = f_north;

        double temp = r_minor / r_major;
        es = 1 - temp * temp;
        e = Math.sqrt(es);
        e0 = 1 - 0.25 * es * (1 + es / 16 * (3 + 1.25 * es));
        e1 = 0.375 * es * (1 + 0.25 * es * (1 + 0.46875 * es));
        e2 = 0.05859375 * es * es * (1 + 0.75 * es);
        e3 = es * es * es * 35 / 3072;
        ml0 = r_major * mlfn(e0, e1, e2, e3, lat_origin);
        esp = es / (1 - es);

        if (es < 0.00001)
            ind = 1;
        else
            ind = 0;
    }

    private static double mlfn(double te0, double te1, double te2, double te3, double tphi)
    {
        return (te0 * tphi - te1 * Math.sin(2 * tphi) + te2 * Math.sin(4 * tphi) - te3 * Math.sin(6 * tphi));
    }

    private static Point tmfor(double lon, double lat)
    {
        double delta_lon;
        double sin_phi, cos_phi;
        double al, als;
        double b, c, t, tq;
        double con, n, ml;

        b = 0;

        double rx;
        double ry;

        delta_lon = lon - lon_center;
        sin_phi = Math.sin(lat);
        cos_phi = Math.cos(lat);



        if (ind != 0)
        {
            b = cos_phi * Math.sin(delta_lon);
            if ((Math.abs(Math.abs(b) - 1)) < 0.0000000001)
            {
                rx = -1;
                ry = -1;
                return new Point(rx, ry);
            }
        }
        else
        {
            rx = 0.5 * r_major * scale_factor * Math.log((1 + b) / (1 - b));
            con = Math.acos(cos_phi * Math.cos(delta_lon) / Math.sqrt(1 - b * b));
            if (lat < 0)
            {
                con = -con;
                ry = r_major * scale_factor * (con - lat_origin);
            }
        }

        al = cos_phi * delta_lon;
        als = al * al;
        c = esp * cos_phi * cos_phi;
        tq = Math.tan(lat);
        t = tq * tq;
        con = 1 - es * sin_phi * sin_phi;
        n = r_major / Math.sqrt(con);
        ml = r_major * mlfn(e0, e1, e2, e3, lat);

        rx = scale_factor * n * al * (1 + als / 6 * (1 - t + c + als / 20 * (5 - 18 * t + t * t + 72 * c - 58 * esp))) + false_easting;
        ry = scale_factor * (ml - ml0 + n * tq * (als * (0.5 + als / 24 * (5 - t + 9 * c + 4 * c * c + als / 30 * (61 - 58 * t + t * t + 600 * c - 330 * esp))))) + false_northing;

        return new Point(rx, ry);
    }

    public static Point WGS_TM(int tmType, double dInputX, double dInputY)
    {
        double lon_temp, lat_temp, input_h;
        double out_lat, out_lon, out_h;

        int df = 1;         //WGS -> BESSEL

        int dX_W2B = 128;
        int dY_W2B = -481;
        int dZ_W2B = -664;

        lon_temp = dInputX * Math.PI / 180;
        lat_temp = dInputY * Math.PI / 180;
        input_h = 0;

        Datum datum = DatumTrans(ID.WGS84_MAJOR, ID.WGS84_MINOR, lat_temp, lon_temp, input_h, ID.BESSEL_MAJOR, ID.BESSEL_MINOR,
                 df * dX_W2B, df * dY_W2B, df * dZ_W2B);

        if (tmType == ID.TM_SOUTH)
            TMInit(ID.BESSEL_MAJOR, ID.BESSEL_MINOR, ID.TM_SOUTH_FACTOR, ID.TM_SOUTH_LONG_CEN, ID.TM_SOUTH_LAT_CEN, ID.TM_SOUTH_FALSE_N, ID.TM_SOUTH_FALSE_E);
        else if (tmType == ID.TM_EAST)
            TMInit(ID.BESSEL_MAJOR, ID.BESSEL_MINOR, ID.TM_EASTH_FACTOR, ID.TM_EASTH_LONG_CEN, ID.TM_EASTH_LAT_CEN, ID.TM_EASTH_FALSE_N, ID.TM_EASTH_FALSE_E);
        else
            TMInit(ID.BESSEL_MAJOR, ID.BESSEL_MINOR, ID.TM_CENTER_FACTOR, ID.TM_CENTER_LONG_CEN, ID.TM_CENTER_LAT_CEN, ID.TM_CENTER_FALSE_N, ID.TM_CENTER_FALSE_E);

        return tmfor(datum.getLon(), datum.getLat());
    }

}
