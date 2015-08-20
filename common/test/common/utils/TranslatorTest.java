package common.utils;

import io.sphere.sdk.models.LocalizedStrings;
import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class TranslatorTest {

    private final Locale english = Locale.ENGLISH;
    private final Locale german =  Locale.GERMAN;
    private final Locale italian = Locale.ITALIAN;
    private final Locale french =  Locale.FRENCH;
    private final Locale korean =  Locale.KOREAN;

    final Map<Locale, String> translations = initTranslations();

    private final Locale preferredLanguage = english;
    private final List<Locale> fallbackLanguages = asList(german, french);
    private final List<Locale> projectLanguages = asList(italian, korean);

    private final Translator translator = Translator.of(preferredLanguage, fallbackLanguages, projectLanguages);

    @Test
    public void findTranslationForPreferredLanguage() {
        final LocalizedStrings localizedStrings = LocalizedStrings.of(translations);

        final String foundTranslation = translator.findTranslation(localizedStrings);

        assertThat(foundTranslation)
                .overridingErrorMessage("A translation for the users preferred language should be found")
                .isEqualTo(translations.get(preferredLanguage));
    }

    @Test
    public void findTranslationForFallbackLanguages() {
        final Map<Locale, String> localizedStringEntries = new HashMap<>(4);
        localizedStringEntries.put(german, "german");
        localizedStringEntries.put(french, "french");
        localizedStringEntries.put(italian, "italian");
        localizedStringEntries.put(korean, "korean");
        final LocalizedStrings localizedStrings = LocalizedStrings.of(localizedStringEntries);

        final String foundTranslation = translator.findTranslation(localizedStrings);

        assertThat(foundTranslation)
                .overridingErrorMessage("A translation for the users fallback languages should be found")
                .isIn(fallbackLanguages.stream().map(translations::get).collect(toList()));
    }

    @Test
    public void findTranslationForProjectLanguages() {
        final Map<Locale, String> localizedStringEntries = new HashMap<>(2);
        localizedStringEntries.put(italian, "italian");
        localizedStringEntries.put(korean, "korean");
        final LocalizedStrings localizedStrings = LocalizedStrings.of(localizedStringEntries);

        final String foundTranslation = translator.findTranslation(localizedStrings);

        assertThat(foundTranslation)
                .overridingErrorMessage("A translation for the users fallback project languages should be found")
                .isIn(projectLanguages.stream().map(translations::get).collect(toList()));
    }

    @Test
    public void findNoTranslation() {
        final Map<Locale, String> localizedStringEntries = new HashMap<>(1);
        localizedStringEntries.put(Locale.CHINESE, "chinese");
        final LocalizedStrings localizedStrings = LocalizedStrings.of(localizedStringEntries);

        final String foundTranslation = translator.findTranslation(localizedStrings);

        assertThat(foundTranslation)
                .overridingErrorMessage("No translation should be found")
                .isEqualTo("");
    }

    private Map<Locale, String> initTranslations() {
        final Map<Locale, String> translations = new HashMap<>(5);
        translations.put(english, "english");
        translations.put(german, "german");
        translations.put(french, "french");
        translations.put(italian, "italian");
        translations.put(korean, "korean");
        return translations;
    }
}
