package nl.adg.rummikub.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.springframework.web.bind.annotation.RestController;

/**
 * Architecture rules for the server, the Java counterpart to the client's lint boundaries. Tests are
 * excluded from the import so a test class sharing a production package doesn't count as a production
 * dependency; generated OpenAPI code is excluded so the rules describe only hand-written code.
 *
 * <p>TODO: add layer-direction rules (domain must not depend on web) and a leaf {@code util} rule
 * once those packages exist.
 */
@AnalyzeClasses(
        packages = "nl.adg.rummikub",
        importOptions = {ImportOption.DoNotIncludeTests.class, ArchitectureTest.ExcludeGenerated.class})
class ArchitectureTest {

    private static final String ROOT = "nl.adg.rummikub";

    @ArchTest
    static final ArchRule packagesAreFreeOfCycles =
            slices().matching(ROOT + ".(*)..").should().beFreeOfCycles();

    /** slices() cannot see root-level classes, so a cycle through a root helper would pass unnoticed. */
    @ArchTest
    static final ArchRule nothingDependsOnTheApplicationRoot = noClasses()
            .that()
            .resideOutsideOfPackage(ROOT)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(ROOT)
            .because("the root package holds only the entry point; shared code belongs in a leaf package");

    @ArchTest
    static final ArchRule controllersLiveInTheWebLayer = classes()
            .that()
            .areAnnotatedWith(RestController.class)
            .should()
            .resideInAPackage("..rummikub.web..")
            .andShould()
            .haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule noStandardStreams = GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    static final ArchRule noGenericExceptions = GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    @ArchTest
    static final ArchRule noJavaUtilLogging = GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    /** Allowed to match nothing: there are no fields yet. It starts checking as soon as there are. */
    @ArchTest
    static final ArchRule noFieldInjection =
            GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION.allowEmptyShould(true);

    /** Excludes the generated OpenAPI code, so the rules describe only hand-written code. */
    static class ExcludeGenerated implements ImportOption {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/generated/");
        }
    }
}
