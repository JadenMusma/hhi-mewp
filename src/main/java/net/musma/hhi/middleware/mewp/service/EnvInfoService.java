package net.musma.hhi.middleware.mewp.service;


import lombok.extern.slf4j.Slf4j;
import net.musma.hhi.middleware.mewp.entity.EnvInfo;
import net.musma.hhi.middleware.mewp.repository.EnvInfoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class EnvInfoService {

    private List<EnvInfo> envInfoList;

    private LocalDate lastUpdateDate;
    private EnvInfoRepository repository;

    public EnvInfoService(EnvInfoRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedRate = 10_000)
    public void findAllEnvInfoList() {


        try {
            //프로그램이 처음 실행되었거나 이전 날짜이면
            if (null == lastUpdateDate || !lastUpdateDate.isEqual(LocalDate.now())) {

//                log.debug("===== findAllEnvInfoList");

                if(envInfoList != null){
                    envInfoList = null;
                }
                //HocCardList의 전체 항목을 조회한다.
                envInfoList = repository.findAll();

//                envInfoList.stream().forEach(System.out::println);

                if (envInfoList.size() > 0) {
                    // 환경 정보를 가져온 날짜를 저장
                    lastUpdateDate = LocalDate.now();

                    //화면 로그
                    log.info("환경 정보 데이터 조회 일자: {}, 건수: {}", lastUpdateDate,    envInfoList.size());
                } else {
                    log.info("환경 정보 데이터 조회 안됨.");
                    log.warn("환경 정보 데이터 조회 안됨.");
                }
            }
        } catch (Exception e) {
            log.warn("환경정보 프로토콜 처리 오류(ER_Info)");
        }
    }

    public List<EnvInfo> getEnvInfo(String danmalId, String ipAddr) {
        if (null == envInfoList) {
            return null;
        }
        return envInfoList.stream()
                .filter(e -> e.getDanmalid().equals(danmalId) || e.getIpaddr().equals(ipAddr))
                .toList();
    }

    public String getAddrIp(String danmalId){

        Optional<EnvInfo> result = envInfoList.stream()
                .filter(e -> e.getDanmalid().equals(danmalId))
                .findFirst();

        if(result.isPresent()){
            return result.get().getIpaddr();
        } else {
            return "";
        }
    }

    public void setLastUpdateDateInit(){
        this.lastUpdateDate = null;
    }

    public List<EnvInfo> getEnvInfoList() {
        return envInfoList.stream()
                .toList();
    }
}
