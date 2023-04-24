package net.musma.hhi.middleware.mewp.service;

import com.google.gson.JsonArray;
import lombok.extern.slf4j.Slf4j;
import net.musma.hhi.middleware.mewp.entity.CrInfo;
import net.musma.hhi.middleware.mewp.repository.CrInfoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Slf4j
@Service
public class CrInfoService {

    private HashMap<String, CrInfo> cardMap;
    private LocalDate lastUpdateDate;


    private List<JsonArray> firstCardList;
    private int firstSendCardQty;
    private int firstSendCardTotalCnt;
    private String firstLastUpdateDate;


    private CrInfoRepository crInfoRepository;

    public CrInfoService(CrInfoRepository crInfoRepository,
                         @Value("${spring.env.first-send-card-qty}") int firstSendCardQty) {

        this.crInfoRepository = crInfoRepository;
        cardMap = new HashMap();
        this.firstSendCardQty = firstSendCardQty;
        lastUpdateDate = LocalDate.of(2000,1,1);
    }

    @Scheduled(fixedRate =  600_000, initialDelay=1_000)
    public void findAllCrInfoList(){

        try{
            //처음 조회 이거나 날짜가 바뀐 경우 처리
            if( cardMap.size() == 0 || !lastUpdateDate.isEqual(LocalDate.now())){

                String lastDateTime = "20000101000000";
                List<CrInfo> dtCrInfoList = crInfoRepository.findAll(lastDateTime);

                if(dtCrInfoList.size() > 0){
                    cardMap.clear();
                    dtCrInfoList.forEach(crInfo -> cardMap.put(crInfo.getCardNo(), crInfo));
                    /*cardMap = new HashMap(dtCrInfoList.stream().collect(Collectors.toMap(
                            CrInfo::getCardNo,
                            crInfo -> crInfo
                    )));*/

                    lastUpdateDate = LocalDate.now();

                    //firstCardList 저장
                    setFirstCardList();

                    log.debug("========인증정보 전체 건수: {}, 인증 유효 건수: {}", cardMap.size(), cardMap.values().stream().filter(crinfo-> crinfo.getUpdDiv().equals("I")).count());
                    //화면 로그
                    log.info("인증 정보 데이터 조회, {},  조회조건 : {}, Count : {}", lastUpdateDate, lastDateTime, dtCrInfoList.size());
                } else {
                    log.info("인증 정보 데이터 조회 안됨. 조회 데이터 : {}", lastDateTime);
                    log.warn("인증 정보 데이터 조회 안됨. 조회 데이터 : {}", lastDateTime);
                }

            } else {
                //10분에 한번씩 업데이트
                String lastDateTime = getLastDtm();

                List<CrInfo> dtCrInfoList = crInfoRepository.findAll(lastDateTime);

                if(dtCrInfoList.size() > 0) {
                    //조회 해온 항목을 cardMap에 업데이트 한다.
                    dtCrInfoList.stream().forEach(
                            e -> cardMap.put(e.getCardNo(), e)
                    );

                    log.info("인증 정보 데이터 조회, {},  조회조건 : {}, Count : {}", lastUpdateDate, lastDateTime, dtCrInfoList.size());
                } else {
                    log.info("인증 정보 데이터 조회 안됨. 조회 데이터 : {}", lastDateTime);
                    log.warn("인증 정보 데이터 조회 안됨. 조회 데이터 : {}", lastDateTime);
                }
            }

        } catch (Exception e) {
            log.warn("인증 프로토콜 처리 오류(CrInfo) : " + e.toString());
        }
    }

    public String getLastDtm(){
        return cardMap.values().stream()
                    .max(Comparator.comparing(CrInfo::getUpdDtm))
                    .get()
                    .getUpdDtm();
    }

    public List<String> getAvailableCardList(){
        return cardMap.values().stream()
                .filter( cardInfo -> cardInfo.getUpdDiv().equals("I") )
                .map( cardInfo -> cardInfo.getCardNo())
                .toList();
    }
    public void setLastUpdateDateInit(){
        this.lastUpdateDate = LocalDate.of(2000,1,1);
        findAllCrInfoList();
    }

    public List<CrInfo> getCardList(){
        return cardMap.values().stream()
                .toList();
    }

    private void setFirstCardList(){
        log.debug("==============setFirstCardList");

        firstCardList = new ArrayList();

        firstLastUpdateDate = getLastDtm();

        List<String> avaliableCardList = cardMap.values().stream()
                .filter( cardInfo -> cardInfo.getUpdDiv().equals("I") )
                .map( cardInfo -> cardInfo.getCardNo())
                .toList();

        firstSendCardTotalCnt = avaliableCardList.size();

        List<List<String>> sendCardList = new ArrayList<>();

        sendCardList.addAll(IntStream.range(0, avaliableCardList.size()).boxed()
                .collect(Collectors.groupingBy(i -> i / firstSendCardQty,
                        Collectors.mapping(avaliableCardList::get, Collectors.toList())))
                .values().stream().toList());

        sendCardList.forEach(cardList -> {

            JsonArray jsonArray = new JsonArray();
            cardList.forEach(s -> {
                jsonArray.add(s);
            });
            firstCardList.add(jsonArray);
        });

    }

    public JsonArray getFirstCardListByIndex(int index){
        return firstCardList.get(index);
    }

    public List<JsonArray> getFirstCardList(){
        return firstCardList;
    }
    public int getFirstCardListQty(){
        return firstCardList.size();
    }

//    public String getFirstLastUpdateDate(){
//        return firstLastUpdateDate;
//    }

    public int getFirstSendCardTotalCnt(){
        return firstSendCardTotalCnt;
    }
}
