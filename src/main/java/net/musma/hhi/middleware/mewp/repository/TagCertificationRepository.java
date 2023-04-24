package net.musma.hhi.middleware.mewp.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import net.musma.hhi.middleware.mewp.dto.DbResponse;
import net.musma.hhi.middleware.mewp.dto.TagCertification;
import org.springframework.stereotype.Repository;


@Repository
public class TagCertificationRepository {

    @PersistenceContext
    EntityManager entityManager;

    public DbResponse requestCertification(TagCertification tc){

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "PKG_HOCCOM_EXP_200717.RCV_DATA_SAVE01"
        );

        query.registerStoredProcedureParameter("IN_DATA_DIV", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_TAG_ID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_DEVICE_ID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_EVENT_DIV", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_RCV_DTM", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_UL_DIV", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_LATITUDE", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_LONGITUDE", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_LATITUDE_D", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_LONGITUDE_D", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_TM_X", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_TM_Y", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_RCV_DATA", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("O_APP_CODE", String.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("O_APP_MSG", String.class, ParameterMode.OUT);
        query.setParameter("IN_DATA_DIV", tc.getDataDiv());
        query.setParameter("IN_TAG_ID", tc.getTagId());
        query.setParameter("IN_DEVICE_ID", tc.getDeviceId());
        query.setParameter("IN_EVENT_DIV", tc.getEventDiv());
        query.setParameter("IN_RCV_DTM", tc.getRcvDtm());
        query.setParameter("IN_UL_DIV", tc.getUlDiv());
        query.setParameter("IN_LATITUDE", tc.getLatitude());
        query.setParameter("IN_LONGITUDE", tc.getLongitude());
        query.setParameter("IN_LATITUDE_D", tc.getLatitudeD());
        query.setParameter("IN_LONGITUDE_D", tc.getLongitudeD());
        query.setParameter("IN_TM_X", tc.getTmX());
        query.setParameter("IN_TM_Y", tc.getTmY());
        query.setParameter("IN_RCV_DATA", tc.getRcvData());

        query.execute();

        return DbResponse.builder()
                .appCode((String)query.getOutputParameterValue("O_APP_CODE"))
                .appMsg((String)query.getOutputParameterValue("O_APP_MSG"))
                .build();
    }
}