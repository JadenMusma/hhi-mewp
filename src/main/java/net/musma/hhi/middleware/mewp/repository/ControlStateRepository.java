package net.musma.hhi.middleware.mewp.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import net.musma.hhi.middleware.mewp.dto.ControlState;
import net.musma.hhi.middleware.mewp.dto.DbResponse;
import org.springframework.stereotype.Repository;


@Repository
public class ControlStateRepository {

    @PersistenceContext
    EntityManager entityManager;

    public DbResponse save(ControlState cs){

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "PKG_HOCCOM_EXP_200717.RCV_DATA_SAVE02"
        );

        query.registerStoredProcedureParameter("IN_DATA_DIV", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_TAG_ID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_DEVICE_ID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_EVENT_DIV", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_RCV_DTM", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_CR_START_YN", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_UL_DIV", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_LATITUDE", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_LONGITUDE", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_LATITUDE_D", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_LONGITUDE_D", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_TM_X", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_TM_Y", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_R1", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_R2", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_R3", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_RCV_DATA", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("O_APP_CODE", String.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("O_APP_MSG", String.class, ParameterMode.OUT);
        query.setParameter("IN_DATA_DIV", cs.getDataDiv());
        query.setParameter("IN_TAG_ID", cs.getTagId());
        query.setParameter("IN_DEVICE_ID", cs.getDeviceId());
        query.setParameter("IN_EVENT_DIV", cs.getEventDiv());
        query.setParameter("IN_RCV_DTM", cs.getRcvDtm());
        query.setParameter("IN_CR_START_YN", cs.getCrStartYn());
        query.setParameter("IN_UL_DIV", cs.getUlDiv());
        query.setParameter("IN_LATITUDE", cs.getLatitude());
        query.setParameter("IN_LONGITUDE", cs.getLongitude());
        query.setParameter("IN_LATITUDE_D", cs.getLatitudeD());
        query.setParameter("IN_LONGITUDE_D", cs.getLongitudeD());
        query.setParameter("IN_TM_X", cs.getTmX());
        query.setParameter("IN_TM_Y", cs.getTmY());
        query.setParameter("IN_R1", cs.getR1());
        query.setParameter("IN_R2", cs.getR2());
        query.setParameter("IN_R3", cs.getR3());
        query.setParameter("IN_RCV_DATA", cs.getRcvData());

        query.execute();

        return DbResponse.builder()
                .appCode((String) query.getOutputParameterValue("O_APP_CODE"))
                .appMsg((String)  query.getOutputParameterValue("O_APP_MSG"))
                .build();
    }
}