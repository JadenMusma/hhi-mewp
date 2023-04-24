package net.musma.hhi.middleware.mewp.service;

import lombok.extern.slf4j.Slf4j;
import net.musma.hhi.middleware.mewp.coordinates.ConvertCoordinates;
import net.musma.hhi.middleware.mewp.coordinates.ID;
import net.musma.hhi.middleware.mewp.coordinates.Point;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CoordinateService {

    public Point tmCalc(String latitude, String longitude){

        try {

//            log.debug("tmCalc:" + latitude + " : " + longitude);
//            log.debug("tmCalc:" + StringUtils.isBlank(latitude));
//            log.debug("tmCalc:" + StringUtils.isBlank(longitude));

            if(StringUtils.isBlank(latitude) || StringUtils.isBlank(longitude) ){
                return new Point(0,0);
            }
//            log.debug("tmCalc:=====================");

            double lat = Double.parseDouble(latitude);
            double lon = Double.parseDouble(longitude);

            if(lat > 0 && lon > 0 ) {
                Point point = ConvertCoordinates.LL2Six(Double.parseDouble(latitude), Double.parseDouble(longitude));
                Point rtnPoint = ConvertCoordinates.WGS_TM(ID.TM_EAST, point.getY(), point.getX());
                return rtnPoint;
            } else {
                return new Point(0,0);
            }
        } catch (Exception e) {
            return new Point(0,0);
        }
    }
}
