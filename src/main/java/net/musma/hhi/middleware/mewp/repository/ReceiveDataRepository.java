package net.musma.hhi.middleware.mewp.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import net.musma.hhi.middleware.mewp.dto.DbResponse;
import net.musma.hhi.middleware.mewp.dto.ReceiveData;
import org.springframework.stereotype.Repository;


@Repository
public class ReceiveDataRepository {

    @PersistenceContext
    EntityManager entityManager;

    public DbResponse save(ReceiveData rd){

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "PKG_HOCCOM_EXP_200717.ROW_DATA_SAVE"
        );

        query.registerStoredProcedureParameter("IN_DTM", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_RS_DIV", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_DEVICE_ID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_DATA_DIV", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_EVENT_DIV", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_DATA", String.class, ParameterMode.IN);

        query.registerStoredProcedureParameter("O_APP_CODE", String.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("O_APP_MSG", String.class, ParameterMode.OUT);

        query.setParameter("IN_DTM", rd.getDtm());
        query.setParameter("IN_RS_DIV", rd.getRsDiv());
        query.setParameter("IN_DEVICE_ID", rd.getDeviceId());
        query.setParameter("IN_DATA_DIV", rd.getDataDiv());
        query.setParameter("IN_EVENT_DIV", rd.getEventDiv());
        query.setParameter("IN_DATA", rd.getData());

        query.execute();

        return DbResponse.builder()
                .appCode((String) query.getOutputParameterValue("O_APP_CODE"))
                .appMsg((String) query.getOutputParameterValue("O_APP_MSG"))
                .build();
    }
}