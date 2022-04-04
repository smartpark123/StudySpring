package com.StudySpring;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = App.class)
public class PackageDependencyTests { //의존성 테스트  - 순환참조되는 것이 있는지 확인하는 역할

    private static final String STUDY = "..modules.study..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    @ArchTest // modules 에 있는건 modules 에 있는거만 참조해야한다
    ArchRule modulesPackageRule = classes().that().resideInAPackage("com.StudySpring.modules..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage("com.StudySpring.modules..");

    @ArchTest  //study 패키지 안에 클래스들은 Event에서만 접근 가능해야한다
    ArchRule studyPachageRule = classes().that().resideInAPackage(STUDY)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY, EVENT);

    @ArchTest //EVENT 패키지에 있는 것들은 study, account, event 만 참조한다
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(STUDY, ACCOUNT, EVENT);

    @ArchTest//account 에 있는 것들은 tag, zone, account, 만 참조한다
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat().resideInAnyPackage(TAG, ZONE, ACCOUNT);

    @ArchTest //com.StudySpring.modules.(*).. 패키지들을 조각 내서 순환 참조 확인
    ArchRule cycleCheck = slices().matching("com.StudySpring.modules.(*)..")
            .should().beFreeOfCycles();

}
