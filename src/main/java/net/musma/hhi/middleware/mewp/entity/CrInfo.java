package net.musma.hhi.middleware.mewp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@Getter
@Entity
@SqlResultSetMapping(
        name = "CrInfoMapping",
        entities = @EntityResult(
                entityClass = CrInfo.class,
                fields={
                        @FieldResult(name = "cardNo", column = "card_no"),
                        @FieldResult(name = "updDiv", column = "upd_div"),
                        @FieldResult(name = "updDtm", column = "upd_dtm")
                }
        )
)
@ToString
public class CrInfo {

    @Id
    private String cardNo;

    @Id
    private String updDiv;

    @Id
    private String updDtm;

}

