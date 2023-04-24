package net.musma.hhi.middleware.mewp.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import net.musma.hhi.middleware.mewp.dto.UpdatedCard;
import net.musma.hhi.middleware.mewp.entity.CrInfo;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class UpdatedCardListRepository {

    @PersistenceContext
    EntityManager entityManager;

    public List<CrInfo> findAll(UpdatedCard request){

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "PKG_HOCCOM_EXP_200717.SEARCH_DATA4",
                "CrInfoMapping"
        );

        query.registerStoredProcedureParameter("ORESULT_CUR", void.class, ParameterMode.REF_CURSOR);
        query.registerStoredProcedureParameter("IN_GBN", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_DANMALID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_IPADDR", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IN_UPD_DTM", String.class, ParameterMode.IN);
        query.setParameter("IN_GBN", request.getGbn());
        query.setParameter("IN_DANMALID", request.getDanmalId());
        query.setParameter("IN_IPADDR", request.getIpAddr());
        query.setParameter("IN_UPD_DTM", request.getUpdDtm());

        List<CrInfo> results = query.getResultList();

        System.out.println("===========result: " + results.size());

//        results.stream().forEach(System.out::println);

        return results;

    }
}