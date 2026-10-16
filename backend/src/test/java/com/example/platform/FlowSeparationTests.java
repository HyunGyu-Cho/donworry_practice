package com.example.platform;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 보안 흐름과 자금 흐름이 서로를 모른다는 것을 테스트로 남긴다.
 *
 * <p>allowedDependencies 가 이미 막고 있지만, 그 선언은 "무엇을 허용하는가"만 적는다.
 * 여기서는 "왜 막는가"를 남긴다. 나중에 누가 선언을 늘릴 때 이 테스트가 먼저 묻는다.
 */
@AnalyzeClasses(
        packages = "com.example.platform",
        importOptions = ImportOption.DoNotIncludeTests.class)
class FlowSeparationTests {

    @ArchTest
    static final ArchRule 보안_흐름은_자금_흐름을_참조하지_않는다 =
            noClasses()
                    .that()
                    .resideInAnyPackage("..platform.risk..", "..platform.investigation..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("..platform.settlement..", "..platform.cashflow..")
                    .because("SYSTEM-04: 보안 사건 조사와 예상 정산·현금흐름은 독립 운영한다");

    @ArchTest
    static final ArchRule 자금_흐름은_보안_흐름을_참조하지_않는다 =
            noClasses()
                    .that()
                    .resideInAnyPackage("..platform.settlement..", "..platform.cashflow..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("..platform.risk..", "..platform.investigation..")
                    .because("SYSTEM-04: 보안 사건 조사와 예상 정산·현금흐름은 독립 운영한다");
}
