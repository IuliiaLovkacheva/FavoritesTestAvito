
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class FavoritesTest {


    @Test
    void addToFavoritesTest() {
        String address = "https://www.avito.ru/nikel/knigi_i_zhurnaly/domain"
                + "-driven_design_distilled_vaughn_vernon_2639542363";
        long itemId = 2639542363L;
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            BrowserContext context = browser.newContext();

            Page page = context.newPage();
            page.navigate(address);

            Locator addToFavorites = page.locator("//div[@class='style-header-add-favorite-M7nA2']//button[1]");
            assertThat(addToFavorites).hasAttribute("data-is-favorite", "false");
            addToFavorites.click();
            assertThat(addToFavorites).hasAttribute("data-is-favorite", "true");

            String itemName = page.locator("//h1[@class='style-title-info-title-eHW9V "
                    + "style-title-info-title-text-CoxZd']").textContent();
            String itemPrice = page.locator("(//div[@itemprop='offers'])[2]").textContent();
            Locator itemTimeLocator =
                    page.locator("//span[@data-marker='item-view/item-id']/following-sibling::span[1]");
            String itemTime = convertTimeToFavoritesFormat(itemTimeLocator.textContent());

            Locator favoritesLink = page.locator("//a[@data-marker='header/favorites']");
            favoritesLink.click();

            assertThat(page).hasURL("https://www.avito.ru/favorites");
            Locator favoriteItems = page.locator("//div[@class='favorite-items-list-favoritesWidgets-JE_Bq"
                    + "']/following-sibling::div[1]");

            String NUMBER_OF_DIVS = "(div) => {return div.children.length}";
            int itemsNumber = (int) favoriteItems.evaluate(NUMBER_OF_DIVS);
            Assertions.assertEquals(1, itemsNumber);

            String itemXPath = String.format("//div[@data-marker='item-%d']", itemId);
            Locator item = page.locator(itemXPath);
            Locator title = item.locator("//p[contains(@class,'styles-module-root-xvjz8 styles-module-size_l-eYNQn')]//strong");
            String nbsp = "\u00a0";
            Assertions.assertEquals(itemName.replaceAll(nbsp," "), title.textContent().replaceAll(nbsp," "));
            Locator price = item.locator("//div[@class='price-line-root-NiZHp']");
            assertThat(price).hasText(itemPrice);
            Locator time = item.locator("//div[@class='location-root-DKKDg']/following-sibling::p");
            assertThat(time).hasText(itemTime);
        }
    }

    private static String convertTimeToFavoritesFormat(String itemTime) {
        final Pattern ITEM_PAGE_TIME_REGEX = Pattern.compile("^ · ((?:(\\d+) )?([а-яё]+)) в ([\\d:]+)$");
        Matcher m = ITEM_PAGE_TIME_REGEX.matcher(itemTime);
        m.find();
        String fullDay = m.group(1);
        String date = m.group(2);
        String month = m.group(3);
        String dayTime = m.group(4);
        if (date == null) {
            itemTime = Character.toUpperCase(fullDay.charAt(0)) + fullDay.substring(1) + ", " + dayTime;
        } else {
            StringBuilder itemTimeBuilder = new StringBuilder();
            if (Integer.parseInt(date) < 10) {
               itemTimeBuilder.append(0);
            }
            itemTimeBuilder.append(date);
            itemTimeBuilder.append(" ");
            itemTimeBuilder.append(month);
            itemTimeBuilder.append(", ");
            itemTimeBuilder.append(dayTime);
            itemTime = itemTimeBuilder.toString();
        }
        return itemTime;
    }

}
