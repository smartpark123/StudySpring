package com.StudySpring.modules.account;

import com.StudySpring.modules.tag.Tag;
import com.StudySpring.modules.zone.Zone;
import com.querydsl.core.types.Predicate;

import java.util.Set;


public class AccountPredicates {

    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones){
        //조회한 account에 zone이나 tag 하나라도 있으면 가져옴
        QAccount account = QAccount.account;
        return QAccount.account.zones.any().in(zones).and(account.tags.any().in(tags));
    }
}
