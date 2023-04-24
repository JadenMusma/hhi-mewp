package net.musma.hhi.middleware.mewp.repository;

import jakarta.persistence.*;
import net.musma.hhi.middleware.mewp.entity.EnvInfo;
import net.musma.hhi.middleware.mewp.entity.EnvInfoByDanmalId;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class EnvInfoRepository  {

    @PersistenceContext
    EntityManager entityManager;

    public List<EnvInfo> findAll(){

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "PKG_HOCCOM_EXP_200717.SEARCH_DATA_ER",
                "EnvInfoMapping"
        );

        query.registerStoredProcedureParameter("ORESULT_CUR", void.class, ParameterMode.REF_CURSOR);

        List<EnvInfo> results = query.getResultList();

        return results;

    }

    public List<EnvInfoByDanmalId> findByDanmalId(String danmalId, String ipAddr){

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "PKG_HOCCOM_EXP_200717.SEARCH_DATA2",
                "EnvInfoByDanmalIdMapping"
        );

        query.registerStoredProcedureParameter("ORESULT_CUR", void.class, ParameterMode.REF_CURSOR);
        query.registerStoredProcedureParameter("IN_DANMALID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_FIRMWARE", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_IPADDR", String.class, ParameterMode.IN);

        query.setParameter("IN_DANMALID", danmalId);
        query.setParameter("IN_FIRMWARE", "");
        query.setParameter("IN_IPADDR", ipAddr);


        List<EnvInfoByDanmalId> results = query.getResultList();

        return results;

    }
}