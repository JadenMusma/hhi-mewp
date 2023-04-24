package net.musma.hhi.middleware.mewp.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.musma.hhi.middleware.mewp.dto.DbResponse;
import net.musma.hhi.middleware.mewp.dto.ReceiveData;
import net.musma.hhi.middleware.mewp.dto.UpdatedCard;
import net.musma.hhi.middleware.mewp.entity.CrInfo;
import net.musma.hhi.middleware.mewp.repository.ReceiveDataRepository;
import net.musma.hhi.middleware.mewp.repository.UpdatedCardListRepository;
import net.musma.hhi.middleware.mewp.websocket.WebSocketHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
//@AllArgsConstructor
public class LuProcessService {

    //    @Value("${spring.env.send-card-qty}")
    private int sendCardQty;

    private int firstSendCardQty;
    final private DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private UpdatedCardListRepository updatedCardListRepository;

    private ResponseService responseService;

    private ReceiveDataRepository receiveDataRepository;

    private WebSocketHandler webSocketHandler;

    private CrInfoService crInfoService;

    public LuProcessService(@Value("${spring.env.send-card-qty}") int sendCardQty,
                            @Value("${spring.env.first-send-card-qty}") int firstSendCardQty,
                            UpdatedCardListRepository updatedCardListRepository,
                            ResponseService responseService,
                            ReceiveDataRepository receiveDataRepository,
                            WebSocketHandler webSocketHandler,
                            CrInfoService crInfoService
    ) {
        this.sendCardQty = sendCardQty;
        this.firstSendCardQty = firstSendCardQty;
        this.updatedCardListRepository = updatedCardListRepository;
        this.responseService = responseService;
        this.receiveDataRepository = receiveDataRepository;
        this.webSocketHandler = webSocketHandler;
        this.crInfoService = crInfoService;
        log.debug("================= firstSendCardQty:" + this.firstSendCardQty);
    }

    @Async
    public void process(String topic, JsonObject payload) {
        Gson gson = new Gson();
        try {
            String danmalId = payload.get("danmal_id").getAsString();
            String lastUpdateDtm = payload.get("last_update_dtm").getAsString();

            //처음 호출이면 미들웨어에서 가지고 있는 가능한 카드 정보 리스트를 내려 준다.
            if (lastUpdateDtm.equals("00000000000000")) {

                int segIndex = payload.get("seg_index").getAsInt();

                JsonArray jsonArray = crInfoService.getFirstCardListByIndex(segIndex);

                JsonObject rtnObj = new JsonObject();
                rtnObj.addProperty("type", "LU");
                rtnObj.addProperty("danmal_id", danmalId);
                rtnObj.addProperty("total_qty", crInfoService.getFirstSendCardTotalCnt());
                rtnObj.addProperty("total_seg", crInfoService.getFirstCardListQty());
                rtnObj.addProperty("card_qty", jsonArray.size());
                rtnObj.addProperty("seg_index", segIndex);
                rtnObj.addProperty("upd_div", "I");
                rtnObj.add("card_list", jsonArray);
                rtnObj.addProperty("last_update_dtm", crInfoService.getLastDtm());

                //단말로 전송
                responseService.sendResponse(topic, rtnObj);

/*
//                long firstCount = 100;

                String resultLastUpdateDtm = crInfoService.getLastDtm();
                List<String> avaliableCardList = crInfoService.getAvailableCardList();

                List<List<String>> sendCardList = new ArrayList<>();

                sendCardList.addAll(IntStream.range(0, avaliableCardList.size()).boxed()
                        .collect(Collectors.groupingBy(i -> i / firstSendCardQty,
                                Collectors.mapping(avaliableCardList::get, Collectors.toList())))
                        .values().stream().toList());

//                sendCardList.stream().forEach(System.out::println);

                //장치로 보내고 DB에 저장한다
                String finalDanmalId = danmalId;

                sendCardList.forEach(cardList -> {

                    JsonArray jsonArray = new JsonArray();
                    cardList.forEach(s -> {
                        jsonArray.add(s);
                    });

                    JsonObject rtnObj = new JsonObject();
                    rtnObj.addProperty("type", "LU");
                    rtnObj.addProperty("danmal_id", finalDanmalId);
                    rtnObj.addProperty("total_qty", avaliableCardList.size());
                    rtnObj.addProperty("card_qty", cardList.size());
                    rtnObj.addProperty("upd_div", "I");
                    rtnObj.add("card_list", jsonArray);
                    rtnObj.addProperty("last_update_dtm", resultLastUpdateDtm);

                    //단말로 전송
                    responseService.sendResponse(topic, rtnObj);

                    // 필요하다면 시간 텀을 주기위해 sleep을 걸어야 할 필요가 있다.
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        log.debug("===== 1초 Delay 오류");
                    }
                });
*/

            } else {

                System.out.println("============" + payload.get("last_update_dtm").getAsString().substring(2));

                String lastUpdatedDtm = payload.get("last_update_dtm").getAsString().substring(2);

                //프로시저 호출(PKG_HOCCOM_EXP_200717.SEARCH_DATA4)
                UpdatedCard requestParam = UpdatedCard.builder()
                        .gbn(">")
                        .danmalId(danmalId)
                        .ipAddr(payload.get("ip_addr").getAsString())
//                        .ipAddr(payload.get("mac").getAsString())
                        .updDtm(lastUpdatedDtm)
                        .build();


//                List<CrInfo> updatedList = updatedCardListRepository.findAll(requestParam);

                List<CrInfo> updatedList = updatedCardListRepository.findAll(requestParam).stream()
                        .filter(crInfo -> !crInfo.getUpdDtm().equals("20" + lastUpdatedDtm))
                        .toList();

                log.debug("====== 인증 정보 업데이트 : {} ", updatedList.size());
                log.debug("====== 인증 정보 업데이트 : {} ", updatedList.toString());

                if (updatedList.size() == 0) {


                    JsonObject rtnObj = new JsonObject();
                    rtnObj.addProperty("type", "LU");
                    rtnObj.addProperty("danmal_id", danmalId);
                    rtnObj.addProperty("total_qty", 0);
                    rtnObj.addProperty("total_seg", 0);
                    rtnObj.addProperty("card_qty", 0);
                    rtnObj.addProperty("seg_index", 0);
                    rtnObj.addProperty("upd_div", "");
                    rtnObj.add("card_list", new JsonArray());
                    rtnObj.addProperty("last_update_dtm", "20" + lastUpdatedDtm);

                    //단말로 전송
                    responseService.sendResponse(topic, rtnObj);

                } else {
                    String resultLastUpdateDtm = updatedList.stream()
                            .max(Comparator.comparing(CrInfo::getUpdDtm))
                            .get()
                            .getUpdDtm();


                    //장치로 보낸 카드의 개수로 분할한다. UPD_DIV로 구분(D, I의 순서) 후 보낼 수만큼 잘라서 넣는다.
                    Map<String, List<CrInfo>> collect = updatedList.stream().collect(Collectors.groupingBy(CrInfo::getUpdDiv));
                    List<List<CrInfo>> sendCardList = new ArrayList<>();

                    if (collect.containsKey("D")) {
                        sendCardList.addAll(IntStream.range(0, collect.get("D").size()).boxed()
                                .collect(Collectors.groupingBy(i -> i / sendCardQty,
                                        Collectors.mapping(collect.get("D")::get, Collectors.toList())))
                                .values().stream().toList());
                    }

                    if (collect.containsKey("I")) {
                        sendCardList.addAll(IntStream.range(0, collect.get("I").size()).boxed()
                                .collect(Collectors.groupingBy(i -> i / sendCardQty,
                                        Collectors.mapping(collect.get("I")::get, Collectors.toList())))
                                .values().stream().toList());
                    }

                    //장치로 보내고 DB에 저장한다
                    String finalDanmalId = danmalId;
                    String finalResultLastUpdateDtm = resultLastUpdateDtm;

                    IntStream.range(0, sendCardList.size()).mapToObj(index -> Pair.of(index,sendCardList.get(index) ))
                    .forEach(cardListPair -> {

                        List<String> cardNoList = cardListPair.getValue().stream().map(CrInfo::getCardNo).toList();

                        JsonArray jsonArray = new JsonArray();
                        cardNoList.forEach(s -> {
                            jsonArray.add(s);
                        });


                        JsonObject rtnObj = new JsonObject();
                        rtnObj.addProperty("type", "LU");
                        rtnObj.addProperty("danmal_id", finalDanmalId);
                        rtnObj.addProperty("total_qty", updatedList.size());
                        rtnObj.addProperty("total_seg", sendCardList.size());
                        rtnObj.addProperty("card_qty", cardListPair.getValue().size());
                        rtnObj.addProperty("seg_index", cardListPair.getKey());
                        rtnObj.addProperty("upd_div", cardListPair.getValue().get(0).getUpdDiv());
                        rtnObj.add("card_list", jsonArray);
                        rtnObj.addProperty("last_update_dtm", finalResultLastUpdateDtm);

                        //단말로 전송
                        responseService.sendResponse(topic, rtnObj);
//            SendResponse(sGBN + sLastUpdateDtm + _sCurrDvi + (sCardList.Length / 8).ToString().PadLeft(2, '0') + sCardList, "0", "SUCCESS", sMWID);

                        //데이터 저장
                        ReceiveData rd = ReceiveData.builder()
                                .dtm(LocalDateTime.now().format(dateTimeFormat))
                                .rsDiv("SND")
                                .deviceId(finalDanmalId)
                                .dataDiv(payload.get("type").getAsString())
                                .data(payload.get("type").getAsString() + lastUpdatedDtm + cardListPair.getValue().get(0).getUpdDiv() + StringUtils.leftPad(Integer.toString(cardNoList.size()), 2, '0') + cardNoList.stream().collect(Collectors.joining("")))
                                .build();

                        DbResponse dr = receiveDataRepository.save(rd);

//            RecieveDataSave(DateTime.Now.ToString("yyyyMMddHHmmss"), "SND", sMWID, sGBN, sGBN + sLastUpdateDtm + _sCurrDvi + (sCardList.Length / 8).ToString().PadLeft(2, '0') + sCardList);

                        if(cardNoList.size() == sendCardQty) {
                            log.debug("===== Delay");
                            // 필요하다면 시간 텀을 주기위해 sleep을 걸어야 할 필요가 있다.
                            try {
                                TimeUnit.SECONDS.sleep(3);
                            } catch (InterruptedException e) {
                                log.debug("===== 3초 Delay 오류");
                            }
                        }
                    });
                }
            }

        } catch (Exception e) {

            webSocketHandler.sendMessage(WebSocketHandler.WS_LOG, "자격 List UL 프로토콜 처리 오류, RcvData:" + gson.toJson(payload));
            log.warn("자격 List UL 프로토콜 처리 오류, RcvData:" + gson.toJson(payload) + "\n" + e.toString());
        }
    }
}
