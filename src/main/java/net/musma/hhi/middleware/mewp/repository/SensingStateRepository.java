package net.musma.hhi.middleware.mewp.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import net.musma.hhi.middleware.mewp.dto.DbResponse;
import net.musma.hhi.middleware.mewp.dto.SensingState;
import org.springframework.stereotype.Repository;


@Repository
public class SensingStateRepository {

    @PersistenceContext
    EntityManager entityManager;

    public DbResponse save(SensingState ss){

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "PKG_HOCCOM_EXP_200717.RCV_DATA_SAVE03"
        );

        query.registerStoredProcedureParameter("IN_DATA_DIV", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_TAG_ID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_DEVICE_ID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_EVENT_DIV", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_RCV_DTM", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_LATITUDE", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_LONGITUDE", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_LATITUDE_D", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_LONGITUDE_D", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_TM_X", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_TM_Y", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_US_SENSING", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_RCV_DATA", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("O_APP_CODE", String.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("O_APP_MSG", String.class, ParameterMode.OUT);
        query.setParameter("IN_DATA_DIV", ss.getDataDiv());
        query.setParameter("IN_TAG_ID", ss.getTagId());
        query.setParameter("IN_DEVICE_ID", ss.getDeviceId());
        query.setParameter("IN_EVENT_DIV", ss.getEventDiv());
        query.setParameter("IN_RCV_DTM", ss.getRcvDtm());
        query.setParameter("IN_LATITUDE", ss.getLatitude());
        query.setParameter("IN_LONGITUDE", ss.getLongitude());
        query.setParameter("IN_LATITUDE_D", ss.getLatitudeD());
        query.setParameter("IN_LONGITUDE_D", ss.getLongitudeD());
        query.setParameter("IN_TM_X", ss.getTmX());
        query.setParameter("IN_TM_Y", ss.getTmY());
        query.setParameter("IN_US_SENSING", ss.getUsSensing());
        query.setParameter("IN_RCV_DATA", ss.getRcvData());

        query.execute();

        return DbResponse.builder()
                .appCode((String) query.getOutputParameterValue("O_APP_CODE"))
                .appMsg((String) query.getOutputParameterValue("O_APP_MSG"))
                .build();
    }
}