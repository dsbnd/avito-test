import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class MainPageTest extends BaseTest {
    private final By CARDS = By.cssSelector("div[class^='_cards_'] > div");
    private final By PRICE = By.cssSelector("div[class^='_card__price_']");
    private final By TIMER = By.cssSelector("span[class*='timeValue']");

    @Test(priority = 1, description = "TC-01: Фильтрация по цене")
    public void testPriceFilter() {
        int min = 1000, max = 8000;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement minInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='От']")));
        WebElement maxInput = driver.findElement(By.cssSelector("input[placeholder='До']"));

        minInput.clear(); minInput.sendKeys(String.valueOf(min));
        maxInput.clear(); maxInput.sendKeys(String.valueOf(max));

        wait.until(d -> d.findElements(CARDS).size() > 0);

        boolean allValid = driver.findElements(CARDS).stream()
                .map(card -> Integer.parseInt(card.findElement(PRICE).getText().replaceAll("[^0-9]", "")))
                .allMatch(p -> p >= min && p <= max);

        Assert.assertTrue(allValid, "Нашлись товары вне диапазона цен!");
    }

    @Test(priority = 2, description = "TC-02: Сортировка по цене (возрастание)")
    public void testPriceSorting() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[text()='Сортировать по']/following-sibling::select")))).selectByValue("price");
        new Select(driver.findElement(By.xpath("//label[text()='Порядок']/following-sibling::select"))).selectByValue("asc");

        List<Integer> prices = driver.findElements(CARDS).stream()
                .map(card -> Integer.parseInt(card.findElement(PRICE).getText().replaceAll("[^0-9]", "")))
                .toList();

        for (int i = 0; i < prices.size() - 1; i++) {
            Assert.assertTrue(prices.get(i) <= prices.get(i + 1), "Сортировка не работает!");
        }
    }

    @Test(priority = 3, description = "TC-03: Фильтрация по категории")
    public void testCategoryFilter() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[text()='Категория']/following-sibling::select")))).selectByValue("7");

        wait.until(d -> d.findElements(CARDS).size() > 0);

        boolean isCorrectCategory = driver.findElements(CARDS).stream()
                .allMatch(card -> card.findElement(By.cssSelector("div[class^='_card__category_']")).getText().equals("Детское"));

        Assert.assertTrue(isCorrectCategory, "В выдаче товары другой категории!");
    }

    @Test(priority = 4, description = "TC-04: Тогл 'Только срочные'")
    public void testUrgentToggle() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("label[class^='_urgentToggle_']"))).click();

        List<WebElement> cards = driver.findElements(CARDS);
        for (WebElement card : cards) {
            boolean hasUrgentLabel = !card.findElements(By.xpath(".//*[contains(@class, 'urgent') or contains(text(), 'Срочн') or contains(text(), '🔥')]")).isEmpty();
            Assert.assertTrue(hasUrgentLabel, "Найдено несрочное объявление при включенном фильтре!");
        }
    }

    @Test(priority = 5, description = "TC-05: Управление таймером (Старт/Стоп/Сброс)")
    public void testTimerLogic() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get("https://cerulean-praline-8e5aa6.netlify.app/");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, '/stats')]"))).click();

        By toggleBtn = By.xpath("//button[contains(@aria-label, 'автообновление') or contains(@title, 'автообновление')]");
        WebElement toggle = wait.until(ExpectedConditions.elementToBeClickable(toggleBtn));

        if (driver.findElements(TIMER).isEmpty()) toggle.click();

        String time1 = driver.findElement(TIMER).getText().replaceAll("\\s+", "");
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementLocated(TIMER, time1)));
        String time2 = driver.findElement(TIMER).getText().replaceAll("\\s+", "");

        Assert.assertNotEquals(time1, time2, "Таймер не идет!");
    }

    @Test(priority = 6, description = "TC-06: Смена темы в мобильной версии")
    public void testMobileTheme() {
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(375, 812));
        driver.get("https://cerulean-praline-8e5aa6.netlify.app/");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement html = driver.findElement(By.tagName("html"));
        String initialTheme = html.getAttribute("data-theme");

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[class*='themeToggle']"))).click();

        wait.until(d -> !d.findElement(By.tagName("html")).getAttribute("data-theme").equals(initialTheme));

        Assert.assertNotEquals(initialTheme, html.getAttribute("data-theme"), "Тема не переключилась!");
    }
}
