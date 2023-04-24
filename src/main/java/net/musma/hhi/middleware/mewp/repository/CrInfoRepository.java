package net.musma.hhi.middleware.mewp.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import net.musma.hhi.middleware.mewp.entity.CrInfo;
import net.musma.hhi.middleware.mewp.entity.CrInfoFirmware;
import net.musma.hhi.middleware.mewp.entity.CrInfoFtp;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class CrInfoRepository {

    @PersistenceContext
    EntityManager entityManager;

    public List<CrInfo> findAll(String datetime){

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "PKG_HOCCOM_EXP_200717.SEARCH_DATA3",
                "CrInfoMapping"
        );

        query.registerStoredProcedureParameter("ORESULT_CUR", void.class, ParameterMode.REF_CURSOR);
        query.registerStoredProcedureParameter("IN_DATA", String.class, ParameterMode.IN);
        query.setParameter("IN_DATA", datetime);

        List<CrInfo> results = query.getResultList();

//        results.stream().forEach(System.out::println);

        return results;

    }

    public List<CrInfoFtp> findForFtp(String danmalId, String ipAddr, String updateDtm){

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "PKG_HOCCOM_EXP_200717.SEARCH_DATA_LS",
                "CrInfoFtpMapping"
        );

        query.registerStoredProcedureParameter("ORESULT_CUR", void.class, ParameterMode.REF_CURSOR);
        query.registerStoredProcedureParameter("IN_DANMALID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_IPADDR", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_UPD_DTM", String.class, ParameterMode.IN);
        query.setParameter("IN_DANMALID", danmalId);
        query.setParameter("IN_IPADDR", ipAddr);
        query.setParameter("IN_UPD_DTM", updateDtm.substring(2));

        List<CrInfoFtp> results = query.getResultList();

        results.stream().forEach(System.out::println);

        return results;

    }

    public List<CrInfoFirmware> findForFirmware(String danmalId, String ipAddr){

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "PKG_HOCCOM_EXP_200717.SEARCH_DATA_SC",
                "CrInfoFirmwareMapping"
        );

        query.registerStoredProcedureParameter("ORESULT_CUR", void.class, ParameterMode.REF_CURSOR);
        query.registerStoredProcedureParameter("IN_DANMALID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_FIRMWARE", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_IPADDR", String.class, ParameterMode.IN);

        query.setParameter("IN_DANMALID", danmalId);
        query.setParameter("IN_FIRMWARE", "");
        query.setParameter("IN_IPADDR", ipAddr);


        List<CrInfoFirmware> results = query.getResultList();

        return results;

    }
}